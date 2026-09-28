package com.app.apexwallet;

import com.app.apexwallet.dto.WalletResponse;
import com.app.apexwallet.entity.User;
import com.app.apexwallet.entity.Wallet;
import com.app.apexwallet.repository.UserRepository;
import com.app.apexwallet.repository.WalletRepository;
import com.app.apexwallet.service.WalletService;
import com.app.apexwallet.dto.WalletTransactionRequest;
import com.app.apexwallet.exception.InsufficientBalanceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApexwalletApplicationTests {

	@Autowired
	private WalletService walletService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WalletRepository walletRepository;

	@Test
	void contextLoads(){}

	@Test
	void concurrentWithdrawalsShouldNotOverdrawWallet() throws Exception {

		// 1. Create test user
		User user = new User();
		user.setName("Concurrency Test User");
		user.setEmail(UUID.randomUUID() + "@test.com");
		user.setPasswordHash("test-password");
		user.setCreatedAt(LocalDateTime.now());
		user.setRole("USER");

		User savedUser = userRepository.save(user);

		// 2. Create wallet with ₹1,000
		Wallet wallet = new Wallet();
		wallet.setUser(savedUser);
		wallet.setBalance(new BigDecimal("1000.00"));
		wallet.setCurrency("INR");
		wallet.setCreatedAt(LocalDateTime.now());
		wallet.setUpdatedAt(LocalDateTime.now());

		walletRepository.save(wallet);

		// 3. Prepare two withdrawals
		WalletTransactionRequest requestA = new WalletTransactionRequest();
		requestA.setAmount(new BigDecimal("700.00"));

		WalletTransactionRequest requestB = new WalletTransactionRequest();
		requestB.setAmount(new BigDecimal("500.00"));

		// 4. Create two threads
		ExecutorService executor = Executors.newFixedThreadPool(2);

		CountDownLatch startSignal = new CountDownLatch(1);

		Callable<Boolean> withdrawalA = () -> {
			startSignal.await();

			try {
				walletService.withdraw(savedUser.getId(), requestA, UUID.randomUUID().toString());
				return true;
			} catch (InsufficientBalanceException e) {
				return false;
			}
		};

		Callable<Boolean> withdrawalB = () -> {
			startSignal.await();

			try {
				walletService.withdraw(savedUser.getId(), requestB, UUID.randomUUID().toString());
				return true;
			} catch (InsufficientBalanceException e) {
				return false;
			}
		};

		// 5. Start both tasks
		Future<Boolean> resultA = executor.submit(withdrawalA);
		Future<Boolean> resultB = executor.submit(withdrawalB);

		// 6. Release both threads at approximately the same time
		startSignal.countDown();

		// 7. Wait for both operations
		boolean successA = resultA.get();
		boolean successB = resultB.get();

		executor.shutdown();

		// 8. Exactly one withdrawal should succeed
		assertNotEquals(successA, successB);

		// 9. Reload wallet from database
		Wallet finalWallet = walletRepository.findById(wallet.getId())
				.orElseThrow();

		// 10. Final balance should be ₹300
		assertTrue(
				finalWallet.getBalance().compareTo(new BigDecimal("300.00")) == 0
						|| finalWallet.getBalance().compareTo(new BigDecimal("500.00")) == 0
		);
	}

	@Test
	void duplicateWithdrawalWithSameIdempotencyKeyShouldNotWithdrawTwice() {

		User user = new User();
		user.setName("Idempotency Test User");
		user.setEmail("idempotency-" + UUID.randomUUID() + "@test.com");
		user.setPasswordHash("test-password");
		user.setCreatedAt(LocalDateTime.now());
		user.setRole("USER");

		User savedUser = userRepository.save(user);

		Wallet wallet = new Wallet();
		wallet.setUser(savedUser);
		wallet.setBalance(new BigDecimal("1000.00"));
		wallet.setCurrency("INR");
		wallet.setCreatedAt(LocalDateTime.now());
		wallet.setUpdatedAt(LocalDateTime.now());

		Wallet savedWallet = walletRepository.save(wallet);

		WalletTransactionRequest request = new WalletTransactionRequest();
		request.setAmount(new BigDecimal("300.00"));

		String idempotencyKey = UUID.randomUUID().toString();

		// First request
		walletService.withdraw(
				savedUser.getId(),
				request,
				idempotencyKey
		);

		// Second request with the SAME idempotency key
		WalletResponse secondResponse = walletService.withdraw(
				savedUser.getId(),
				request,
				idempotencyKey
		);

		Wallet finalWallet =
				walletRepository.findById(savedWallet.getId()).orElseThrow();

		assertEquals(
				0,
				new BigDecimal("700.00").compareTo(finalWallet.getBalance())
		);

		assertEquals(
				0,
				new BigDecimal("700.00").compareTo(secondResponse.getBalance())
		);
	}

	@Test
	void sameIdempotencyKeyWithDifferentAmountShouldBeRejected() {

		User user = new User();
		user.setName("Idempotency Conflict Test User");
		user.setEmail("idempotency-conflict-" + UUID.randomUUID() + "@test.com");
		user.setPasswordHash("test-password");
		user.setCreatedAt(LocalDateTime.now());
		user.setRole("USER");

		User savedUser = userRepository.save(user);

		Wallet wallet = new Wallet();
		wallet.setUser(savedUser);
		wallet.setBalance(new BigDecimal("1000.00"));
		wallet.setCurrency("INR");
		wallet.setCreatedAt(LocalDateTime.now());
		wallet.setUpdatedAt(LocalDateTime.now());

		Wallet savedWallet = walletRepository.save(wallet);

		WalletTransactionRequest firstRequest = new WalletTransactionRequest();
		firstRequest.setAmount(new BigDecimal("300.00"));

		WalletTransactionRequest secondRequest = new WalletTransactionRequest();
		secondRequest.setAmount(new BigDecimal("500.00"));

		String idempotencyKey = UUID.randomUUID().toString();

		// First request
		walletService.withdraw(
				savedUser.getId(),
				firstRequest,
				idempotencyKey
		);

		// Same key, different amount
		assertThrows(
				IllegalArgumentException.class,
				() -> walletService.withdraw(
						savedUser.getId(),
						secondRequest,
						idempotencyKey
				)
		);

		Wallet finalWallet =
				walletRepository.findById(savedWallet.getId()).orElseThrow();

		// Only the first withdrawal should have happened.
		assertEquals(
				0,
				new BigDecimal("700.00").compareTo(finalWallet.getBalance())
		);
	}
}
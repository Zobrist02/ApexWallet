package com.app.apexwallet.service;

import com.app.apexwallet.dto.LoginRequest;
import com.app.apexwallet.dto.RegisterRequest;
import com.app.apexwallet.entity.User;
import com.app.apexwallet.exception.UserAlreadyExistsException;
import com.app.apexwallet.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        String passwordHash =
                passwordEncoder.encode(request.getPassword());

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordHash);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole("USER");

        userRepository.save(user);
    }

    public String login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null){
            return null;
        }
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!passwordMatches){
            return null;
        }
        return jwtService.generateToken(user);
    }
}

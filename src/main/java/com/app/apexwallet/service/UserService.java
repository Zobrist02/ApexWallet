package com.app.apexwallet.service;

import com.app.apexwallet.dto.UserCreateRequest;
import com.app.apexwallet.dto.UserResponse;
import com.app.apexwallet.dto.UserUpdateRequest;
import com.app.apexwallet.entity.User;
import com.app.apexwallet.exception.UserAlreadyExistsException;
import com.app.apexwallet.exception.UserNotFoundException;
import com.app.apexwallet.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserCreateRequest request){

        if(userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException(
                    "User with this email already exists"
            );
        }
        else{
         User user = new User();
         user.setName(request.getName());
         user.setEmail(request.getEmail());
         user.setPasswordHash(request.getPassword());
         user.setCreatedAt(LocalDateTime.now());

         User savedUser = userRepository.save(user);
            return new UserResponse(
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    savedUser.getCreatedAt());
        }
    }

    public UserResponse updateUser(UserUpdateRequest request, Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with this ID does not exist"));
        if (request.getEmail().equals(user.getEmail())){
            user.setName(request.getName());
            User savedUser = userRepository.save(user);
            return new UserResponse(savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    savedUser.getCreatedAt());
        }
        else{
            if (userRepository.existsByEmail(request.getEmail())){
                throw new UserAlreadyExistsException("User with this email already exists");
            }
            else{
                user.setName(request.getName());
                user.setEmail(request.getEmail());
                User savedUser = userRepository.save(user);
                return new UserResponse(savedUser.getId(),
                        savedUser.getName(),
                        savedUser.getEmail(),
                        savedUser.getCreatedAt());
            }
        }
    }

    public String deleteUser(Long id){
        User user = userRepository.findById(id). orElseThrow(() -> new UserNotFoundException("User does not exist"));
        userRepository.deleteById(id);
        return "User has been deleted";
    }

    public UserResponse getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with this ID does not exist"));
        return new UserResponse(user.getId(),user.getName(),user.getEmail(),user.getCreatedAt());
    }

    public UserResponse getUserByEmail(String email){
        User user = userRepository.findByEmail(email);
        if (user == null){
            throw new UserNotFoundException("User with this email does not exist");
        }
        return new UserResponse(user.getId(),user.getName(),user.getEmail(),user.getCreatedAt());
    }

    public List<UserResponse> getAllUsers(){
        List<User> users = userRepository.findAll();
        List<UserResponse> userinfo = new ArrayList<>();
        for (User user : users){
            UserResponse response = new UserResponse(user.getId(),user.getName(),user.getEmail(),user.getCreatedAt());
            userinfo.add(response);
        }
        return userinfo;
    }
}

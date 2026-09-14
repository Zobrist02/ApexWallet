package com.app.apexwallet.controller;

import com.app.apexwallet.dto.UserCreateRequest;
import com.app.apexwallet.dto.UserResponse;
import com.app.apexwallet.dto.UserUpdateRequest;
import com.app.apexwallet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request){
        return userService.createUser(request);
    }

    @PatchMapping("/{id}")
    public UserResponse updateUser(@Valid @RequestBody UserUpdateRequest request, @PathVariable Long id){
        return userService.updateUser(request, id);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id){
        return userService.deleteUser(id);
    }

    @GetMapping
    public List<UserResponse> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id){
        return userService.getUserById(id);
    }

    @GetMapping("/email")
    public UserResponse getUserByEmail(@RequestParam String email){
        return userService.getUserByEmail(email);
    }
}

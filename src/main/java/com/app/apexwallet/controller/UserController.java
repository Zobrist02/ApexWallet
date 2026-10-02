package com.app.apexwallet.controller;

import com.app.apexwallet.dto.UserResponse;
import com.app.apexwallet.dto.UserUpdateRequest;
import com.app.apexwallet.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@securityService.isOwner(#id)")
    public UserResponse updateUser(@Valid @RequestBody UserUpdateRequest request, @PathVariable Long id){
        return userService.updateUser(request, id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isOwner(#id)")
    public String deleteUser(@PathVariable Long id){
        return userService.deleteUser(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isOwner(#id)")
    public UserResponse getUser(@PathVariable Long id){
        return userService.getUserById(id);
    }

    @GetMapping("/email")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserByEmail(@RequestParam String email){
        return userService.getUserByEmail(email);
    }
}

package com.app.apexwallet.service;

import com.app.apexwallet.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    public boolean isOwner(Long userId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof User)) {
            return false;
        }

        User authenticatedUser =
                (User) authentication.getPrincipal();

        return authenticatedUser.getId().equals(userId);
    }
}
package com.backend.stockAllocation.authentication;

import com.backend.stockAllocation.entity.AppUser;
import com.backend.stockAllocation.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public static AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in context");
        }
        return (AppUser) auth.getPrincipal();
    }


     // Returns the subscriberId of the currently logged-in user.
     // Returns null for ADMIN / ANALYST users.

    public static Long getCurrentSubscriberId() {
        return getCurrentUser().getSubscriberId();
    }


     //Returns the userId of the currently logged-in user.
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }


     // Checks if the current user has a specific role.
    public static boolean hasRole(Role role) {
        return getCurrentUser().getRole() == role;
    }


     // Returns true if current user is ADMIN.
    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

     // Returns true if current user is a SUBSCRIBER.
    public static boolean isSubscriber() {
        return hasRole(Role.SUBSCRIBER);
    }

    /**
     * Checks if the current user can access data for the given subscriberId.
     * ADMIN  can access any subscriber.
     * SUBSCRIBER can only access their own data.
     */
    public static boolean canAccessSubscriber(Long subscriberId) {
        AppUser user = getCurrentUser();
        if (user.getRole() == Role.ADMIN ) {
            return true;
        }
        // SUBSCRIBER can only see own data
        return subscriberId.equals(user.getSubscriberId());
    }
}

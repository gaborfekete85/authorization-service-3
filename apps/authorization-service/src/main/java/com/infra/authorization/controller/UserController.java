package com.infra.authorization.controller;

import com.infra.authorization.controller.dto.UpdateImageRequest;
import com.infra.authorization.controller.dto.UpdateRoleRequest;
import com.infra.authorization.model.UserDetail;
import com.infra.authorization.persistence.entities.ERole;
import com.infra.authorization.persistence.entities.User;
import com.infra.authorization.persistence.repository.RoleRepository;
import com.infra.authorization.persistence.repository.UserRepository;
import com.infra.authorization.utils.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/test")
    public String test() {
        return "Hello World";
    }

    @GetMapping("/user/all")
    public List<User> getAllUsers(UserDetail userPrincipal) {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "email"));
    }

    @GetMapping("/user/me")
    public User getCurrentUser(final Authentication authentication) {
        UserDetail userPrincipal = (UserDetail) authentication.getPrincipal();
        User user = userRepository.findByEmail(userPrincipal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userPrincipal.getUsername()));
        return user;
    }

    @GetMapping({"/check_token", "/check-token"})
//    @PreAuthorize("hasRole('USER')")
    public User checkToken(@Autowired Authentication authentication) {
        UserDetail userPrincipal = (UserDetail) authentication.getPrincipal();
        User user = userRepository.findByEmail(userPrincipal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userPrincipal.getUsername()));
        return user;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public User findById(@PathVariable String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
    }
    
    @PostMapping("/user/find")
    public List<User> findByIds(@RequestBody List<UUID> userIds) {
        return userRepository.findByIds(userIds);
    }

    @PostMapping("/user/update")
    @PreAuthorize("hasRole('USER')")
    public User updateUser(Principal principal, @RequestBody UpdateImageRequest updateImageRequest) {
        String userEmail = SecurityUtil.getUserEmail(principal);
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new EntityNotFoundException("User does not exists: " + userEmail));
        if(updateImageRequest.getName() != null) {
            user.setName(updateImageRequest.getName());
        }
        if(updateImageRequest.getPhone() != null) {
            user.setPhone(updateImageRequest.getPhone());
        }
        if(updateImageRequest.getImageUrl() != null) {
            user.setImageUrl(updateImageRequest.getImageUrl());
        }
        return userRepository.save(user);
    }

    @PostMapping("/user/update-role")
    @PreAuthorize("hasRole('MODERATOR')")
    public User updateUserRoles(@RequestBody UpdateRoleRequest updateRoleRequest) {
        UUID userId = updateRoleRequest.getUserId();
        User user = userRepository.findById(userId).get();
        List<ERole> eroles = collectRoles(updateRoleRequest.getRole());
        user.setRoles(new HashSet<>());
        user.setRoles(eroles.stream().map(x -> roleRepository.findByName(x)).collect(Collectors.toSet()));
        return userRepository.save(user);
    }

    @PostMapping("/user/update-image")
    @PreAuthorize("hasRole('USER')")
    public User updateImage(Principal principal, @RequestBody UpdateImageRequest updateImageRequest) {
        String userEmail = SecurityUtil.getUserEmail(principal);
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new EntityNotFoundException("User does not exists: " + userEmail));
        if(updateImageRequest.getImageUrl() != null) {
            user.setImageUrl(updateImageRequest.getImageUrl());
        }
        return userRepository.save(user);
    }

    private List<ERole> collectRoles(ERole role) {
        if(ERole.ROLE_USER.equals(role)) {
            return List.of(ERole.ROLE_USER);
        }
        if(ERole.ROLE_SERVICE.equals(role)) {
            return List.of(ERole.ROLE_USER, ERole.ROLE_SERVICE);
        }
        if(ERole.ROLE_MODERATOR.equals(role)) {
            return List.of(ERole.ROLE_USER, ERole.ROLE_SERVICE, ERole.ROLE_MODERATOR);
        }
        if(ERole.ROLE_ADMIN.equals(role)) {
            return List.of(ERole.ROLE_USER, ERole.ROLE_SERVICE, ERole.ROLE_MODERATOR, ERole.ROLE_ADMIN);
        }
        return List.of();
    }
}

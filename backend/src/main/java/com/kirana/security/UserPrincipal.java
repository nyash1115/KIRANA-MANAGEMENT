package com.kirana.security;

import com.kirana.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final Long businessId;
    private final Long storeId;
    private final String username;
    private final String password;
    private final String fullName;
    private final String role;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.businessId = user.getBusiness() != null ? user.getBusiness().getId() : null;
        this.storeId = user.getStore() != null ? user.getStore().getId() : null;
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.active = user.isActive();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    public Long getId() { return id; }
    public Long getBusinessId() { return businessId; }
    public Long getStoreId() { return storeId; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return active; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}

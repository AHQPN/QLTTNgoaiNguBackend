package org.example.qlttngoaingu.security.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.qlttngoaingu.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    @Getter
    private final Integer id;
    private final String phoneNumber;
    private final String password;
    @Getter
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;
    @Getter
    private final boolean isVerified;


    // build từ entity User
    public static UserDetailsImpl build(User user) {
        String roleName = "";
        if (user.getRole() != null) roleName += user.getRole();
        else roleName += "STUDENT";

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase());

        return new UserDetailsImpl(
                user.getUserId(),
                user.getPhoneNumber(),
                user.getPasswordHash(),
                roleName,
                Collections.singletonList(authority)
                ,user.getIsVerified()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return phoneNumber; // Spring Security dùng đây làm username
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetailsImpl)) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

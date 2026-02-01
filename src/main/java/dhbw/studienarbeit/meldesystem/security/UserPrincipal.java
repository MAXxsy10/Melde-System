package dhbw.studienarbeit.meldesystem.security;


import dhbw.studienarbeit.meldesystem.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@lombok.Data
@lombok.AllArgsConstructor
@lombok.Builder
public class UserPrincipal implements UserDetails {

    private Long id;
    private String email;
    private String password;
    private String nickname;
    private Boolean isAnonymous;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .isAnonymous(user.getIsAnonymous())
                .authorities(authorities)
                .build();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
}

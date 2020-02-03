package org.fantasizer.theblog.admin.security;

import org.fantasizer.common.enums.Status;
import org.fantasizer.theblog.config.security.SecurityUser;
import org.fantasizer.theblog.xo.entity.Administrator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

public final class SecurityUserFactory {

    private SecurityUserFactory() {
    }

    public static SecurityUser create(Administrator admin) {
        boolean enabled = (admin.getStatus() == Status.ENABLED) ? true : false;
        return new SecurityUser(
                admin.getUid(),
                admin.getUserName(),
                admin.getPassWord(),
                enabled,
                mapToGrantedAuthorities(admin.getRoleNames())
        );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<String> authorities) {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}

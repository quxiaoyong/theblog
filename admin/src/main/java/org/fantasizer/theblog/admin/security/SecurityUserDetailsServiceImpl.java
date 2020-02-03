package org.fantasizer.theblog.admin.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.xo.entity.Administrator;
import org.fantasizer.theblog.xo.entity.Role;
import org.fantasizer.theblog.xo.service.AdministratorService;
import org.fantasizer.theblog.xo.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AdministratorService administratorService;

    @Autowired
    private RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        QueryWrapper<Administrator> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.USER_NAME, username);
        Administrator admin = administratorService.getOne(queryWrapper);

        if (admin == null) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {
            //查询出角色信息封装到admin中
            List<String> roleNames = new ArrayList<>();
            Role role = roleService.getById(admin.getRoleUid());
            roleNames.add(role.getRoleName());

            admin.setRoleNames(roleNames);

            return SecurityUserFactory.create(admin);
        }
    }
}

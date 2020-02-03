package org.fantasizer.theblog.admin.security;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.theblog.config.jwt.Audience;
import org.fantasizer.theblog.config.jwt.JwtHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static Logger log = LogManager.getLogger(JwtAuthenticationTokenFilter.class);

    @Autowired
    private Audience audience;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtHelper jwtHelper;


    @Value(value = "${tokenHead}")
    private String tokenHead;

    @Value(value = "${tokenHeader}")
    private String tokenHeader;

    @Value(value = "${audience.expiresSecond}")
    private Long expiresSecond;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        /**
         *
         * 得到请求头信息authorization信息
         *
         * 设定为Authorization
         */
        final String authHeader = request.getHeader(tokenHeader);

        log.error("传递过来的token为:" + authHeader);

        //请求头 'Authorization': tokenHead + token
        if (authHeader != null && authHeader.startsWith(tokenHead)) {

            final String token = authHeader.substring(tokenHead.length());

            //判断token是否过期
            if (!jwtHelper.isExpiration(token, audience.getBase64Secret())) {
                //刷新token过期时间
                jwtHelper.refreshToken(token, audience.getBase64Secret(), expiresSecond);
                log.info("token未过期，刷新token");
            } else {
                chain.doFilter(request, response);
                return;
            }

            String username = jwtHelper.getUsername(token, audience.getBase64Secret());
            String adminUid = jwtHelper.getUserUid(token, audience.getBase64Secret());

            //把adminUid存储到request中
            request.setAttribute("adminUid", adminUid);
            logger.info("解析出来用户 : " + username);
            logger.info("解析出来的用户Uid : " + adminUid);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtHelper.validateToken(token, userDetails, audience.getBase64Secret())) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(
                            request));
                    logger.info("authenticated user " + username + ", setting security context");
                    /**
                     * //以后可以security中取得SecurityUser信息
                     */
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
		


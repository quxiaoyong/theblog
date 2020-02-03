package org.fantasizer.theblog.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.fantasizer.theblog.config.security.SecurityUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * @Author Cruise Qu
 * @Date 2020-01-29 22:28
 */
@Component
public class JwtHelper {
    /**
     * 解析jwt
     */
    public Claims parseJWT(String token, String base64Security) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(base64Security))
                    .parseClaimsJws(token).getBody();
            return claims;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 构建jwt
     *
     * @param userName       账户名
     * @param adminUid       账户id
     * @param roleName       账户拥有角色名
     * @param audience       代表这个Jwt的接受对象
     * @param issuer         代表这个Jwt的签发主题
     * @param TTLMillis      jwt有效时间
     * @param base64Security 加密方式
     * @return
     */
    public String createJWT(String userName, String adminUid, String roleName,
                            String audience, String issuer, long TTLMillis, String base64Security) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        //生成签名密钥
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(base64Security);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        //添加构成JWT的参数
        JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
                .claim("adminUid", adminUid)
                .claim("role", roleName)
                .claim("creatTime", now)
                .setSubject(userName)
                .setIssuer(issuer)
                .setAudience(audience)
                .signWith(signatureAlgorithm, signingKey);
        //添加Token过期时间
        if (TTLMillis >= 0) {
            long expMillis = nowMillis + TTLMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp).setNotBefore(now);
        }
        //生成JWT
        return builder.compact();
    }


    /**
     * 判断token是否已过期
     *
     * @param token
     * @param base64Security
     * @return
     */
    public boolean isExpiration(String token, String base64Security) {
        if (parseJWT(token, base64Security) == null) {
            return true;
        } else {
            return parseJWT(token, base64Security).getExpiration().before(new Date());
        }
    }


    /**
     * 效验token
     *
     * @param token
     * @param userDetails
     * @param base64Security
     * @return
     */
    public Boolean validateToken(String token, UserDetails userDetails, String base64Security) {
        SecurityUser SecurityUser = (SecurityUser) userDetails;
        final String username = getUsername(token, base64Security);
        final boolean expiration = isExpiration(token, base64Security);
        return username.equals(SecurityUser.getUsername()) && !expiration;
    }


    /**
     * 从token中获取用户名
     *
     * @param token
     * @param base64Security
     * @return
     */
    public String getUsername(String token, String base64Security) {
        return parseJWT(token, base64Security).getSubject();
    }

    /**
     * 从token中获取用户UID
     *
     * @param token
     * @param base64Security
     * @return
     */
    public String getUserUid(String token, String base64Security) {
        //TODO：这里不应该写死
        return parseJWT(token, base64Security).get("adminUid", String.class);
    }

    /**
     * 从token中获取audience
     *
     * @param token
     * @param base64Security
     * @return
     */
    public String getAudience(String token, String base64Security) {
        return parseJWT(token, base64Security).getAudience();
    }

    /**
     * 从token中获取issuer
     *
     * @param token
     * @param base64Security
     * @return
     */
    public String getIssuer(String token, String base64Security) {
        return parseJWT(token, base64Security).getIssuer();
    }

    /**
     * 获取过期时间
     *
     * @param token
     * @param base64Security
     * @return
     */
    public Date getExpiration(String token, String base64Security) {
        return parseJWT(token, base64Security).getExpiration();
    }

    /**
     * token是否可以更新
     *
     * @param token
     * @param base64Security
     * @return
     */
    public Boolean canTokenBeRefreshed(String token, String base64Security) {
        return !isExpiration(token, base64Security);
    }

    /**
     * 更新token
     *
     * @param token
     * @param base64Security
     * @param TTLMillis
     * @return
     */
    public String refreshToken(String token, String base64Security, long TTLMillis) {
        String refreshedToken;
        try {
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            //生成签名密钥
            byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(base64Security);
            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

            final Claims claims = parseJWT(token, base64Security);
            claims.put("creatDate", new Date());
            JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
                    .setClaims(claims)
                    .setSubject(getUsername(token, base64Security))
                    .setIssuer(getIssuer(token, base64Security))
                    .setAudience(getAudience(token, base64Security))
                    .signWith(signatureAlgorithm, signingKey);
            //添加Token过期时间
            if (TTLMillis >= 0) {
                long expMillis = nowMillis + TTLMillis;
                Date exp = new Date(expMillis);
                builder.setExpiration(exp).setNotBefore(now);
            }
            refreshedToken = builder.compact();
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }
}

package org.fantasizer.theblog.web.restapi;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.theblog.common.helper.IpUtils;
import org.fantasizer.theblog.common.helper.JsonHelper;
import org.fantasizer.theblog.common.helper.ResultUtil;
import org.fantasizer.theblog.common.helper.StringUtils;
import org.fantasizer.theblog.common.vo.FileVO;
import org.fantasizer.theblog.web.feign.PictureFeignClient;
import org.fantasizer.theblog.web.global.MessageConfiguration;
import org.fantasizer.theblog.web.global.SQLConfiguration;
import org.fantasizer.theblog.web.global.SystemConfiguration;
import org.fantasizer.theblog.xo.entity.User;
import org.fantasizer.theblog.xo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 第三方登录认证
 */
@RestController
@RequestMapping("/oauth")
@Api(value = "认证RestApi", tags = {"AuthRestApi"})
public class AuthRestApi {

    private static Logger log = LogManager.getLogger(IndexRestApi.class);

    @Autowired
    private UserService userService;
    @Value(value = "${justAuth.clientId.gitee}")
    private String giteeClienId;
    @Value(value = "${justAuth.clientSecret.gitee}")
    private String giteeClientSecret;
    @Value(value = "${justAuth.clientId.github}")
    private String githubClienId;
    @Value(value = "${justAuth.clientSecret.github}")
    private String githubClientSecret;
    @Value(value = "${data.webSite.url}")
    private String webSiteUrl;
    @Value(value = "${data.web.url}")
    private String moguWebUrl;
    @Value(value = "${BLOG.USER_TOKEN_SURVIVAL_TIME}")
    private Long userTokenSurvivalTime;
    @Value(value = "${PROJECT_NAME_EN}")
    private String PROJECT_NAME_EN;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PictureFeignClient pictureFeignClient;

    @ApiOperation(value = "获取认证", notes = "获取认证")
    @RequestMapping("/render")
    public String renderAuth(String source, HttpServletResponse response) throws IOException {
        log.info("进入render:" + source);
        AuthRequest authRequest = getAuthRequest(source);
        String token = AuthStateUtils.createState();
        String authorizeUrl = authRequest.authorize(token);
        Map<String, String> map = new HashMap<>();
        map.put(SQLConfiguration.URL, authorizeUrl);
        return ResultUtil.result(SystemConfiguration.SUCCESS, map);
    }


    /**
     * oauth平台中配置的授权回调地址，以本项目为例，在创建gitee授权应用时的回调地址应为：http://127.0.0.1:8603/oauth/callback/gitee
     */
    @RequestMapping("/callback/{source}")
    public void login(@PathVariable("source") String source, AuthCallback callback, HttpServletRequest request, HttpServletResponse httpServletResponse) throws IOException {
        log.info("进入callback：" + source + " callback params：" + JSONObject.toJSONString(callback));
        AuthRequest authRequest = getAuthRequest(source);
        AuthResponse response = authRequest.login(callback);
        String result = JSONObject.toJSONString(response);
        Map<String, Object> map = JsonHelper.jsonToMap(result);
        Map<String, Object> data = JsonHelper.jsonToMap(JsonHelper.objectToJson(map.get(SystemConfiguration.DATA)));
        Map<String, Object> token = JsonHelper.jsonToMap(JsonHelper.objectToJson(data.get(SystemConfiguration.TOKEN)));
        String accessToken = token.get(SystemConfiguration.ACCESS_TOKEN).toString();

        Boolean exist = false;
        User user = null;
        //判断user是否存在
        if (data.get(SystemConfiguration.UUID) != null && data.get(SystemConfiguration.SOURCE) != null) {
            user = userService.getUserBySourceAnduuid(data.get(SystemConfiguration.SOURCE).toString(), data.get(SystemConfiguration.UUID).toString());
            if (user != null) {
                exist = true;
            } else {
                user = new User();
            }

        } else {
            return;
        }
        if (data.get(SystemConfiguration.EMAIL) != null) {
            String email = data.get(SystemConfiguration.EMAIL).toString();
            user.setEmail(email);
        }
        if (data.get(SystemConfiguration.AVATAR) != null) {
            // 获取到头像，然后上传到自己服务器
            FileVO fileVO = new FileVO();
            fileVO.setAdminUid("uid00000000000000000000000000000000");
            fileVO.setUserUid(user.getUid());
            fileVO.setProjectName(SystemConfiguration.BLOG);
            fileVO.setSortName(SystemConfiguration.ADMIN);
            List<String> urlList = new ArrayList<>();
            urlList.add(data.get(SystemConfiguration.AVATAR).toString());
            fileVO.setUrlList(urlList);
            String res = this.pictureFeignClient.uploadPicsByUrl(fileVO);
            Map<String, Object> resultMap = JsonHelper.jsonToMap(res);
            if (resultMap.get(SystemConfiguration.CODE) != null && SystemConfiguration.SUCCESS.equals(resultMap.get(SystemConfiguration.CODE).toString())) {
                if (resultMap.get(SystemConfiguration.DATA) != null) {
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) resultMap.get(SystemConfiguration.DATA);
                    if (listMap != null && listMap.size() > 0) {
                        Map<String, Object> pictureMap = listMap.get(0);
                        if (pictureMap != null && pictureMap.get(SystemConfiguration.PIC_URL) != null && pictureMap.get(SystemConfiguration.UID) != null) {
                            user.setAvatar(pictureMap.get(SystemConfiguration.UID).toString());
                            user.setPhotoUrl(pictureMap.get(SystemConfiguration.PIC_URL).toString());
                        }
                    }
                }
            }
        }
        if (data.get(SystemConfiguration.NICKNAME) != null) {
            user.setNickName(data.get(SystemConfiguration.NICKNAME).toString());
        }
        if (user.getLoginCount() == null) {
            user.setLoginCount(0);
        } else {
            user.setLoginCount(user.getLoginCount() + 1);
        }

        user.setLastLoginTime(new Date());
        user.setLastLoginIp(IpUtils.getIpAddr(request));
        if (exist) {
            user.updateById();
        } else {
            user.setUuid(data.get(SystemConfiguration.UUID).toString());
            user.setSource(data.get(SystemConfiguration.SOURCE).toString());
            user.setUserName(PROJECT_NAME_EN.concat("_").concat(user.getSource()).concat("_").concat(user.getUuid()));
            //产生(0,999999]之间的随机数
            Integer randNum = (int) (Math.random() * (999999) + 1);
            //进行六位数补全
            String workPassWord = String.format("%06d", randNum);
            user.setPassword(workPassWord);
            user.insert();
        }

        if (user != null) {
            //将从数据库查询的数据缓存到redis中
            stringRedisTemplate.opsForValue().set(SystemConfiguration.USER_TOEKN + SystemConfiguration.REDIS_SEGMENTATION + accessToken, JsonHelper.objectToJson(user), userTokenSurvivalTime, TimeUnit.SECONDS);
        }

        httpServletResponse.sendRedirect(webSiteUrl + "?token=" + accessToken);
    }

    @RequestMapping("/revoke/{source}/{token}")
    public Object revokeAuth(@PathVariable("source") String source, @PathVariable("token") String token) throws IOException {
        AuthRequest authRequest = getAuthRequest(source);
        return authRequest.revoke(AuthToken.builder().accessToken(token).build());
    }

    @RequestMapping("/refresh/{source}")
    public Object refreshAuth(@PathVariable("source") String source, String token) {
        AuthRequest authRequest = getAuthRequest(source);
        return authRequest.refresh(AuthToken.builder().refreshToken(token).build());
    }

    @ApiOperation(value = "获取用户信息", notes = "获取用户信息")
    @GetMapping("/verify/{accessToken}")
    public String verifyUser(@PathVariable("accessToken") String accessToken) {
        String userInfo = stringRedisTemplate.opsForValue().get(SystemConfiguration.USER_TOEKN + SystemConfiguration.REDIS_SEGMENTATION + accessToken);
        if (StringUtils.isEmpty(userInfo)) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.INVALID_TOKEN);
        } else {
            Map<String, Object> map = JsonHelper.jsonToMap(userInfo);
            return ResultUtil.result(SystemConfiguration.SUCCESS, map);
        }
    }

    @ApiOperation(value = "删除accessToken", notes = "删除accessToken")
    @RequestMapping("/delete/{accessToken}")
    public String deleteUserAccessToken(@PathVariable("accessToken") String accessToken) {
        stringRedisTemplate.delete(SystemConfiguration.USER_TOEKN + SystemConfiguration.REDIS_SEGMENTATION + accessToken);
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.DELETE_SUCCESS);
    }


    private AuthRequest getAuthRequest(String source) {
        AuthRequest authRequest = null;
        switch (source) {
            case SystemConfiguration.GITHUB:
                authRequest = new AuthGithubRequest(AuthConfig.builder()
                        .clientId(githubClienId)
                        .clientSecret(githubClientSecret)
                        .redirectUri(moguWebUrl + "/oauth/callback/github")
                        .build());
                break;
            case SystemConfiguration.GITEE:
                authRequest = new AuthGiteeRequest(AuthConfig.builder()
                        .clientId(giteeClienId)
                        .clientSecret(giteeClientSecret)
                        .redirectUri(moguWebUrl + "/oauth/callback/gitee")
                        .build());
                break;
            default:
                break;
        }
        if (null == authRequest) {
            throw new AuthException(MessageConfiguration.OPERATION_FAIL);
        }
        return authRequest;
    }
}

package org.fantasizer.theblog.xo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.fantasizer.common.global.BasicSQLConfiguration;
import org.fantasizer.common.helper.IpUtils;
import org.fantasizer.common.helper.JsonHelper;
import org.fantasizer.common.service.impl.BaseServiceImpl;
import org.fantasizer.theblog.xo.entity.User;
import org.fantasizer.theblog.xo.mapper.UserMapper;
import org.fantasizer.theblog.xo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 20:02
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserService userService;

    @Override
    public User insertUserInfo(HttpServletRequest request, String response) {
        Map<String, Object> map = JsonHelper.jsonToMap(response);
        boolean exist = false;
        User user = new User();
        Map<String, Object> data = JsonHelper.jsonToMap(JsonHelper.objectToJson(map.get("data")));
        if (data.get("uuid") != null && data.get("source") != null) {
            if (getUserBySourceAnduuid(data.get("source").toString(), data.get("uuid").toString()) != null) {
                user = getUserBySourceAnduuid(data.get("source").toString(), data.get("uuid").toString());
                exist = true;
            }
        } else {
            System.out.println("未获取到uuid或source");
            return null;
        }

        if (data.get("email") != null) {
            user.setEmail(data.get("email").toString());
        }
        if (data.get("avatar") != null) {
            user.setAvatar(data.get("avatar").toString());
        }
        if (data.get("nickname") != null) {
            user.setNickName(data.get("nickname").toString());
        }
        user.setLoginCount(user.getLoginCount() + 1);
        user.setLastLoginTime(new Date());
        user.setLastLoginIp(IpUtils.getIpAddr(request));
        if (exist) {
            user.updateById();
            System.out.println("updata");
        } else {
            /*初始化*/
            user.setUuid(data.get("uuid").toString());
            user.setSource(data.get("source").toString());
            user.setUserName("mg".concat(user.getSource()).concat(user.getUuid()));
            /**
             * //产生(0,999999]之间的随机数
             */
            Integer randNum = (int) (Math.random() * (999999) + 1);

            /**
             * //进行六位数补全
             */
            String workPassWord = String.format("%06d", randNum);
            user.setPassword(workPassWord);
            user.insert();
            System.out.println("insert");
        }
        return user;
    }

    @Override
    public User getUserBySourceAnduuid(String source, String uuid) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BasicSQLConfiguration.UUID, uuid).eq(BasicSQLConfiguration.SOURCE, source);
        return userService.getOne(queryWrapper);

    }
}

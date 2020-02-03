package org.fantasizer.theblog.xo.service;

import org.fantasizer.common.service.BaseService;
import org.fantasizer.theblog.xo.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:50
 */
public interface UserService extends BaseService<User> {

    /**
     * 记录用户信息
     *
     * @param response
     */
    User insertUserInfo(HttpServletRequest request, String response);

    /**
     * 通过source uuid获取用户类
     *
     * @param source
     * @param uuid
     * @return
     */
    User getUserBySourceAnduuid(String source, String uuid);
}

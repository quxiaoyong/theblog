package org.fantasizer.theblog.xo.service;

import org.fantasizer.common.service.BaseService;
import org.fantasizer.theblog.xo.entity.Todo;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:50
 */
public interface TodoService extends BaseService<Todo> {

    /**
     * 批量更新代办事项的状态
     *
     * @param done     : 状态
     * @param adminUid : 管理员UID
     */
    void toggleAll(Integer done, String adminUid);
}

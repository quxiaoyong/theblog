package org.fantasizer.theblog.xo.service.impl;

import org.fantasizer.theblog.common.service.impl.BaseServiceImpl;
import org.fantasizer.theblog.xo.entity.Todo;
import org.fantasizer.theblog.xo.mapper.TodoMapper;
import org.fantasizer.theblog.xo.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @Author Cruise Qu
 * @Date 2020-01-30 20:02
 */
@Service
public class TodoServiceImpl extends BaseServiceImpl<TodoMapper, Todo> implements TodoService {

    @Autowired
    TodoMapper todoMapper;

    @Override
    public void toggleAll(Integer done, String adminUid) {
        todoMapper.toggleAll(done, adminUid);
    }

}

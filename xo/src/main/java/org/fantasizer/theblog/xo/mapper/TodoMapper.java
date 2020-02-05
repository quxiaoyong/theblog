package org.fantasizer.theblog.xo.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.fantasizer.theblog.common.mapper.TheBlogMapper;
import org.fantasizer.theblog.xo.entity.Todo;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:38
 */
public interface TodoMapper extends TheBlogMapper<Todo> {
    /**
     * 批量更新未删除的代表事项的状态
     * <p>
     * TODO:此处的1应该换成枚举
     *
     * @param done
     */
    @Select("UPDATE t_todo SET done = #{done} WHERE STATUS = 1 AND admin_uid = #{adminUid}")
    void toggleAll(@Param("done") Integer done, @Param("adminUid") String adminUid);
}

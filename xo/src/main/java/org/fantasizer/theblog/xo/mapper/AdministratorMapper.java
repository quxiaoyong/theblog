package org.fantasizer.theblog.xo.mapper;

import org.apache.ibatis.annotations.Param;
import org.fantasizer.theblog.common.mapper.TheBlogMapper;
import org.fantasizer.theblog.xo.entity.Administrator;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:30
 */
public interface AdministratorMapper extends TheBlogMapper<Administrator> {

    /**
     * 通过uid获取管理员
     *
     * @return
     */
    Administrator getAdminByUid(@Param("uid") String uid);
}
package org.fantasizer.theblog.xo.vo;

import lombok.Data;
import org.fantasizer.common.validator.annnotation.NotBlank;
import org.fantasizer.common.validator.group.Insert;
import org.fantasizer.common.validator.group.Update;
import org.fantasizer.common.vo.BaseVO;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:46
 */
@Data
public class RoleVO extends BaseVO<RoleVO> {


    /**
     * 角色名称
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String roleName;

    /**
     * 介绍
     */
    private String summary;

    /**
     * 该角色所能管辖的区域
     */
    private String categoryMenuUids;

}
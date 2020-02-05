package org.fantasizer.theblog.xo.vo;

import lombok.Data;
import org.fantasizer.theblog.common.validator.annnotation.NotBlank;
import org.fantasizer.theblog.common.validator.group.Insert;
import org.fantasizer.theblog.common.validator.group.Update;
import org.fantasizer.theblog.common.vo.BaseVO;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:46
 */
@Data
public class LinkVO extends BaseVO<LinkVO> {

    /**
     * 友链标题
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String title;
    /**
     * 友链介绍
     */
    private String summary;
    /**
     * 友链地址
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String url;

    /**
     * 无参构造方法，初始化默认值
     */
    LinkVO() {

    }

}
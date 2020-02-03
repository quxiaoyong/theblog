package org.fantasizer.theblog.xo.vo;

import lombok.Data;
import org.fantasizer.common.validator.annnotation.NotBlank;
import org.fantasizer.common.validator.group.Insert;
import org.fantasizer.common.validator.group.Update;
import org.fantasizer.common.vo.BaseVO;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:43
 */
@Data
public class BlogCatalogVO extends BaseVO<BlogCatalogVO> {

    /**
     * 分类名
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String catalogName;
    /**
     * 分类介绍
     */
    private String content;

    /**
     * 无参构造方法
     */
    BlogCatalogVO() {

    }

}

package org.fantasizer.theblog.xo.vo;

import lombok.Data;
import org.fantasizer.common.validator.annnotation.NotBlank;
import org.fantasizer.common.validator.group.Insert;
import org.fantasizer.common.validator.group.Update;
import org.fantasizer.common.vo.BaseVO;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:47
 */
@Data
public class TagVO extends BaseVO<TagVO> {

    /**
     * 标签内容
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String content;

    /**
     * 无参构造方法，初始化默认值
     */
    TagVO() {

    }

}
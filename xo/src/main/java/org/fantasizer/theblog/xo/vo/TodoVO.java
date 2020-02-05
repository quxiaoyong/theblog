package org.fantasizer.theblog.xo.vo;

import lombok.Data;
import org.fantasizer.theblog.common.validator.annnotation.BooleanNotNull;
import org.fantasizer.theblog.common.validator.annnotation.NotBlank;
import org.fantasizer.theblog.common.validator.group.FetchOne;
import org.fantasizer.theblog.common.validator.group.Insert;
import org.fantasizer.theblog.common.validator.group.Update;
import org.fantasizer.theblog.common.vo.BaseVO;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:47
 */
@Data
public class TodoVO extends BaseVO<TodoVO> {

    /**
     * 内容
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String text;
    /**
     * 表示事项是否完成
     */
    @BooleanNotNull(groups = {Update.class, FetchOne.class})
    private Boolean done;


    /**
     * 无参构造方法，初始化默认值
     */
    TodoVO() {

    }

}
package org.fantasizer.common.vo;


import lombok.Data;
import org.fantasizer.common.validator.annnotation.IdValid;
import org.fantasizer.common.validator.group.Delete;
import org.fantasizer.common.validator.group.Update;

/**
 * 基础的VO对象，表现层的基础对象
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 21:48
 */
@Data
public class BaseVO<T> extends PageInfo<T> {

    /**
     * 唯一UID
     */
    @IdValid(groups = {Update.class, Delete.class})
    private String uid;

    private Integer status;

}

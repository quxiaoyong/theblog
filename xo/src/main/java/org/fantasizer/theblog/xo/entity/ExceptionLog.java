package org.fantasizer.theblog.xo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.fantasizer.common.entity.BaseEntity;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:19
 */
@Data
@TableName("t_exception_log")
public class ExceptionLog extends BaseEntity<ExceptionLog> {

    /**
     * 异常对象json格式
     */
    private String exceptionJson;

    /**
     * 异常简单信息,等同于e.getMessage
     */
    private String exceptionMessage;


}
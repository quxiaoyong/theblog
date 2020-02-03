package org.fantasizer.theblog.config.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;


/**
 * 在新增和删除时，自动填充一些字段的值
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 22:28
 */
@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {

    Logger log = LoggerFactory.getLogger(MetaObjectHandlerConfig.class);

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("插入方法填充");
        setFieldValByName("create_time", new Date(), metaObject);
        setFieldValByName("update_time", new Date(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("更新方法填充");
        setFieldValByName("update_time", new Date(), metaObject);
    }
}

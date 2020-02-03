package org.fantasizer.theblog.picture.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.fantasizer.theblog.picture.global.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {

    Logger log = LoggerFactory.getLogger(MetaObjectHandlerConfig.class);


    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("插入方法填充");
        setFieldValByName(SystemConfiguration.CREATE_TIME, new Date(), metaObject);
        setFieldValByName(SystemConfiguration.UPDATE_TIME, new Date(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("更新方法填充");
        setFieldValByName(SystemConfiguration.UPDATE_TIME, new Date(), metaObject);
    }
}

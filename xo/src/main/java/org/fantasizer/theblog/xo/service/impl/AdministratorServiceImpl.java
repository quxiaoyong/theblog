package org.fantasizer.theblog.xo.service.impl;

import org.fantasizer.common.service.impl.BaseServiceImpl;
import org.fantasizer.theblog.xo.entity.Administrator;
import org.fantasizer.theblog.xo.mapper.AdministratorMapper;
import org.fantasizer.theblog.xo.service.AdministratorService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 20:02
 */
public class AdministratorServiceImpl extends BaseServiceImpl<AdministratorMapper, Administrator> implements AdministratorService {


    @Autowired
    AdministratorMapper adminMapper;

    @Override
    public Administrator getAdministratorByUid(String uid) {
        return adminMapper.getAdminByUid(uid);
    }
}

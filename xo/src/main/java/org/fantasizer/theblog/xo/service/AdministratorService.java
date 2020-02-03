package org.fantasizer.theblog.xo.service;

import org.fantasizer.common.service.BaseService;
import org.fantasizer.theblog.xo.entity.Administrator;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:50
 */
public interface AdministratorService extends BaseService<Administrator> {

    Administrator getAdministratorByUid(String uid);

}

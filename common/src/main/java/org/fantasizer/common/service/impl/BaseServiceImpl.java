package org.fantasizer.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.fantasizer.common.mapper.TheBlogMapper;
import org.fantasizer.common.service.BaseService;

/**
 * SuperService 实现类（ 泛型：M 是  mapper(dao) 对象，T 是实体 ）
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 22:18
 */
public class BaseServiceImpl<M extends TheBlogMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<T> {

}

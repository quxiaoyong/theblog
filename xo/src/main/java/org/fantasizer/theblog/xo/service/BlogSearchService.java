package org.fantasizer.theblog.xo.service;


import org.fantasizer.theblog.xo.entity.Blog;

import java.util.List;
import java.util.Map;


/**
 * solr索引维护
 *
 * @Author Cruise Qu
 * @Date 2020-01-30 19:50
 */
public interface BlogSearchService {

    /**
     * 搜索
     *
     * @param keywords    关键字
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @return
     */
    Map<String, Object> search(String collection, String keywords, Integer currentPage, Integer pageSize);

    /**
     * 初始化索引
     *
     * @param
     * @return
     */
    void initIndex(String collection, List<Blog> blogList);

    /**
     * 添加索引
     *
     * @param
     * @return
     */
    void addIndex(String collection, Blog blog);

    /**
     * 更新索引
     *
     * @param
     * @return
     */
    void updateIndex(String collection, Blog blog);

    /**
     * 删除索引
     *
     * @param id
     * @return
     */
    void deleteIndex(String collection, String id);

    /**
     * 批量删除索引
     *
     * @param collection
     * @param ids
     */
    void deleteBatchIndex(String collection, List<String> ids);

    /**
     * 删除全部索引
     *
     * @param
     * @return
     */
    void deleteAllIndex(String collection);
}

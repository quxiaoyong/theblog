package org.fantasizer.theblog.xo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.fantasizer.theblog.common.service.BaseService;
import org.fantasizer.theblog.xo.entity.Blog;

import java.util.List;
import java.util.Map;


/**
 * 博客服务
 *
 * @Author Cruise Qu
 * @Date 2020-01-30 19:50
 */
public interface BlogService extends BaseService<Blog> {

    /**
     * 给博客列表设置标签
     *
     * @return
     */
    List<Blog> setTagByBlogList(List<Blog> list);

    /**
     * 给博客设置标签
     *
     * @param blog
     * @return
     */
    Blog setTagByBlog(Blog blog);

    /**
     * 给博客设置分类
     *
     * @param blog
     * @return
     */
    Blog setCatalogByBlog(Blog blog);

    /**
     * 通过推荐等级获取博客列表
     *
     * @param level
     * @return
     */
    List<Blog> getBlogListByLevel(Integer level);

    /**
     * 通过推荐等级获取博客Page
     *
     * @param level
     * @return
     */
    IPage<Blog> getBlogPageByLevel(Page<Blog> page, Integer level);

    /**
     * 通过状态获取博客数量
     *
     * @author xzx19950624@qq.com
     * @date 2018年10月22日下午3:30:28
     */
    Integer getBlogCount(Integer status);

    /**
     * TODO:未传参
     * 通过标签获取博客数目
     *
     * @author Administrator
     * @date 2019年6月19日16:28:16
     */
    List<Map<String, Object>> getBlogCountByTag();

    /**
     * 通过标签获取博客数目
     *
     * @author Administrator
     * @date 2019年11月27日13:14:34
     */
    List<Map<String, Object>> getBlogCountByBlogCatalog();

    /**
     * 设置博客版权
     *
     * @param blog
     * @return
     */
    void setBlogCopyright(Blog blog);

    /**
     * 获取一年内的文章贡献数
     *
     * @return
     */
    Map<String, Object> getBlogContributeCount();

}

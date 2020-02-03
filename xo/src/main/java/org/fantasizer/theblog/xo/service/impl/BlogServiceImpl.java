package org.fantasizer.theblog.xo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.fantasizer.common.enums.Publish;
import org.fantasizer.common.enums.Status;
import org.fantasizer.common.global.BasicSQLConfiguration;
import org.fantasizer.common.helper.DateUtils;
import org.fantasizer.common.helper.StringUtils;
import org.fantasizer.common.service.impl.BaseServiceImpl;
import org.fantasizer.theblog.xo.entity.Blog;
import org.fantasizer.theblog.xo.entity.BlogCatalog;
import org.fantasizer.theblog.xo.entity.Tag;
import org.fantasizer.theblog.xo.mapper.BlogCatalogMapper;
import org.fantasizer.theblog.xo.mapper.BlogMapper;
import org.fantasizer.theblog.xo.mapper.TagMapper;
import org.fantasizer.theblog.xo.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 20:02
 */
@Service
public class BlogServiceImpl extends BaseServiceImpl<BlogMapper, Blog> implements BlogService {

    @Autowired
    TagMapper tagMapper;

    @Autowired
    BlogCatalogMapper blogCatalogMapper;

    @Autowired
    BlogMapper blogMapper;

    @Override
    public List<Blog> setTagByBlogList(List<Blog> list) {
        for (Blog item : list) {
            if (item != null) {
                setTagByBlog(item);
            }
        }
        return list;
    }

    @Override
    public Blog setTagByBlog(Blog blog) {
        String tagUid = blog.getTagUid();
        if (!StringUtils.isEmpty(tagUid)) {
            String uids[] = tagUid.split(",");
            List<Tag> tagList = new ArrayList<Tag>();
            for (String uid : uids) {
                Tag tag = tagMapper.selectById(uid);
                if (tag != null && tag.getStatus() != Status.DELETED) {
                    tagList.add(tag);
                }
            }
            blog.setTagList(tagList);
        }
        return blog;
    }

    @Override
    public Blog setCatalogByBlog(Blog blog) {

        if (blog != null && !StringUtils.isEmpty(blog.getBlogSortUid())) {
            BlogCatalog blogCatalog = blogCatalogMapper.selectById(blog.getBlogSortUid());
            blog.setBlogCatalog(blogCatalog);
        }
        return blog;
    }

    @Override
    public List<Blog> getBlogListByLevel(Integer level) {
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BasicSQLConfiguration.LEVEL, level);
        queryWrapper.eq(BasicSQLConfiguration.IS_PUBLISH, Publish.PUBLISH);

        List<Blog> list = blogMapper.selectList(queryWrapper);
        return list;
    }

    @Override
    public IPage<Blog> getBlogPageByLevel(Page<Blog> page, Integer level) {
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BasicSQLConfiguration.LEVEL, level);
        queryWrapper.eq(BasicSQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.eq(BasicSQLConfiguration.IS_PUBLISH, Publish.PUBLISH);

        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals("content"));

        return blogMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Integer getBlogCount(Integer status) {
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BasicSQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.eq(BasicSQLConfiguration.IS_PUBLISH, Publish.PUBLISH);

        return blogMapper.selectCount(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getBlogCountByTag() {

        List<Map<String, Object>> blogCoutByTagMap = blogMapper.getBlogCountByTag();

        Map<String, Integer> tagMap = new HashMap<String, Integer>();

        for (Map<String, Object> item : blogCoutByTagMap) {
            String tagUid = String.valueOf(item.get("tag_uid"));
            // java.lang.Number是Integer,Long的父类
            Number num = (Number) item.get("count");
            Integer count = num.intValue();

            //如果只有一个UID的情况
            if (tagUid.length() == 32) {

                //如果没有这个内容的话，就设置
                if (tagMap.get(tagUid) == null) {
                    tagMap.put(tagUid, count);
                } else {
                    Integer tempCount = tagMap.get(tagUid) + count;
                    tagMap.put(tagUid, tempCount);
                }

            } else {
                //如果长度大于32，说明含有多个UID
                if (StringUtils.isNotEmpty(tagUid)) {
                    List<String> strList = StringUtils.changeStringToString(tagUid, ",");
                    for (String strItem : strList) {
                        if (tagMap.get(strItem) == null) {
                            tagMap.put(strItem, count);
                        } else {
                            Integer tempCount = tagMap.get(strItem) + count;
                            tagMap.put(strItem, tempCount);
                        }
                    }
                }
            }
        }

        //把查询到的Tag放到Map中
        Set<String> tagUids = tagMap.keySet();
        Collection<Tag> tagCollection = new ArrayList<>();

        if (tagUids.size() > 0) {
            tagCollection = tagMapper.selectBatchIds(tagUids);
        }

        Map<String, String> tagEntityMap = new HashMap<>();
        for (Tag tag : tagCollection) {
            if (StringUtils.isNotEmpty(tag.getContent())) {
                tagEntityMap.put(tag.getUid(), tag.getContent());
            }
        }

        List<Map<String, Object>> resultMap = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {

            String tagUid = entry.getKey();

            if (tagEntityMap.get(tagUid) != null) {
                String tagName = tagEntityMap.get(tagUid);
                Integer count = entry.getValue();

                Map<String, Object> itemResultMap = new HashMap<>();
                itemResultMap.put("tagUid", tagUid);
                itemResultMap.put("name", tagName);
                itemResultMap.put("value", count);
                resultMap.add(itemResultMap);
            }
        }

        return resultMap;

    }

    @Override
    public List<Map<String, Object>> getBlogCountByBlogCatalog() {

        List<Map<String, Object>> blogCoutByBlogSortMap = blogMapper.getBlogCountByBlogSort();

        Map<String, Integer> blogSortMap = new HashMap<>();

        for (Map<String, Object> item : blogCoutByBlogSortMap) {

            String blogSortUid = String.valueOf(item.get("blog_sort_uid"));
            // java.lang.Number是Integer,Long的父类
            Number num = (Number) item.get("count");
            Integer count = 0;
            if (num != null) {
                count = num.intValue();
            }

            blogSortMap.put(blogSortUid, count);
        }

        //把查询到的BlogSort放到Map中
        Set<String> blogSortUids = blogSortMap.keySet();
        Collection<BlogCatalog> blogCatalogCollection = new ArrayList<>();

        if (blogSortUids.size() > 0) {
            blogCatalogCollection = blogCatalogMapper.selectBatchIds(blogSortUids);
        }

        Map<String, String> blogSortEntityMap = new HashMap<>();
        for (BlogCatalog blogCatalog : blogCatalogCollection) {
            if (StringUtils.isNotEmpty(blogCatalog.getCatalogName())) {
                blogSortEntityMap.put(blogCatalog.getUid(), blogCatalog.getCatalogName());
            }
        }

        List<Map<String, Object>> resultMap = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Integer> entry : blogSortMap.entrySet()) {

            String blogSortUid = entry.getKey();

            if (blogSortEntityMap.get(blogSortUid) != null) {
                String blogSortName = blogSortEntityMap.get(blogSortUid);
                Integer count = entry.getValue();

                Map<String, Object> itemResultMap = new HashMap<>();
                itemResultMap.put("blogSortUid", blogSortUid);
                itemResultMap.put("name", blogSortName);
                itemResultMap.put("value", count);
                resultMap.add(itemResultMap);
            }
        }

        return resultMap;

    }

    @Override
    public void setBlogCopyright(Blog blog) {

        //如果是原创的话
        if (blog.getIsOriginal().equals("1")) {
            String str = "本文为蘑菇博客原创文章, 转载无需和我联系, 但请注明来自蘑菇博客 http://www.moguit.cn";
            blog.setCopyright(str);
        } else {
            String str = "本着开源共享、共同学习的精神, 本文转载自 " + blog.getArticlesPart() + " , 版权归 " + blog.getAuthor() + " 所有，如果侵权之处，请联系博主进行删除，谢谢~";
            blog.setCopyright(str);
        }
    }

    @Override
    public Map<String, Object> getBlogContributeCount() {

        // 获取今天结束时间
        String endTime = DateUtils.getNowTime();

        // 获取365天前的日期
        Date temp = DateUtils.getDate(endTime, -365);

        String startTime = DateUtils.dateTimeToStr(temp);

        List<Map<String, Object>> blogContributeMap = blogMapper.getBlogContributeCount(startTime, endTime);

        List<String> dateList = DateUtils.getDayBetweenDates(startTime, endTime);

        Map<String, Object> dateMap = new HashMap<>();

        for (Map<String, Object> itemMap : blogContributeMap) {

            dateMap.put(itemMap.get("DATE").toString(), itemMap.get("COUNT"));
        }

        List<List<Object>> resultList = new ArrayList<>();
        for (String item : dateList) {
            Integer count = 0;
            if (dateMap.get(item) != null) {
                count = Integer.valueOf(dateMap.get(item).toString());
            }
            List<Object> objectList = new ArrayList<>();
            objectList.add(item);
            objectList.add(count);
            resultList.add(objectList);
        }

        Map<String, Object> resultMap = new HashMap<>();
        List<String> contributeDateList = new ArrayList<>();
        contributeDateList.add(startTime);
        contributeDateList.add(endTime);
        resultMap.put("contributeDate", contributeDateList);
        resultMap.put("blogContributeCount", resultList);

        return resultMap;
    }

}

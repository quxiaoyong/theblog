package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.common.enums.Status;
import org.fantasizer.common.helper.ResultUtil;
import org.fantasizer.common.helper.StringUtils;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.xo.entity.Menu;
import org.fantasizer.theblog.xo.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


@RestController
@RequestMapping("/menu")
public class MenuRestApi {

    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);
    @Autowired
    MenuService menuService;

    @ApiOperation(value = "获取菜单列表", notes = "获取菜单列表", response = String.class)
    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    public String getList(HttpServletRequest request,
                          @ApiParam(name = "keyword", value = "关键字", required = false) @RequestParam(name = "keyword", required = false) String keyword,
                          @ApiParam(name = "menuLevel", value = "菜单级别", required = false) @RequestParam(name = "menuLevel", required = false, defaultValue = "0") Integer menuLevel,
                          @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                          @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(keyword) && !StringUtils.isEmpty(keyword.trim())) {
            queryWrapper.like(SQLConfiguration.NAME, keyword.trim());
        }

        if (menuLevel != 0) {
            queryWrapper.eq(SQLConfiguration.MENU_LEVEL, menuLevel);
        }

        Page<Menu> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        IPage<Menu> pageList = menuService.page(page, queryWrapper);
        List<Menu> list = pageList.getRecords();

        List<String> ids = new ArrayList<String>();
        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getParentUid())) {
                ids.add(item.getParentUid());
            }
        });

        if (ids.size() > 0) {
            Collection<Menu> parentList = menuService.listByIds(ids);

            Map<String, Menu> map = new HashMap<String, Menu>();
            parentList.forEach(item -> {
                map.put(item.getUid(), item);
            });

            list.forEach(item -> {
                if (StringUtils.isNotEmpty(item.getParentUid())) {
                    item.setParentCategoryMenu(map.get(item.getParentUid()));
                }
            });

            resultMap.put(SystemConfiguration.OTHER_DATA, parentList);
        }

        pageList.setRecords(list);
        log.info("返回结果");

        resultMap.put(SystemConfiguration.DATA, pageList);
        return ResultUtil.result(SystemConfiguration.SUCCESS, resultMap);
    }

    @ApiOperation(value = "获取所有菜单列表", notes = "获取所有列表", response = String.class)
    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    public String getAll(HttpServletRequest request) {

        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.MENU_LEVEL, "1");
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        List<Menu> list = menuService.list(queryWrapper);

        //获取所有的ID，去寻找他的子目录
        List<String> ids = new ArrayList<String>();
        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getUid())) {
                ids.add(item.getUid());
            }
        });

        QueryWrapper<Menu> childWrapper = new QueryWrapper<>();
        childWrapper.in(SQLConfiguration.PARENT_UID, ids);
        Collection<Menu> childList = menuService.list(childWrapper);
        for (Menu parentItem : list) {

            List<Menu> tempList = new ArrayList<>();

            for (Menu item : childList) {

                if (item.getParentUid().equals(parentItem.getUid())) {
                    tempList.add(item);
                }
            }
            Collections.sort(tempList, new Comparator<Menu>() {

                /*
                 * int compare(CategoryMenu p1, CategoryMenu p2) 返回一个基本类型的整型，
                 * 返回负数表示：p1 小于p2，
                 * 返回0 表示：p1和p2相等，
                 * 返回正数表示：p1大于p2
                 */
                @Override
                public int compare(Menu o1, Menu o2) {

                    //按照CategoryMenu的Sort进行降序排列
                    if (o1.getSort() > o2.getSort()) {
                        return -1;
                    }
                    if (o1.getSort().equals(o2.getSort())) {
                        return 0;
                    }
                    return 1;
                }

            });
            parentItem.setChildCategoryMenu(tempList);
        }
        return ResultUtil.result(SystemConfiguration.SUCCESS, list);
    }

    @OperationLogger(value = "增加菜单")
    @ApiOperation(value = "增加菜单", notes = "增加菜单", response = String.class)
    @PostMapping("/add")
    public String add(HttpServletRequest request,
                      @ApiParam(name = "categoryMenu", value = "菜单", required = false) @RequestBody(required = false) Menu categoryMenu) {

        if (StringUtils.isEmpty(categoryMenu.getName()) || StringUtils.isEmpty(categoryMenu.getUrl())) {
            return ResultUtil.result(SystemConfiguration.ERROR, "必填项不能为空");
        }
        //如果是一级菜单，将父ID清空
        if (categoryMenu.getMenuLevel() == 1) {
            categoryMenu.setParentUid("");
        }
        categoryMenu.insert();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "添加成功");
    }

    @ApiOperation(value = "编辑菜单", notes = "编辑菜单", response = String.class)
    @PostMapping("/edit")
    public String edit(HttpServletRequest request,
                       @ApiParam(name = "categoryMenu", value = "菜单", required = false) @RequestBody(required = false) Menu categoryMenu) {

        if (StringUtils.isEmpty(categoryMenu.getUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }
        categoryMenu.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "编辑成功");
    }

    @OperationLogger(value = "删除菜单")
    @ApiOperation(value = "删除菜单", notes = "删除菜单", response = String.class)
    @PostMapping("/delete")
    public String delete(HttpServletRequest request,
                         @ApiParam(name = "uid", value = "唯一UID", required = true) @RequestParam(name = "uid", required = true) String uid) {

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }
        Menu blogSort = menuService.getById(uid);
        blogSort.setStatus(Status.DELETED);
        blogSort.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "删除成功");
    }

    /**
     * 如果是一级菜单，直接置顶在最前面，二级菜单，就在一级菜单内置顶
     *
     * @author xzx19950624@qq.com
     * @date 2018年11月29日上午9:22:59
     */
    @OperationLogger(value = "置顶菜单")
    @ApiOperation(value = "置顶菜单", notes = "置顶菜单", response = String.class)
    @PostMapping("/stick")
    public String stick(HttpServletRequest request,
                        @ApiParam(name = "uid", value = "唯一UID", required = true) @RequestParam(name = "uid", required = true) String uid) {


        Menu categoryMenu = menuService.getById(uid);

        if (categoryMenu == null) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }

        //查找出最大的那一个
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();

        //如果是二级目录，就在当前的兄弟中，找出最大的一个
        if (categoryMenu.getMenuLevel() == 2) {

            queryWrapper.eq(SQLConfiguration.PARENT_UID, categoryMenu.getParentUid());

        }

        queryWrapper.eq(SQLConfiguration.MENU_LEVEL, categoryMenu.getMenuLevel());

        queryWrapper.orderByDesc(SQLConfiguration.SORT);

        queryWrapper.last("limit 1");

        Menu maxSort = menuService.getOne(queryWrapper);

        if (StringUtils.isEmpty(maxSort.getUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }

        Integer sortCount = maxSort.getSort() + 1;

        categoryMenu.setSort(sortCount);

        categoryMenu.updateById();

        return ResultUtil.result(SystemConfiguration.SUCCESS, "置顶成功");
    }

}


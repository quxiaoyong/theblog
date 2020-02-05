package org.fantasizer.theblog.common.controller;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO:JSON处理换成fastjson
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 22:08
 */
public class BaseController {
    /**
     * 获取一个map
     * <p>
     * TODO:意义何在
     *
     * @return
     */
    @Deprecated
    public static Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();
        return map;
    }

    /**
     * 将map转换成json字符串
     *
     * @param map
     * @return
     */
    public String toJson(Map<String, Object> map) {
        return JSONObject.fromObject(map).toString();
    }

    public <T> String toJson(List<T> list) {
        return JSONObject.fromObject(list).toString();
    }
}

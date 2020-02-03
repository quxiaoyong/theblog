package org.fantasizer.theblog.xo.service;

import org.fantasizer.common.service.BaseService;
import org.fantasizer.theblog.xo.entity.WebVisit;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:50
 */
public interface WebVisitService extends BaseService<WebVisit> {

    /**
     * 增加访问记录
     *
     * @param userUid
     * @param request
     * @param behavior
     * @param moduleUid
     * @param otherData
     */
    void addWebVisit(String userUid, HttpServletRequest request, String behavior, String moduleUid, String otherData);

    /**
     * 获取今日网站访问人数
     *
     * @return
     */
    int getWebVisitCount();

    /**
     * 获取近七天的访问量
     *
     * @return {
     * date: ["2019-6-20","2019-6-21","2019-6-22","2019-6-23","2019-6-24",,"2019-6-25","2019-6-26"]
     * pv: [10,5,6,7,5,3,2]
     * uv: [5,3,4,4,5,2,1]
     * }
     * 注：PV表示访问量   UV表示独立用户数
     */
    Map<String, Object> getVisitByWeek();

}

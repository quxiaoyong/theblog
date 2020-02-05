package org.fantasizer.theblog.xo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.fantasizer.theblog.common.enums.Status;
import org.fantasizer.theblog.common.global.BasicSQLConfiguration;
import org.fantasizer.theblog.common.helper.DateUtils;
import org.fantasizer.theblog.common.helper.IpUtils;
import org.fantasizer.theblog.common.service.impl.BaseServiceImpl;
import org.fantasizer.theblog.xo.entity.WebVisit;
import org.fantasizer.theblog.xo.mapper.WebVisitMapper;
import org.fantasizer.theblog.xo.service.WebVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 20:02
 */
@Service
public class WebVisitServiceImpl extends BaseServiceImpl<WebVisitMapper, WebVisit> implements WebVisitService {

    @Autowired
    WebVisitMapper webVisitMapper;

    @Override
    public void addWebVisit(String userUid, HttpServletRequest request, String behavior, String moduleUid, String otherData) {

        //增加记录（可以考虑使用AOP）
        Map<String, String> map = IpUtils.getOsAndBrowserInfo(request);
        String os = map.get("OS");
        String browser = map.get("BROWSER");
        WebVisit webVisit = new WebVisit();
        webVisit.setIp(IpUtils.getIpAddr(request));
        webVisit.setOs(os);
        webVisit.setBrowser(browser);
        webVisit.setUserUid(userUid);
        webVisit.setBehavior(behavior);
        webVisit.setModuleUid(moduleUid);
        webVisit.setOtherData(otherData);
        webVisit.insert();
    }

    @Override
    public int getWebVisitCount() {

        QueryWrapper<WebVisit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BasicSQLConfiguration.STATUS, Status.ENABLED);
        // 获取今日开始和结束时间
        String startTime = DateUtils.getToDayStartTime();
        String endTime = DateUtils.getToDayEndTime();
        queryWrapper.between(BasicSQLConfiguration.CREATE_TIME, startTime, endTime);
        List<WebVisit> list = webVisitMapper.selectList(queryWrapper);
        Set<String> ipSet = new HashSet<String>();

        // 根据IP统计访问今日访问次数
        for (WebVisit webVisit : list) {
            if (!"".equals(webVisit.getIp())) {
                ipSet.add(webVisit.getIp());
            }
        }

        return ipSet.size();
    }

    @Override
    public Map<String, Object> getVisitByWeek() {

        // 获取到今天结束的时间
        String todayEndTime = DateUtils.getToDayEndTime();

        //获取最近七天的日期
        Date sevenDaysDate = DateUtils.getDate(todayEndTime, -6);

        String sevenDays = DateUtils.getOneDayStartTime(sevenDaysDate);

        // 获取最近七天的数组列表
        List<String> sevenDaysList = DateUtils.getDaysByN(7, "yyyy-MM-dd");
        // 获得最近七天的访问量
        List<Map<String, Object>> pvMap = webVisitMapper.getPVByWeek(sevenDays, todayEndTime);
        // 获得最近七天的独立用户
        List<Map<String, Object>> uvMap = webVisitMapper.getUVByWeek(sevenDays, todayEndTime);

        Map<String, Object> countPVMap = new HashMap<String, Object>();
        Map<String, Object> countUVMap = new HashMap<String, Object>();

        for (Map<String, Object> item : pvMap) {
            countPVMap.put(item.get("DATE").toString(), item.get("COUNT"));
        }
        for (Map<String, Object> item : uvMap) {
            countUVMap.put(item.get("DATE").toString(), item.get("COUNT"));
        }
        // 访问量数组
        List<Integer> pvList = new ArrayList<>();
        // 独立用户数组
        List<Integer> uvList = new ArrayList<>();

        for (String day : sevenDaysList) {
            if (countPVMap.get(day) != null) {
                Number pvNumber = (Number) countPVMap.get(day);
                Number uvNumber = (Number) countUVMap.get(day);
                pvList.add(pvNumber.intValue());
                uvList.add(uvNumber.intValue());
            } else {
                pvList.add(0);
                uvList.add(0);
            }
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();

        // 不含年份的数组格式
        List<String> resultSevenDaysList = DateUtils.getDaysByN(7, "MM-dd");

        resultMap.put("date", resultSevenDaysList);
        resultMap.put("pv", pvList);
        resultMap.put("uv", uvList);

        return resultMap;
    }

}

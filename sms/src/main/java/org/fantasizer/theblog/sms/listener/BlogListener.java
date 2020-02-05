package org.fantasizer.theblog.sms.listener;

import org.fantasizer.theblog.common.helper.JsonHelper;
import org.fantasizer.theblog.sms.global.SystemConfiguration;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * 博客监听器(用于更新Redis和索引)
 *
 * @author Cruise qu
 */
@Component
public class BlogListener {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

// TODO 在这里同时需要对Redis和Solr进行操作，同时利用MQ来保证数据一致性

    @RabbitListener(queues = "mogu.blog")
    public void updateRedis(Map<String, String> map) throws ParseException {

        if (map != null) {

            String comment = map.get(SystemConfiguration.COMMAND);

            //从Redis清空对应的数据
            stringRedisTemplate.opsForValue().set(SystemConfiguration.BLOG_LEVEL + SystemConfiguration.REDIS_SEGMENTATION + SystemConfiguration.ONE, "");
            stringRedisTemplate.opsForValue().set(SystemConfiguration.BLOG_LEVEL + SystemConfiguration.REDIS_SEGMENTATION + SystemConfiguration.TWO, "");
            stringRedisTemplate.opsForValue().set(SystemConfiguration.BLOG_LEVEL + SystemConfiguration.REDIS_SEGMENTATION + SystemConfiguration.THREE, "");
            stringRedisTemplate.opsForValue().set(SystemConfiguration.BLOG_LEVEL + SystemConfiguration.REDIS_SEGMENTATION + SystemConfiguration.FOUR, "");
            stringRedisTemplate.opsForValue().set(SystemConfiguration.HOT_BLOG, "");
            stringRedisTemplate.opsForValue().set(SystemConfiguration.NEW_BLOG, "");

            if(SystemConfiguration.DELETE_BATCH.equals(comment)) {

                stringRedisTemplate.opsForValue().set("BLOG_SORT_BY_MONTH:", "");
                stringRedisTemplate.opsForValue().set("MONTH_SET", "");

            } else {
                String level = map.get(SystemConfiguration.LEVEL);

                String createTime = map.get(SystemConfiguration.CREATE_TIME);
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");
                Date myString = DateFormat.getDateTimeInstance().parse(createTime);
                String sd = sdf.format(myString);
                String [] list = sd.split("-");
                System.out.println(createTime);
                String year = list[0];
                String month = list[1];
                String key = year + "年" + month + "月";
                System.out.println(key);
                stringRedisTemplate.opsForValue().set("BLOG_SORT_BY_MONTH:" + key, "");

                String jsonResult = stringRedisTemplate.opsForValue().get("MONTH_SET");
                ArrayList<String> monthSet = (ArrayList<String>) JsonHelper.jsonArrayToArrayList(jsonResult);
                Boolean haveMonth = false;
                if(monthSet != null) {
                    for (String item : monthSet) {
                        if (item.equals(key)) {
                            haveMonth = true;
                            break;
                        }
                    }
                    if(!haveMonth) {
                        monthSet.add(key);
                        stringRedisTemplate.opsForValue().set("MONTH_SET", JsonHelper.objectToJson(monthSet));
                    }
                }
            }
        }
    }
}

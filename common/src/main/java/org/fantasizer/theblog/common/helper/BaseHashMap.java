package org.fantasizer.theblog.common.helper;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO:该类的用意何在？？
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 22:19
 */
public class BaseHashMap implements ApplicationContextAware {

    private BaseHashMap() {

    }

    private static ApplicationContext applicationContext;

    /**
     * 获取一个map
     *
     * @return
     */
    public static Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();
        return map;
    }

    /**
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (BaseHashMap.applicationContext == null) {
            BaseHashMap.applicationContext = applicationContext;
        }
    }


    /**
     * 通过name获取 Bean.
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }


    /**
     * 通过class获取Bean.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}
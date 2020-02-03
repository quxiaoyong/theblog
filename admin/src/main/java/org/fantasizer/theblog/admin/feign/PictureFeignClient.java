package org.fantasizer.theblog.admin.feign;

import org.fantasizer.theblog.admin.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author Cruise Qu
 * @Date 2020-01-29 22:45
 */

@FeignClient(name = "picture", url = "http://localhost:8602/", configuration = FeignConfiguration.class)
public interface PictureFeignClient {


    /**
     * 获取文件的信息接口
     *
     * @return
     * @ApiImplicitParam(name = "fileIds", value = "fileIds", required = false, dataType = "String"),
     * @ApiImplicitParam(name = "code", value = "分割符", required = false, dataType = "String")
     */
    @RequestMapping(value = "/file/getPicture", method = RequestMethod.GET)
    String getPicture(@RequestParam("fileIds") String fileIds, @RequestParam("code") String code);

    @RequestMapping(value = "/file/hello", method = RequestMethod.GET)
    String hello();
}
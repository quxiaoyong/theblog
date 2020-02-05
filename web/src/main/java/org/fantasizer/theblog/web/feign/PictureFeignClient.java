package org.fantasizer.theblog.web.feign;

import org.fantasizer.theblog.common.vo.FileVO;
import org.fantasizer.theblog.web.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mogu-picture", url = "http://localhost:8602/", configuration = FeignConfiguration.class)
public interface PictureFeignClient {


    /**
     * 获取文件的信息接口
     * @param fileIds
     * @param code
     * @return
     */
    @RequestMapping(value = "/file/getPicture", method = RequestMethod.GET)
     String getPicture(@RequestParam("fileIds") String fileIds, @RequestParam("code") String code);

    /**
     * 通过URL List上传图片
     * @param fileVO
     * @return
     */
    @RequestMapping(value = "/file/uploadPicsByUrl", method = RequestMethod.POST)
     String uploadPicsByUrl(FileVO fileVO);

    @RequestMapping(value = "/file/hello", method = RequestMethod.GET)
     String hello();
}

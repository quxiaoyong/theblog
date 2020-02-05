package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.fantasizer.theblog.common.helper.ResultUtil;
import org.fantasizer.theblog.common.helper.StringUtils;
import org.fantasizer.theblog.common.helper.WebUtils;
import org.fantasizer.theblog.common.validator.group.Update;
import org.fantasizer.theblog.admin.feign.PictureFeignClient;
import org.fantasizer.theblog.admin.global.MessageConfiguration;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.xo.entity.WebConfig;
import org.fantasizer.theblog.xo.service.WebConfigService;
import org.fantasizer.theblog.xo.vo.WebConfigVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@Api(value = "系统配置RestApi", tags = {"WebConfigRestApi"})
@RestController
@RequestMapping("/webConfig")
public class WebConfigRestApi {

    @Autowired
    WebConfigService webConfigService;

    @Autowired
    private PictureFeignClient pictureFeignClient;

    @ApiOperation(value = "获取网站配置", notes = "获取网站配置")
    @GetMapping("/getWebConfig")
    public String getWebConfig() {

        QueryWrapper<WebConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);
        WebConfig webConfig = webConfigService.getOne(queryWrapper);

        //获取图片
        if (webConfig != null && StringUtils.isNotEmpty(webConfig.getLogo())) {
            String pictureList = this.pictureFeignClient.getPicture(webConfig.getLogo(), SystemConfiguration.FILE_SEGMENTATION);
            webConfig.setPhotoList(WebUtils.getPicture(pictureList));
        }

        //获取支付宝收款二维码
        if (webConfig != null && StringUtils.isNotEmpty(webConfig.getAliPay())) {
            String pictureList = this.pictureFeignClient.getPicture(webConfig.getAliPay(), SystemConfiguration.FILE_SEGMENTATION);
            if (WebUtils.getPicture(pictureList).size() > 0) {
                webConfig.setAliPayPhoto(WebUtils.getPicture(pictureList).get(0));
            }

        }
        //获取微信收款二维码
        if (webConfig != null && StringUtils.isNotEmpty(webConfig.getWeixinPay())) {
            String pictureList = this.pictureFeignClient.getPicture(webConfig.getWeixinPay(), SystemConfiguration.FILE_SEGMENTATION);
            if (WebUtils.getPicture(pictureList).size() > 0) {
                webConfig.setWeixinPayPhoto(WebUtils.getPicture(pictureList).get(0));
            }

        }

        return ResultUtil.result(SystemConfiguration.SUCCESS, webConfig);
    }

    @OperationLogger(value = "修改网站配置")
    @ApiOperation(value = "修改网站配置", notes = "修改网站配置")
    @PostMapping("/editWebConfig")
    public String editWebConfig(@Validated({Update.class}) @RequestBody WebConfigVO webConfigVO, BindingResult result) {

        if (StringUtils.isEmpty(webConfigVO.getUid())) {

            WebConfig webConfig = new WebConfig();
            webConfig.setLogo(webConfigVO.getLogo());
            webConfig.setName(webConfigVO.getName());
            webConfig.setTitle(webConfigVO.getTitle());
            webConfig.setSummary(webConfigVO.getSummary());
            webConfig.setKeyword(webConfigVO.getKeyword());
            webConfig.setAuthor(webConfigVO.getAuthor());
            webConfig.setRecordNum(webConfigVO.getRecordNum());
            webConfig.setAliPay(webConfigVO.getAliPay());
            webConfig.setWeixinPay(webConfigVO.getWeixinPay());
            webConfig.setStartComment(webConfigVO.getStartComment());

            webConfigService.save(webConfig);
        } else {
            WebConfig webConfig = webConfigService.getById(webConfigVO.getUid());
            webConfig.setLogo(webConfigVO.getLogo());
            webConfig.setName(webConfigVO.getName());
            webConfig.setTitle(webConfigVO.getTitle());
            webConfig.setSummary(webConfigVO.getSummary());
            webConfig.setKeyword(webConfigVO.getKeyword());
            webConfig.setAuthor(webConfigVO.getAuthor());
            webConfig.setRecordNum(webConfigVO.getRecordNum());
            webConfig.setAliPay(webConfigVO.getAliPay());
            webConfig.setWeixinPay(webConfigVO.getWeixinPay());
            webConfig.setStartComment(webConfigVO.getStartComment());
            webConfigService.updateById(webConfig);
        }
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.UPDATE_SUCCESS);
    }
}


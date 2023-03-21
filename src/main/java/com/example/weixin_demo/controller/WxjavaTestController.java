package com.example.weixin_demo.controller;

import com.example.weixin_demo.utils.RedisService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/wxjava/mp")
@Slf4j
public class WxjavaTestController {
    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private WxMpConfigStorage wxMpConfigStorage;

    @Autowired
    WxMpMessageRouter wxMpMessageRouter;

    @Autowired
    RedisService redisUtils;

    /**
     * @param signature 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param echostr   随机字符串
     * @return
     */
    @GetMapping("message")
    public String configAccess(String signature, String timestamp, String nonce, String echostr) {
        // 校验签名
        if (wxMpService.checkSignature(timestamp, nonce, signature)) {
            // 校验成功原样返回echostr
            log.info(signature);
            log.info(timestamp);
            log.info(nonce);
            log.info(echostr);
            return echostr;
        }
        // 校验失败
        return null;
    }

    @PostMapping(value = "message", produces = "application/xml; charset=UTF-8")
    public String handleMessage(@RequestBody String requestBody,
                                @RequestParam("signature") String signature,
                                @RequestParam("timestamp") String timestamp,
                                @RequestParam("nonce") String nonce) {
        log.info("接到请求");
        // 校验消息是否来自微信
        if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }
        // 解析消息体，封装为对象
        WxMpXmlMessage wxMessage = WxMpXmlMessage.fromXml(requestBody);
        Long msgId = wxMessage.getMsgId();
        Object cacheObject = redisUtils.getCacheObject("msgId:" + msgId);
        if (cacheObject != null) return cacheObject.toString();
        WxMpXmlOutMessage outMessage;
        try {
            // 将消息路由给对应的处理器，获取响应
            outMessage = wxMpMessageRouter.route(wxMessage);
        } catch (Exception e) {
            log.error("微信消息路由异常", e);
            outMessage = null;
        }
        // 将响应消息转换为xml格式返回
        String result = outMessage == null ? "" : outMessage.toXml();
        redisUtils.setCacheObject("msgId:" + msgId, result, 15L, TimeUnit.SECONDS);
        return result;
    }

}

package com.example.weixin_demo.handler;

import com.example.weixin_demo.utils.ChatUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class TextHandler implements WxMpMessageHandler {


    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context,
                                    WxMpService wxMpService,
                                    WxSessionManager sessionManager) throws WxErrorException {
        long l = System.currentTimeMillis();
        // 接收的消息内容
        String inContent = wxMessage.getContent();

//        log.info("接收的消息内容:" + inContent);
        // 响应的消息内容
        String outContent = "";
        if (StringUtils.isNotBlank(inContent)) {
            outContent = ChatUtils.chat(inContent);
            log.info("返回的消息内容:" + outContent);
        }
        long l2 = System.currentTimeMillis();
        log.info("耗时:" + (l2 - l) / 1000 + "秒");
        // 构造响应消息对象
        return WxMpXmlOutMessage.TEXT().content(outContent).fromUser(wxMessage.getToUser())
                .toUser(wxMessage.getFromUser()).build();
    }
}
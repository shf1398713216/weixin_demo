package com.example.weixin_demo.utils;

import cn.hutool.core.convert.ConvertException;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatUtils {

    /**
     * 聊天端点
     */
    public static final String chatEndpoint = "https://api.openai.com/v1/chat/completions";
    /**
     * api密匙
     */
    public static final String apiKey = "Bearer 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";

    /**
     * 发送消息
     *
     * @param txt 内容
     * @return {@link String}
     */
    public static String chat(String txt) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("model", "gpt-3.5-turbo");
        paramMap.put("max_tokens", 1000);
        List<Map<String, Object>> dataList = new ArrayList<>();
        dataList.add(new HashMap<String, Object>() {{
            put("role", "user");
            put("content", txt);

        }});
        paramMap.put("messages", dataList);
        String s = JSONUtil.toJsonStr(paramMap);
        JSONObject message = null;
        try {
            String body = HttpRequest.post(chatEndpoint)
                    .setHttpProxy("127.0.0.1", 7890)
                    .header("Authorization", apiKey)
                    .header("Content-Type", "application/json")
                    .body(s)
                    .execute()
                    .body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            JSONArray choices = jsonObject.getJSONArray("choices");
            JSONObject result = choices.get(0, JSONObject.class, Boolean.TRUE);
            message = result.getJSONObject("message");
        } catch (HttpException e) {
            return "出现了异常";
        } catch (ConvertException e) {
            return "出现了异常";
        }
        return message.getStr("content");
    }
}

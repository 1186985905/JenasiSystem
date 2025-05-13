package com.bx.implatform.util;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class AIMsg {

    private static String AIKey = "app-hqRMumwmUvpRrZTsXqeCHe89";

    public static String getAIMsg(String userMessage) {
        System.out.println("正在发送请求到 Dify API...");
        System.out.println("提问内容: " + userMessage);

        try {
            // 创建 URL 对象
            URL url = new URL("http://jenasi.ai:9980/v1/chat-messages");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为 POST
            connection.setRequestMethod("POST");

            // 设置请求头
            connection.setRequestProperty("Authorization", "Bearer " + AIKey);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");

            // 启用输出流以发送请求体
            connection.setDoOutput(true);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("inputs", new JSONObject()); // 空的 inputs 对象
            requestBody.put("query", userMessage);
            requestBody.put("response_mode", "blocking");
            requestBody.put("conversation_id", "");
            requestBody.put("user", "user_asr");

            // 发送请求体
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 获取响应码
            int responseCode = connection.getResponseCode();
            System.out.println("API 响应: " + responseCode);

            // 读取响应内容
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 解析 JSON 响应
                JSONObject jsonResponse = new JSONObject(response.toString());
                System.out.println("响应内容: " + jsonResponse.toString());

                // 返回答案字段
                return jsonResponse.optString("answer", "抱歉，我现在无法回答");
            } else {
                // 读取错误流
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                System.out.println("错误响应: " + errorResponse.toString());

                return "请求失败，响应码：" + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "调用 Dify API 出错: " + e.getMessage();
        }
    }
}

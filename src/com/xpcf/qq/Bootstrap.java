package com.xpcf.qq;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author XPCF
 * @version 1.0
 * @date 4/20/2022 5:53 PM
 */
public class Bootstrap {
    private static FileInputStream stream = null;
    private static Properties properties = null;
    private static String url;
    static {

        try {
            stream = new FileInputStream(new File(System.getProperty("user.dir"), "/config.properties"));
            properties = new Properties();
            properties.load(stream);
            url = "http://" + properties.get("opq.url") + "/v1/LuaApiCaller?qq=" + properties.getProperty("robot.qq") + "&funcname=SendMsgV2";
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        //Build Connection using URI, Query(optional), forceNew(optional), reconnection(optional), transports


        System.out.println(System.getProperty("user.dir"));


        IO.Options options = new IO.Options();
        options.transports = new String[]{"websocket"};
        Socket socket = null;
        try {
            socket = IO.socket("http://" + properties.getProperty("opq.url"), options);
        } catch (URISyntaxException e) {
            System.out.println("opq url error occur");
            System.exit(-1);
            e.printStackTrace();
        }


        socket.on("OnGroupMsgs", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                System.out.println();
            }
        }).on("OnFriendMsgs", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    processMsg(0, args);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println();
            }
        }).on("OnEvents", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println();
            }
        });

        socket.connect();
        System.out.println("forward server success start listen opq server at : " + properties.getProperty("opq.url"));
    }

    /**
     *
     * 解析forward数据
     * @param fromType
     * @param args
     * @throws JSONException
     */
    private static void processMsg(int fromType, Object... args) throws JSONException {

        JSONObject jsonObjects = (JSONObject) args[0];
        String rawData = jsonObjects.getJSONObject("CurrentPacket").getJSONObject("Data").getString("Content");
        rawData = rawData.replace("{\"Content\":\"", "");
        rawData = rawData.replace("\"}", "");

        sendToAllGroup(rawData);

    }

    /**
     * 对所有群转发
     * @param rawData
     * @throws JSONException
     */
    private static void sendToAllGroup(String rawData) throws JSONException {
        String[] groups = properties.getProperty("group.qq").split(" ");
        for (int i = 0; i < groups.length; i++) {
            sendToGroup(buildJson(Integer.valueOf(groups[i]), 2, "XmlMsg", rawData), url);
        }
    }

    private static void sendToGroup(JSONObject jsonObject, String url) {
        //TODO
        sendPost(jsonObject, url);
    }

    /**
     * # ToUserUid 好友QQ/群ID/私聊对象QQ
     * # SendToType  1 为好友消息 2 发送群消息  3 发送私聊消息
     * # Content 为XML或Json
     * # SendMsgType XmlMsg
     * @param uid
     * @param type
     * @param msgType
     * @param rawData
     * @return
     */
    private static JSONObject buildJson(int uid, int type, String msgType, String rawData) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ToUserUid", uid);
        jsonObject.put("SendToType", type);
        jsonObject.put("SendMsgType", msgType);
        jsonObject.put("Content", rawData);
        return jsonObject;
    }


    public static String sendPost(JSONObject jsonText,String URL) {

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(URL);
        post.setHeader("Content-Type", "application/json");
        String result = "";

        try {
//            String json = StringEscapeUtils.unescapeJava(jsonText.toString());
            // 解决转义问题
            StringEntity s = new StringEntity(StringEscapeUtils.unescapeJava(jsonText.toString()), "utf-8");
            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            post.setEntity(s);

            // 发送请求
            HttpResponse httpResponse = client.execute(post);

            // 获取响应输入流
            InputStream inStream = httpResponse.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inStream, "utf-8"));
            StringBuilder strber = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
                strber.append(line + "\n");
            inStream.close();

            result = strber.toString();
            System.out.println(result);

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                System.out.println("请求服务器成功，做相应处理");

            } else {

                System.out.println("请求服务端失败");

            }


        } catch (Exception e) {
            System.out.println("请求异常");
            throw new RuntimeException(e);
        }

        return result;
    }



}




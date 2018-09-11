package com.boomzz.util;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpClientUtil {

	/**
     * 处理get请求.
     * @param url  请求路径
     * @return  json
     */
    public static String get(String url){
        //实例化httpclient
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //实例化get方法
        HttpGet httpget = new HttpGet(url);
        //请求结果
        CloseableHttpResponse response = null;
        String content ="";
        try {
            //执行get方法
            response = httpclient.execute(httpget);
//            System.out.println("HTTP return code: "+response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode()==200){
                content = EntityUtils.toString(response.getEntity(),"utf-8");
//                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
    public static void main(String[] args) {
    	String word[]={"apple","pen"};
    	for(String str:word){
    		System.out.println(str+"----------------------------------");
    		String url="http://dict.youdao.com/dictvoice?audio="+str+"&type=2";
    		String contentString=HttpClientUtil.get(url);
    		System.out.println(contentString);
    	}
//    	String url="http://dict.youdao.com/jsonapi?q=frayed";
//		System.out.println(url);
//		String contentString=HttpClientUtil.get(url);
	}
}

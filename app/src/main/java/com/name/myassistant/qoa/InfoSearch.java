package com.name.myassistant.qoa;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 信息检索：
 * 把问题字符串直接作为检索式，
 * 提交给百度搜索引擎搜索
 * 下载搜索结果的网页
 * 对搜索结果的10个网页片段进行处理提取候选答案
 */
public class InfoSearch {
    /**
     * 根据关键词组成检索式检索，下载检索到的网页的HTML文本
     * 对HTML解析提取网页摘要
     * @param questionStr：问题字符串
     * @return ：网页摘要列表
     * @throws IOException
     */
    static List<String> searchAndGetSnippetList(String questionStr) throws IOException {
        String snippetTag="<a class=\"result_title\".*?</a>";
        String htmlTag="<.*?>";
        List<String> webPageDigestList = new ArrayList<>();

        String link="http://m.baidu.com/s?from=1086k&word="+ URLEncoder.encode(questionStr,"utf-8");
        Log.d("xzx","link=> "+link);

        URL url = new URL(link);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        InputStream inputStream=conn.getInputStream();
        InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
        BufferedReader bufferedReader=new BufferedReader(inputStreamReader);

        //获取网页片段的HTML文本
        Pattern p = Pattern.compile(snippetTag);
        Matcher m;
        String tempStr ;
        String webPageDigest;
        while ((tempStr = bufferedReader.readLine()) != null) {
            m=p.matcher(tempStr);

            while(m.find()){
                //去除网页片段的HTML文本的HTML标签
                tempStr=m.group();
//                Log.d("xzx","tempStr=> "+tempStr);
                webPageDigest = tempStr.replaceAll(htmlTag, "");
//                Log.d("xzx","webPageDigest=> "+webPageDigest);
                webPageDigestList.add(webPageDigest);
            }
        }
        inputStream.close();
        inputStreamReader.close();
        bufferedReader.close();
        conn.disconnect();
        return webPageDigestList;
    }
}

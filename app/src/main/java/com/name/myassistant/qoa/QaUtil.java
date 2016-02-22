package com.name.myassistant.qoa;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 问答系统工具类
 * 分词和词性标注
 * 初始化停用词集
 */
public class QaUtil {

    /**
     * 分词和词性标注
     *
     * @param sentence：需处理的句子
     * @param pattern：调用哈工大讯飞语言云的分析模式，pos为词性标注，ws为分词,ner为命名实体识别
     * @return ：处理后的字符串
     * @throws IOException
     */
    static String lexicalAnalysis(String sentence,String pattern) throws IOException {
        String analysisResultStr = "";

        String api_key = "p47180F0frlP1u1OQZhaNrwryyytjY2hEDhzcHdy";
        //输出的格式为简洁文本格式
        String format = "plain";
        sentence = URLEncoder.encode(sentence, "utf-8");
        String link="http://api.ltp-cloud.com/analysis/?api_key="+ api_key
                + "&" + "text=" + sentence + "&" + "pattern=" + pattern
                + "&" + "format=" + format;

        URL url = new URL(link);
        URLConnection conn = url.openConnection();
        conn.connect();

        InputStream inputStream=conn.getInputStream();
        InputStreamReader inputStreamReader=new InputStreamReader(inputStream,"utf-8");
        BufferedReader reader = new BufferedReader(inputStreamReader);

        String tempStr;
        while ((tempStr = reader.readLine()) != null) {
            analysisResultStr = analysisResultStr + tempStr;
        }
        inputStream.close();
        inputStreamReader.close();
        reader.close();
        if(!pattern.equals("ws")){
            Log.d("xzx","pattern="+pattern+" analysisResultStr=> "+analysisResultStr);
        }
        return analysisResultStr;
    }

}

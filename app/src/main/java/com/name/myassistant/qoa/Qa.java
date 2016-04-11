package com.name.myassistant.qoa;

import android.content.Context;
import android.util.Log;

import com.name.myassistant.util.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 与外部交互的操作类
 */
public class Qa {
    //存放同义词集
    static Map<String, String[]> synonymsMap = new HashMap<>();
    //用来存放停用词的集合
    static Set<String> stopWordSet = new HashSet<>();

    /**
     * 获取答案
     * @param questionStr：问题字符串
     * @return answer：答案
     * @throws IOException
     */
    public static String getAnswer(String questionStr) throws IOException {
        String answer;
        QuestionAnalyze.analyze(questionStr);
        List<String> webPageDigestList=InfoSearch.searchAndGetSnippetList(questionStr);
        answer=GetAnswer.getAnswer(QuestionAnalyze.questionKeyWordList,QuestionAnalyze.keyWordSynonymMap,QuestionAnalyze.questionType, webPageDigestList);
        return answer;
    }

    /**
     * 初始化同义词集和停用词集，程序启动时调用
     * @param context：使用场景
     */
    public static void initData(Context context){
        try {
            Log.d("xzx","initData begin");
            //初始化同义词集
            InputStream inputStream = context.getAssets().open("Synonyms.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "gbk");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String tmpStr;
            String[] tmpWordArray;
            while ((tmpStr = bufferedReader.readLine()) != null) {
                if (tmpStr.charAt(7) == '=') {
                    tmpWordArray = tmpStr.split("\\s");
                    for (int i = 1; i < tmpWordArray.length; i++) {
                        synonymsMap.put(tmpWordArray[i], tmpWordArray);
                    }
                }
            }
            inputStream.close();
            inputStreamReader.close();
            bufferedReader.close();
            // TODO: 2016/1/21 开两个线程，初始化同义词和停用词同时进行；

            //初始化停用词集
            inputStream = context.getAssets().open("StopWords.txt");
            inputStreamReader = new InputStreamReader(inputStream, "gbk");
            bufferedReader = new BufferedReader(inputStreamReader);
            String stopWord;
            while ((stopWord = bufferedReader.readLine()) != null) {
                stopWordSet.add(stopWord);
            }
            Log.d("xzx","initData stop");
            inputStream.close();
            inputStreamReader.close();
            bufferedReader.close();
        } catch (IOException e) {
            LogUtil.e("xzx", "e=> " + e.toString());
            e.printStackTrace();
        }
    }
}

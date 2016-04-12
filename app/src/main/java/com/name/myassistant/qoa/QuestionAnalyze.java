package com.name.myassistant.qoa;

import android.util.Log;

import com.name.myassistant.util.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 问题分析：问题分类以及提取关键词
 */
public class QuestionAnalyze {
    //问题关键词列表
    static ArrayList<String> questionKeyWordList;
    //map <关键词以及扩展的同义词，关键词>
    static Map<String, String> keyWordSynonymMap;
    //问题类型编号
    static int questionType;
    //问题中提取到的疑问词
    static String interrogative;

    /**
     * 问题分析
     *
     * @param question：问题
     * @throws IOException
     */
    static void analyze(String question) throws IOException {
        questionKeyWordList = new ArrayList<>();
        keyWordSynonymMap = new HashMap<>();

        //关键词列表对应的词性列表，本来用来加权重，但是没有加
        ArrayList<String> partOfSpeechList = new ArrayList<>();
        //分词，词性标注
        String[] wordsAndPartOfSpeechList = QaUtil.lexicalAnalysis(question, "pos").split("_+|\\s+");
        LogUtil.d("xzx","wordsAndPartOfSpeechList.length=> "+wordsAndPartOfSpeechList.length);

        String tmpWord;
        for (int i = 0; i < wordsAndPartOfSpeechList.length; i++) {
            tmpWord = wordsAndPartOfSpeechList[i];
            questionKeyWordList.add(tmpWord);
            i = i + 1;
            partOfSpeechList.add(wordsAndPartOfSpeechList[i]);
        }

        //问题分类
        questionType = questionClassify(questionKeyWordList, partOfSpeechList);
        LogUtil.d("xzx", "questionType=> " + questionType);

        //提取关键词
        //如果有疑问词，去掉疑问词
        if (interrogative != null) {
            questionKeyWordList.remove(interrogative);
        }
        //去停用词
        for (String stopWord : Qa.stopWordSet) {
            if (questionKeyWordList.contains(stopWord)) {
                questionKeyWordList.remove(stopWord);
            }
        }

        //关键词扩展
        //询问时间地点原因的问题类型可扩展的关键词
        switch (questionType) {
            case 1:
                questionKeyWordList.add("因为");
                break;
            case 2:
                questionKeyWordList.add("年");
                questionKeyWordList.add("月");
                questionKeyWordList.add("日");
                questionKeyWordList.add("时");
                questionKeyWordList.add("点");
                questionKeyWordList.add("分");
                questionKeyWordList.add("秒");
                questionKeyWordList.add("（");
                questionKeyWordList.add("）");
                break;
            case 3:
                questionKeyWordList.add("位于");
                break;
        }
        //使用哈工大词林扩展关键词同义词
        int length = questionKeyWordList.size();
        String tmpKeyWord;
        String[] tmpWordArray;
        for (int i = 0; i < length; i++) {
            tmpKeyWord = questionKeyWordList.get(i);
            LogUtil.d("xzx", "tmpKeyWord=> " + tmpKeyWord);
            if (Qa.synonymsMap.containsKey(tmpKeyWord)) {
                tmpWordArray = Qa.synonymsMap.get(tmpKeyWord);
                for (int j = 1; j < tmpWordArray.length; j++) {
                    keyWordSynonymMap.put(tmpWordArray[j], tmpKeyWord);
                    Log.d("xzx", "synonyms word=> " + tmpWordArray[j]);
                }
            } else {
                keyWordSynonymMap.put(tmpKeyWord, tmpKeyWord);
            }
        }
    }

    /**
     * 问题分类
     *
     * @param keyWordList：分词以及去停用词后得到的词列表
     * @param partOfSpeech：词列表对应的词性列表
     * @return ：问题类型编号：1原因；2时间；3地点；4人；5询问一般事物；
     * 6询问动作；7询问数字；8询问状态；9其它
     * 0：需要再问句中寻找一个名词作为疑问修饰词，问题类型是这个疑问修饰词对应的类型号。
     * -1：疑问词后面有动词，问题类型为“询问动作”；否则，为“其它”。
     * -2：疑问词后面紧跟一个形容词时，问题类型为“询问状态”；否则，为“其它”
     */
    static int questionClassify(ArrayList<String> keyWordList, ArrayList<String> partOfSpeech) {
        // 疑问代词数组
        String[] interrogativepronoun = {"为什么", "为何", "何时", "何地", "何处", "哪里"
                , "哪儿", "谁", "怎么", "怎样", "如何", "多少"
                , "几", "什么", "哪", "哪个", "哪些", "多"};
        //问题类型编号
        int[] questiontype = {1, 1, 2, 3, 3, 3,
                3, 4, -1, -1, -1, 7,
                7, 0, 0, 0, 0, -2};
        //疑问修饰词词组
        String[] qualifier = {"原因",
                "时间", "时候", "年", "月", "日",
                "地点", "地方", "国家", "省份", "城市", "城镇",
                "人"};
        //疑问修饰词对应问题类型编号
        int[] qualifierquestiontype = {1,
                2, 2, 2, 2, 2,
                3, 3, 3, 3, 3, 3,
                4};

        //初始化问题类型为其他
        int type = 9;
        for (int i = 0; i < interrogativepronoun.length; i++) {
            if (keyWordList.contains(interrogativepronoun[i])) {
                type = questiontype[i];
                interrogative = interrogativepronoun[i];
                LogUtil.d("xzx","interrogative=> "+interrogative);
                if (type == 0) {
                    type = 5;
                    for (int j = 0; j < qualifier.length; j++) {
                        if (keyWordList.contains(qualifier[j])) {
                            type = qualifierquestiontype[j];
                            break;
                        }
                    }
                    return type;
                }

                if (type == -1) {
                    if (partOfSpeech.get(keyWordList.indexOf(interrogative) + 1).equals("v"))
                        type = 6;
                    else
                        type = 9;
                    return type;
                }

                if (type == -2) {
                    if (partOfSpeech.get(keyWordList.indexOf(interrogative) + 1).equals("a"))
                        type = 8;
                    else
                        type = 9;
                    return type;
                }

                return type;
            }
        }
        return type;
    }
}

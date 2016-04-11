package com.name.myassistant.qoa;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 答案抽取
 */
public class GetAnswer {
    static String[] fileWordArray;
    static String[] partOfSpeechList;

    public static String getAnswer(ArrayList<String> keyWordList, Map<String, String> keyWordSynonymMap, int questionType, List<String> webPageDigestList) throws IOException {
        String punctuationStr = "[，。？：；‘’！“”—……、－{2}\\[ \\]（）【】{}《》\\s]";
        if (questionType == 3 || questionType == 4) {
            Map<String, Integer> entityCoOccurrenceMap = new HashMap<>();
            //命名实体识别
            String[] tempSentenceArray;
            for (int i = 0; i < webPageDigestList.size(); i++) {
                tempSentenceArray = webPageDigestList.get(i).split("[。？！……\\n]");
                for (String aTempSentenceArray : tempSentenceArray) {

                    // FIXME: 16/4/11 这里不用再次请求命名实体
                    String tempStr = QaUtil.lexicalAnalysis(aTempSentenceArray, "ner");
                    //命名实体
                    String entityStr;
                    //命名实体对应属性
                    String entityAttribute;
                    Pattern pattern = Pattern.compile("\\[.*?\\]\\w{2}");
                    Matcher matcher = pattern.matcher(tempStr);
                    String[] tempEntityAndAttributeArray;
                    String[] tempSentenceWordArray = null;
                    Set<String> sentenceKeyWordSet = new HashSet<>();
                    while (matcher.find()) {
                        tempEntityAndAttributeArray = matcher.group().replaceAll("\\[", "").split("\\]");
                        entityStr = tempEntityAndAttributeArray[0];
                        entityAttribute = tempEntityAndAttributeArray[1];

                        if (keyWordList.contains(entityStr)) {
                            Log.d("xzx","-----------------"+entityStr);
                            continue;
                        }
                        Log.d("xzx","-----------------entityStr"+entityStr);

                        if(entityStr.equals("中国")){
                            Log.d("xzx","keyWordSynonymMap=> "+keyWordSynonymMap.toString());
                        }

                        //类型_实体属性：3_Ns,4_Nh
                        String answerEntityAttribute = "Ns";
                        if (questionType == 4) {
                            answerEntityAttribute = "Nh";
                        }
                        if (entityAttribute.equals(answerEntityAttribute)) {
                            if (tempSentenceWordArray == null) {
                                tempSentenceWordArray = QaUtil.lexicalAnalysis(aTempSentenceArray, "ws").split(punctuationStr);
                            }
                            for (String sentenceWord : tempSentenceWordArray) {
                                if (keyWordSynonymMap.containsKey(sentenceWord)) {
                                    sentenceKeyWordSet.add(keyWordSynonymMap.get(sentenceWord));
                                }
                            }
                            int score = sentenceKeyWordSet.size();
                            if (entityCoOccurrenceMap.containsKey(entityStr)) {
                                score = score + entityCoOccurrenceMap.get(entityStr);
                            }
                            entityCoOccurrenceMap.put(entityStr, score);
                        }
                    }
                }
            }
            int score = 0;
            String answer = "找不到答案";
            for (String entityStr : entityCoOccurrenceMap.keySet()) {

                int tempEntityScore = entityCoOccurrenceMap.get(entityStr);
                if (tempEntityScore > score) {
                    score = tempEntityScore;
                    answer = entityStr;
                }
            }
            return answer;
        }


        Map<String, Map<Integer, Double>> index = buildIndex(webPageDigestList, keyWordSynonymMap, "ws");
        int digestPosition = getDocument(keyWordList, index);
        Log.d("xzx", "digestPosition=> " + digestPosition);
        String content = webPageDigestList.get(digestPosition);             //文档内容
        String[] sentence = content.split("[。？！……\\n]");
        List<String> sentenceList = new ArrayList<>();
        sentenceList.addAll(Arrays.asList(sentence));
        index = buildIndex(sentenceList, QuestionAnalyze.keyWordSynonymMap, "ws");
        int sentencePosition = getDocument(keyWordList, index);
        return sentenceList.get(sentencePosition);
    }


    static int getDocument(ArrayList<String> keyWordList, Map<String, Map<Integer, Double>> index) {
        //根据问句关键词查找文档
        Map<Integer, Double> fileScoreMap = new HashMap<>();
        Map<Integer, Double> fileTfIdfValueMap;
        for (String keyWord : keyWordList) {
            fileTfIdfValueMap = index.get(keyWord);
            if (null == fileTfIdfValueMap) {
                continue;
            }
            for (int filePosition : fileTfIdfValueMap.keySet()) {
                Double TfIdfValue = fileTfIdfValueMap.get(filePosition);
                if (null == fileScoreMap.get(filePosition)) {
                    fileScoreMap.put(filePosition, TfIdfValue);
                } else {
                    Double fileScore = fileScoreMap.get(filePosition);
                    fileScore = fileScore + TfIdfValue;
                    fileScoreMap.put(filePosition, fileScore);
                }
            }
        }

        double theHighestScore = 0;
        double fileScore;
        int theHighestScoreFilePosition = 0;
        for (Integer filePosition : fileScoreMap.keySet()) {
            fileScore = fileScoreMap.get(filePosition);
            if (theHighestScore < fileScore) {
                theHighestScore = fileScore;
                theHighestScoreFilePosition = filePosition;
            }
        }
        return theHighestScoreFilePosition;
    }

    /**
     * 建立索引
     *
     * @param fileList：百度得到的网页摘要的列表
     * @return ：倒排索引 <词，<文档坐标，tf-idf值>>
     * @throws IOException
     */
    static Map<String, Map<Integer, Double>> buildIndex(List<String> fileList, Map<String, String> keyWordMap, String pattern) throws IOException {
        //Map<关键词,Map<文档坐标,词频>>
        Map<String, Map<Integer, Integer>> wordFrequencyMap = new HashMap<>();
        //Map<关键词,Map<文档坐标,tf-idf值>>
        Map<String, Map<Integer, Double>> tfIdfValueMap = new HashMap<>();

        int fileNum = fileList.size();


        //去除文档中的标点符号，没有去除（）因为很多时间的信息是在括号中出现的。例如，毛泽东，（１８９３ —１９７６）
        String punctuationStr = "[，。？：；‘’！“”—……、－{2}\\[ \\]（）【】{}《》\\s]";

        //遍历文档列表中的文档
        for (int i = 0; i < fileNum; i++) {
            if (pattern.equals("ws")) {
                String answer = QaUtil.lexicalAnalysis(fileList.get(i), pattern);

                fileWordArray = answer.split(punctuationStr);

            } else {
                String[] wordsAndPartOfSpeechList = QaUtil.lexicalAnalysis(fileList.get(i), "pos").split("_|\\s");

                String tmpWord;
                fileWordArray = new String[wordsAndPartOfSpeechList.length / 2];
                partOfSpeechList = new String[wordsAndPartOfSpeechList.length / 2];
                for (int j = 0; j < fileWordArray.length; j++) {
                    tmpWord = wordsAndPartOfSpeechList[j * 2];
                    fileWordArray[j] = tmpWord;
                    partOfSpeechList[j] = wordsAndPartOfSpeechList[j * 2 + 1];
                }
            }


            //词频
            int frequency;
            for (String str : fileWordArray) {
                //如果关键词map中出现过该词
                if (keyWordMap.containsKey(str)) {
                    String keyWord = keyWordMap.get(str);
                    //如果所有文档都没出现过这个词，这个词是首次出现，设该词的这篇文档的词频为1
                    if (null == wordFrequencyMap.get(keyWord)) {
                        wordFrequencyMap.put(keyWord, new HashMap<Integer, Integer>());
                        wordFrequencyMap.get(keyWord).put(i, 1);
                    }
                    //如果这篇文档中这个关键词是首次出现，设词频为1
                    else if (null == wordFrequencyMap.get(keyWord).get(i)) {
                        wordFrequencyMap.get(keyWord).put(i, 1);
                    }
                    //如果这篇文档中这个关键词是已经出现过，词频加1
                    else {
                        frequency = wordFrequencyMap.get(keyWord).get(i) + 1;
                        wordFrequencyMap.get(keyWord).put(i, frequency);
                    }
                }
            }
        }

        double TFtd, dft, Wtd;
        //词频TFtd表示词t出现在文档d中的次数
        //文档频率dft是指包含词t的文档的总数
        //一个词t的tf-idf权重值为Wtd
        Map<Integer, Double> map;
        for (Map.Entry<String, Map<Integer, Integer>> entry : wordFrequencyMap.entrySet()) {
            dft = entry.getValue().size();
            map = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry1 : entry.getValue().entrySet()) {
                TFtd = entry1.getValue();
                Wtd = (1 + log(TFtd, (double) 10)) * (log((fileNum / dft), (double) 10));
                map.put(entry1.getKey(), Wtd);
            }
            tfIdfValueMap.put(entry.getKey(), map);
        }
        return tfIdfValueMap;
    }

    //改写log函数
    static double log(double value, double base) {
        return Math.log(value) / Math.log(base);
    }
}

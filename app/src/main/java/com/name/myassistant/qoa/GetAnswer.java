package com.name.myassistant.qoa;

import android.util.Log;

import com.name.myassistant.util.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 答案抽取
 */
public class GetAnswer {
    static String[] fileWordArray;
    static String[] partOfSpeechList;

    public static String getAnswer(ArrayList<String> keyWordList, Map<String, String> keyWordSynonymMap, int questionType, List<String> webPageDigestList) throws IOException {
        String punctuationStr = "[，。？：；‘’！“”—……、－{2}\\[ \\]（）【】{}《》\\s]";
        List<String> endSentenceTagList=new ArrayList<>();
        endSentenceTagList.add("。");
        endSentenceTagList.add("？");
        endSentenceTagList.add("！");
        endSentenceTagList.add("……");
        endSentenceTagList.add("\n");



        if (questionType == 3 || questionType == 4) {
            Map<String, Integer> entityCoOccurrenceMap = new HashMap<>();
            Map<String,Integer> keyWordDistanceMap=new HashMap<>();
            //命名实体识别
//            String[] tempSentenceArray;
            for (int i = 0; i < webPageDigestList.size(); i++) {
                //将网页摘要整个文本进行词性标注
                String posStr=QaUtil.lexicalAnalysis(webPageDigestList.get(i),"pos");
                //词列表
                List<String> wordList=new ArrayList<>();
                //词性列表
                List<String> posList=new ArrayList<>();

                String[] tempArray=posStr.split("_+|\\s+");
                LogUtil.d("xzx","tempArray=> "+ Arrays.toString(tempArray));
//                LogUtil.d("xzx","tempArray.length=> "+tempArray.length);

                for(int j=0;j<tempArray.length;j++){
                    wordList.add(tempArray[j]);
                    j=j+1;
                    posList.add(tempArray[j]);
                }
                LogUtil.d("xzx","wordList=> "+wordList.toString());
                LogUtil.d("xzx","posList=> "+posList.toString());


                //类型_实体属性：3_Ns,4_Nh
                String answerEntityAttribute = "ns";
                if (questionType == 4) {
                    answerEntityAttribute = "nh";
                }

                //计算候选答案与关键词的共现

                Set<String> sentenceKeyWordSet = new HashSet<>();
                List<String> sentenceWordList=new ArrayList<>();
                List<String> sentenceKeyWordList=new ArrayList<>();
                String tempAnswer=null;
                for(int j=0;j<posList.size();j++){
                    String word=wordList.get(j);
                    sentenceWordList.add(word);

                    //如果遍历到句号问号等句子完结的标志，则将候选关键词与对应共现次数存起来，次数清零重新计算下一个候选词
                    if(endSentenceTagList.contains(word)||j+1>=posList.size()){
                        if(tempAnswer!=null){
                            //计算共现次数
                            int score=sentenceKeyWordSet.size();
                            if (entityCoOccurrenceMap.containsKey(tempAnswer)) {
                                score = score + entityCoOccurrenceMap.get(tempAnswer);
                            }
                            LogUtil.d("xzx","tempAnswer=> "+tempAnswer+" score=> "+score);
                            entityCoOccurrenceMap.put(tempAnswer,score);

                            //计算与关键词的距离
                            int indexOfTempAnswer=sentenceWordList.indexOf(tempAnswer);
                            int distance=10000;
                            for(int k=0;k<sentenceKeyWordList.size();k++){
                                String keyWord=sentenceKeyWordList.get(k);
                                int tempDistance=Math.abs(sentenceWordList.indexOf(keyWord)-indexOfTempAnswer);
                                if(tempDistance<distance){
                                    distance=tempDistance;
                                    LogUtil.d("xzx","keyWord=> "+keyWord+" distance=> "+distance);
                                }
                            }
                            keyWordDistanceMap.put(tempAnswer,distance);
                        }
                        sentenceKeyWordSet=new HashSet<>();
                        LogUtil.d("xzx","word=> "+word);
                        continue;
                    }

                    //如果遍历到关键词
                    if(keyWordSynonymMap.containsKey(word)){
                        sentenceKeyWordSet.add(keyWordSynonymMap.get(word));
                        sentenceKeyWordList.add(word);
                    }

                    //如果遍历到的词的对应问题所需答案的命名实体，该词为候选答案
                    if(posList.get(j).equals(answerEntityAttribute)){
                        tempAnswer=word;
                        LogUtil.d("xzx","tempAnswer=> "+tempAnswer);
                    }
                }
                LogUtil.d("xzx","entityCoOccurrenceMap=> "+entityCoOccurrenceMap.toString());

//                tempSentenceArray = webPageDigestList.get(i).split("[。？！……\\n]");
//                for (String aTempSentenceArray : tempSentenceArray) {
//
//                    // FIXME: 16/4/11 这里不用再次请求命名实体
//                    String tempStr = QaUtil.lexicalAnalysis(aTempSentenceArray, "ner");
//                    //命名实体
//                    String entityStr;
//                    //命名实体对应属性
//                    String entityAttribute;
//                    Pattern pattern = Pattern.compile("\\[.*?\\]\\w{2}");
//                    Matcher matcher = pattern.matcher(tempStr);
//                    String[] tempEntityAndAttributeArray;
//                    String[] tempSentenceWordArray = null;
//                    Set<String> sentenceKeyWordSet = new HashSet<>();
//                    while (matcher.find()) {
//                        tempEntityAndAttributeArray = matcher.group().replaceAll("\\[", "").split("\\]");
//                        entityStr = tempEntityAndAttributeArray[0];
//                        entityAttribute = tempEntityAndAttributeArray[1];
//
//                        // TODO: 16/4/12 这里留意下，忘记用来干嘛了
//                        if (keyWordList.contains(entityStr)) {
//                            Log.d("xzx","-----------------"+entityStr);
//                            continue;
//                        }
//
//                        //类型_实体属性：3_Ns,4_Nh
//                        String answerEntityAttribute = "Ns";
//                        if (questionType == 4) {
//                            answerEntityAttribute = "Nh";
//                        }
//                        if (entityAttribute.equals(answerEntityAttribute)) {
//                            if (tempSentenceWordArray == null) {
//                                tempSentenceWordArray = QaUtil.lexicalAnalysis(aTempSentenceArray, "ws").split(punctuationStr);
//                            }
//                            for (String sentenceWord : tempSentenceWordArray) {
//                                if (keyWordSynonymMap.containsKey(sentenceWord)) {
//                                    sentenceKeyWordSet.add(keyWordSynonymMap.get(sentenceWord));
//                                }
//                            }
//                            int score = sentenceKeyWordSet.size();
//                            if (entityCoOccurrenceMap.containsKey(entityStr)) {
//                                score = score + entityCoOccurrenceMap.get(entityStr);
//                            }
//                            entityCoOccurrenceMap.put(entityStr, score);
//                        }
//                    }
//                }
            }

            LogUtil.d("xzx","entityCoOccurrenceMap=> "+entityCoOccurrenceMap.toString());
            LogUtil.d("xzx","keyWordDistanceMap"+keyWordDistanceMap.toString());
            int score = 0;
            int distance=10000;
            String answer = "找不到答案，长按查看更多内容";
            for (String entityStr : entityCoOccurrenceMap.keySet()) {

                int tempEntityScore = entityCoOccurrenceMap.get(entityStr);
                if(tempEntityScore<score){
                    continue;
                }

                int tempDistance=keyWordDistanceMap.get(entityStr);
                if (tempEntityScore > score) {
                    score = tempEntityScore;
                    distance=tempDistance;
                    answer = entityStr;
                    continue;
                }

                if(tempDistance<distance){
                    distance=tempDistance;
                    answer=entityStr;
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

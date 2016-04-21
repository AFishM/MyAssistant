package com.name.myassistant.m;

/**
 * Created by xu on 16-2-18.
 */
public class Chat {
    //是否属于用户的输入
    public boolean isUserInput;
    //消息文本
    public String chatStr;
    //是否属于语音助手针对用户提问所做出的回答
    private boolean isRobotAnswer;

    public Chat(boolean isUserInput, String chatStr) {
        this.isUserInput = isUserInput;
        this.chatStr = chatStr;
    }

    public boolean isRobotAnswer() {
        return isRobotAnswer;
    }

    public void setIsRobotAnswer(boolean isRobotAnswer) {
        this.isRobotAnswer = isRobotAnswer;
    }
}

package com.name.myassistant.m;

/**
 * Created by xu on 16-2-18.
 */
public class Chat {
    public boolean isUserInput;
    public String chatStr;
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

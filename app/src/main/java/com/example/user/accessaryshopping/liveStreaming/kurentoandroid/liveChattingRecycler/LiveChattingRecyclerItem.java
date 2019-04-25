package com.example.user.accessaryshopping.liveStreaming.kurentoandroid.liveChattingRecycler;

public class LiveChattingRecyclerItem {

    private String nickName,message;

    public LiveChattingRecyclerItem(String nickName, String message){
        this.nickName = nickName;
        this.message = message;
    }

    public String getNickName() {
        return nickName;
    }

    public String getMessage() {
        return message;
    }
}

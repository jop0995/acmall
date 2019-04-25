package com.example.user.accessaryshopping.liveStreaming;

public class LiveRecyclerItem {

    private String title, img, nickname;

    public LiveRecyclerItem(String title, String img, String nickname) {
        this.title = title;
        this.img = img;
        this.nickname = nickname;
    }

    public String getImg() {
        return img;
    }

    public String getTitle() {
        return title;
    }

    public String getNickname() {
        return nickname;
    }
}

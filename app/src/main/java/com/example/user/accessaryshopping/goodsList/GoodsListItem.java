package com.example.user.accessaryshopping.goodsList;

import java.util.ArrayList;

public class GoodsListItem {

    private String title,img,price,no;

    public GoodsListItem(String title,String price ,String img ,String no) {
        this.title = title;
        this.img = img;
        this.price = price;
        this.no = no;

    }

    public String getImg() {
        return img;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getNo() {
        return no;
    }
}

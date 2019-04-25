package com.example.user.accessaryshopping.liveStreaming.kurentoandroid.main;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.EditText;

import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.example.user.accessaryshopping.R;
import com.example.user.accessaryshopping.liveStreaming.kurentoandroid.broadcaster.BroadCasterActivity_;
import com.example.user.accessaryshopping.liveStreaming.kurentoandroid.one2one.One2OneActivity_;
import com.example.user.accessaryshopping.liveStreaming.kurentoandroid.viewer.ViewerActivity_;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

/**
 * Created by nhancao on 9/18/16.
 */

@EActivity(R.layout.activity_main)
public class MainActivity extends MvpActivity<MainView, MainPresenter> implements MainView {
    private static final String TAG = MainActivity.class.getName();



    EditText room;
    String roomName ;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    @Click(R.id.btBroadCaster)
    protected void btBroadCasterClick() {
        //presenter 방번호 저장 하기 위해서
        sp = getApplicationContext().getSharedPreferences("webrtc_roomname",MODE_PRIVATE);
        editor = sp.edit();

        room = (EditText)findViewById(R.id.roomName);
        roomName=room.getText().toString();

        editor.putString("roomname",roomName);
        editor.commit();


        BroadCasterActivity_.intent(this).start();
    }

    @Click(R.id.btViewer)
    protected void btViewerClick() {
        //viewer 방번호 저장 하기 위해서
        sp = getApplicationContext().getSharedPreferences("webrtc_roomname",MODE_PRIVATE);
        editor = sp.edit();

        room = (EditText)findViewById(R.id.roomName);
        roomName=room.getText().toString();

        editor.putString("roomname",roomName);
        editor.commit();
        ViewerActivity_.intent(this).start();
    }

    @Click(R.id.btOne2One)
    protected void btOne2OneClick() {
        One2OneActivity_.intent(this).start();
    }

    @NonNull
    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter(getApplication());
    }

}

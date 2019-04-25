package com.example.user.accessaryshopping;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.user.accessaryshopping.liveStreaming.LiveRecyclerAdapter;
import com.example.user.accessaryshopping.liveStreaming.LiveRecyclerItem;
import com.example.user.accessaryshopping.liveStreaming.RoomAddActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class LiveActivity extends AppCompatActivity {

    //어플 하단바 아이콘 이미지뷰
    ImageView goodsListIcon, cameraIcon, homeIcon;

    //recyclerview 에 필요한 변수들
    RecyclerView recyclerView;
    LiveRecyclerAdapter recyclerAdapter;
    ArrayList<LiveRecyclerItem> items = new ArrayList<>();

    //플로팅 버튼 누르기
    FloatingActionButton roomAddBtn;

    //레트로핏 변수 설정
    Retrofit retrofit;
    APIService service;

    ProgressDialog progressDialog;

    //방 리스트 json으로 받아오기
    JSONArray roomList = null;

    //email 존재 여부 확인 변수
    SharedPreferences loginConSp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        retrofitStart();

        //리사이클러뷰 화면 리사이클뷰랑 연결
        recyclerView = (RecyclerView) findViewById(R.id.liveRecyclerView);

        //goodsList 아이콘 클릭시
        goodsListIcon = (ImageView) findViewById(R.id.goodslist_inactive);
        goodsListIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goodsListIntent = new Intent(getApplicationContext(), GoodsListActivity.class);
                goodsListIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(goodsListIntent);
                overridePendingTransition(0, 0); // 화면 전환시 애니매이션 효과 없애기위 해서
                finish();
            }
        });

        //camera 아이콘 클릭시
        cameraIcon = (ImageView) findViewById(R.id.camera_inactive);
        cameraIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
                cameraIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(cameraIntent);
                overridePendingTransition(0, 0); // 화면 전환시 애니매이션 효과 없애기위 해서
                finish();

            }
        });

        //home 아이콘 클릭시
        homeIcon = (ImageView) findViewById(R.id.home_inactive);
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(homeIntent);
                overridePendingTransition(0, 0); // 화면 전환시 애니매이션 효과 없애기위 해서
                finish();
            }
        });

        //roomAddBtn 버튼 클릭시
        roomAddBtn = (FloatingActionButton) findViewById(R.id.roomAddBtn);
        roomAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginConSp = getApplicationContext().getSharedPreferences("member_autologin",MODE_PRIVATE);
//                Log.e("오류 확인",loginConSp.getString("email","fail"));
                if (loginConSp.getString("nickname","fail") == "fail"){
                    Log.e("오류 확인",loginConSp.getString("nickname","fail"));
                    Toast.makeText(getApplicationContext(),"로그인 해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent roomAddIntent = new Intent(getApplicationContext(), RoomAddActivity.class);
                roomAddIntent.putExtra("nickname",loginConSp.getString("nickname","fail"));
                startActivity(roomAddIntent);
                //Toast.makeText(LiveActivity.this, "플로팅 버튼 잘눌리네", Toast.LENGTH_SHORT).show();
            }
        });

        roomListHttpConnect();



    }//oncreate

    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(LiveActivity.this,"onResume",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Toast.makeText(LiveActivity.this,"onRestart",Toast.LENGTH_SHORT).show();
        roomListHttpConnect();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Toast.makeText(LiveActivity.this,"onStart",Toast.LENGTH_SHORT).show();
    }

    //레트로핏 동기식 처리시 asynctask 를 이용해야함.
    public void roomListHttpConnect(){
        progressDialog = ProgressDialog.show(LiveActivity.this, "로딩중..", "please wait", false, false);
        class syncTaskUploadClass extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                items.clear();

            }

            @Override
            protected String doInBackground(Void... voids) {
                service = retrofit.create(APIService.class);
                Call<ResponseBody> call = service.roomList();

                try {
                    return call.execute().body().string();
                } catch (IOException  e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                if (s.equals(null)){

                }
                Log.e("errordddd",s);

                try {
                    JSONObject jsonObj = new JSONObject(s);
                    roomList = jsonObj.getJSONArray("result");
                    if (roomList.isNull(0)){
                        Log.e("errordddd","아무것도없다.");
                        progressDialog.dismiss();
                    }else {
                        Log.e("errordddd","있다.");
                        for (int i = 0; i < roomList.length(); i++) {
                            JSONObject roomdetail = roomList.getJSONObject(i);
                            String room_name = roomdetail.getString("room_name");
                            String img = roomdetail.getString("img");
                            String nickname = roomdetail.getString("nickname");
                            //Log.e("roomde",nickname);

                            items.add(new LiveRecyclerItem(room_name,img,nickname));
                            progressDialog.dismiss();

                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                setRecyclerView();
            }
        }

        syncTaskUploadClass asyncTaskUploadClassRe = new syncTaskUploadClass();
        asyncTaskUploadClassRe.execute();
    }


    //recyclerview 실행 메소드
    void setRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        //adapter에 데이터 넣는 줄
        recyclerAdapter = new LiveRecyclerAdapter(getApplicationContext(), items);
        recyclerView.setAdapter(recyclerAdapter);

    }

    //retrofit 을 이용한 http 통신 하기 위한 메소드
    public void retrofitStart() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://13.209.144.49/")
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

    }
}

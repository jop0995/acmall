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

import com.example.user.accessaryshopping.goodsList.GoodsListAdapter;
import com.example.user.accessaryshopping.goodsList.GoodsListAddActivity;
import com.example.user.accessaryshopping.goodsList.GoodsListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class GoodsListActivity extends AppCompatActivity {

    //어플 하단바 아이콘 이미지뷰
    ImageView liveIcon, cameraIcon, homeIcon;
    FloatingActionButton goodListAddBtn;

    //recyclerview 에 필요한 변수들
    RecyclerView recyclerView;
    GoodsListAdapter goodsListAdapter;
    ArrayList<GoodsListItem> items = new ArrayList<>();

    //다이얼로그 띄우기
    ProgressDialog progressDialog;

    //닉네임를 라이브스트리밍 이용할때 쓰기위해 sharedPreference 사용
    SharedPreferences membersp;
    SharedPreferences.Editor editor;

    //아이디 별로 상품 리스트 json으로 받아오기
    JSONArray goodsList = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_list);
        //닉네임 정보 가져오기 위해서
        membersp = getApplicationContext().getSharedPreferences("member_autologin", MODE_PRIVATE);
        if (membersp.getString("nickname", "error").equals("error")) {
            Toast.makeText(GoodsListActivity.this, "로그인을 해주세요.", Toast.LENGTH_SHORT).show();
            Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(homeIntent);
            overridePendingTransition(0, 0); // 화면 전환시 애니매이션 효과 없애기위 해서
            finish();
        }

        //리사이클러뷰 화면 리사이클뷰랑 연결
        recyclerView = (RecyclerView) findViewById(R.id.goodsListRecyclerView);

        //live 아이콘 클릭시
        liveIcon = (ImageView) findViewById(R.id.live_inactive);
        liveIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent liveiconIntent = new Intent(getApplicationContext(), LiveActivity.class);
                liveiconIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(liveiconIntent);
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

        goodListAddBtn = (FloatingActionButton) findViewById(R.id.goodsAddBtn);
        goodListAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goodsAddIntent = new Intent(getApplicationContext(), GoodsListAddActivity.class);
                goodsAddIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(goodsAddIntent);
            }
        });

        //goodsListHttpConnect();
    }

    //자기가 등록한 상품 리스트 위한 서버와의 통신 메소드
    public void goodsListHttpConnect() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);

        APIService service = retrofit.create(APIService.class);

        class syncTaskUploadClass extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                items.clear();
                progressDialog = ProgressDialog.show(GoodsListActivity.this, "로딩중..", "please wait", false, false);
            }

            @Override
            protected String doInBackground(Void... voids) {

                Map<String, Object> params = new HashMap<>();
                params.put("nickname", membersp.getString("nickname", "error"));

                Call<ResponseBody> call = service.goodsListRecycler(params);
                try {
                    return call.execute().body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    JSONObject jsonObj = new JSONObject(s);
                    goodsList = jsonObj.getJSONArray("result");
                    if (goodsList.isNull(0)) {
                        //값이 없을때
                        progressDialog.dismiss();
                    } else {
                        //값이 있을때
                        for (int i = 0; i < goodsList.length(); i++) {
                            JSONObject goodsdetail = goodsList.getJSONObject(i);
                            String goodsTitle = goodsdetail.getString("goodstitle");
                            String goodsPrice = goodsdetail.getString("goodsprice");
                            String img = goodsdetail.getString("img");
                            String no = goodsdetail.getString("no");
                            Log.e("goodsTitle", goodsTitle);
                            items.add(new GoodsListItem(goodsTitle, goodsPrice, img, no));
                            progressDialog.dismiss();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Log.e("s", s);
                progressDialog.dismiss();
                setRecyclerView();

            }

        }//syncTaskUploadClass
        syncTaskUploadClass asyncTaskUploadClassRe = new syncTaskUploadClass();
        asyncTaskUploadClassRe.execute();

    }//goodsListHttpConnect

    @Override
    protected void onRestart() {
        super.onRestart();


    }

    //recyclerview 실행 메소드
    void setRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        //adapter에 데이터 넣는 줄
        //GoodsListActivity.this 넣어야 recyclerView 클릭시 다이얼로그가 띄어짐 !!
        goodsListAdapter = new GoodsListAdapter(GoodsListActivity.this, items);
        recyclerView.setAdapter(goodsListAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("onStart","onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume","onResume");
        goodsListHttpConnect();
    }
}

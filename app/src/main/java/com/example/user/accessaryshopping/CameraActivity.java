package com.example.user.accessaryshopping;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.user.accessaryshopping.facefilter.FaceFilterActivity;

import java.io.File;


public class CameraActivity extends AppCompatActivity {

    //어플 하단바 아이콘 이미지뷰
    ImageView liveIcon, goodsListIcon, homeIcon, faceMaskView;
    Button faceDetectionBtn, shareBtn;

    //넘어온 이미지 파일 이름
    String imageFileName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        //이미지 띄어주기 변수
        faceMaskView = (ImageView)findViewById(R.id.faceMaskView);

        //액세서리 착용해보기 버튼
        shareBtn = (Button)findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(imageFileName);
                Uri mSaveImageUri = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/jpg");
                intent.putExtra(Intent.EXTRA_STREAM, mSaveImageUri);
                startActivity(intent.createChooser(intent,"Choose"));
            }
        });

        //사진으로 가는 버튼
        faceDetectionBtn = (Button) findViewById(R.id.faceDetectionBtn);
        faceDetectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FaceFilterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, 1000);
            }
        });
        //live 아이콘 클릭시
        liveIcon = (ImageView) findViewById(R.id.live_inactive);
        liveIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent liveiconIntent = new Intent(getApplicationContext(), LiveActivity.class);
                liveiconIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(liveiconIntent);
                overridePendingTransition(0,0); // 화면 전환시 애니매이션 효과 없애기위 해서
                finish();
            }
        });

        //goodsList 아이콘 클릭시
        goodsListIcon = (ImageView) findViewById(R.id.goodslist_inactive);
        goodsListIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goodsListIntent = new Intent(getApplicationContext(), GoodsListActivity.class);
                goodsListIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(goodsListIntent);
                overridePendingTransition(0,0); // 화면 전환시 애니매이션 효과 없애기위 해서
                finish();
            }
        });

        homeIcon = (ImageView) findViewById(R.id.home_inactive);
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(homeIntent);
                overridePendingTransition(0,0); // 화면 전환시 애니매이션 효과 없애기위 해서
                finish();
            }
        });
    }

    //마스크 씌운후 인식값 넘어오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode==RESULT_OK){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            imageFileName = data.getStringExtra("filename");
            Bitmap bitmap = BitmapFactory.decodeFile(data.getStringExtra("filename"), options);
            faceMaskView.setImageBitmap(bitmap);
            Log.e("실행","실행");
            Log.e("data.getStringExtra",data.getStringExtra("filename")+"");
            shareBtn.setVisibility(View.VISIBLE);

        }
    }
}

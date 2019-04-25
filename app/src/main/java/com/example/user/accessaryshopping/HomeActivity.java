package com.example.user.accessaryshopping;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.user.accessaryshopping.token.TokenActivity;

import com.nhn.android.naverlogin.OAuthLogin;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    //권한설정
    private String[] permissions = {Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE
                                    ,Manifest.permission.CAMERA ,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int MULTIPLE_PERMISSIONS = 101;

    //어플 하단바 아이콘 이미지뷰
    ImageView cameraIcon, goodsListIcon, liveIcon, profileImage;

    //로그인 안했을시 로그인 버튼
    Button loginBtn, logoutBtn, tokenConBtn;
    TextView homeText, nicknameTxt;

    //로그인 자동저장 판별
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    //네이버 로그인 로그아웃 위한 인스턴스
    private static String OAUTH_CLIENT_ID = "JwEy12vPegzQ6ndcEfIy";
    private static String OAUTH_CLIENT_SECRET = "ZGog9MqKRu";
    private static String OAUTH_CLIENT_NAME = "네이버 아이디로 로그인 테스트";
    private static OAuthLogin mOAuthLoginInstance;
    private static Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //자동로그인 때문에 있다.
        sp = getSharedPreferences("member_autologin", MODE_PRIVATE);
        //권한요청
        checkPermissions();


        //프로필 이미지
        profileImage = (ImageView) findViewById(R.id.frofile_image);
        //프로필 닉네임 텍스트뷰
        nicknameTxt = (TextView) findViewById(R.id.nicknameTxt);

        //로그인 안되었을시 로그인 안내 문구("이용하기위해 로그인이 필요합니다.")
        homeText = (TextView) findViewById(R.id.homeText);

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

        //로그인 안되어있을시 로그인 버튼으로 로그인
        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(loginIntent, 1000);

            }
        });

        //로그인 되있을시 로그아웃 버튼 클릭
        logoutBtn = (Button) findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //네이버 로그아웃 인스턴스 초기화
                naverinitData();
                editor = sp.edit();
                editor.clear();
                editor.commit();
                mOAuthLoginInstance.logout(mContext);
                loginBtn.setVisibility(View.VISIBLE);
                homeText.setVisibility(View.VISIBLE);
                logoutBtn.setVisibility(View.INVISIBLE);
                profileImage.setVisibility(View.INVISIBLE);
                nicknameTxt.setVisibility(View.INVISIBLE);
                tokenConBtn.setVisibility(View.INVISIBLE);
            }
        });
        //토큰 클릭시 토큰 보여주기

        tokenConBtn = (Button) findViewById(R.id.tokenConBtn);
        tokenConBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tokenIntent = new Intent(getApplicationContext(), TokenActivity.class);
                tokenIntent.putExtra("tokennumber",sp.getString("nickname","이름없음"));
                tokenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(tokenIntent);


            }
        });


    }//onCreate

    @Override
    protected void onStart() {
        super.onStart();
        //Log.e("onStart", "onStart");

        //자동로그인
        editor = sp.edit();

        if (sp.getString("email", "fail") != "fail") {
            //로그인 돼있을때
            //Toast.makeText(HomeActivity.this, "키있다", Toast.LENGTH_SHORT).show();
            loginBtn.setVisibility(View.INVISIBLE);
            homeText.setVisibility(View.INVISIBLE);
            tokenConBtn.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.VISIBLE);
            profileImage.setVisibility(View.VISIBLE);
            nicknameTxt.setVisibility(View.VISIBLE);
            nicknameTxt.setText(sp.getString("nickname","이름없음"));

            //api로그인 아닐시 프로필 사진 바꾸도록 설정하기 위해서
            if (sp.getString("apilogin","false")=="false"){
                //일반 로그인
                Glide.with(getApplicationContext())
                        .load(sp.getString("image","http://13.209.144.49/images/jientlogo.png"))
                        .into(profileImage);

                profileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(HomeActivity.this,"클릭된다.",Toast.LENGTH_SHORT).show();
                        //사진 찍기 다이얼로그 나온다.
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        final String str[] = {"사진촬영", "앨범선택"};
                        builder.setTitle("프로필 사진 선택").setNegativeButton("취소",null).setItems(str, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "선택된것은 :" + str[which], Toast.LENGTH_SHORT).show();
                              if (which == 0){

                              }else {
                                  Log.e("앨번","앨범선택");
                              }
                            }
                        });//alert 안에 클릭 리스너
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

            }else {
                //api로그인
                Glide.with(getApplicationContext())
                        .load(sp.getString("image","http://13.209.144.49/images/jientlogo.png"))
                        .into(profileImage);

            }

        } else {
            //로그인 안돼있을때
            //Toast.makeText(HomeActivity.this, "키없다.", Toast.LENGTH_SHORT).show();
            loginBtn.setVisibility(View.VISIBLE);
            homeText.setVisibility(View.VISIBLE);
            tokenConBtn.setVisibility(View.INVISIBLE);
            logoutBtn.setVisibility(View.INVISIBLE);
            profileImage.setVisibility(View.INVISIBLE);
            nicknameTxt.setVisibility(View.INVISIBLE);
        }


    }

    //네이버 로그인 인스턴스 초기화
    private void naverinitData() {
        mContext = this;
        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);
    }


    //권한주기
    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청합니다.
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    //아래는 권한 요청 Callback 함수입니다. PERMISSION_GRANTED로 권한을 획득했는지 확인할 수 있습니다. 아래에서는 !=를 사용했기에
    //권한 사용에 동의를 안했을 경우를 if문으로 코딩되었습니다.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        } else if (permissions[i].equals(this.permissions[2])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }
    }


    //권한 획득에 동의를 하지 않았을 경우 아래 Toast 메세지를 띄우며 해당 Activity를 종료시킵니다.
    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 모두동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK) {
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Log.e("onRestart", "onRestart");
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Log.e("onResume", "onResume");
    }
}//class

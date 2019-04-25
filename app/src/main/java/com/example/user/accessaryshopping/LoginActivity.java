package com.example.user.accessaryshopping;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.accessaryshopping.signUp.SingUpFinal;
import com.example.user.accessaryshopping.signUp.SingUpMobileConfirm;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.nhn.android.naverlogin.OAuthLogin.mOAuthLoginHandler;

public class LoginActivity extends AppCompatActivity {

    ImageView cancelBtn;
    TextView singup;
    EditText emailEdit, passwordEdit;
    Button loginBtn;

    //retrofit 이용하기 위한 변수
    Retrofit retrofit;
    APIService service;

    //로그인 자동저장
    SharedPreferences sp;
    SharedPreferences.Editor editor;


    //네이버 로그인을 위한 정보들
    private static String OAUTH_CLIENT_ID = "JwEy12vPegzQ6ndcEfIy";
    private static String OAUTH_CLIENT_SECRET = "ZGog9MqKRu";
    private static String OAUTH_CLIENT_NAME = "네이버 아이디로 로그인 테스트";
    private static OAuthLogin mOAuthLoginInstance;
    private static Context mContext;

    //네이버 로그인 버튼
    OAuthLoginButton mOAuthLoginButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEdit = (EditText)findViewById(R.id.emailEdit);
        passwordEdit = (EditText)findViewById(R.id.passwordEdit);
        loginBtn = (Button)findViewById(R.id.loginBtn);


        //네이버 로그인 버튼
        mOAuthLoginButton= (OAuthLoginButton) findViewById(R.id.buttonOAuthLoginImg);
        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);


        //취소 버튼 클릭
        cancelBtn = (ImageView) findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //회원가입 버튼 클릭
        singup = (TextView)findViewById(R.id.singUpBtn);
        singup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent singupIntent = new Intent(getApplicationContext(), SingUpFinal.class);
                singupIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(singupIntent, 1001);
            }
        });

        //retrofit 빌드
        retrofitStart();

        //일반 로그인 버튼
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (emailEdit.getText().toString().length() == 0) {
                    Toast.makeText(LoginActivity.this, "이메일을 입력하세요", Toast.LENGTH_SHORT).show();
                    emailEdit.requestFocus();
                    return;
                }
                if (passwordEdit.getText().toString().length() == 0) {
                    Toast.makeText(LoginActivity.this, "비밀번호 입력하세요", Toast.LENGTH_SHORT).show();
                    passwordEdit.requestFocus();
                    return;
                }



                //로그인 연결 판별
                loginHttpConnect();
            }
        });




        naverinitData();
    }//onCreate

    //네이버 로그인 핸들러
    static private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {



            if(success){
                //로그인 성공
                Toast.makeText(mContext,"로그인 성공",Toast.LENGTH_SHORT).show();
                new RequestApiTask().execute();

            }else {
                //로그인 실패시
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Toast.makeText(mContext, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();

            }
        }
    };

    //네이버 로그인 api
    //정보를 받는과정에서 쓰레드가 필요해서 사용함.

    private static class RequestApiTask extends AsyncTask<Void, Void, String>{
        //네이버 자동 로그인 저장때문에 다시 변수 선언
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = "https://openapi.naver.com/v1/nid/me";
            String at = mOAuthLoginInstance.getAccessToken(mContext);
            return mOAuthLoginInstance.requestApi(mContext, at, url);
        }

        @Override
        protected void onPostExecute(String content) {
            super.onPostExecute(content);
            try {
                //정보는 제이슨으로 받아야돼 !
                JSONObject jsonObject = new JSONObject(content);
                JSONObject response = jsonObject.getJSONObject("response");
                String email = response.getString("email");
                String nickname = response.getString("nickname");
                String image = response.getString("profile_image");
                sp=mContext.getSharedPreferences("member_autologin", MODE_PRIVATE);
                editor = sp.edit();
                editor.putString("email",email);
                editor.putString("nickname",nickname);
                editor.putString("image", image);
                editor.putString("apilogin","true");
                editor.commit();

                Intent result = new Intent(mContext, HomeActivity.class);
                result.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(result);



            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //네이버 로그인 인스턴스 초기화
    private void naverinitData(){
        mContext = this;
        mOAuthLoginInstance  = OAuthLogin.getInstance();
//        mOAuthLoginInstance.showDevelopersLog(true);
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);
    }

    //다시 돌아오는 값 받기, 왜 하냐면 액티비티를 새로 실행시키지 않아된다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1001 && resultCode==RESULT_OK){
            Toast.makeText(LoginActivity.this,"회원가입이 완료되었습니다.",Toast.LENGTH_SHORT).show();
            emailEdit.setText(data.getStringExtra("email"));

        }

    }

    //retrofit 을 이용한 http 통신 하기 위한 메소드
    public void retrofitStart() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://13.209.144.49/")
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

    }

    //로그인을 위한 asynctask 이메일값 패스워드를 데이터 베이스가 판별해 클라이언트에게 보내준다.
    public void loginHttpConnect(){
        class syncTaskUploadClass extends AsyncTask<Void, Void, String>{

            @Override
            protected String doInBackground(Void... voids) {
                //서버에 보낼값 정의
                Map<String, Object> params = new HashMap<>();
                params.put("email", emailEdit.getText().toString());
                params.put("password", passwordEdit.getText().toString());
                service = retrofit.create(APIService.class);
                Call<ResponseBody> call = service.login(params);
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
                Log.e("s",s);
                String[] idconfirm = s.split("//");
                //Log.e("split",idconfirm[1]);


                if (idconfirm[0].equals("true")){
                    //email 와 비밀번호 일치
                    //자동로그인 을 위해 sharedpreference에 저장
                    sp=getSharedPreferences("member_autologin", MODE_PRIVATE);
                    editor = sp.edit();
                    editor.putString("email",emailEdit.getText().toString());
                    editor.putString("nickname",idconfirm[1]);
                    editor.commit();
                    Intent result = new Intent();
                    result.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    setResult(RESULT_OK, result);
                    finish();


                }else {
                    //email 와 비밀번호 불일치
                    Toast.makeText(LoginActivity.this,"이메일과 비밀번호 정보가 맞지 않습니다.",Toast.LENGTH_SHORT).show();
                }

            }
        }
        syncTaskUploadClass asyncTaskUploadClassRe = new syncTaskUploadClass();
        asyncTaskUploadClassRe.execute();
    }
}//Class

package com.example.user.accessaryshopping.signUp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.accessaryshopping.R;
import com.example.user.accessaryshopping.APIService;
import com.google.gson.JsonArray;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class SingUpFinal extends AppCompatActivity {
    ImageView cancleBtn, nextBtn;
    EditText nickNameEdit, emailEdit, passwordEdit;
    TextView nickNameCon, emailCon, passwordCon;



    //retrofit 이용하기 위한 변수

    Retrofit retrofit;
    APIService service;

    //회원가입 완료 버튼 이 뜨게 확인 변수
    Boolean nickNameSucess, emailSucess, passwordSucess;

    LinearLayout singUpLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up_final);

        //처음에 불린 값들이 null 값 이기 때문에
        nickNameSucess = false;
        emailSucess = false;
        passwordSucess = false;

        //화면 왼쪽 상단 x버튼
        cancleBtn = (ImageView) findViewById(R.id.cancelBtn);

        nickNameEdit = (EditText) findViewById(R.id.nickNameEdit);
        nickNameCon = (TextView) findViewById(R.id.nickNameCon);

        emailCon = (TextView) findViewById(R.id.emailCon);
        emailEdit = (EditText) findViewById(R.id.emailEdit);

        passwordEdit = (EditText) findViewById(R.id.passwordEdit);
        passwordCon = (TextView) findViewById(R.id.passwordCon);

        singUpLayout = (LinearLayout) findViewById(R.id.singUpLayer);
        //회원 가입 완료 버튼
        nextBtn = (ImageView) findViewById(R.id.nextBtn);

        //통신을위해 retrofit 빌드 해주기
        retrofitStart();
        //닉네임 입력후 유효성 판단
        //닉네임 적을시 2~8자릿수 판단해서 텍스트뷰로 보여주기 또는 닉네임 중복 확인
        nickNameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                nickNameCon.setText("");
                if (!hasFocus) {
                    String nickNameNum = nickNameEdit.getText().toString();
                    //닉네이 중복 확인 닉네임 문자가 2개 보다 적을 경우
                    if (nickNameNum.length() < 2) {
                        nickNameCon.setVisibility(View.VISIBLE);
                        nickNameCon.setText("2-10자로 입력해주세요.");
                        nickNameCon.setTextColor(Color.parseColor("#ffcc0000"));
                        nickNameSucess = false;
                    } else {
                        nickNameCon.setVisibility(View.VISIBLE);
                        //동기식 통식은 위해 AsyncTask 이용 (이유 : 비동기식 방식을 이용하면 쓰레드가 겹쳐서 ui가 움직이지 않기 때문이다.)
                        nickNameCheckhttpConnect();
                    }
                    //닉네임 , 이메일 , 비밀번호 유효성이 다 알맞으면 회원가입 버튼 생성 메소드
                    singUpSuccess();
                }
            }
        });


        //이메일 입력후 유효성 체크
        emailEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                emailCon.setText("");
                if (!hasFocus) {
                    //이메일 값 가져오기
//                String emailNum = emailEdit.getText().toString();
                    //이메일 유효성 체크
                    Pattern p = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
                    Matcher m = p.matcher((emailEdit).getText().toString());
                    if (!m.matches()) {
                        emailCon.setVisibility(View.VISIBLE);
                        emailCon.setText("이메일 형식이 아닙니다.");
                        emailCon.setTextColor(Color.parseColor("#ffcc0000"));
                        emailSucess = false;
                    } else {
                        emailCon.setVisibility(View.VISIBLE);
                        emailCheckhttpConnect();
                    }
                    singUpSuccess();
                }
            }
        });

        //비밀 번호 유효성 체크
        //비밀 번호 입력후 확인
        //마지막 항문 이기 때문에 즉시 조건에 만족할 경우 버튼이 바로뜨게함.
        passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String passwordNum = passwordEdit.getText().toString();
                if (passwordNum.length() > 7) {
                    passwordCon.setVisibility(View.VISIBLE);
                    passwordCon.setText("사용 가능합니다.");
                    passwordCon.setTextColor(Color.parseColor("#ff669900"));
                    passwordSucess = true;
                } else {
                    passwordCon.setVisibility(View.INVISIBLE);
                    passwordSucess = false;
                }
                singUpSuccess();

            } // onTextChanged

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        //onSearch();
        //next 이미지 버튼 클릭시( 회원가입 버튼 )
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //http 통신을 위한 빌드 (메소드 실행)
                retrofitStart();

                //hash map 값으로 서버에 넘겨줍니다.
                //비동기식 방식
                Map<String, Object> params = new HashMap<>();
                params.put("nickName", nickNameEdit.getText().toString());
                params.put("email", emailEdit.getText().toString());
                params.put("password", passwordEdit.getText().toString());
                service = retrofit.create(APIService.class);
                Call<ResponseBody> call = service.singUp(params);

                call.enqueue(new Callback<ResponseBody>() {
                    //서버에서 보내주는 값을 받는곳
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        try {
                            Log.e("test", response.body().string());

                            //자신을 호출한 Activity로 데이터를 보낸다.
                            Intent result = new Intent();
                            result.putExtra("email", emailEdit.getText().toString());
                            result.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            setResult(RESULT_OK, result);
                            finish();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    //서버에 연결이 실패되면 실행 되는곳
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(SingUpFinal.this, "네트워크를 확인해주세요", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        //화면 왼쪽 상단 x버튼 클릭시
        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }//onCreate

    //닉네임 , 이메일 , 비밀번호 유효성이 다 알맞으면 회원가입 버튼 생성
    public void singUpSuccess() {
        if (nickNameSucess && emailSucess && passwordSucess) {
            singUpLayout.setVisibility(View.VISIBLE);
        } else {
            singUpLayout.setVisibility(View.INVISIBLE);
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

    //http 통신을 통 값을 받아서 정리 닉네임 중복을 위해 있는것
    //동기식 방식
    public void nickNameCheckhttpConnect() {

        class syncTaskUploadClass extends AsyncTask<Void, Void, String> {


            @Override
            protected String doInBackground(Void... voids) {

                //서버에 보낼값 정의
                Map<String, Object> params = new HashMap<>();
                params.put("nickName", nickNameEdit.getText().toString());
                service = retrofit.create(APIService.class);
                Call<ResponseBody> call = service.nickNameCheck(params);
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
                    Log.e("nicknamdcheck", s);
                    String nickNameCheck = s;
                    //닉네임 중복체크
                    if (nickNameCheck.equals("true")) {
                        //닉네임이 중복 안되는 경우
                        nickNameCon.setText("이용 가능합니다.");
                        nickNameCon.setTextColor(Color.parseColor("#ff669900"));
                        nickNameSucess = true;
                    } else {
                        //닉네임이 중복되는 경우
                        nickNameCon.setText("중복되었습니다.");
                        nickNameCon.setTextColor(Color.parseColor("#ffcc0000"));
                        nickNameSucess = false;
                    }
                }catch (NullPointerException e){
                    Toast.makeText(SingUpFinal.this,"네트워크 오류",Toast.LENGTH_SHORT).show();
                }

            }
        }
        syncTaskUploadClass asyncTaskUploadClassRe = new syncTaskUploadClass();
        asyncTaskUploadClassRe.execute();
    }

    //email 중복 체크를 위한 것
    public void emailCheckhttpConnect(){
        class syncTaskUploadClass extends AsyncTask<Void, Void, String> {


            @Override
            protected String doInBackground(Void... voids) {

                //서버에 보낼값 정의
                Map<String, Object> params = new HashMap<>();
                params.put("email", emailEdit.getText().toString());
                service = retrofit.create(APIService.class);
                Call<ResponseBody> call = service.emailChenk(params);
                try {

                    return call.execute().body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                call.enqueue(new Callback<ResponseBody>() {
//                    @Override
//                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                        try {
//                            return call.execute().body().string();
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ResponseBody> call, Throwable t) {
//
//                    }
//                });
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.e("emailcheck", s);
                String emailCheck = s;
                //닉네임 중복체크
                if (emailCheck.equals("true")) {
                    //닉네임이 중복 안되는 경우
                    emailCon.setText("이용가능합니다.");
                    emailCon.setTextColor(Color.parseColor("#ff669900"));
                    emailSucess = true;
                } else {
                    //닉네임이 중복되는 경우
                    emailCon.setText("중복되었습니다.");
                    emailCon.setTextColor(Color.parseColor("#ffcc0000"));
                    emailSucess = false;
                }
            }
        }
        syncTaskUploadClass asyncTaskUploadClassRe = new syncTaskUploadClass();
        asyncTaskUploadClassRe.execute();
    }


}//Class

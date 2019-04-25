package com.example.user.accessaryshopping.signUp;

import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.accessaryshopping.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SingUpMobileConfirm extends AppCompatActivity {

    //휴대전화 인증번호를 통해 회원가입을 시도할려고 했으나 돈이들어 사용 못함

    ImageView cancleBtn;
    EditText phoneEdit;
    TextView phoneNumValid, phoConTxt;
    Button phoneConBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up_mobile_confirm);

        //화면 왼쪽 상단 x버튼
        cancleBtn = (ImageView) findViewById(R.id.cancelBtn);
        //번호 입력받는 edittext
        phoneEdit = (EditText) findViewById(R.id.phoneEdit);
        //번호 유효성 검사 안맞을시 나오는 빨간 텍스트
        phoneNumValid = (TextView) findViewById(R.id.phoneNumValid);
        //번호가 유효성 이 맞지 않거나,아무것도 없을시 나오는 인증번호 텍스트뷰
        phoConTxt = (TextView) findViewById(R.id.phoConTxt);
        //번호 유효성 검사 맞을시 나오는 인증번호 받기 버튼
        phoneConBtn = (Button) findViewById(R.id.phoneConBtn);

        //화면 왼쪽 상단 x버튼 클릭시
        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //휴대전화 번호 실시간 유효성 체크
        phoneEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phoneNumConfirm = phoneEdit.getText().toString();
                int phoneNumCount = phoneNumConfirm.length();
                Pattern p = Pattern.compile("^\\s*(010|011|012|013|014|015|016|017|018|019)(-|\\)|\\s)*(\\d{3,4})(-|\\s)*(\\d{4})\\s*$");
                Matcher m = p.matcher((phoneEdit).getText().toString());

                //토스트로 갯수가 어떻게 측정되는지 확인하기 위해서
                //Toast.makeText(SingUpMobileConfirm.this, phoneNumConfirm.length()+"",Toast.LENGTH_SHORT).show();

                //휴대전화 번호가 9개 넘어가면 그때부터 유효성 검사 체크 , 유효성 검사가 올바르면 빨간텍스트가 안뜬다.
                //번호가 9개 이하이면 인증번호 받기 버튼도 없어지면서 텍스트뷰로 바뀐다.
                if (phoneNumConfirm.length() > 9) {
                    if (!m.matches()) {
                        phoneNumValid.setVisibility(View.VISIBLE);
                    } else {
                        phoneConBtn.setVisibility(View.VISIBLE);
                        phoConTxt.setVisibility(View.INVISIBLE);

                    }
                } else {
                    phoneNumValid.setVisibility(View.INVISIBLE);
                    phoneConBtn.setVisibility(View.INVISIBLE);
                    phoConTxt.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }//onCreate


}//SingUpMobileConfirm

package com.example.user.accessaryshopping.goodsList;

import android.content.Intent;
import android.media.Image;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.accessaryshopping.R;

import kr.co.bootpay.Bootpay;
import kr.co.bootpay.BootpayAnalytics;
import kr.co.bootpay.CancelListener;
import kr.co.bootpay.CloseListener;
import kr.co.bootpay.ConfirmListener;
import kr.co.bootpay.DoneListener;
import kr.co.bootpay.ErrorListener;
import kr.co.bootpay.ReadyListener;
import kr.co.bootpay.enums.Method;
import kr.co.bootpay.enums.PG;




public class GoodsPaymentActivity extends AppCompatActivity {

    Button  completeBtn;
    TextView mailView,addressView , priceTxt;
    EditText detailAddrresEdit, requestEdit, phoneNumEdit;
    String mailnum, address , goodsPrice, goodsTitle;
    ImageView backBtn;
    boolean addressCheck=false;
    private int stuck = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_payment);
        // 초기설정 - 해당 프로젝트(안드로이드)의 application id 값을 설정합니다. 결제와 통계를 위해 꼭 필요합니다.
        BootpayAnalytics.init(GoodsPaymentActivity.this, "5c5c0919b6d49c219295876b");



        detailAddrresEdit = (EditText) findViewById(R.id.addressDetailEdit);
        requestEdit = (EditText) findViewById(R.id.requestEdit);
        phoneNumEdit = (EditText) findViewById(R.id.phoneNumEdit);

        //넘어 오는값 받기
        Intent intent = getIntent();
        goodsPrice = intent.getStringExtra("goodsprice");
        goodsTitle = intent.getStringExtra("goodstitle");
        addressView = (TextView) findViewById(R.id.addressTxtView);
        addressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DaumWebViewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent,1000);
            }
        });
        priceTxt = (TextView)findViewById(R.id.priceTxt);
        priceTxt.setText(goodsPrice+"원");

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




        completeBtn = (Button) findViewById(R.id.completeBtn);
        completeBtn.setText(goodsPrice+"원 결제하기");
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!addressCheck){
                    Toast.makeText(GoodsPaymentActivity.this,"주소를 입력해 주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (detailAddrresEdit.getText().length()==0){
                    Toast.makeText(GoodsPaymentActivity.this,"상세주소를 입력해 주세요.",Toast.LENGTH_SHORT).show();
                    detailAddrresEdit.requestFocus();
                    return;
                }
                if (requestEdit.getText().length()==0){
                    Toast.makeText(GoodsPaymentActivity.this,"요청사항을 입력해 주세요.",Toast.LENGTH_SHORT).show();
                    requestEdit.requestFocus();
                    return;
                }
                if (phoneNumEdit.getText().length()==0){
                    Toast.makeText(GoodsPaymentActivity.this,"핸드폰 번호를 입력해 주세요.",Toast.LENGTH_SHORT).show();
                    phoneNumEdit.requestFocus();
                    return;
                }
                Bootpay.init(getFragmentManager())
                        .setApplicationId("5c5c0919b6d49c219295876b") // 해당 프로젝트(안드로이드)의 application id 값
                        .setPG(PG.INICIS) // 결제할 PG 사
                        .setUserPhone(phoneNumEdit.getText().toString()) // 구매자 전화번호
                        //.setMethod() // 결제수단
                        .setName(goodsTitle) // 결제할 상품명
                        .setOrderId("1234") //고유 주문번호로, 생성하신 값을 보내주셔야 합니다.
                        .setPrice(Integer.parseInt(goodsPrice)) // 결제할 금액
                        //.setAccountExpireAt("2018-09-22") // 가상계좌 입금기간 제한 ( yyyy-mm-dd 포멧으로 입력해주세요. 가상계좌만 적용됩니다. 오늘 날짜보다 더 뒤(미래)여야 합니다 )
                        .setQuotas(new int[] {0,2,3}) // 일시불, 2개월, 3개월 할부 허용, 할부는 최대 12개월까지 사용됨 (5만원 이상 구매시 할부허용 범위)
                        .addItem("마우스", 1, "ITEM_CODE_MOUSE", 100) // 주문정보에 담길 상품정보, 통계를 위해 사용
                        .addItem("키보드", 1, "ITEM_CODE_KEYBOARD", 200, "패션", "여성상의", "블라우스") // 주문정보에 담길 상품정보, 통계를 위해 사용
                        .onConfirm(new ConfirmListener() { // 결제가 진행되기 바로 직전 호출되는 함수로, 주로 재고처리 등의 로직이 수행
                            @Override
                            public void onConfirm(@Nullable String message) {
                                if (0 < stuck) Bootpay.confirm(message); // 재고가 있을 경우.
                                else Bootpay.removePaymentWindow(); // 재고가 없어 중간에 결제창을 닫고 싶을 경우
                                Log.e("confirm", message);
                            }
                        })
                        .onDone(new DoneListener() { // 결제완료시 호출, 아이템 지급 등 데이터 동기화 로직을 수행합니다
                            @Override
                            public void onDone(@Nullable String message) {
                                Log.e("done", message);
                                Toast.makeText(GoodsPaymentActivity.this,"결제가 완료됬습니다.",Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .onReady(new ReadyListener() { // 가상계좌 입금 계좌번호가 발급되면 호출되는 함수입니다.
                            @Override
                            public void onReady(@Nullable String message) {
                                Log.e("ready", message);
                            }
                        })
                        .onCancel(new CancelListener() { // 결제 취소시 호출
                            @Override
                            public void onCancel(@Nullable String message) {
                                Log.e("cancel", message);
                            }
                        })
                        .onError(new ErrorListener() { // 에러가 났을때 호출되는 부분
                            @Override
                            public void onError(@Nullable String message) {
                                Log.e("error", message);
                            }
                        })
                        .onClose(new CloseListener() { //결제창이 닫힐때 실행되는 부분
                            @Override
                            public void onClose(String message) {
                                Log.e("close", "close");
                            }
                        })
                        .show();



            }
        });
    }//onCreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000 && resultCode==RESULT_OK){
            mailnum = data.getStringExtra("mailnum"); //우편번호
            address = data.getStringExtra("address"); //일반주소
            addressView.setText("("+mailnum+") "+address);
            addressCheck=true;
        }

    }
}

package com.example.user.accessaryshopping.goodsList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.user.accessaryshopping.APIService;
import com.example.user.accessaryshopping.NetworkClient;
import com.example.user.accessaryshopping.R;

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

public class GoodsListDetailActivity extends AppCompatActivity {

    TextView goodsTxtView, goodPriceTxtView;
    Button paymentBtn;
    ImageView closeBtn;
    //이전 엑티비티 에서 넘어온 no 값
    String no, goodsTitle, goodsPrice;
    private ViewPager viewPager;

    ArrayList<String> imgArr = new ArrayList<>();

    ProgressDialog progressDialog;

    //아이디 별로 상품 리스트 json으로 받아오기
    JSONArray goodsList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_list_detail);
        goodsTxtView = (TextView) findViewById(R.id.goodsTxtView);
        goodPriceTxtView = (TextView) findViewById(R.id.goodsPriceTxtView);

        //상품 리스트에서 넘어온 값 받아오기
        Intent intent = getIntent();
        no = intent.getStringExtra("no");
        //Toast.makeText(GoodsListDetailActivity.this,intent.getStringExtra("no"),Toast.LENGTH_SHORT).show();

        paymentBtn = (Button) findViewById(R.id.paymentBtn);
        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GoodsPaymentActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("goodsprice",goodsPrice);
                intent.putExtra("goodstitle",goodsTitle);
                startActivity(intent);
            }
        });

        closeBtn = (ImageView) findViewById(R.id.closeBtn2);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        goodsDetailHttpConnect();
    }//onCreate

    public void goodsDetailHttpConnect() {
        //레트로핏으로 빌드
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        APIService service = retrofit.create(APIService.class);
        class syncTaskUploadClass extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(GoodsListDetailActivity.this, "이미지 로딩중", "please wait", false, false);
                //이미지 arr 클리어
                imgArr.clear();
            }

            @Override
            protected String doInBackground(Void... voids) {

                Map<String, Object> params = new HashMap<>();
                params.put("no", no);

                Call<ResponseBody> call = service.goodsListDetail(params);

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

                JSONObject jsonObj = null;
                try {
                    jsonObj = new JSONObject(s);
                    goodsList = jsonObj.getJSONArray("result");

                    for (int i = 0; i < goodsList.length(); i++) {
                        JSONObject goodsdetail = goodsList.getJSONObject(i);
                        goodsTitle = goodsdetail.getString("goodstitle");
                        goodsPrice = goodsdetail.getString("goodsprice");
                        //if (!goodsdetail.getBoolean("img2")){
                        Log.e("s", goodsdetail.getString("img2").isEmpty() + "");
                        //이미지가 최대 3개라서 이미지 빼오기 위해 for 문
                        for (int k = 0; k < 3; k++) {
                            //이미지값이 비어있다면 멈춰라.
                            if (goodsdetail.getString("img" + k).isEmpty()) {
                                break;
                            }
                            //imgArr 추가
                            imgArr.add(goodsdetail.getString("img" + k));
                        }
                        Log.e("imgsize", imgArr.size() + "");
                        //}
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //txt 뷰 값 서버에 가져와서 화면에 띄우기
                goodsTxtView.setText(goodsTitle);
                goodPriceTxtView.setText(goodsPrice);

                progressDialog.dismiss();

                //adapter view 띄우기
                viewPager = (ViewPager) findViewById(R.id.imageViewPager);
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(GoodsListDetailActivity.this);
                viewPager.setAdapter(viewPagerAdapter);

            }
        }//syncTaskUploadClass
        syncTaskUploadClass asyncTaskUploadClassRe = new syncTaskUploadClass();
        asyncTaskUploadClassRe.execute();
    }

    //ViewPagerAdapter 를 안에 넣은것은 받아오는 값때문에 클래스를 여기에 만들어둠
    public class ViewPagerAdapter extends PagerAdapter {
        private LayoutInflater inflater;
        private Context context;

        public ViewPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return imgArr.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == ((ConstraintLayout) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.slider, container, false);
            //slider 에 있는 이미지 뷰 설정
            ImageView imageView = (ImageView) v.findViewById(R.id.sliderImageView);
            TextView pageNum = (TextView) v.findViewById(R.id.pageNum);
            //imageView.setImageURI(mSelected.get(position));
            //Log.e("position", lowList.get(position) + "");
            Glide.with(context)
                    .load("http://13.209.144.49/goodsimages/" + imgArr.get(position))
                    .into(imageView);
            pageNum.setText(position + 1 + "/" + imgArr.size());
            //imageView.setImageBitmap(lowList.get(position));
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.invalidate();
        }
    }//ViewPagerAdapter
}

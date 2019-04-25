package com.example.user.accessaryshopping.goodsList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;


import com.example.user.accessaryshopping.APIService;
import com.example.user.accessaryshopping.NetworkClient;
import com.example.user.accessaryshopping.R;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class GoodsListAddActivity extends AppCompatActivity {

    private static final int INTENT_REQUEST_GET_IMAGES = 13;

    private ViewPager viewPager;
    ImageView closeBtn;
    Button imgAddBtn, finalGoodsAddBtn;
    EditText goodsTitleEdit, goodsPriceEdit;
    //넘어온용 사진 저장
    //ArrayList<Uri> imgUri = new ArrayList<>();
    //저화질용
    ArrayList<Bitmap> lowList = new ArrayList<>();
    //서버에 넘기기위한 변환된 사진 변수들
    ArrayList<String> convertImage = new ArrayList<>();
    //넘어온 사진 저장
    List<Uri> imgUri;
    List<String> imgString = null;

    ProgressDialog progressDialog;
    //이미지 등록 체크
    Boolean imageCheck = false;

    //닉네임 정보 가져오기 위해
    SharedPreferences membersp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_list_add);

        goodsTitleEdit = (EditText) findViewById(R.id.goodsTitleEdit);
        goodsPriceEdit = (EditText) findViewById(R.id.goodsPriceEdit);


        //클로즈 뷰 버튼
        closeBtn = (ImageView) findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //이미지 선택 버튼
        imgAddBtn = (Button) findViewById(R.id.goodsImgAdd);
        imgAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //다중
                //Pix.start(GoodsListAddActivity.this,INTENT_REQUEST_GET_IMAGES,3);
                Matisse.from(GoodsListAddActivity.this)
                        .choose(MimeType.ofImage())
                        .countable(true)
                        .maxSelectable(3)
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .thumbnailScale(1f)
                        .imageEngine(new Glide4Engine())
                        .forResult(INTENT_REQUEST_GET_IMAGES);

            }
        });

        finalGoodsAddBtn = (Button) findViewById(R.id.finalGoodsAddBtn);
        finalGoodsAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!imageCheck) {
                    Toast.makeText(GoodsListAddActivity.this, "사진을 등록하세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                //상품 이름 유효성 체크
                if (goodsTitleEdit.getText().toString().length() == 0) {
                    Toast.makeText(GoodsListAddActivity.this, "상품 이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                    goodsTitleEdit.requestFocus();
                    return;
                }
                //상품 가격 유효성 체크
                if (goodsPriceEdit.getText().toString().length() == 0) {
                    Toast.makeText(GoodsListAddActivity.this, "상품 가격을 입력해주세요", Toast.LENGTH_SHORT).show();
                    goodsPriceEdit.requestFocus();
                    return;
                }

                goodsAddHttpConnect();
            }
        });
    }//oncreate


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_REQUEST_GET_IMAGES && resultCode == RESULT_OK) {
            //초기화를 위해서
            if (imgString != null) {
                imgString.clear();
                lowList.clear();
            }

            //이미지 유효성 체크
            imageCheck = true;

            imgUri = Matisse.obtainResult(data);
            imgString = Matisse.obtainPathResult(data);
            Log.e("Matisse", "mSelected: " + imgString.get(0));

            progressDialog = ProgressDialog.show(GoodsListAddActivity.this, "이미지 로딩중", "please wait", false, false);
            //uri 다중 이미지 다중 비트맵 으로 변환 왜냐하면 이미지 사이즈 줄이기 위해서
            LoadBitmap loadBitmap = new LoadBitmap();
            loadBitmap.execute();
        }

    }


    //ViewPagerAdapter 를 안에 넣은것은 받아오는 값때문 ?
    public class ViewPagerAdapter extends PagerAdapter {
        private LayoutInflater inflater;
        private Context context;

        public ViewPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return lowList.size();
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
            Log.e("position", lowList.get(position) + "");

            pageNum.setText(position + 1 + "/" + lowList.size());
            imageView.setImageBitmap(lowList.get(position));
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.invalidate();
        }
    }//ViewPagerAdapter

    class LoadBitmap extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            //다시한번 할경우에 뷰페이저가 계속 쌓이기 때문에


            for (int i = 0; i < imgString.size(); i++) {
                String path = imgString.get(i);
                Log.e("path", path);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                try {
                    ExifInterface exif = new ExifInterface(path);
                    int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int exifDegree = exifOrientationToDegrees(exifOrientation);
                    bitmap = resize(bitmap, exifDegree);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                lowList.add(bitmap);

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();

            //비트맵 처리가 끝난 다음에 뷰페이저를 띄어준다.

            viewPager = (ViewPager) findViewById(R.id.imageViewPager);
            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(GoodsListAddActivity.this);
            viewPager.setAdapter(viewPagerAdapter);
            //testimg.setImageBitmap(lowList.get(0));
        }
    }

    //기본적으로 사진을 촬영하면 가로로 찍힌다. 그래서 사진을 90도 돌려주기 위해서.
    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }//exifOrientationToDegrees

    //사진의 크기를 줄여주기위한 메소드.
    private Bitmap resize(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        //화면 화질 비율 선택
        bitmap = Bitmap.createScaledBitmap(bitmap, 720, 480, true);
        return bitmap;
    }//rotate

    public void goodsAddHttpConnect() {

        //레트로핏으로 빌드
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        APIService service = retrofit.create(APIService.class);
        progressDialog = ProgressDialog.show(GoodsListAddActivity.this, "이미지 로딩중", "please wait", false, false);

        //닉네임 정보 가져오기 위해서
        membersp = getApplicationContext().getSharedPreferences("member_autologin", MODE_PRIVATE);


        convertImage.clear();
        class syncTaskUploadClass extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected String doInBackground(Void... voids) {
                //map 형태로 서버에 정보 전달.
                Map<String, RequestBody> params = new HashMap<>();
                params.put("nickname",createPartFromString(membersp.getString("nickname","error")));
                params.put("goodstitle",createPartFromString(goodsTitleEdit.getText().toString()));
                params.put("goodsprice",createPartFromString(goodsPriceEdit.getText().toString()));
                params.put("imgsize",createPartFromString(lowList.size()+""));

                MultipartBody.Part[] surveyImagesParts = new MultipartBody.Part[lowList.size()];
                //List<MultipartBody.Part> list = new ArrayList<>();
                //Map<String, RequestBody> imgmap = new HashMap<>();
                //비트맵 전송을 위해서
                for(int i = 0 ; i < lowList.size() ; i++){
                    //File file = new File(lowList.get(i),"dd");

                    //이미지를 바이트로 변화시켜 서버에 넘기기
                    byte[] byteArray;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    lowList.get(i).compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byteArray = byteArrayOutputStream.toByteArray();
                    RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/jpeg"),byteArray);
                    surveyImagesParts[i] = MultipartBody.Part.createFormData("file[]","image"+i,fileReqBody);

                    //imgmap.put("image"+i,fileReqBody);
                    //convertImage.add(Base64.encodeToString(byteArray, Base64.DEFAULT));
                    //params.put("image"+i, convertImage);//이미지 키값
                    //Log.e("image",convertImage+"");
                }


                Call<ResponseBody> call = service.goodsList(params,surveyImagesParts);

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
                progressDialog.dismiss();
                try {
                    Log.e("s",s);
                    Toast.makeText(GoodsListAddActivity.this,"상품이 등록되었습니다.",Toast.LENGTH_SHORT).show();
                    finish();
                }catch (NullPointerException e){

                }


            }
        }//syncTaskUploadClass
        syncTaskUploadClass asyncTaskUploadClassRe = new syncTaskUploadClass();
        asyncTaskUploadClassRe.execute();

    }//goodsAddHttpConnect

    //multipart 로 보낼때 변환이 필요해서 일반 스트링으로 넣으면 오류난다.
    @NonNull
    private RequestBody createPartFromString(String descriptionString){
        return RequestBody.create(
                MultipartBody.FORM,descriptionString);
    }

}

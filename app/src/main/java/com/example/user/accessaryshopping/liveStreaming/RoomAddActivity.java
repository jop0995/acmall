package com.example.user.accessaryshopping.liveStreaming;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.accessaryshopping.APIService;
import com.example.user.accessaryshopping.HomeActivity;
import com.example.user.accessaryshopping.NetworkClient;
import com.example.user.accessaryshopping.R;
import com.example.user.accessaryshopping.liveStreaming.kurentoandroid.broadcaster.BroadCasterActivity_;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RoomAddActivity extends AppCompatActivity {

    ImageView close , thumbnailImg;
    TextView title;
    Button addBtn;
    EditText roomName;
    String roomStringname;

    //retrofit 이용하기 위한 변수
//    Retrofit retrofit;
//    APIService service;

    //방번호를 라이브스트리밍 이용할때 쓰기위해 sharedPreference 사용
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    //이미지 값 돌아왔을때 사용하는 변수들
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_CAMERA = 3;
    private static int previousPhotoPath = 0; // 이전 포토 임시저장 파일 지울때 판별역활
    private String mCurrentPhotoPath;
    private Uri photoUri;
    Bitmap fixBitmap; //url 비트맵으로
    byte[] byteArray; //서버에 넘기기위해서 byte 로 바꿔줌
    ByteArrayOutputStream byteArrayOutputStream;
    String convertImage;
    ProgressDialog progressDialog;

    String endImageName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_add);



        //이미지 전송 함수들
        byteArrayOutputStream = new ByteArrayOutputStream();

        thumbnailImg = (ImageView) findViewById(R.id.thumnailImage);
        //방번호 입력값 변수
        roomName = (EditText) findViewById(R.id.roomNameEdit);

        //타이틀 글꼴 설정 해주기
        title = (TextView) findViewById(R.id.title);
        Typeface type = Typeface.createFromAsset(getApplication().getAssets(),"fonts/lee.ttf");
        title.setTypeface(type);

        //클로즈 버튼 클릭시
        close = (ImageView) findViewById(R.id.closeImageView);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //방 최종 추가 버튼
        addBtn = (Button)findViewById(R.id.addbtn);
        addBtn.setTypeface(type);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomStringname = roomName.getText().toString();

                roomAddHttpConnect();
//                samplehttp();
            }
        });

        //이미지 클릭시
        thumbnailImg = (ImageView) findViewById(R.id.thumnailImage);
        thumbnailImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(HomeActivity.this,"클릭된다.",Toast.LENGTH_SHORT).show();
                //사진 찍기 다이얼로그 나온다.
                AlertDialog.Builder builder = new AlertDialog.Builder(RoomAddActivity.this);
                final String str[] = {"사진촬영", "앨범선택"};
                builder.setTitle("프로필 사진 선택").setNegativeButton("취소",null).setItems(str, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "선택된것은 :" + str[which], Toast.LENGTH_SHORT).show();
                        if (which == 0){
                            takePhoto();
                        }else {
                            goToAlbum();
                            Log.e("앨범","앨범선택");
                        }
                    }
                });//alert 안에 클릭 리스너
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });




    }//oncreate



    //로그인을 위한 asynctask 이메일값 패스워드를 데이터 베이스가 판별해 클라이언트에게 보내준다.
    public void roomAddHttpConnect(){
//        fixBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
//        byteArray = byteArrayOutputStream.toByteArray();
//        convertImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        //retrofit 빌드 해주기
        //retrofitStart();

        Retrofit retrofit= NetworkClient.getRetrofitClient(this);

        APIService service = retrofit.create(APIService.class);

        class syncTaskUploadClass extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(RoomAddActivity.this, "로딩중..", "please wait", false, false);
            }

            @Override
            protected String doInBackground(Void... voids) {
                Intent intent = getIntent();


                //map 형태로 서버에 정보 전달.
                Map<String, RequestBody> params = new HashMap<>();
                params.put("roomName",createPartFromString(roomStringname));
                params.put("nickname",createPartFromString(intent.getStringExtra("nickname")));

                //이미지 업로드 위해서 해준다.
                File file = new File(endImageName); //retrofit 시험
                RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
                MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);


//                service = retrofit.create(APIService.class);
                Call<ResponseBody> call = service.roomAdd(params,part);
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

                Log.e("errordddd",s);
                if (s.equals("true")){
                    //방제목이 중복되었을때
                    Toast.makeText(RoomAddActivity.this,"방제목이 중복되었습니다. 다시 입력해주세요.",Toast.LENGTH_SHORT).show();
                }else {
                    //라이브 스트리밍 방송 시작
                    //presenter 방번호 저장 하기 위해서
                    sp = getApplicationContext().getSharedPreferences("webrtc_roomname",MODE_PRIVATE);
                    editor = sp.edit();
                    editor.putString("roomname",roomStringname);
                    editor.commit();

                    BroadCasterActivity_.intent(RoomAddActivity.this).start();
                    finish();
                }
                progressDialog.dismiss();
            }
        }

        syncTaskUploadClass asyncTaskUploadClassRe = new syncTaskUploadClass();
        asyncTaskUploadClassRe.execute();
    }

    //multipart 로 보낼때 변환이 필요해서 일반 스트링으로 넣으면 오류난다.
    @NonNull
    private RequestBody createPartFromString(String descriptionString){
        return RequestBody.create(
            MultipartBody.FORM,descriptionString);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_ALBUM) {
            if (data == null) {
                return;
            }
            photoUri = data.getData();
            cropImage();
        } else if (requestCode == PICK_FROM_CAMERA) {
            cropImage();
            // 갤러리에 나타나게
            MediaScannerConnection.scanFile(RoomAddActivity.this,
                    new String[]{photoUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        } else if (requestCode == CROP_FROM_CAMERA) {

            thumbnailImg.setImageURI(null);

            thumbnailImg.setImageURI(photoUri);
            Log.e("mcu 보기", "" + mCurrentPhotoPath);
            Log.e("photoUri 보기", "" + photoUri);
            try {
                fixBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(RoomAddActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
            }

            if (previousPhotoPath == 1) { // 카메라에서 사진을 가져올경우 임시파일삭제 하고 앨범에서 가져올때 삭제 하지 마라
                File storageDir = new File(mCurrentPhotoPath);//카메라 켜고 크롭했을때 이전 임시파일삭제
                if (storageDir.exists()) {
                    storageDir.delete();
                }
            }


        }
    }
    //카메라로 찍으라고 요청 메소드
    private void takePhoto() {
        previousPhotoPath = 0;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(RoomAddActivity.this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(RoomAddActivity.this,
                    "com.example.user.provider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
            Log.e("photoUri 주소 :", "" + mCurrentPhotoPath);
        }
        previousPhotoPath = 1;
    }

    //갤러리에서 가져오라는 요청 메소드
    private void goToAlbum() {
        previousPhotoPath = 2;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);

    }

    //    이미지가 저장될 파일을 만드는 함수 이름의 유일성을 주기위해서 시분초 를 이용한다.
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "nostest_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/NOSTest/");

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        if (previousPhotoPath == 0) {   //크롭을 했을때 이전 임시파일 제거
            mCurrentPhotoPath = image.getAbsolutePath();

        }
        endImageName=image+"";
        Log.e("image :", "" + image);
        return image;
    }

    //Android N crop image
    public void cropImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.grantUriPermission("com.android.camera", photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            grantUriPermission(list.get(0).activityInfo.packageName, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            /*Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();*/

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            File croppedFileName = null;

            try {
                croppedFileName = createImageFile();     //이때 mCurrentPhotoPath 주소가 크롭된 사진으로 바뀐다.
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + "/NOSTest/");
            File tempFile = new File(folder.toString(), croppedFileName.getName());


            photoUri = FileProvider.getUriForFile(RoomAddActivity.this,
                    "com.example.user.provider", tempFile);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                grantUriPermission(res.activityInfo.packageName, photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, CROP_FROM_CAMERA);
        }
    }
}//class

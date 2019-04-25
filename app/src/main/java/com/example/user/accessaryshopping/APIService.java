package com.example.user.accessaryshopping;

import com.example.user.accessaryshopping.liveStreaming.kurentoandroid.rtc_peer.kurento.models.response.ServerResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {

//    @Headers({
//            "Content-Type:application/json"
//    })

    //post 다중으로 값 내보낼때 쓰이는 방법
    @FormUrlEncoded
    @POST("signUp.php")
    Call<ResponseBody> singUp(@FieldMap Map<String, Object> map);

    @FormUrlEncoded
    @POST("nickNameCheck.php")
    Call<ResponseBody> nickNameCheck(@FieldMap Map<String, Object> map);

    @FormUrlEncoded
    @POST("emailCheck.php")
    Call<ResponseBody> emailChenk(@FieldMap Map<String, Object> map);

    @FormUrlEncoded
    @POST("login.php")
    Call<ResponseBody> login(@FieldMap Map<String, Object> map);

    //방 추가 할때 같이 추가하기 위해서
    @Multipart
    @POST("roomAdd.php")
    Call<ResponseBody> roomAdd(@PartMap Map<String, RequestBody> map, @Part MultipartBody.Part file);


    @POST("liveRoomList.php")
    Call<ResponseBody> roomList();


    @Multipart
    @POST("goodslistadd.php")
    Call<ResponseBody> goodsList(@PartMap Map<String, RequestBody> map, @Part MultipartBody.Part[] file);
//    @Part MultipartBody.Part[] file @PartMap Map<String, RequestBody> file

    @FormUrlEncoded
    @POST("goodsList.php")
    Call<ResponseBody> goodsListRecycler(@FieldMap Map<String, Object> map);

    @FormUrlEncoded
    @POST("goodsListDetail.php")
    Call<ResponseBody> goodsListDetail(@FieldMap Map<String, Object> map);

    @FormUrlEncoded
    @POST("goodListDelete.php")
    Call<ResponseBody> goodsListDelete(@FieldMap Map<String, Object> map);




}

package com.example.user.accessaryshopping.goodsList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.user.accessaryshopping.APIService;
import com.example.user.accessaryshopping.GoodsListActivity;
import com.example.user.accessaryshopping.NetworkClient;
import com.example.user.accessaryshopping.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class GoodsListAdapter extends RecyclerView.Adapter<GoodsListAdapter.ViewHolder> {
    Context context;
    ArrayList<GoodsListItem> items = new ArrayList<>();
    GoodsListActivity goodsListActivity = new GoodsListActivity();
    //다이얼로그 띄우기
    ProgressDialog progressDialog;

    public GoodsListAdapter(Context context, ArrayList<GoodsListItem> items) {

        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //adapter 가 뷰 아이템 뷰를 연결 해주는 역활
        View view = LayoutInflater.from(context).inflate(R.layout.goodslist_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GoodsListItem item = items.get(position);

        //상품 이름
        holder.itemTitle.setText("상품 이름 : "+item.getTitle());
        //상품 가격
        holder.itemPrice.setText("상품 가격 : "+item.getPrice());
        //상품 대표 이미지
        Glide.with(context)
                .load("http://13.209.144.49/goodsimages/"+item.getImg())
                .into(holder.itemImage);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context,item.getNo()+"\n"+position+item.getTitle()+item.getPrice(),Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, GoodsListDetailActivity.class);
                intent.putExtra("no",item.getNo());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle("확인")
                        .setMessage("정말 삭제하시겠습니까?")
                        .setPositiveButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //리스트 에 있는 아이템 롱클릭시 삭제하기 위한 메소드
                                goodsListDeleteHttpConnect(item.getNo());

                                Intent intent = new Intent(context,GoodsListActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                context.startActivity(intent);

                            }
                        });

                AlertDialog dialog = alertDialog.create();
                dialog.show();



                //true 로 해놓는 이유는 false로 할경우 롱클릭되고 짧은 클릭도 허용됨 동시에 두개가 허용
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView itemImage;
        TextView itemTitle, itemPrice;

        public final View mView;
        ViewHolder(View view){
            super(view);
            mView = view;

            itemImage = view.findViewById(R.id.goodsitemimage);
            itemTitle = view.findViewById(R.id.titletextView);
            itemPrice = view.findViewById(R.id.pricetextView);


        }
    }//class ViewHolder

    public void goodsListDeleteHttpConnect(String no){

        Retrofit retrofit = NetworkClient.getRetrofitClient(context);

        APIService service = retrofit.create(APIService.class);
        class syncTaskUploadClass extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(context, "삭제중..", "please wait", false, false);
            }

            @Override
            protected String doInBackground(Void... voids) {
                Map<String, Object> params = new HashMap<>();
                //아이템 고유의 값 넣어 주기
                params.put("no",no);

                Call<ResponseBody> call = service.goodsListDelete(params);
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
            }
        }//syncTaskUploadClass
        syncTaskUploadClass asyncTaskUploadClassRe = new syncTaskUploadClass();
        asyncTaskUploadClassRe.execute();
    }//goodsListDeleteHttpConnect
}

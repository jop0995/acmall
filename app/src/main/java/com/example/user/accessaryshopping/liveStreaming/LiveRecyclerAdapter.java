package com.example.user.accessaryshopping.liveStreaming;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.user.accessaryshopping.R;
import com.example.user.accessaryshopping.liveStreaming.kurentoandroid.viewer.ViewerActivity;
import com.example.user.accessaryshopping.liveStreaming.kurentoandroid.viewer.ViewerActivity_;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class LiveRecyclerAdapter extends RecyclerView.Adapter<LiveRecyclerAdapter.ViewHolder> {

    Context context;
    ArrayList<LiveRecyclerItem> items = new ArrayList<>();

    //방번호를 라이브스트리밍 이용할때 쓰기위해 sharedPreference 사용
    SharedPreferences sp , loginConSp;
    SharedPreferences.Editor editor;

    public LiveRecyclerAdapter(Context context, ArrayList<LiveRecyclerItem> items) {

        this.context = context;
        this.items = items;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //adapter 가 뷰 아이템 뷰를 연결 해주는 역활
        View view = LayoutInflater.from(context).inflate(R.layout.live_item,parent,false);
        /*ViewHolder holder = new ViewHolder(view);*/



        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //아이템 텍스트뷰의 글꼴을 위한 정의
        Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/lee.ttf");

        //item 에 순서값
        LiveRecyclerItem item = items.get(position);

        //글꼴 선언
        holder.itemTitle.setTypeface(type);

        //타이틀 텍스트뷰 값 넣어주기
        holder.itemTitle.setText(item.getTitle());
        //holder.itemImage.setImageResource(R.mipmap.ic_launcher);
        Glide.with(context)
                .load("http://13.209.144.49/images/"+item.getImg())
                .into(holder.itemImage);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginConSp = context.getSharedPreferences("member_autologin",MODE_PRIVATE);

                if (loginConSp.getString("email","fail") == "fail"){
                    Toast.makeText(context,"로그인 해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }


                Toast.makeText(context,item.getTitle(),Toast.LENGTH_SHORT).show();
                sp = context.getSharedPreferences("webrtc_roomname",MODE_PRIVATE);
                editor = sp.edit();
                editor.putString("roomname",item.getTitle());
                //방송상품목록을 보기위해서 방만들 사람의 닉네임 저장
                editor.putString("nickname",item.getNickname());
                editor.commit();
                Intent viewerIntent = new Intent(context, ViewerActivity_.class);
                viewerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(viewerIntent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView itemImage;
        TextView itemTitle;

        public final View mView;
        ViewHolder(View view) {
            super(view);
            mView = view;

            itemImage = view.findViewById(R.id.itemImage);
            itemTitle = view.findViewById(R.id.itemTitle);
        }
    }
}

package com.example.user.accessaryshopping.liveStreaming.kurentoandroid.liveChattingRecycler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.user.accessaryshopping.R;

import java.util.ArrayList;

public class LiveChattingRecyclerAdapter extends RecyclerView.Adapter<LiveChattingRecyclerAdapter.ViewHolder> {


    Context context;
    ArrayList<LiveChattingRecyclerItem> items = new ArrayList<>();

    public LiveChattingRecyclerAdapter(Context context, ArrayList<LiveChattingRecyclerItem> items){
        this.context = context;
        this.items = items;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //adapter 가 뷰 아이템 뷰를 연결 해주는 역활
        View view = LayoutInflater.from(context).inflate(R.layout.chatting_recycler_item,parent,false);



        return new ViewHolder(view);//new RecyclerView.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //item 에 순서값
        LiveChattingRecyclerItem item = items.get(position);

        holder.nickName.setText(item.getNickName());
        holder.messageTxt.setText(item.getMessage());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView nickName, messageTxt;

        public final View mView;
        ViewHolder(View view) {
            super(view);
            mView = view;

            nickName = view.findViewById(R.id.nickNameTxtView);
            messageTxt = view.findViewById(R.id.messageTxtView);

        }
    }

}


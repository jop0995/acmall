package com.example.user.accessaryshopping.liveStreaming;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.user.accessaryshopping.R;
import com.example.user.accessaryshopping.goodsList.GoodsListDetailActivity;
import com.example.user.accessaryshopping.goodsList.GoodsListItem;

import java.util.ArrayList;

public class LiveDialogRecyclerAdapter extends RecyclerView.Adapter<LiveDialogRecyclerAdapter.ViewHolder> {
    Context context;
    ArrayList<GoodsListItem> items = new ArrayList<>();

    public LiveDialogRecyclerAdapter(Context context, ArrayList<GoodsListItem> items){
        this.context = context;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.goodslist_item,parent,false);
        /*ViewHolder holder = new ViewHolder(view);*/

        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        GoodsListItem item = items.get(position);

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
    }

}

package com.example.serverside.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serverside.Common.Common;
import com.example.serverside.Interface.ItemClickListener;
import com.example.serverside.R;

public class FoodVIewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener{

    public ImageView imageFood;
    public TextView txtFood;
    public ItemClickListener itemClickListener;

    public FoodVIewHolder(@NonNull View itemView) {
        super(itemView);
        imageFood = itemView.findViewById(R.id.imageFoodList);
        txtFood = itemView.findViewById(R.id.txtNameFoodList);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.OnClick(v, getAdapterPosition(), false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Tùy chọn");
        menu.add(0,0,getAdapterPosition(), "Cập nhật");
        menu.add(0,1,getAdapterPosition(), "Xóa");
        menu.add(0,2,getAdapterPosition(),"Cập nhật giá");
    }
}

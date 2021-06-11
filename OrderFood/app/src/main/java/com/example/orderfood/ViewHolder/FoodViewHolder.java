package com.example.orderfood.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.Interface.ItemClickListener;
import com.example.orderfood.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView imageFood,imageFavorite, imageShare, imageCart;
    public TextView txtFood, txtPrice;
    private ItemClickListener itemClickListener;
    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);
        imageFood = itemView.findViewById(R.id.imageFoodList);
        txtFood = itemView.findViewById(R.id.txtNameFoodList);
        imageFavorite = itemView.findViewById(R.id.imageFavorite);
        imageShare = itemView.findViewById(R.id.imageShare);
        txtPrice = itemView.findViewById(R.id.txtPriceFoodList);
        imageCart = itemView.findViewById(R.id.imageCartFoodList);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.OnClick(v, getAdapterPosition(), false);
    }
}

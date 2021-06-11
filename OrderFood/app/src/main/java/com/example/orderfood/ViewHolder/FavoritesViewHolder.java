package com.example.orderfood.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.Interface.ItemClickListener;
import com.example.orderfood.R;

public class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView imageFood , imageCart;
    public TextView txtFood, txtPrice;
    private ItemClickListener itemClickListener;

    public RelativeLayout viewBackground;
    public LinearLayout viewForeground;

    public FavoritesViewHolder(@NonNull View itemView) {
        super(itemView);
        imageFood = itemView.findViewById(R.id.imageFoodFavoritesList);
        txtFood = itemView.findViewById(R.id.txtNameFoodFavoritesList);
        txtPrice = itemView.findViewById(R.id.txtPriceFoodFavoritesList);
        imageCart = itemView.findViewById(R.id.imageCartFoodFavoritesList);

        viewBackground = itemView.findViewById(R.id.viewBackground);
        viewForeground = itemView.findViewById(R.id.viewForeground);

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
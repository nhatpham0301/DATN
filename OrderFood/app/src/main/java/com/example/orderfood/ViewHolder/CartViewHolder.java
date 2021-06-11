package com.example.orderfood.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.orderfood.Common.Common;
import com.example.orderfood.Interface.ItemClickListener;
import com.example.orderfood.R;

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnCreateContextMenuListener{

    public TextView txtPrice, txtCartName;
    public ElegantNumberButton btnQuantity;
    public ImageView cartImage;

    public RelativeLayout viewBackground;
    public LinearLayout viewForeground;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);

        txtPrice = itemView.findViewById(R.id.txtCartItemPrice);
        txtCartName = itemView.findViewById(R.id.txtCartItemName);
        btnQuantity = itemView.findViewById(R.id.numberButtonFoodQuantityCart);
        cartImage = itemView.findViewById(R.id.cartImage);

        viewBackground = itemView.findViewById(R.id.viewBackground);
        viewForeground = itemView.findViewById(R.id.viewForeground);

        itemView.setOnCreateContextMenuListener(this);

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select action");
        menu.add(0,0,getAdapterPosition(), Common.DELETE);
    }
}

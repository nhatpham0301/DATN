package com.example.serverside.ViewHolder;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.serverside.Interface.ItemClickListener;

import com.example.serverside.R;

public class ShipperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public Button btnEditShipper, btnRemoveShipper;
    public TextView txtPhoneShipper, txtNameShipper;
    public ItemClickListener itemClickListener;

    public ShipperViewHolder(@NonNull View itemView) {
        super(itemView);

        btnEditShipper = itemView.findViewById(R.id.btnEditShipper);
        btnRemoveShipper = itemView.findViewById(R.id.btnRemoveShipper);
        txtNameShipper = itemView.findViewById(R.id.txtNameShipper);
        txtPhoneShipper = itemView.findViewById(R.id.txtPhoneShipper);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.OnClick(v, getAdapterPosition(), false);
    }
}

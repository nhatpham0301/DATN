package com.android.devhp.shipperapp.ViewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.devhp.shipperapp.R;

public class OrderViewHolder extends RecyclerView.ViewHolder{

    public TextView txtId, txtStatus, txtPhone, txtAddress, txtDate;
    public Button btnShipping, btnEdit;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        txtId = itemView.findViewById(R.id.txtOrderId);
        txtPhone = itemView.findViewById(R.id.txtOrderPhone);
        txtStatus = itemView.findViewById(R.id.txtOrderStatus);
        txtAddress = itemView.findViewById(R.id.txtOrderAddress);
        txtDate = itemView.findViewById(R.id.txtDate);
        btnShipping = itemView.findViewById(R.id.btnShipping);
        btnEdit = itemView.findViewById(R.id.btnEdit);
    }
}

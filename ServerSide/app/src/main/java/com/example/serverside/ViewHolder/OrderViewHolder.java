package com.example.serverside.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serverside.R;

public class OrderViewHolder extends RecyclerView.ViewHolder{

    public TextView txtId, txtStatus, txtPhone, txtAddress, txtDate;
    public Button btnEdit, btnDetail, btnDirection, btnCall;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        txtId = itemView.findViewById(R.id.txtOrderId);
        txtPhone = itemView.findViewById(R.id.txtOrderPhone);
        txtStatus = itemView.findViewById(R.id.txtOrderStatus);
        txtAddress = itemView.findViewById(R.id.txtOrderAddress);
        txtDate = itemView.findViewById(R.id.txtDate);
        btnEdit = itemView.findViewById(R.id.btnEditOrder);
        btnDetail = itemView.findViewById(R.id.btnDetailOrder);
        btnDirection = itemView.findViewById(R.id.btnDirectionOrder);
        btnCall = itemView.findViewById(R.id.btnCall);
    }
}

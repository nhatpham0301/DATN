package com.example.orderfood.ViewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.Interface.ItemClickListener;
import com.example.orderfood.R;

public class OrderShippingViewHolder extends RecyclerView.ViewHolder {
    public TextView txtId, txtStatus, txtPhone, txtAddress, txtDate;
    public Button btnDetail, btnDirection;

    public OrderShippingViewHolder(@NonNull View itemView) {
        super(itemView);
        txtId = itemView.findViewById(R.id.txtOrderId);
        txtPhone = itemView.findViewById(R.id.txtOrderPhone);
        txtStatus = itemView.findViewById(R.id.txtOrderStatus);
        txtAddress = itemView.findViewById(R.id.txtOrderAddress);
        txtDate = itemView.findViewById(R.id.txtOrderDate);
        btnDetail = itemView.findViewById(R.id.btnDetailOrder);
        btnDirection = itemView.findViewById(R.id.btnDirectionOrder);
    }
}

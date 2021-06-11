package com.android.devhp.shipperapp.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.devhp.shipperapp.R;

public class OrderProcessingViewHolder extends RecyclerView.ViewHolder {

    public TextView txtId, txtStatus, txtPhone, txtAddress, txtDate;

    public OrderProcessingViewHolder(@NonNull View itemView) {
        super(itemView);
        txtId = itemView.findViewById(R.id.txtOrderProcessingId);
        txtPhone = itemView.findViewById(R.id.txtOrderProcessingPhone);
        txtStatus = itemView.findViewById(R.id.txtOrderProcessingStatus);
        txtAddress = itemView.findViewById(R.id.txtOrderProcessingAddress);
        txtDate = itemView.findViewById(R.id.txtOrderProcessingDate);
    }
}

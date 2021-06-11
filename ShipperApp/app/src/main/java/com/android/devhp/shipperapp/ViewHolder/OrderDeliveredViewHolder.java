package com.android.devhp.shipperapp.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.devhp.shipperapp.R;

public class OrderDeliveredViewHolder extends RecyclerView.ViewHolder {

    public TextView txtId, txtStatus, txtPhone, txtAddress, txtDate;

    public OrderDeliveredViewHolder(@NonNull View itemView) {
        super(itemView);
        txtId = itemView.findViewById(R.id.txtOrderDeliveredId);
        txtPhone = itemView.findViewById(R.id.txtOrderDeliveredPhone);
        txtStatus = itemView.findViewById(R.id.txtOrderDeliveredStatus);
        txtAddress = itemView.findViewById(R.id.txtOrderDeliveredAddress);
        txtDate = itemView.findViewById(R.id.txtOrderDeliveredDate);
    }
}

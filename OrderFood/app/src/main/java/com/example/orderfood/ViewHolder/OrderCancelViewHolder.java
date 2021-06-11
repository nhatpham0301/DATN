package com.example.orderfood.ViewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.R;

public class OrderCancelViewHolder extends RecyclerView.ViewHolder {
    public TextView txtId, txtStatus, txtPhone, txtAddress, txtDate;
    public Button btnDetail;


    public OrderCancelViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtOrderIdCancel);
            txtPhone = itemView.findViewById(R.id.txtOrderPhoneCancel);
            txtStatus = itemView.findViewById(R.id.txtOrderStatusCancel);
            txtAddress = itemView.findViewById(R.id.txtOrderAddressCancel);
            txtDate = itemView.findViewById(R.id.txtOrderDateCancel);
            btnDetail = itemView.findViewById(R.id.btnDetailOrderCancel);
    }
}

package com.example.orderfood.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.Activity.OrderDetail;
import com.example.orderfood.Activity.TrackingOrder;
import com.example.orderfood.Common.Common;
import com.example.orderfood.Model.Request;
import com.example.orderfood.R;
import com.example.orderfood.ViewHolder.OrderShippingViewHolder;

import java.util.List;

public class OrderShippingAdapter extends RecyclerView.Adapter<OrderShippingViewHolder>{

    List<Request> listOrder;
    Context mContext;

    public OrderShippingAdapter(List<Request> listOrder, Context mContext) {
        this.listOrder = listOrder;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public OrderShippingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.row_order_shipping, parent, false);
        return new OrderShippingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderShippingViewHolder viewHolder, final int position) {

        final String orderId = listOrder.get(position).getId();
        viewHolder.txtId.setText(orderId);
        viewHolder.txtDate.setText(Common.getDate(Long.parseLong(orderId)));
        viewHolder.txtAddress.setText(listOrder.get(position).getAddress());
        viewHolder.txtPhone.setText(listOrder.get(position).getPhone());
        viewHolder.txtStatus.setText(Common.convertCodeToStatus(listOrder.get(position).getStatus()));
        viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent orderDetail = new Intent(mContext, OrderDetail.class);
                Common.currentRequest = listOrder.get(position);
                orderDetail.putExtra(Common.ORDER_ID, orderId);
                orderDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(orderDetail);
            }
        });
        viewHolder.btnDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String orderId = listOrder.get(position).getId();
                Intent orderDirection = new Intent(mContext, TrackingOrder.class);
                orderDirection.putExtra(Common.ORDER_ID, orderId);
                orderDirection.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(orderDirection);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOrder.size();
    }
}

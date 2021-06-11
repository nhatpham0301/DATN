package com.example.orderfood.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.Activity.OrderDetail;
import com.example.orderfood.Common.Common;
import com.example.orderfood.Model.DataMessage;
import com.example.orderfood.Model.MyResponse;
import com.example.orderfood.Model.Request;
import com.example.orderfood.Model.Token;
import com.example.orderfood.R;
import com.example.orderfood.Remote.APIService;
import com.example.orderfood.ViewHolder.OrderCancelViewHolder;
import com.example.orderfood.ViewHolder.OrderViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderCancelAdapter extends RecyclerView.Adapter<OrderCancelViewHolder>{

    List<Request> listOrder;
    Context mContext;

    public OrderCancelAdapter(List<Request> listOrder, Context mContext) {
        this.listOrder = listOrder;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public OrderCancelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.row_order_cancle, parent, false);
        return new OrderCancelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderCancelViewHolder viewHolder, final int position) {

        final String orderId = listOrder.get(position).getId();
        viewHolder.txtDate.setText(Common.getDate(Long.parseLong(orderId)));
        viewHolder.txtId.setText(orderId);
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
    }

    @Override
    public int getItemCount() {
        return listOrder.size();
    }
}

package com.example.serverside.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serverside.Model.Order;
import com.example.serverside.R;

import java.util.List;

public class OrderDetailViewHolder extends RecyclerView.Adapter<MyViewHolder> {

    List<Order> listOrder;

    public OrderDetailViewHolder(List<Order> listOrder) {
        this.listOrder = listOrder;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_order_detail_food_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Order order = listOrder.get(position);
        holder.txtProductName.setText(String.format("Tên: %s",order.getProductName()));
        holder.txtProductQuantity.setText(String.format("Số lượng: %s",order.getQuantity()));
        holder.txtProductPrice.setText(String.format("Giá: %s",order.getPrice()));
        holder.txtProductDiscount.setText(String.format("Khuyến mãi: %s",order.getDiscount()));
    }

    @Override
    public int getItemCount() {
        return listOrder.size();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder{

    public TextView txtProductName, txtProductQuantity, txtProductPrice, txtProductDiscount;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        txtProductName = itemView.findViewById(R.id.txtProductName);
        txtProductQuantity = itemView.findViewById(R.id.txtProductQuantity);
        txtProductDiscount = itemView.findViewById(R.id.txtProductDiscount);
        txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
    }
}

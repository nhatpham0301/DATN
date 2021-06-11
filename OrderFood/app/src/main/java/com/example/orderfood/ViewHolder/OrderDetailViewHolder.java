package com.example.orderfood.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.Activity.FoodDetail;
import com.example.orderfood.Activity.Home;
import com.example.orderfood.Common.Common;
import com.example.orderfood.Interface.ItemClickListener;
import com.example.orderfood.Model.Foods;
import com.example.orderfood.Model.Order;
import com.example.orderfood.R;

import java.text.DecimalFormat;
import java.util.List;

public class OrderDetailViewHolder extends RecyclerView.Adapter<MyViewHolder> {

    List<Order> listOrder;
    Context mContext;
    DecimalFormat formatter;

    public OrderDetailViewHolder(Context mContext, List<Order> listOrder) {
        this.listOrder = listOrder;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_order_detail_food_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final Order order = listOrder.get(position);
        formatter = new DecimalFormat("###,###,###");
        holder.txtProductName.setText(String.format("Tên: %s",order.getProductName()));
        holder.txtProductQuantity.setText(String.format("Số lượng: %s",order.getQuantity()));
        holder.txtProductPrice.setText(formatter.format(Integer.parseInt(order.getPrice())) + " VND");
        holder.txtProductDiscount.setText(String.format("Khuyến mãi: %s",order.getDiscount()));

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void OnClick(View view, int position, boolean isLongClick) {

                Intent intent = new Intent(view.getContext(), FoodDetail.class);
                intent.putExtra(Foods.FOOD_ID, listOrder.get(position).getProductId());
                intent.putExtra(Foods.FOOD_NAME, listOrder.get(position).getProductName());
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOrder.size();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView txtProductName, txtProductQuantity, txtProductPrice, txtProductDiscount, txtDate;

    private ItemClickListener itemClickListener;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        txtProductName = itemView.findViewById(R.id.txtProductName);
        txtProductQuantity = itemView.findViewById(R.id.txtProductQuantity);
        txtProductDiscount = itemView.findViewById(R.id.txtProductDiscount);
        txtProductPrice = itemView.findViewById(R.id.txtProductPrice);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.OnClick(v, getAdapterPosition(), false);
    }
}

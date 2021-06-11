package com.example.orderfood.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.orderfood.Activity.Cart;
import com.example.orderfood.Common.Common;
import com.example.orderfood.Database.Database;
import com.example.orderfood.Interface.ItemClickListener;
import com.example.orderfood.Model.Order;
import com.example.orderfood.R;
import com.example.orderfood.ViewHolder.CartViewHolder;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {

    private List<Order> listOrder = new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listOrder, Cart cart) {
        this.listOrder = listOrder;
        this.cart = cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(cart).inflate(R.layout.row_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, final int position) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        int itemPrice = Integer.parseInt(listOrder.get(position).getPrice());
        int itemQuantity = Integer.parseInt(listOrder.get(position).getQuantity());
        int price = itemPrice * itemQuantity;
        holder.txtPrice.setText(formatter.format(price) + " VND");
        holder.txtCartName.setText(listOrder.get(position).getProductName());
        Picasso.get().load(listOrder.get(position).getImage())
                .resize(70, 70)
                .centerCrop()
                .into(holder.cartImage);

        holder.btnQuantity.setNumber(listOrder.get(position).getQuantity());
        holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order1 = listOrder.get(position);
                order1.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order1);

                // Update total
                int total = 0;
                List<Order> orders = new Database(cart).getCarts(Common.currentUser.getPhone());
                for (Order item : orders) {
                    int itemPrice = Integer.parseInt(item.getPrice());
                    int itemQuantity = Integer.parseInt(item.getQuantity());
                    total += itemPrice * itemQuantity;
                }

                DecimalFormat formatter = new DecimalFormat("###,###,###");
                cart.txtTotalPrice.setText(formatter.format(total) + " VND");

            }
        });

    }

    @Override
    public int getItemCount() {
        return listOrder.size();
    }

    public Order getItem(int position){

        return listOrder.get(position);
    }

    public void removeItem(int position){

        listOrder.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Order item, int position){

        listOrder.add(position, item);
        notifyItemRemoved(position);
    }
}

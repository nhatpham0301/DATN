package com.example.orderfood.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.orderfood.Activity.FoodDetail;
import com.example.orderfood.Activity.FoodList;
import com.example.orderfood.Common.Common;
import com.example.orderfood.Database.Database;
import com.example.orderfood.Interface.ItemClickListener;
import com.example.orderfood.Model.Favorites;
import com.example.orderfood.Model.Foods;
import com.example.orderfood.Model.Order;
import com.example.orderfood.R;
import com.example.orderfood.ViewHolder.FavoritesViewHolder;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesViewHolder> {

    Context mContext;
    List<Favorites> favoritesList;
    DecimalFormat formatter;

    public FavoritesAdapter(Context mContext, List<Favorites> favoritesList) {
        this.mContext = mContext;
        this.favoritesList = favoritesList;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.row_favorites_foood, parent, false);
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder viewHolder, final int position) {

        formatter = new DecimalFormat("###,###,###");
        viewHolder.txtFood.setText(favoritesList.get(position).getFoodName());
        viewHolder.txtPrice.setText(formatter.format(Integer.parseInt(favoritesList.get(position).getFoodPrice()))+ " VND");
        Glide.with(mContext)
                .load(favoritesList.get(position).getFoodImage())
                .placeholder(R.drawable.my_bg)
                .into(viewHolder.imageFood);
        viewHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void OnClick(View view, int position, boolean isLongClick) {
                Intent intent = new Intent(mContext, FoodDetail.class);
                intent.putExtra(Foods.FOOD_ID, favoritesList.get(position).getFoodId());
                intent.putExtra(Foods.FOOD_NAME, favoritesList.get(position).getFoodName());
                mContext.startActivity(intent);
            }
        });

        viewHolder.imageCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isExists = new Database(mContext).checkFoodExists(
                        favoritesList.get(position).getFoodId(), Common.currentUser.getPhone()
                );

                if (!isExists) {
                    new Database(mContext).addToCart(new Order(
                            Common.currentUser.getPhone(),
                            favoritesList.get(position).getFoodId(),
                            favoritesList.get(position).getFoodName(),
                            "1",
                            favoritesList.get(position).getFoodPrice(),
                            favoritesList.get(position).getFoodDiscount(),
                            favoritesList.get(position).getFoodImage()
                    ));
                }else {
                    new Database(mContext).increaseCart(
                            Common.currentUser.getPhone(), favoritesList.get(position).getFoodId()
                    );
                }
                Toast.makeText(mContext, "Thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {

        return favoritesList.size();
    }

    public Favorites getItem(int position){

        return favoritesList.get(position);
    }

    public void removeItem(int position){

        favoritesList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Favorites item, int position){

        favoritesList.add(position, item);
        notifyItemRemoved(position);
    }
}

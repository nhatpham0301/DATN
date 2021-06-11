package com.example.serverside.ViewHolder;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serverside.R;

public class UpdateManyPriceViewHolder extends RecyclerView.ViewHolder {

    public TextView idFood, nameFood,priceOld;
    public EditText priceNew;

    public UpdateManyPriceViewHolder(@NonNull View itemView) {
        super(itemView);
        idFood = itemView.findViewById(R.id.txtIdFoodUpdatePrice);
        nameFood = itemView.findViewById(R.id.txtNameFoodUpdatePrice);
        priceOld = itemView.findViewById(R.id.txtPriceOld);
        priceNew = itemView.findViewById(R.id.editTextPriceNew);
    }
}

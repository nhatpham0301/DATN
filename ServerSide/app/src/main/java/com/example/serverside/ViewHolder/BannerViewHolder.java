package com.example.serverside.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serverside.Common.Common;
import com.example.serverside.Interface.ItemClickListener;
import com.example.serverside.R;

public class BannerViewHolder extends RecyclerView.ViewHolder implements
        View.OnCreateContextMenuListener{

    public ImageView imageBanner;
    public TextView txtNameBanner;

    public BannerViewHolder(@NonNull View itemView) {
        super(itemView);
        imageBanner = itemView.findViewById(R.id.imageBanner);
        txtNameBanner = itemView.findViewById(R.id.txtNameBanner);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select the action");
        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(), Common.DELETE);
    }
}

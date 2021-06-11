package com.example.serverside.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.serverside.Common.Common;
import com.example.serverside.R;
import com.example.serverside.ViewHolder.OrderDetailViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

public class OrderDetail extends AppCompatActivity {

    TextView txtId, txtPhone, txtTotal, txtAddress, txtComment;
    String orderIdValue = "Order ID";
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    OrderDetailViewHolder adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        if(getIntent() != null)
            orderIdValue = getIntent().getStringExtra("OrderId");

        setupActionBar();

        addControl();
    }

    private void setupActionBar() {

        getSupportActionBar().setTitle(orderIdValue);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void addControl() {
        txtId = findViewById(R.id.txtOrderDetailId);
        txtPhone = findViewById(R.id.txtOrderDetailPhone);
        txtTotal = findViewById(R.id.txtOrderDetailTotal);
        txtAddress = findViewById(R.id.txtOrderDetailAddress);
        txtComment = findViewById(R.id.txtOrderDetailComment);
        recyclerView = findViewById(R.id.recycler_OrderDetailFoodList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Set Value
        txtId.setText(orderIdValue);
        txtPhone.setText(Common.currentRequest.getPhone());
        txtTotal.setText(Common.currentRequest.getTotal());
        txtAddress.setText(Common.currentRequest.getAddress());
        txtComment.setText(Common.currentRequest.getComment());

        adapter = new OrderDetailViewHolder(Common.currentRequest.getFoods());
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }
}

package com.android.devhp.shipperapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.devhp.shipperapp.Common.Common;
import com.android.devhp.shipperapp.Model.Request;
import com.android.devhp.shipperapp.R;
import com.android.devhp.shipperapp.ViewHolder.OrderProcessingViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class OrderProcessing extends AppCompatActivity {

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseRecyclerAdapter<Request, OrderProcessingViewHolder> orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_processing);

        getSupportActionBar().setTitle("Chờ xử lí");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addControl();
        loadMenu(Common.currentShipper.getPhone());
    }

    private void loadMenu(String phone) {
        final Query orderProcessing = reference.child(phone).orderByChild("status").equalTo("4");

        FirebaseRecyclerOptions<Request> listOrder = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(orderProcessing, Request.class)
                .build();
        orderAdapter = new FirebaseRecyclerAdapter<Request, OrderProcessingViewHolder>(listOrder) {
            @Override
            protected void onBindViewHolder(@NonNull OrderProcessingViewHolder orderViewHolder, final int i, @NonNull final Request request) {

                orderViewHolder.txtAddress.setText(request.getAddress());
                orderViewHolder.txtDate.setText(Common.getDate(Long.parseLong(orderAdapter.getRef(i).getKey())));
                orderViewHolder.txtId.setText(orderAdapter.getRef(i).getKey());
                orderViewHolder.txtPhone.setText(request.getPhone());
                orderViewHolder.txtStatus.setText(Common.convertCodeToStatus(request.getStatus()));

            }

            @NonNull
            @Override
            public OrderProcessingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.row_order_processing, parent, false);
                return new OrderProcessingViewHolder(view);
            }
        };
        orderAdapter.startListening();
        orderAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(orderAdapter);
    }

    private void addControl() {
        recyclerView = findViewById(R.id.recyclerOrderProcessing);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.ORDER_NEED_SHIP_TABLE);
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadMenu(Common.currentShipper.getPhone());
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (orderAdapter != null) {
            orderAdapter.stopListening();
        }
    }
}
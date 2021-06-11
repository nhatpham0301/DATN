package com.example.orderfood.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.Common.Common;
import com.example.orderfood.Interface.ItemClickListener;
import com.example.orderfood.Model.Request;
import com.example.orderfood.R;
import com.example.orderfood.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        addControl();
        loadOrder(Common.currentUser.getPhone());
    }

    private void addControl() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Requests");
        recyclerView = findViewById(R.id.recycler_orderStatus);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(getIntent() == null)
            loadOrder(Common.currentUser.getPhone());
        else{
            if(getIntent().getStringExtra("userPhone") == null)
                loadOrder(Common.currentUser.getPhone());
            else
                loadOrder(getIntent().getStringExtra("userPhone"));
        }
    }

    private void loadOrder(String phone) {
        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(reference.orderByChild("phone").equalTo(phone), Request.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(OrderViewHolder viewHolder, final int i, Request model) {
                viewHolder.txtId.setText(adapter.getRef(i).getKey());
                viewHolder.txtAddress.setText(model.getAddress());
                viewHolder.txtPhone.setText(model.getPhone());
                viewHolder.txtStatus.setText(Common.convertCodeToStatus(model.getStatus()));

//                viewHolder.setItemClickListener(new ItemClickListener() {
//                    @Override
//                    public void OnClick(View view, int position, boolean isLongClick) {
//                        Common.currentKey = adapter.getRef(i).getKey();
//                        startActivity(new Intent(getApplicationContext(), TrackingOrder.class));
//                    }
//                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.row_order, parent, false);
                return new OrderViewHolder(v);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }
}

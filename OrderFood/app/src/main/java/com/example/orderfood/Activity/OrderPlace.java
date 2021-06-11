package com.example.orderfood.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.orderfood.Common.Common;
import com.example.orderfood.Model.Request;
import com.example.orderfood.R;
import com.example.orderfood.Adapter.OrderAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderPlace extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    private OrderAdapter orderAdapter;
    private List<Request> listOrder;

    private FirebaseDatabase database;
    private DatabaseReference requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_place);

        setupActionBar();
        initFirebase();
        initView();
        loadOrders(Common.currentUser.getPhone());
    }

    private void loadOrders(String phone) {

        requests.orderByChild("phone").equalTo(phone)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listOrder = new ArrayList<>();
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Request request = snapshot.getValue(Request.class);

                        if (request.getStatus().equals("0"))
                            listOrder.add(request);
                    }
                }

                orderAdapter = new OrderAdapter(listOrder, getApplicationContext());
                orderAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(orderAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView() {

        recyclerView = findViewById(R.id.orderPlaceAct_recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void initFirebase() {

        database = FirebaseDatabase.getInstance();
        requests = database.getReference(Common.REQUEST_TABLE);
    }

    private void setupActionBar() {

        getSupportActionBar().setTitle("Đơn hàng mới");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }
}
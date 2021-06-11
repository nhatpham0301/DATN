package com.example.serverside.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.serverside.Common.Common;
import com.example.serverside.Model.DataMessage;
import com.example.serverside.Model.MyResponse;
import com.example.serverside.Model.Request;
import com.example.serverside.Model.Token;
import com.example.serverside.R;
import com.example.serverside.Remote.APIService;
import com.example.serverside.TrackingOrder;
import com.example.serverside.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderCancel extends AppCompatActivity {

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    private FirebaseDatabase database;
    private DatabaseReference requests;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_cancel);

        setupActionBar();
        initFirebase();
        addControl();
        loadOrders();
    }

    private void setupActionBar() {

        getSupportActionBar().setTitle("Đã hủy");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initFirebase() {

        database = FirebaseDatabase.getInstance();
        requests = database.getReference(Common.REQUEST_TABLE);
    }

    private void loadOrders() {

        Query orderCancel = requests.orderByChild(Common.STATUS).equalTo("3");

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(orderCancel,Request.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder orderViewHolder, final int i, @NonNull final Request request) {

                orderViewHolder.txtAddress.setText(request.getAddress());
                orderViewHolder.txtDate.setText(Common.getDate(Long.parseLong(adapter.getRef(i).getKey())));
                orderViewHolder.txtId.setText(adapter.getRef(i).getKey());
                orderViewHolder.txtPhone.setText(request.getPhone());
                orderViewHolder.txtStatus.setText(Common.convertCodeToStatus(request.getStatus()));


                orderViewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(OrderCancel.this, OrderDetail.class);
                        Common.currentRequest = request;
                        intent.putExtra("OrderId", adapter.getRef(i).getKey());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.row_order_cancel,parent,false);

                return new OrderViewHolder(view);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void addControl() {
        mService = Common.getFCMService();
        recyclerView = findViewById(R.id.orderCancelAct_recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void deleteOrder(String key) {
        requests.child(key).removeValue();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Deleted !!!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }
}
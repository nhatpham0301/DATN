package com.example.serverside.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

public class OrderPlace extends AppCompatActivity {

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    private FirebaseDatabase database;
    private DatabaseReference requests;

    MaterialSpinner statusSpinner, shipperSpinner;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_place);

        setupActionBar();
        initFirebase();
        addControl();
        loadOrders();
    }

    private void setupActionBar() {

        getSupportActionBar().setTitle("Đặt hàng");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initFirebase() {

        database = FirebaseDatabase.getInstance();
        requests = database.getReference(Common.REQUEST_TABLE);
    }

    private void loadOrders() {

        Query orderPlace = requests.orderByChild(Common.STATUS).equalTo("0");

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(orderPlace,Request.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder orderViewHolder, final int i, @NonNull final Request request) {

                orderViewHolder.txtAddress.setText(request.getAddress());
                orderViewHolder.txtDate.setText(Common.getDate(Long.parseLong(adapter.getRef(i).getKey())));
                orderViewHolder.txtId.setText(adapter.getRef(i).getKey());
                orderViewHolder.txtPhone.setText(request.getPhone());
                orderViewHolder.txtStatus.setText(Common.convertCodeToStatus(request.getStatus()));

                orderViewHolder.btnCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:"+ request.getPhone()));
                        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                            return;
                        }
                        startActivity(intent);
                    }
                });
                orderViewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateDialog(adapter.getRef(i).getKey(), adapter.getItem(i));
                    }
                });

                orderViewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(OrderPlace.this, OrderDetail.class);
                        Common.currentRequest = request;
                        intent.putExtra("OrderId", adapter.getRef(i).getKey());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.row_order_place,parent,false);

                return new OrderViewHolder(view);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void addControl() {
        mService = Common.getFCMService();
        recyclerView = findViewById(R.id.orderPlaceAct_recyclerView);
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

    private void showUpdateDialog(String key, final Request item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Cập nhật đơn hàng");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.update_order, null);

        statusSpinner = view.findViewById(R.id.statusSpinner);
        shipperSpinner = view.findViewById(R.id.shipperSpinner);

        statusSpinner.setItems("Đặt hàng","Đang giao", "Đã giao", "Đã hủy");

        alertDialog.setView(view);

        final String localKey = key;

        // Load all shipper phone to spinner
        final List<String> shipperList = new ArrayList<>();

        // Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference shippers = database.getReference(Common.SHIPPER_TABLE);

        // Set shippers for spinner
        shippers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    shipperList.add(data.getKey());
                }

                shipperSpinner.setItems(shipperList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                item.setStatus(String.valueOf(statusSpinner.getSelectedIndex()));
                requests.child(localKey).setValue(item);
                adapter.notifyDataSetChanged();

                // Order Shipping
                if (item.getStatus().equals("1")){
                    // Copy item to table "OrdersNeedShip"
                    FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_SHIP_TABLE)
                            .child(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString())
                            .child(localKey)
                            .setValue(item);

                    sendOrderStatusToUser(localKey, item);
                    sendOrderStatusToShipper(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString(), item);
                }
                // Order is Cancel
                if (item.getStatus().equals("3")) {
                    sendOrderStatusToUser(localKey, item);

                    Toast.makeText(OrderPlace.this, "Updated!!!", Toast.LENGTH_SHORT).show();
                }

                // Order is finish
                if (item.getStatus().equals("2")) {
                    Common.setPhoneToFoodWhenOrderFinish(item);
                    Toast.makeText(OrderPlace.this, "Updated!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void sendOrderStatusToShipper(String phoneShipper, final Request item) {

        DatabaseReference tokens =  FirebaseDatabase.getInstance().getReference("Tokens");

        tokens.child(phoneShipper)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            Token token = dataSnapshot.getValue(Token.class);

                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("title", "OrderFood App");
                            dataSend.put("message", "Có đơn hàng mới");
                            dataSend.put("status", item.getStatus());

                            DataMessage dataMessage = new DataMessage(token.getToken(), dataSend);

                            mService.sendNotification(dataMessage)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if (response.body().success == 1) {
                                                Toast.makeText(OrderPlace.this, "Send to shipper", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(OrderPlace.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendOrderStatusToUser(final String localKey,final Request item) {
        DatabaseReference tokens =  FirebaseDatabase.getInstance().getReference("Tokens");
        tokens.orderByKey().equalTo(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            Token token = ds.getValue(Token.class);

                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("title", "OrderFood App");
                            dataSend.put("message","Đơn hàng " + localKey + " đã được cập nhật");
                            dataSend.put("status", item.getStatus());

                            DataMessage dataMessage = new DataMessage(token.getToken(), dataSend);

                            mService.sendNotification(dataMessage)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success == 1){
                                                Toast.makeText(OrderPlace.this, "Order was updated", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(OrderPlace.this, "Order was updated!!! But failed to send notification", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR",t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }
}
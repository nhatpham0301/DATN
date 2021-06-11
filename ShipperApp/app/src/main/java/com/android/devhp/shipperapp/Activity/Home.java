package com.android.devhp.shipperapp.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.devhp.shipperapp.Common.Common;
import com.android.devhp.shipperapp.Model.DataMessage;
import com.android.devhp.shipperapp.Model.MyResponse;
import com.android.devhp.shipperapp.Model.Order;
import com.android.devhp.shipperapp.Model.Request;
import com.android.devhp.shipperapp.Model.Token;
import com.android.devhp.shipperapp.R;
import com.android.devhp.shipperapp.Remote.APIService;
import com.android.devhp.shipperapp.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.jaredrummler.materialspinner.MaterialSpinner;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;
    View viewHeader;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    TextView txtFullName;

    FirebaseDatabase database;
    DatabaseReference orders, requests;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> orderAdapter;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location mLastLocation;

    MaterialSpinner statusOrder;

    APIService mService;
    LocationManager lm;
    boolean gps_enabled = false;
    boolean network_enabled = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Paper.init(this);

        // Check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CALL_PHONE
                }, Common.REQUEST_CODE);
        } else {
            buildLocationRequest();
            buildLocationCallback();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }

        addControl();

        toolbar.setTitle("Đơn hàng cần giao");
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        addEvent();
        updateToken(FirebaseInstanceId.getInstance().getToken());
        loadOrder(Common.currentShipper.getPhone());
    }

    private void loadOrder(String phone) {

        final Query orderOfShipper = orders.child(phone).orderByChild("status").equalTo("1");

        FirebaseRecyclerOptions<Request> listOrder = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(orderOfShipper, Request.class)
                .build();

        orderAdapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(listOrder) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder orderViewHolder, final int i, @NonNull final Request request) {

                orderViewHolder.txtAddress.setText(request.getAddress());
                orderViewHolder.txtDate.setText(Common.getDate(Long.parseLong(orderAdapter.getRef(i).getKey())));
                orderViewHolder.txtId.setText(orderAdapter.getRef(i).getKey());
                orderViewHolder.txtPhone.setText(request.getPhone());
                orderViewHolder.txtStatus.setText(Common.convertCodeToStatus(request.getStatus()));

                orderViewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateDialog(orderAdapter.getRef(i).getKey(), orderAdapter.getItem(i));
                    }
                });

                orderViewHolder.btnShipping.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!lm.isProviderEnabled( LocationManager.GPS_PROVIDER ) ){
                            Toast.makeText(Home.this, "Vui lòng bật định vị GPS", Toast.LENGTH_SHORT).show();
                        } else {
                            Common.createShippingOrder(orderAdapter.getRef(i).getKey(), Common.currentShipper.getPhone(), mLastLocation);
                            Common.currentRequest = request;
                            Common.currentKey = orderAdapter.getRef(i).getKey();
                            startActivity(new Intent(getApplicationContext(), TrackingOrder.class));
                        }
                    }
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.row_order, parent, false);
                return new OrderViewHolder(view);
            }
        };
        orderAdapter.startListening();
        orderAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(orderAdapter);
    }

    private void showUpdateDialog(String key, final Request item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Cập nhật đơn hàng");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.update_order, null);

        statusOrder = view.findViewById(R.id.statusSpinner);

        statusOrder.setItems("Đang chờ xử lí", "Đã giao");

        alertDialog.setView(view);

        final String localKey = key;

        alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int status = statusOrder.getSelectedIndex();
                // processing
                if(status == 0)
                    item.setStatus("4");
                // finish
                if(status == 1)
                    item.setStatus("2");


                requests.child(localKey).setValue(item);
                orders.child(Common.currentShipper.getPhone()).child(localKey).setValue(item);
                orderAdapter.notifyDataSetChanged();

                // processing
                if(item.getStatus().equals("4")){
                    sendOrderStatusToServer(localKey, item);
                    sendOrderStatusToUser(localKey, item);
                }
                // finish
                if(item.getStatus().equals("2")){
                    setPhoneToFoodWhenOrderFinish(item);
                    sendOrderStatusToServer(localKey, item);
                    sendOrderStatusToUser(localKey, item);
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

    private void setPhoneToFoodWhenOrderFinish(Request request){

        DatabaseReference ratingWithPhone = FirebaseDatabase.getInstance().getReference("OrderFoodWithPhone");
        for (Order order : request.getFoods()){
            ratingWithPhone.child(order.getProductId())
                    .child(request.getPhone()).push().setValue("1");
        }
    }

    private void sendOrderStatusToUser(final String localKey, final Request item) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
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
                                                Toast.makeText(getApplicationContext(), "Order was updated", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Order was updated!!! But failed to send notification", Toast.LENGTH_SHORT).show();
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

    private void sendOrderStatusToServer(final String localKey, final Request item) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        tokens.orderByChild("serverToken").equalTo(true)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            Token token = ds.getValue(Token.class);

                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("title", " ServerFood App");
                            dataSend.put("message"," YourOrder" + localKey + " was update");
                            dataSend.put("status", item.getStatus());

                            DataMessage dataMessage = new DataMessage(token.getToken(), dataSend);

                            mService.sendNotification(dataMessage)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success == 1){
                                                Toast.makeText(getApplicationContext(), "Order was updated", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Order was updated!!! But failed to send notification", Toast.LENGTH_SHORT).show();
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

    private void updateToken(String token) {

        DatabaseReference tokens = database.getReference("Tokens");
        Token data = new Token(token, false);
        tokens.child(Common.currentShipper.getPhone()).setValue(data);
    }

    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
    }

    private void buildLocationCallback() {

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                mLastLocation = locationResult.getLastLocation();

            }
        };
    }

    private void addEvent() {
        viewHeader = navigationView.getHeaderView(0);
        txtFullName = viewHeader.findViewById(R.id.txtFullName);
        String name = Common.currentShipper.getName();
        txtFullName.setText(name);
    }

    private void addControl() {
        mService = Common.getFCMService();
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        recyclerView = findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager =new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();
        orders = database.getReference(Common.ORDER_NEED_SHIP_TABLE);
        requests = database.getReference("Requests");
        lm = (LocationManager) getApplicationContext().getSystemService(getApplicationContext().LOCATION_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_order:
                startActivity(new Intent(getApplicationContext(), Home.class));
                Toast.makeText(this, "Order", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_order_delivered:
                startActivity(new Intent(getApplicationContext(), OrderDelivered.class));
                Toast.makeText(this, "Order Delivered", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_order_processing:
                startActivity(new Intent(getApplicationContext(), OrderProcessing.class));
                Toast.makeText(this, "Order Processing", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_sign_out:
                Paper.book().destroy();
                Intent signOut = new Intent(getApplicationContext(), MainActivity.class);
                signOut.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signOut);
                Toast.makeText(this, "Sign out", Toast.LENGTH_SHORT).show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadOrder(Common.currentShipper.getPhone());
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (orderAdapter != null) {
            orderAdapter.stopListening();
        }
        if(fusedLocationProviderClient != null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Common.REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        buildLocationRequest();
                        buildLocationCallback();

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }else {
                        Toast.makeText(this, "You should assign permission", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }
}
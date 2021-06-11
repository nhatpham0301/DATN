package com.example.orderfood.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.Common.Common;
import com.example.orderfood.Common.Config;
import com.example.orderfood.Database.Database;
import com.example.orderfood.Helper.RecyclerItemTouchHelper;
import com.example.orderfood.Interface.RecyclerItemTouchHelperListener;
import com.example.orderfood.Model.DataMessage;
import com.example.orderfood.Model.MyResponse;
import com.example.orderfood.Model.Order;
import com.example.orderfood.Model.Request;
import com.example.orderfood.Model.Token;
import com.example.orderfood.R;
import com.example.orderfood.Remote.APIService;
import com.example.orderfood.Remote.IGoogleService;
import com.example.orderfood.Adapter.CartAdapter;
import com.example.orderfood.ViewHolder.CartViewHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity implements RecyclerItemTouchHelperListener,
                        GoogleApiClient.ConnectionCallbacks,
                        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final int PAYPAL_REQUEST_CODE = 999;

    RelativeLayout rootLayout;

    Button btnPlaceOrder;
    public TextView txtTotalPrice;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    Place shippingAddress;
    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);

    List<Order> cart = new ArrayList<>();
    CartAdapter cartAdapter;
    APIService mService;
    String lat, lng;

    // PayPal payment
    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);

    String address_pay, comment;

    IGoogleService mGoogleMapService;

    // Location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 5000;
    private static final int DISPLACEMENT = 5000;

    private static final int LOCATION_REQUEST_CODE=9999;
    private static final int PLAY_SERVICES_REQUEST=9997;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_REQUEST_CODE);
        } else {
            if(checkPlayService())
            {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        mGoogleMapService = Common.getGoogleMapApi();

        // Setup actionBar
        getSupportActionBar().setTitle("Giỏ hàng");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Init payPal;
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        addControl();
        loadListCart();
        addEvent();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();

    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_REQUEST).show();
            } else {
                Toast.makeText(this, "Thiết bị này không được hỗ trợ", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void loadListCart() {
        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        cartAdapter = new CartAdapter(cart, this);
        cartAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(cartAdapter);
        int total = 0;
        for (Order order : cart) {
            int itemPrice = Integer.parseInt(order.getPrice());
            int itemQuantity = Integer.parseInt(order.getQuantity());
            total += itemPrice * itemQuantity;
        }

            DecimalFormat formatter = new DecimalFormat("###,###,###");
            txtTotalPrice.setText(formatter.format(total) + " VND");
    }

    private void addEvent() {
        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                kiemTraBomHang(Common.currentUser.getPhone());
//            if(cart.size() > 0)
//                showAlertDialog();
//            else
//                Toast.makeText(Cart.this, "Giỏ hàng rỗng !!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Đặt hàng");
        alertDialog.setMessage("Nhập địa chỉ của bạn ");

        LayoutInflater inflater = this.getLayoutInflater();
        View view_order = inflater.inflate(R.layout.send_order,null);

        Places.initialize(this, "AIzaSyBUSxjiuk7vKn_Ou27vDEN329brEu8dEcU");
        placesClient = Places.createClient(this);

        final AutocompleteSupportFragment edtAddress = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        edtAddress.getView().findViewById(R.id.places_autocomplete_search_button).setVisibility(View.GONE);
        ((EditText)edtAddress.getView().findViewById(R.id.places_autocomplete_search_input)).setHint("Địa chỉ");
        ((EditText)edtAddress.getView().findViewById(R.id.places_autocomplete_search_input)).setTextSize(18);

        edtAddress.setPlaceFields(placeFields);
        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                shippingAddress = place;
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e("ERROR",status.getStatusMessage());
            }
        });
        final MaterialEditText edtComment = view_order.findViewById(R.id.editTextCommentOrder);

        // Radio
        final RadioButton rdiShipToAddress = view_order.findViewById(R.id.rdiShipToAddress);
        final RadioButton rdiHomeAddress = view_order.findViewById(R.id.rdiHomeAddress);

        final RadioButton rdiCOD = view_order.findViewById(R.id.rdiCOD);
        final RadioButton rdiPaypal = view_order.findViewById(R.id.rdiPaypal);


        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    String url = "https://maps.google.com/maps/api/geocode/json?latlng=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&key=AIzaSyBUSxjiuk7vKn_Ou27vDEN329brEu8dEcU";
                    mGoogleMapService.getAddressName(url).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().toString());
                                    JSONArray resultArray = jsonObject.getJSONArray("results");
                                    JSONObject firstObject = resultArray.getJSONObject(0);
                                    address_pay = firstObject.getString("formatted_address");
                                    ((EditText)edtAddress.getView().findViewById(R.id.places_autocomplete_search_input)).setText(address_pay);
                                } catch (JSONException e){
                                    e.printStackTrace();
;                                }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });
                }
            }
        });

        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(Common.currentUser.getHomeAddress() != null ||
                        !TextUtils.isEmpty(Common.currentUser.getHomeAddress())){
                        address_pay = Common.currentUser.getHomeAddress();
                        ((EditText)edtAddress.getView().findViewById(R.id.places_autocomplete_search_input)).setText(address_pay);
                    } else {
                        Toast.makeText(Cart.this, "Vui lòng cập nhật địa chỉ nhà của bạn!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        alertDialog.setView(view_order);
        alertDialog.setIcon(R.drawable.ic_cart);
        alertDialog.setPositiveButton("Đặt hàng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Show PayPal to payment
                // First get Address and Comment from alert Dialog
                if(!rdiShipToAddress.isChecked() && !rdiHomeAddress.isChecked())
                    if(shippingAddress != null){
                        address_pay = shippingAddress.getAddress().toString();
                    } else {
                        Toast.makeText(Cart.this, "Vui lòng cung cấp địa chỉ nhận hàng!", Toast.LENGTH_SHORT).show();
                        getSupportFragmentManager().beginTransaction()
                                .remove(getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
                        return;
                    }

                if(TextUtils.isEmpty(address_pay) == true){
                    Toast.makeText(Cart.this, "you choose this location!", Toast.LENGTH_SHORT).show();
                        // fix crash fragment
                    getSupportFragmentManager().beginTransaction()
                            .remove(getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                }
                comment = edtComment.getText().toString();

                // Check payment
                if (!rdiCOD .isChecked() && !rdiPaypal.isChecked()){
                    Toast.makeText(Cart.this, "Vui lòng chọn phương thức thanh toán!", Toast.LENGTH_SHORT).show();
                }else {
                    if (rdiPaypal.isChecked()){
                        String formatAmount = txtTotalPrice.getText().toString()
                                .replace("VND", "")
                                .replace(",", "");

                        float amount = Float.parseFloat(formatAmount);
                        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(amount),
                                getString(R.string.USD),
                                getString(R.string.App_Order_Food),
                                PayPalPayment.PAYMENT_INTENT_SALE);
                        Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                        startActivityForResult(intent, PAYPAL_REQUEST_CODE);

                    }else if (rdiCOD.isChecked()){
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address_pay,
                                txtTotalPrice.getText().toString(),
                                "0",
                                comment,
                                "COD",
                                "Unpaid",
//                                String.format("%s,%s",shippingAddress.getLatLng().latitude,shippingAddress.getLatLng().longitude),
                                "",
                                cart
                        );

                        String order_number = String.valueOf(System.currentTimeMillis());
                        request.setId(order_number);
                        requests.child(order_number)
                                .setValue(request);
                        new Database(getBaseContext()).cleanToCart(Common.currentUser.getPhone());
                        loadListCart();
                        sendNotificationOrder(order_number);
                        finish();
                    }
                }
            }
        }).setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getSupportFragmentManager().beginTransaction()
                                .remove(getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
            }
        });

        alertDialog.show();
    }

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        final Query data = tokens.orderByChild("serverToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    Token serverToken = ds.getValue(Token.class);

                    Map<String, String> dataSend = new HashMap<>();
                    dataSend.put("title", " OrderFood App");
                    dataSend.put("message", " Có đơn hàng mới " + order_number);
                    dataSend.put("status", "0");

                    DataMessage dataMessage = new DataMessage(serverToken.getToken(), dataSend);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(Cart.this, "Cảm ơn bạn đã đặt hàng", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkPlayService())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);

                        // Create new request
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address_pay,
                                txtTotalPrice.getText().toString(),
                                "0",
                                comment,
                                "Paypal",
                                jsonObject.getJSONObject("response").getString("state"),
                                "",
                                cart
                        );

                        // Submit FireBase
                        // We will using System.CurrentMilli to key
                        String order_number = String.valueOf(System.currentTimeMillis());
                        requests.child(order_number)
                                .setValue(request);
                        new Database(getBaseContext()).cleanToCart(Common.currentUser.getPhone());
                        loadListCart();
                        sendNotificationOrder(order_number);
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == Activity.RESULT_CANCELED)
                Toast.makeText(this, "Payment cancel", Toast.LENGTH_SHORT).show();
            else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
                Toast.makeText(this, "Invalid Payment", Toast.LENGTH_SHORT).show();
        }
    }

    private void addControl() {
        mService = Common.getFCMService();
        database = FirebaseDatabase.getInstance();
        requests = database.getReference(Common.REQUEST_TABLE);
        rootLayout = findViewById(R.id.rootLayout);

        btnPlaceOrder = findViewById(R.id.btnCartPlaceOrder);
        txtTotalPrice = findViewById(R.id.txtCartTotal);

        recyclerView = findViewById(R.id.recycler_cart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Swipe to delete
        ItemTouchHelper.SimpleCallback simpleCallback = new RecyclerItemTouchHelper(0,
                ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals(Common.DELETE)){
            deleteCart(item.getOrder());
        }
        return true;
    }

    private void deleteCart(int order) {
        cart.remove(order);

        new Database(this).cleanToCart(Common.currentUser.getPhone());

        for (Order item: cart)
            new Database(this).addToCart(item);

        loadListCart();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if (viewHolder instanceof CartViewHolder){
            String name = ((CartAdapter)recyclerView.getAdapter())
                    .getItem(viewHolder.getAdapterPosition())
                    .getProductName();

            final Order deleteItem = ((CartAdapter)recyclerView.getAdapter())
                    .getItem(viewHolder.getAdapterPosition());

            final int deleteIndex = viewHolder.getAdapterPosition();

            cartAdapter.removeItem(deleteIndex);
            new Database(getApplicationContext()).removeFromCart(deleteItem.getProductId(), Common.currentUser.getPhone());

            // Update total
            int total = 0;
            List<Order> orders = new Database(getApplicationContext()).getCarts(Common.currentUser.getPhone());
            for (Order item : orders) {
                int itemPrice = Integer.parseInt(item.getPrice());
                int itemQuantity = Integer.parseInt(item.getQuantity());
                total += itemPrice * itemQuantity;
            }
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            txtTotalPrice.setText(formatter.format(total) + " VND");

            // Make snackbar
            Snackbar snackbar = Snackbar.make(rootLayout, name + "xóa khỏi giỏ hàng!", Snackbar.LENGTH_LONG);
            snackbar.setAction("Hoàn lại", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    cartAdapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getApplicationContext()).addToCart(deleteItem);

                    // Update total
                    int total = 0;
                    List<Order> orders = new Database(getApplicationContext()).getCarts(Common.currentUser.getPhone());
                    for (Order item : orders) {
                        int itemPrice = Integer.parseInt(item.getPrice());
                        int itemQuantity = Integer.parseInt(item.getQuantity());
                        total += itemPrice * itemQuantity;
                    }
                    
                    DecimalFormat formatter = new DecimalFormat("###,###,###");
                    txtTotalPrice.setText(formatter.format(total) + " VND");
                }
            });

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null){
            Log.d("LOCATION","YOUR LOCATION: " + mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
        } else {
            Log.d("LOCATION","Could not get location");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    private void kiemTraBomHang(String phone){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(Common.REQUEST_TABLE);
        reference.orderByChild("phone").equalTo(phone)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        int count = 0;

                        if (dataSnapshot.exists()){
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                Request request = snapshot.getValue(Request.class);

                                if (request.getStatus().equals("4"))
                                    count ++;
                            }

                            if (count > 0){
                                showThongBaoKhoaDatHang();
                            }else {
                                kiemTraGioHangRong();
                            }
                        }else {
                            kiemTraGioHangRong();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void showThongBaoKhoaDatHang(){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Thông báo");
        alertDialog.setMessage("Chức năng đặt hàng của bạn đã bị khóa \n Vui lòng liên hệ quản trị viên để biết thêm chi tiết");
        alertDialog.setIcon(R.drawable.ic_notifi);
        alertDialog.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.cancel();
            }
        });
        alertDialog.show();
    }

    private void kiemTraGioHangRong(){
        if(cart.size() > 0)
            showAlertDialog();
    }
}

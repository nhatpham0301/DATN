package com.android.devhp.shipperapp.Activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.devhp.shipperapp.Common.Common;
import com.android.devhp.shipperapp.Helper.DirectionJSONParer;
import com.android.devhp.shipperapp.Model.DataMessage;
import com.android.devhp.shipperapp.Model.MyResponse;
import com.android.devhp.shipperapp.Model.Request;
import com.android.devhp.shipperapp.Model.Token;
import com.android.devhp.shipperapp.R;
import com.android.devhp.shipperapp.Remote.APIService;
import com.android.devhp.shipperapp.Remote.IGeoCoordinates;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location mLastLocation;
    Button btnCall, btnShipped;

    Polyline polyline;
    Marker mCurrentMarker;
    IGeoCoordinates mService;
    APIService mServiceNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_orer);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mServiceNo = Common.getFCMService();
        btnCall = findViewById(R.id.btnCall);
        btnShipped = findViewById(R.id.btnShipped);

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+Common.currentRequest.getPhone()));
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                startActivity(intent);
            }
        });

        btnShipped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shippedOrder();
            }
        });

        mService = Common.getGeoCodeService();

        buildLocationRequest();
        buildLocationCallback();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void shippedOrder() {
        final Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put("status","2");
        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_NEED_SHIP_TABLE)
                .child(Common.currentShipper.getPhone())
                .child(Common.currentKey)
                .updateChildren(updateStatus)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseDatabase.getInstance()
                                .getReference("Requests")
                                .child(Common.currentKey)
                                .updateChildren(updateStatus)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                            FirebaseDatabase.getInstance()
                                                    .getReference(Common.SHIPPER_INFO_TABLE)
                                                    .child(Common.currentKey)
                                                    .removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            sendOrderStatusToServer();
                                                            sendOrderStatusToUser();
                                                            Toast.makeText(TrackingOrder.this, "Shipped!", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    });
                                    }
                                });
                    }
                });
    }

    private void sendOrderStatusToUser() {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        tokens.orderByKey().equalTo(Common.currentRequest.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            Token token = ds.getValue(Token.class);

                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("title", "OrderFood App");
                            dataSend.put("message","Đơn hàng " + Common.currentKey + " đã được cập nhật");
                            dataSend.put("status", Common.currentRequest.getStatus());

                            DataMessage dataMessage = new DataMessage(token.getToken(), dataSend);

                            mServiceNo.sendNotification(dataMessage)
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

    private void sendOrderStatusToServer() {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        tokens.orderByChild("serverToken").equalTo(true)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            Token token = ds.getValue(Token.class);

                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("title", " ServerFood App");
                            dataSend.put("message","Đơn hàng" + Common.currentKey + " đã được cập nhật");
                            dataSend.put("status", Common.currentRequest.getStatus());

                            DataMessage dataMessage = new DataMessage(token.getToken(), dataSend);

                            mServiceNo.sendNotification(dataMessage)
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLastLocation = location;
                LatLng yourLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mCurrentMarker = mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
            }
        });
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

                if(mCurrentMarker != null){
                    mCurrentMarker.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLatitude()));
                }

                Common.updateShippingInformation(Common.currentKey, mLastLocation);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));

                mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));

                drawRoute(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), Common.currentRequest);
            }
        };
    }

    private void drawRoute(final LatLng yourLocation, Request request) {
        if(polyline != null){
            polyline.remove();
        }
        if(request.getAddress() != null && !request.getAddress().isEmpty()){
            mService.getGeoCode(request.getAddress(),"AIzaSyBUSxjiuk7vKn_Ou27vDEN329brEu8dEcU").enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        String lat = ((JSONArray) jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lat").toString();

                        String lng = ((JSONArray) jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lng").toString();

                        LatLng orderLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.order);
                        bitmap = Common.scaleBitmap(bitmap, 70, 70);

                        MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                .title("Order of " + Common.currentRequest.getPhone())
                                .position(orderLocation);

                        mMap.addMarker(marker);

                        mService.getDirections(yourLocation.latitude + "," + yourLocation.longitude,
                                orderLocation.latitude + "," + orderLocation.longitude,"AIzaSyBUSxjiuk7vKn_Ou27vDEN329brEu8dEcU")
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        new ParserTask().execute(response.body().toString());
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {

                                    }
                                });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        } else {
            if(request.getLatLng() != null && !request.getLatLng().isEmpty()){
                String[] latLng = request.getLatLng().split(",");
                LatLng orderLocation = new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.order);
                bitmap = Common.scaleBitmap(bitmap, 70, 70);

                MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title("Order of " + Common.currentRequest.getPhone())
                        .position(orderLocation);

                mMap.addMarker(marker);

                mService.getDirections(mLastLocation.getLatitude()+","+mLastLocation.getLongitude(),
                        orderLocation.latitude+","+orderLocation.longitude,
                        "AIzaSyBUSxjiuk7vKn_Ou27vDEN329brEu8dEcU")
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                new ParserTask().execute(response.body().toString());
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {

                            }
                        });
            }
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        AlertDialog dialog = new SpotsDialog.Builder().setContext(TrackingOrder.this).build();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
            dialog.setMessage("Please waiting...");
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParer parer = new DirectionJSONParer();

                routes = parer.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            dialog.dismiss();

            ArrayList points = null;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < lists.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = lists.get(i);

                for (int j = 0; j < path.size(); j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat,lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }

//            mMap.addPolyline(lineOptions);
            if(polyline == null)
                polyline = mMap.addPolyline(lineOptions);
            else{
                polyline.remove();
                polyline = mMap.addPolyline(lineOptions);
            }
        }
    }

    @Override
    protected void onStop() {
        if(fusedLocationProviderClient != null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        super.onStop();
    }
}
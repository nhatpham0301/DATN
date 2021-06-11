package com.example.orderfood.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.example.orderfood.Common.Common;
import com.example.orderfood.Helper.DirectionJSONParer;
import com.example.orderfood.Model.Request;
import com.example.orderfood.Model.ShippingInformation;
import com.example.orderfood.R;
import com.example.orderfood.Remote.IGoogleService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback, ValueEventListener {

    private GoogleMap mMap;

    FirebaseDatabase database;
    DatabaseReference requests, shippingOrder;

    Request currentOrder;
    IGoogleService mService;

    Marker shipperMarker;
    Polyline polyline;

    String orderId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        if (getIntent() != null){
            orderId = getIntent().getStringExtra(Common.ORDER_ID);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        shippingOrder = database.getReference("ShippingOrder");
        shippingOrder.addValueEventListener(this);

        mService = Common.getGoogleMapApi();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        shippingOrder.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(orderId)){
                    trackingLocation();
                } else {
                    showDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Đơn hàng của bạn chưa được vận chuyển !!!");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        onBackPressed();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void trackingLocation() {
        requests.child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        currentOrder = dataSnapshot.getValue(Request.class);

                        if(currentOrder.getAddress() != null && !currentOrder.getAddress().isEmpty()){
                            mService.getLocationFromAddress(new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?address=")
                            .append(currentOrder.getAddress()).toString(), Common.KEY_API)
                                    .enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {

                                            try {
                                                JSONObject jsonObject = new JSONObject(response.body());
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

                                                LatLng location = new LatLng(Double.parseDouble(lat),
                                                        Double.parseDouble(lng));

                                                mMap.addMarker(new MarkerOptions().position(location)
                                                        .title("Order destination")
                                                        .icon(BitmapDescriptorFactory.defaultMarker()));

                                                // Set Shipper location
                                                shippingOrder.child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                        ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);

                                                        LatLng shipperLocation = new LatLng(shippingInformation.getLat(), shippingInformation.getLng());

                                                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.shipper);
                                                        bitmap = Common.scaleBitmap(bitmap, 125, 125);
                                                        if (shipperMarker == null){
                                                            shipperMarker = mMap.addMarker(
                                                                    new MarkerOptions()
                                                                    .position(shipperLocation)
                                                                    .title("Shipper #" + shippingInformation.getOrderId())
                                                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                                            );
                                                        }else {
                                                            shipperMarker.setPosition(shipperLocation);
                                                        }

                                                        // Update Camera
                                                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                .target(shipperLocation)
                                                                .zoom(16)
                                                                .bearing(0)
                                                                .tilt(45)
                                                                .build();
                                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                        // Draw routes
                                                        if (polyline != null){
                                                            polyline.remove();
                                                        }
                                                        mService.getDirection(shipperLocation.latitude + "," + shipperLocation.longitude,
                                                                currentOrder.getAddress(), Common.KEY_API)
                                                                .enqueue(new Callback<String>() {
                                                                    @Override
                                                                    public void onResponse(Call<String> call, Response<String> response) {

                                                                        if(polyline == null)
                                                                            new ParserTask().execute(response.body());
                                                                        else {
                                                                            polyline.remove();
                                                                            new ParserTask().execute(response.body());
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<String> call, Throwable t) {

                                                                    }
                                                                });
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

                                        }
                                    });

                        } else if(currentOrder.getLatLng() != null && !currentOrder.getLatLng().isEmpty()){

                            mService.getLocationFromAddress(new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?latlng=")
                                    .append(currentOrder.getLatLng()).toString(), Common.KEY_API)
                                    .enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {

                                            try {
                                                JSONObject jsonObject = new JSONObject(response.body());
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

                                                LatLng location = new LatLng(Double.parseDouble(lat),
                                                        Double.parseDouble(lng));

                                                mMap.addMarker(new MarkerOptions().position(location)
                                                        .title("Order destination")
                                                        .icon(BitmapDescriptorFactory.defaultMarker()));

                                                // Set Shipper location
                                                shippingOrder.child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                        ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);

                                                        LatLng shipperLocation = new LatLng(shippingInformation.getLat(), shippingInformation.getLng());
                                                        if (shipperMarker == null){
                                                            shipperMarker = mMap.addMarker(
                                                                    new MarkerOptions()
                                                                            .position(shipperLocation)
                                                                            .title("Shipper #" + shippingInformation.getOrderId())
                                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                                            );
                                                        }else {
                                                            shipperMarker.setPosition(shipperLocation);
                                                        }

                                                        // Update Camera
                                                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                .target(shipperLocation)
                                                                .zoom(16)
                                                                .bearing(0)
                                                                .tilt(45)
                                                                .build();
                                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                        // Draw routes
                                                        if (polyline != null){
                                                            polyline.remove();
                                                        }
                                                        mService.getDirection(shipperLocation.latitude + "," + shipperLocation.longitude,
                                                                currentOrder.getLatLng(), Common.KEY_API)
                                                                .enqueue(new Callback<String>() {
                                                                    @Override
                                                                    public void onResponse(Call<String> call, Response<String> response) {

                                                                        if(polyline == null)
                                                                            new ParserTask().execute(response.body());
                                                                        else {
                                                                            polyline.remove();
                                                                            new ParserTask().execute(response.body());
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<String> call, Throwable t) {

                                                                    }
                                                                });
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

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
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        shippingOrder.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(orderId)){
                    trackingLocation();
                } else {
                    showDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    protected void onStop() {

        shippingOrder.removeEventListener(this);
        super.onStop();
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

            if(polyline == null)
                polyline = mMap.addPolyline(lineOptions);
            else{
                polyline.remove();
                polyline = mMap.addPolyline(lineOptions);
            }
        }
    }
}
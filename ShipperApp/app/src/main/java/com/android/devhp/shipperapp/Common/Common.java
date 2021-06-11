package com.android.devhp.shipperapp.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.devhp.shipperapp.Model.Request;
import com.android.devhp.shipperapp.Model.Shipper;
import com.android.devhp.shipperapp.Model.ShippingInformation;
import com.android.devhp.shipperapp.Remote.APIService;
import com.android.devhp.shipperapp.Remote.FCMRetrofitClient;
import com.android.devhp.shipperapp.Remote.IGeoCoordinates;
import com.android.devhp.shipperapp.Remote.RetrofitClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Common {

    public final static String SHIPPER_TABLE = "Shippers";
    public final static String USER_KEY = "User";
    public final static String PASSWORD_KEY = "Password";
    public static final String ORDER_NEED_SHIP_TABLE = "OrdersNeedShip";
    public static final String SHIPPER_INFO_TABLE = "ShippingOrder";
    public static final int REQUEST_CODE = 1000;
    public static Request currentRequest;
    public static String currentKey;
    public static Shipper currentShipper;
    public static final String baseUrl = "https://maps.googleapis.com";
    private static final String fcmUrl = "https://fcm.googleapis.com/";

    public static String convertCodeToStatus(String code) {
        if(code.equals("0"))
            return "Đặt hàng";
        else if(code.equals("1"))
            return "Đang giao";
        else if(code.equals("2"))
            return "Đã giao";
        else if (code.equals("3"))
            return "Đã hủy";
        else return "Đang chờ xử lí";
    }

    public static String getDate(long time){

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(DateFormat.format("dd-MM-yyy HH:mm", calendar).toString());

        return date.toString();
    }

    public static IGeoCoordinates getGeoCodeService(){
        return RetrofitClient.getRetrofit(baseUrl).create(IGeoCoordinates.class);
    }

    public static APIService getFCMService(){
        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }

    public static void createShippingOrder(String key, String phone, Location mLastLocation) {
        ShippingInformation shippingInformation = new ShippingInformation();
        shippingInformation.setOrderId(key);
        shippingInformation.setShipperIphone(phone);
        shippingInformation.setLat(mLastLocation.getLatitude());
        shippingInformation.setLng(mLastLocation.getLongitude());

        // create new item on ShipperInformation table

        FirebaseDatabase.getInstance()
                .getReference(SHIPPER_INFO_TABLE)
                .child(key)
                .setValue(shippingInformation)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ERROR",e.getMessage());
                    }
                });

    }

    public static void updateShippingInformation(String currentKey, Location mLastLocation) {
        Map<String, Object> updateLocation = new HashMap<>();
        updateLocation.put("lat", mLastLocation.getLatitude());
        updateLocation.put("lng", mLastLocation.getLongitude());

        FirebaseDatabase.getInstance()
                .getReference(SHIPPER_INFO_TABLE)
                .child(currentKey)
                .updateChildren(updateLocation)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ERROR", e.getMessage());
                    }
                });
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap scaleBitmap2 = Bitmap.createBitmap(newWidth, newHeight,Bitmap.Config.ARGB_8888);

        float scaleX = newWidth /(float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0; float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas = new Canvas(scaleBitmap2);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaleBitmap2;
    }
}

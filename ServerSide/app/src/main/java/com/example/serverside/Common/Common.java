package com.example.serverside.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.format.DateFormat;

import com.example.serverside.Model.Order;
import com.example.serverside.Model.Request;
import com.example.serverside.Model.User;
import com.example.serverside.Remote.APIService;
import com.example.serverside.Remote.FCMRetrofitClient;
import com.example.serverside.Remote.IGeoCoordinates;
import com.example.serverside.Remote.RetrofitClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Common {
    public static final String SHIPPER_TABLE = "Shippers";
    public static final String ORDER_FOOD_WITH_PHONE_TABLE = "OrderFoodWithPhone";
    public static final String ORDER_SHIPPER_FINISH = "OrderShipperFinish";
    public static final String REQUEST_TABLE = "Requests";
    public static final String ORDER_NEED_SHIP_TABLE = "OrdersNeedShip";
    public static final String STATUS = "status";
    public static final int REQUEST_CODE = 1000;
    public static final String USER_KEY = "User";
    public static final String PASSWORD_KEY = "Password";

    public static User currentUser;
    public static Request currentRequest;

    public static String topicName = "News";

    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final String UPDATE_MANY_PRICES = "Update Many Prices";

    public static final String baseUrl = "https://maps.googleapis.com";

    public static final int PICK_IMAGE_REQUEST = 71;

    private static final String fcmUrl = "https://fcm.googleapis.com/";
    public static String PHONE_TEXT = "userPhone";

    public static APIService getFCMService(){
        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }

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

    public static IGeoCoordinates getGeoCodeService(){
        return RetrofitClient.getRetrofit(baseUrl).create(IGeoCoordinates.class);
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

    public static String getDate(long time){

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(DateFormat.format("dd-MM-yyy HH:mm", calendar).toString());

        return date.toString();
    }

    public static void setPhoneToFoodWhenOrderFinish(Request request){

        DatabaseReference ratingWithPhone = FirebaseDatabase.getInstance().getReference(Common.ORDER_FOOD_WITH_PHONE_TABLE);
        for (Order order : request.getFoods()){
            ratingWithPhone.child(order.getProductId())
                    .child(request.getPhone()).push().setValue("1");
        }
    }
}

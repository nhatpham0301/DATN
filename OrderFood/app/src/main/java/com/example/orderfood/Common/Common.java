package com.example.orderfood.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;

import com.example.orderfood.Model.Address;
import com.example.orderfood.Model.Request;
import com.example.orderfood.Model.User;
import com.example.orderfood.Remote.APIService;
import com.example.orderfood.Remote.IGoogleService;
import com.example.orderfood.Remote.RetrofitClient;
import com.example.orderfood.Remote.RetrofitGoogleAPI;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Common {

    public static final String SHIPPER_TABLE = "Shippers";
    public static final String RATING_TABLE = "Rating";
    public static final String STATUS = "status";
    public static final String ORDER_FOOD_WITH_PHONE_TABLE = "OrderFoodWithPhone";
    public static final String ORDER_SHIPPER_FINISH = "OrderShipperFinish";
    public static final String REQUEST_TABLE = "Requests";
    public static final String ORDER_NEED_SHIP_TABLE = "OrdersNeedShip";
    public static final String KEY_API = "AIzaSyBUSxjiuk7vKn_Ou27vDEN329brEu8dEcU";

    public static final String ORDER_ID = "Order Id";

    public static Request currentRequest;
    public static User currentUser;

    public static String currentKey;

    public static String topicName = "News";

    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static final String INTENT_FOOD_ID = "foodId";
    public static String PHONE_TEXT = "userPhone";

    public static APIService getFCMService(){
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapApi(){
        return RetrofitGoogleAPI.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
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

    public static final String DELETE  = "Delete";
    public static final String USER_KEY  = "User";
    public static final String PWD_KEY  = "Password";

    public static boolean isConnectToInternet(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager != null){
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();

            if(info != null){
                for(int i = 0; i< info.length; i++){
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static String getDate(long time){

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(DateFormat.format("dd-MM-yyy HH:mm", calendar).toString());

        return date.toString();
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

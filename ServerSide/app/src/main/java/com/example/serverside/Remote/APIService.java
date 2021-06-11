package com.example.serverside.Remote;

import com.example.serverside.Model.DataMessage;
import com.example.serverside.Model.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAcG-YX8k:APA91bFiJuAiQnIGBT8L5RgtYD-m8jyxTEYhN_37zCKbAdHydV5C619vb3uLR4QIAMVkFQmm1YOA8MeJ-mAMaEE06f8pP4pb4IW2h-q37LaZVZqtr5VSQw6WsEEyx80is5Xi4jMM9OAR"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);
}

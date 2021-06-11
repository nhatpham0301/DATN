package com.example.serverside.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.serverside.Common.Common;
import com.example.serverside.Model.DataMessage;
import com.example.serverside.Model.MyResponse;
import com.example.serverside.R;
import com.example.serverside.Remote.APIService;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendMessage extends AppCompatActivity {

    private MaterialEditText metTitle, metMessage;
    private Button btnSendMessage;

    APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        setupActionBar();
        initView();
        sendMessage();
    }

    private void setupActionBar() {

        getSupportActionBar().setTitle("Send Message");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void sendMessage() {

        apiService = Common.getFCMService();

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = metTitle.getText().toString();
                String message = metMessage.getText().toString();

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(message)) {
//                    Notification notification = new Notification(title, message);
//                    Sender toTopic = new Sender();
//                    toTopic.to = new StringBuilder("/topics/").append(Common.topicName).toString();
//                    toTopic.notification = notification;

                    Map<String, String> dataSend = new HashMap<>();
                    dataSend.put("title", title);
                    dataSend.put("message", message);

                    DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.topicName).toString(),
                            dataSend);
                    apiService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    if (response.isSuccessful())
                                        Toast.makeText(SendMessage.this, "Message sent", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                    Toast.makeText(SendMessage.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }else {
                    Toast.makeText(SendMessage.this, R.string.Please_fill_all_information, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initView() {

        metMessage = findViewById(R.id.sendMessageAct_metMessage);
        metTitle = findViewById(R.id.sendMessageAct_metTitle);
        btnSendMessage = findViewById(R.id.sendMessageAct_btnSendMessage);
    }

    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }
}
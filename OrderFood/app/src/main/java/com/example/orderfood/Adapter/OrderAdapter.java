package com.example.orderfood.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.Activity.OrderDetail;
import com.example.orderfood.Common.Common;
import com.example.orderfood.Model.DataMessage;
import com.example.orderfood.Model.MyResponse;
import com.example.orderfood.Model.Request;
import com.example.orderfood.Model.Token;
import com.example.orderfood.R;
import com.example.orderfood.Remote.APIService;
import com.example.orderfood.ViewHolder.OrderViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder>{

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Requests");
    List<Request> listOrder;
    Context mContext;
    APIService mService;

    public OrderAdapter(List<Request> listOrder, Context mContext) {
        this.listOrder = listOrder;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.row_order_place, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder viewHolder, final int position) {

        final String orderId = listOrder.get(position).getId();
        viewHolder.txtDate.setText(Common.getDate(Long.parseLong(orderId)));
        viewHolder.txtId.setText(orderId);
        viewHolder.txtAddress.setText(listOrder.get(position).getAddress());
        viewHolder.txtPhone.setText(listOrder.get(position).getPhone());
        viewHolder.txtStatus.setText(Common.convertCodeToStatus(listOrder.get(position).getStatus()));
        viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent orderDetail = new Intent(mContext, OrderDetail.class);
                Common.currentRequest = listOrder.get(position);
                orderDetail.putExtra(Common.ORDER_ID, orderId);
                orderDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(orderDetail);
            }
        });
        viewHolder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService = Common.getFCMService();
                Request request = listOrder.get(position);
                request.setStatus("3");
                reference.child(request.getId()).setValue(request);
                sendOrderStatusToServer(request.getId(), request);
                Toast.makeText(mContext, "Cancel", Toast.LENGTH_SHORT).show();
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
                            dataSend.put("title", "OrderFood App");
                            dataSend.put("message","Đơn hàng " + localKey + " được cập nhật");
                            dataSend.put("status", item.getStatus());

                            DataMessage dataMessage = new DataMessage(token.getToken(), dataSend);

                            mService.sendNotification(dataMessage)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success == 1){
                                                Toast.makeText(mContext, "Order was updated", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(mContext, "Order was updated!!! But failed to send notification", Toast.LENGTH_SHORT).show();
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
    public int getItemCount() {
        return listOrder.size();
    }
}

package com.example.serverside.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.serverside.Common.Common;
import com.example.serverside.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Orders extends AppCompatActivity {

    private Button btnPlace, btnShipping, btnFinish, btnCancel, btnProcessing;
    private FirebaseDatabase database;
    private DatabaseReference requests;
    private int countPlace, countShipping, countFinish, countCancel, countProcessing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        setupActionBar();
        initView();
        initFirebase();

        // Show order count in button
        countOrderPlace();
        countOrderShipping();
        countOrderFinish();
        countOrderCancel();
        countOrderProcessing();

        // event Click button
        showOrderPlace();
        showOrderShipping();
        showOrderCancel();
        showOrderFinish();
        showOrderProcessing();
    }

    private void showOrderProcessing() {

        btnProcessing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent orderProcessing = new Intent(getApplicationContext(), OrderProcessing.class);
                startActivity(orderProcessing);
            }
        });
    }

    private void showOrderFinish() {

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent orderFinish = new Intent(getApplicationContext(), OrderFinish.class);
                startActivity(orderFinish);
            }
        });
    }

    private void showOrderCancel() {

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent orderCancel = new Intent(getApplicationContext(), OrderCancel.class);
                startActivity(orderCancel);
            }
        });
    }

    private void showOrderShipping() {

        btnShipping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent orderShipping = new Intent(getApplicationContext(), OrderShipping.class);
                startActivity(orderShipping);
            }
        });
    }

    private void showOrderPlace() {

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent orderPlace = new Intent(getApplicationContext(), OrderPlace.class);
                startActivity(orderPlace);
            }
        });
    }

    private void countOrderPlace() {

        Query ordersPlace = requests.orderByChild(Common.STATUS).equalTo("0");
        ordersPlace.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    countPlace = (int) dataSnapshot.getChildrenCount();
                    btnPlace.setText(getString(R.string.Place) + " - " + Integer.toString(countPlace));
                }else {
                    btnPlace.setText(getString(R.string.Place) + " - 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void countOrderCancel() {

        Query orderCancel = requests.orderByChild(Common.STATUS).equalTo("3");
        orderCancel.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    countCancel = (int) dataSnapshot.getChildrenCount();
                    btnCancel.setText(getString(R.string.Cancel) + " - " + Integer.toString(countCancel));
                }else {
                    btnCancel.setText(getString(R.string.Cancel) + " - 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void countOrderFinish() {

        Query orderFinish = requests.orderByChild(Common.STATUS).equalTo("2");

        orderFinish.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    countFinish = (int) dataSnapshot.getChildrenCount();
                    btnFinish.setText(getString(R.string.Finish) + " - " + Integer.toString(countFinish));
                }else {
                    btnFinish.setText(getString(R.string.Finish) + " - 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void countOrderShipping() {

        Query orderShipping = requests.orderByChild(Common.STATUS).equalTo("1");
        orderShipping.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    countShipping = (int) dataSnapshot.getChildrenCount();
                    btnShipping.setText(getString(R.string.Shipping) + " - " + Integer.toString(countShipping));
                }else {
                    btnShipping.setText(getString(R.string.Shipping) + " - 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void countOrderProcessing() {

        Query orderProcessing = requests.orderByChild(Common.STATUS).equalTo("4");
        orderProcessing.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    countProcessing = (int) dataSnapshot.getChildrenCount();
                    btnProcessing.setText(getString(R.string.Processing) + " - " + Integer.toString(countProcessing));
                }else {
                    btnProcessing.setText(getString(R.string.Processing) + " - 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void initFirebase() {

        database = FirebaseDatabase.getInstance();
        requests = database.getReference(Common.REQUEST_TABLE);
    }

    private void initView() {

        btnPlace = findViewById(R.id.ordersAct_btnPlace);
        btnShipping = findViewById(R.id.ordersAct_btnShipping);
        btnFinish = findViewById(R.id.ordersAct_btnFinish);
        btnCancel = findViewById(R.id.ordersAct_btnCancel);
        btnProcessing = findViewById(R.id.ordersAct_btnProcessing);
    }

    private void setupActionBar() {

        getSupportActionBar().setTitle("Đơn hàng");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }
}
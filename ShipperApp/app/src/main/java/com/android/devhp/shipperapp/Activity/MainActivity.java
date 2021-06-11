package com.android.devhp.shipperapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.devhp.shipperapp.Common.Common;
import com.android.devhp.shipperapp.Model.Shipper;
import com.android.devhp.shipperapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private MaterialEditText metPhone, metPassword;
    private Button btnLogin;
    private CheckBox cbRemember;

    private FirebaseDatabase database;
    private DatabaseReference shippers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Paper.init(this);

        initView();
        initFirebase();
        login();
    }

    private void login() {

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phone = metPhone.getText().toString();
                final String password = metPassword.getText().toString();

                if (cbRemember.isChecked()){

                    Paper.book().write(Common.USER_KEY, phone);
                    Paper.book().write(Common.PASSWORD_KEY, password);
                }

                shippers.child(phone)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    Shipper shipper = dataSnapshot.getValue(Shipper.class);

                                    if (shipper.getPassword().equals(password)){

                                        Intent intent = new Intent(MainActivity.this, Home.class);
                                        Common.currentShipper = shipper;
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(MainActivity.this, R.string.Password_incorrect, Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    Toast.makeText(MainActivity.this,
                                            R.string.Your_phone_s_Shipper_not_exist, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });
    }

    private void initFirebase() {

        database = FirebaseDatabase.getInstance();
        shippers = database.getReference(Common.SHIPPER_TABLE);
    }

    private void initView() {

        cbRemember = findViewById(R.id.mainAct_cbRemember);
        metPhone = findViewById(R.id.mainAct_metPhone);
        metPassword = findViewById(R.id.mainAct_metPassword);
        btnLogin = findViewById(R.id.mainAct_btnLogin);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String phone = Paper.book().read(Common.USER_KEY);
        String password = Paper.book().read(Common.PASSWORD_KEY);

        if (phone != null && password != null)
            if (!phone.isEmpty() && !password.isEmpty()){
                signIn(phone, password);
            }
    }

    private void signIn(String phone, final String password) {

        shippers.child(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            Shipper shipper = dataSnapshot.getValue(Shipper.class);

                            if (shipper.getPassword().equals(password)){

                                Intent intent = new Intent(MainActivity.this, Home.class);
                                Common.currentShipper = shipper;
                                startActivity(intent);
                                finish();
                            }
                            else {
                                Toast.makeText(MainActivity.this, R.string.Password_incorrect, Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(MainActivity.this,
                                    R.string.Your_phone_s_Shipper_not_exist, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
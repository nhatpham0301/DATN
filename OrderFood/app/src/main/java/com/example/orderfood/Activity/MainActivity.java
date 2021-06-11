package com.example.orderfood.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfood.Common.Common;
import com.example.orderfood.Model.User;
import com.example.orderfood.R;
import com.facebook.FacebookSdk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    Button btnContinue;
    TextView txtSlogan;
    ProgressDialog progress;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
    FirebaseUser fUser;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        addControl();
        addEvent();

        printKeyHash();
    }

    private void printKeyHash() {
        try{
            PackageInfo info = getPackageManager().getPackageInfo("com.example.orderfood",
                    PackageManager.GET_SIGNATURES);

            for (Signature signature:info.signatures){
                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT));
            }
        }catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    private void addEvent() {

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        User user = dataSnapshot.child("070").getValue(User.class);
//                        Common.currentUser = user;
//                        Intent intent = new Intent(getApplicationContext(), Home.class);
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    private void login(String phone, String pwd) {
        if(Common.isConnectToInternet(getBaseContext())){

            progress.setMessage("Please waiting...");
            progress.show();
            signIn(phone,pwd);
        } else {
            Toast.makeText(MainActivity.this, "Please Check your connection !!!", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void signIn(final String phone, final String pwd) {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(phone).exists()) {
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone);
                        if (user.getPassword().equals(pwd)) {
                            progress.dismiss();
                            Common.currentUser = user;
                            Intent intent = new Intent(getApplicationContext(), Home.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                            Toast.makeText(MainActivity.this, "Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            progress.dismiss();
                            Toast.makeText(MainActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progress.dismiss();
                        Toast.makeText(MainActivity.this, "User not exists", Toast.LENGTH_SHORT).show();
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addControl() {
        btnContinue = findViewById(R.id.btnContinue);
        txtSlogan = findViewById(R.id.txtSlogan);
        progress = new ProgressDialog(this);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/NABILA.TTF");
        txtSlogan.setTypeface(face);

        Paper.init(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        progress.setMessage("Please waiting...");
        progress.show();

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fUser != null){
            Toast.makeText(this, ""+ fUser.getPhoneNumber(), Toast.LENGTH_SHORT).show();
            reference.child(fUser.getPhoneNumber())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            progress.dismiss();
                            User localUser = dataSnapshot.getValue(User.class);
                            Intent homeIntent = new Intent(getApplicationContext(), Home.class);
                            Common.currentUser = localUser;
                            startActivity(homeIntent);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else {
            progress.dismiss();
        }
    }
}

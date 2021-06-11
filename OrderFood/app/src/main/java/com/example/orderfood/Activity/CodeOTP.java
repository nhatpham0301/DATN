package com.example.orderfood.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.orderfood.Common.Common;
import com.example.orderfood.Model.User;
import com.example.orderfood.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class CodeOTP extends AppCompatActivity {

    private EditText etCode;
    private Button btnVerifyOTP;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser fUser;

    String codeOTP;
    String phone;
    String verificationCodeBySystem;
    DatabaseReference user = FirebaseDatabase.getInstance().getReference("Users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_o_t_p);

        initView();
        initFirebase();

        phone = getIntent().getStringExtra("phone");
        sendVerificationToUser(phone);
        btnVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = etCode.getText().toString();
                if (code.isEmpty() || code.length() < 6){
                    etCode.setError("Wrong OTP...");
                    etCode.requestFocus();
                    return;
                }
//                progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        });
    }

    private void sendVerificationToUser(String phone) {

        Toast.makeText(this, "+84" + phone, Toast.LENGTH_SHORT).show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+84" + phone,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();
            if (code != null){

                progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            Toast.makeText(CodeOTP.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("ERROR", e.getMessage());
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            verificationCodeBySystem = s;
            Toast.makeText(CodeOTP.this, "onCodeSent", Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode(String codeByUser) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        signInTheUserByCredentials(credential);
    }

    private void signInTheUserByCredentials(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(CodeOTP.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            progressBar.setVisibility(View.VISIBLE);
                            fUser = mAuth.getCurrentUser();
                                if(fUser != null)
                                    createNewUserAndLogin();
                        }else
                            Toast.makeText(CodeOTP.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNewUserAndLogin() {
        user.orderByKey().equalTo(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.child(fUser.getPhoneNumber()).exists()){
                            User newUser = new User();
                            newUser.setPhone(fUser.getPhoneNumber());
                            newUser.setName("");

                            user.child(fUser.getPhoneNumber())
                                    .setValue(newUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
//                                                Toast.makeText(CodeOTP.this, "User register successful !!!", Toast.LENGTH_SHORT).show();

                                            // Login
                                            user.child(fUser.getPhoneNumber())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                                        }
                                    });
                        } else {
                            // just login
                            user.child(fUser.getPhoneNumber())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void initFirebase() {

        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();
    }

    private void initView() {

        etCode = findViewById(R.id.codeOTPAct_code);
        btnVerifyOTP = findViewById(R.id.codeOTPAct_btnVerifyOTP);
        progressBar = findViewById(R.id.codeOTPAct_progressBar);
    }
}
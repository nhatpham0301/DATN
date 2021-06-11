package com.example.orderfood.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.orderfood.Common.Common;
import com.example.orderfood.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {

    private EditText etPhoneNumber;
    private Button btnGenerate;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    public final static String CODE_OTP = "CodeOTP";

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        initFirebase();
        LoginWithPhone();
    }

    private void LoginWithPhone() {

            btnGenerate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(etPhoneNumber.getText().toString().matches("") || etPhoneNumber.getText().toString() == null){
                        Toast.makeText(Login.this, "Vui lòng nhập số điện thoại của bạn !!!", Toast.LENGTH_SHORT).show();
                    } else {
                        String phone = etPhoneNumber.getText().toString().substring(1);
                        Intent intent = new Intent(getApplicationContext(), CodeOTP.class);
                        intent.putExtra("phone", phone);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            });
    }

    private void sendUserToHome() {

        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
    }

    private void initFirebase() {

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
    }

    private void initView() {

        etPhoneNumber = findViewById(R.id.loginAct_etPhoneNumber);
        btnGenerate = findViewById(R.id.loginAct_btnGenerate);
    }
}
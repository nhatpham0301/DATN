package com.example.orderfood.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfood.Common.Common;
import com.example.orderfood.Model.User;
import com.example.orderfood.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUp extends AppCompatActivity {

    Button btnSignUp;
    MaterialEditText editTextPhone, editTextName, editTextPassword;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        addControl();
        addEvent();
    }

    private void addControl() {
        btnSignUp = findViewById(R.id.btnSignUp_SignUp);
        editTextPhone = findViewById(R.id.editTextPhoneNumberSignUp);
        editTextName = findViewById(R.id.editTextNameSignUp);
        editTextPassword = findViewById(R.id.editTextPasswordSignUp);
        progress = new ProgressDialog(this);
    }

    private void addEvent() {
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Common.isConnectToInternet(getBaseContext())){
                    progress.setMessage("Please waiting...");
                    progress.show();
                    signUp();
                } else {
                    Toast.makeText(SignUp.this, "Please Check your connection !!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    private void signUp() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(editTextPhone.getText().toString()).exists()){
                    progress.dismiss();
                    Toast.makeText(SignUp.this, "User already !!!", Toast.LENGTH_SHORT).show();
                } else {
                    progress.dismiss();
                    User user = new User(editTextName.getText().toString(), editTextPassword.getText().toString());
                    reference.child(editTextPhone.getText().toString()).setValue(user);
                    Toast.makeText(SignUp.this, "Register successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

package com.example.orderfood.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

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

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignIn extends AppCompatActivity {

    TextView txtForgotPassword;
    CheckBox ckbRemember;
    Button btnSignIn;
    MaterialEditText editTextPhone, editTextPassword;
    ProgressDialog progress;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        addControl();
        addEvent();
    }


    private void addEvent() {
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if(Common.isConnectToInternet(getBaseContext())){

                    if(ckbRemember.isChecked()){
                        Paper.book().write(Common.USER_KEY,editTextPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY,editTextPassword.getText().toString());

                    }
                    progress.setMessage("Please waiting...");
                    progress.show();
                    signIn();
                } else {
                    Toast.makeText(SignIn.this, "Please Check your connection !!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignIn.this, "Forgot Password?", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signIn() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!editTextPhone.equals("") && !editTextPassword.equals("")){

                    if (dataSnapshot.child(editTextPhone.getText().toString()).exists()) {
                        User user = dataSnapshot.child(editTextPhone.getText().toString()).getValue(User.class);
                        user.setPhone(editTextPhone.getText().toString());
                        if (user.getPassword().equals(editTextPassword.getText().toString())) {
                            progress.dismiss();
                            Common.currentUser = user;
                            Intent intent = new Intent(getApplicationContext(), Home.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                            reference.removeEventListener(this);

                            Toast.makeText(SignIn.this, "Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            progress.dismiss();
                            Toast.makeText(SignIn.this, "Wrong password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progress.dismiss();
                        Toast.makeText(SignIn.this, "User not exists", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignIn.this, "Please enter full", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addControl() {
        ckbRemember = findViewById(R.id.ckbRemember);
        btnSignIn = findViewById(R.id.btnSignIn_SignIn);
        editTextPhone = findViewById(R.id.editTextPhoneNumberSignIn);
        editTextPassword = findViewById(R.id.editTextPasswordSignIn);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        progress = new ProgressDialog(SignIn.this);

        Paper.init(this);
    }
}

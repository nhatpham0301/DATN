package com.example.orderfood.Service;

import com.example.orderfood.Common.Common;
import com.example.orderfood.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenRefresh = FirebaseInstanceId.getInstance().getToken();
        if(Common.currentUser != null)
            updateTokenService(tokenRefresh);
    }

    private void updateTokenService(String tokenRefresh) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference reference = db.getReference("Tokens");
        Token token = new Token(tokenRefresh, false);
        reference.child(Common.currentUser.getPhone()).setValue(token);
    }
}

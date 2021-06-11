package com.example.serverside.Service;

import com.example.serverside.Common.Common;
import com.example.serverside.Model.Token;
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
            updateToken(tokenRefresh);
    }

    private void updateToken(String tokenRefresh) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference reference = db.getReference("Tokens");
        Token token = new Token(tokenRefresh, true);
        reference.child(Common.currentUser.getPhone()).setValue(token);
    }
}

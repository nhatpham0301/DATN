package com.android.devhp.shipperapp.Service;

import com.android.devhp.shipperapp.Common.Common;
import com.android.devhp.shipperapp.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenRefresh = FirebaseInstanceId.getInstance().getToken();
        if(Common.currentShipper != null)
            updateToken(tokenRefresh);
    }

    private void updateToken(String tokenRefresh) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference reference = db.getReference("Tokens");
        Token token = new Token(tokenRefresh, true);
        reference.child(Common.currentShipper.getPhone()).setValue(token);
    }
}

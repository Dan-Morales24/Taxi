package com.example.rastreosgps.taxi.Services;

import androidx.annotation.NonNull;

import com.example.rastreosgps.taxi.Common.Common;
import com.example.rastreosgps.taxi.Utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            UserUtils.updateToken(this,s);
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> dataRecv = remoteMessage.getData();
        if (dataRecv != null){

            Common.showNotification(this, new Random().nextInt(),
            dataRecv.get(Common.NOTI_TITLE),
            dataRecv.get(Common.NOTI_CONTENT),
            null);

        }
    }
}

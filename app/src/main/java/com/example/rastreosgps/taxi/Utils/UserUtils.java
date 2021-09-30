package com.example.rastreosgps.taxi.Utils;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.rastreosgps.taxi.Common.Common;
import com.example.rastreosgps.taxi.Model.DriverGeoModel;
import com.example.rastreosgps.taxi.Model.EventBus.SelectedPlaceEvent;
import com.example.rastreosgps.taxi.Model.FCMSendData;
import com.example.rastreosgps.taxi.Model.TokenModel;
import com.example.rastreosgps.taxi.R;
import com.example.rastreosgps.taxi.Remote.IFCMService;
import com.example.rastreosgps.taxi.Remote.RetrofitClient;
import com.example.rastreosgps.taxi.Remote.RetrofitFCMClient;
import com.example.rastreosgps.taxi.RequestDriverActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UserUtils {
    public static void updateUser(View view, Map<String, Object> updateData){
        FirebaseDatabase.getInstance()
                //RIDER
                .getReference(Common.RIDER_INFO_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT).show())
                .addOnSuccessListener(aVoid -> Snackbar.make(view, "Actualizacion de informacion completada",Snackbar.LENGTH_SHORT).show());
    }

    public static void updateToken(Context context, String token){

        TokenModel tokenModel = new TokenModel(token);
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(tokenModel)
                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()).addOnSuccessListener(aVoid ->{


        });

    }

    public static void sendRequestToDriver(Context context, RelativeLayout main_layout, DriverGeoModel foundDriver, SelectedPlaceEvent selectedPlaceEvent) {

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        //Obtener el Token de cloudMessaging

        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REFERENCE_DRIVERS)
                .child(foundDriver.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){
                            TokenModel tokenModel = snapshot.getValue(TokenModel.class);
                            Map<String,String> notificationData = new HashMap<>();
                            notificationData.put(Common.NOTI_TITLE,Common.REQUEST_DRIVER_TITLE);
                            notificationData.put(Common.NOTI_CONTENT,"Solicitud de viaje en proceso");
                            notificationData.put(Common.RIDER_KEY,FirebaseAuth.getInstance().getCurrentUser().getUid());

                            notificationData.put(Common.RIDER_PICKUP_LOCATION_STRING,selectedPlaceEvent.getOriginString());
                             notificationData.put(Common.RIDER_PICKUP_LOCATION, new StringBuilder("")
                            .append(selectedPlaceEvent.getOrigin().latitude)
                            .append(",")
                            .append(selectedPlaceEvent.getOrigin().longitude)
                            .toString());


                            notificationData.put(Common.RIDER_DESTINATION_STRING,selectedPlaceEvent.getDestinationString());
                            notificationData.put(Common.RIDER_DESTINATION, new StringBuilder("")
                                    .append(selectedPlaceEvent.getDestination().latitude)
                                    .append(",")
                                    .append(selectedPlaceEvent.getDestination().longitude)
                                    .toString());

                            FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(),notificationData);

                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(fcmResponse -> {
                                if (fcmResponse.getSuccess() == 0){

                                    compositeDisposable.clear();
                                    Snackbar.make(main_layout,context.getString(R.string.request_driver_failed),Snackbar.LENGTH_LONG).show();

                                }


                            }, throwable -> {

                                compositeDisposable.clear();
                                Snackbar.make(main_layout,throwable.getMessage(),Snackbar.LENGTH_LONG).show();

                            }));

                        }else{
                            Snackbar.make(main_layout,context.getString(R.string.token_not_found),Snackbar.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Snackbar.make(main_layout,error.getMessage(),Snackbar.LENGTH_LONG).show();
                    }
                });

    }
}

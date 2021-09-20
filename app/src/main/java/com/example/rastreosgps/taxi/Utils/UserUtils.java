package com.example.rastreosgps.taxi.Utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.example.rastreosgps.taxi.Common.Common;
import com.example.rastreosgps.taxi.Model.TokenModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

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

}

package com.example.rastreosgps.taxi;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.rastreosgps.taxi.Common.Common;
import com.example.rastreosgps.taxi.Model.RiderModel;
import com.example.rastreosgps.taxi.Utils.UserUtils;
import com.example.rastreosgps.taxi.datosCliente.ClientInfo;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;

public class LoginTypeUber extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE = 7171;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    FirebaseDatabase database;
    DatabaseReference riderInfoRef;
    @BindView(R.id.progress_login_bar)
    ProgressBar progress_login_bar;


    @Override
    public void onStart() {
        super.onStart();
        delaySplashScreen();
    }


    @Override
    public void onStop() {
        if(firebaseAuth != null && listener !=null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.progress_bar_login);
        init();
    }

    private void init() {

        ButterKnife.bind(this);
        database = FirebaseDatabase.getInstance();
        //RIDER
        riderInfoRef = database.getReference(Common.RIDER_INFO_REFERENCE);
        firebaseAuth = FirebaseAuth.getInstance();

        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );


        listener = myFirebaseAuth -> {

            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if(user != null) {

                        FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                    return;
                                }

                                // Get new FCM registration token
                                String token = task.getResult();
                                Log.d("Token final: ", token);
                                Toast.makeText(LoginTypeUber.this, token, Toast.LENGTH_SHORT).show();
                                UserUtils.updateToken(LoginTypeUber.this,token);

                                    }
                                });

                                checkUserFromFirebase();
                                }
                                    else {
                                            showLoginLayout();
                                           }
                                };

                            }


    private void checkUserFromFirebase() {

        riderInfoRef.child(FirebaseAuth.getInstance().getCurrentUser()
                .getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                   // Toast.makeText(LoginTypeUber.this, "Usuario ya registrado: ", Toast.LENGTH_SHORT).show();
                    RiderModel riderModel = snapshot.getValue(RiderModel.class);
                    goToHomeActivity(riderModel);

                }
                else{

                    showRegisterLayout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void goToHomeActivity(RiderModel riderModel) {

        Common.currentRide = riderModel;
       startActivity(new Intent(LoginTypeUber.this,NavigationClientActivity.class));
        finish();


    }

    private void showRegisterLayout() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        TextInputEditText edt_first_name =(TextInputEditText)itemView.findViewById(R.id.edt_first_name);
        TextInputEditText edt_last_name =(TextInputEditText)itemView.findViewById(R.id.edt_last_name);
        TextInputEditText edt_email =(TextInputEditText)itemView.findViewById(R.id.edt_email);
        TextInputEditText edt_number =(TextInputEditText)itemView.findViewById(R.id.edt_phone_number);

        Button btn_continue = (Button) itemView.findViewById(R.id.btn_register);

        //set data
        if(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null && !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser()
        .getPhoneNumber()))edt_number.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();


        btn_continue.setOnClickListener(v -> {

            if(TextUtils.isEmpty(edt_first_name.getText().toString())){

                Toast.makeText(this,"Porfavor ingresa tu Nombre", Toast.LENGTH_SHORT).show();
                return;

            }
                else if(TextUtils.isEmpty(edt_last_name.getText().toString())){

                Toast.makeText(this,"Porfavor ingresa tus apellidos", Toast.LENGTH_SHORT).show();
                return;

            }

            else if(TextUtils.isEmpty(edt_email.getText().toString())){

                Toast.makeText(this,"Porfavor ingresa tu correo electronico", Toast.LENGTH_SHORT).show();
                return;

            }

            else if(TextUtils.isEmpty(edt_number.getText().toString())){

                Toast.makeText(this,"Porfavor ingresa tu numero de telefono", Toast.LENGTH_SHORT).show();

                return;

            }

            else{

                RiderModel model = new RiderModel();

                model.setFirstName(edt_first_name.getText().toString());
                model.setLastName(edt_last_name.getText().toString());
                model.setEmail(edt_email.getText().toString());
                model.setPhoneNumber(edt_number.getText().toString());
                model.setRating(0.0);

                riderInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(model)
                        .addOnFailureListener(e ->

                                {
                                    dialog.dismiss();
                                    Toast.makeText(LoginTypeUber.this, e.getMessage(),Toast.LENGTH_SHORT).show();

                                }

                                )

                        .addOnSuccessListener(unused -> {

                            Toast.makeText(this,"Usuario registrado",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            goToHomeActivity(model);
                        });
            }

        });
    }


    private void delaySplashScreen() {


        progress_login_bar.setVisibility(View.VISIBLE);
        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(() ->

                        firebaseAuth.addAuthStateListener(listener)

                        );

    }


    private void showLoginLayout() {

        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.activity_login_type_uber)
                .setPhoneButtonId(R.id.btn_phone_sign_in)
                .setGoogleButtonId(R.id.btn_google_sign_in)
                .build();

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setIsSmartLockEnabled(false).setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .build(),LOGIN_REQUEST_CODE);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOGIN_REQUEST_CODE) {

            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK){

                FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();

            }
            else{

                Toast.makeText(this, "[ERROR]: "+response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }



}
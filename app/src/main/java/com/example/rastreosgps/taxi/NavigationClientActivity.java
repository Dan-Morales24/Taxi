package com.example.rastreosgps.taxi;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.rastreosgps.taxi.Common.Common;
import com.example.rastreosgps.taxi.Utils.UserUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rastreosgps.taxi.databinding.ActivityNavigationClientBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class NavigationClientActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1000;
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ActivityNavigationClientBinding binding;
    private NavController navController;
    private ImageView img_avatar;
    private Uri imageUrl;
    private AlertDialog waitingDialog;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNavigationClientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarNavigationClient.toolbar);
    /*    binding.appBarNavigationClient.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */
         drawer = binding.drawerLayout;
         navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
         navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation_client);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
            init();

    }

    private void init(){

        storageReference = FirebaseStorage.getInstance().getReference();
        waitingDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Espere...")
                .create();

        navigationView.setNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_sign_out)
            {
            AlertDialog.Builder builder = new AlertDialog.Builder(NavigationClientActivity.this);
            builder.setTitle("Alerta")
                    .setMessage("Estas seguro que quieres cerrar sesion")
                    .setNegativeButton("Cancelar", (dialogInterface, i) -> dialogInterface.dismiss())
                    .setPositiveButton("Cerrar Sesión", (DialogInterface, i) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(NavigationClientActivity.this, LoginTypeUber.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }).setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(NavigationClientActivity.this,android.R.color.holo_red_dark));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(NavigationClientActivity.this,R.color.colorAccent));


            });

            dialog.show();

        }
        return true;
    });

        View headerView = navigationView.getHeaderView(0);
        TextView txt_name = (TextView) headerView.findViewById(R.id.txt_name);
        TextView txt_phone = (TextView) headerView.findViewById(R.id.txt_phone);
        img_avatar = (ImageView) headerView.findViewById(R.id.img_avatar);

        txt_name.setText(Common.builderWelcomeMessage());
        txt_phone.setText(Common.currentRide !=null ? Common.currentRide.getPhoneNumber():"");

        img_avatar.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,PICK_IMAGE_REQUEST);
            
        });

        if(Common.currentRide != null && Common.currentRide.getAvatar()!= null &&
        !TextUtils.isEmpty(Common.currentRide.getAvatar()))
        {
            Glide.with(this)
                    .load(Common.currentRide.getAvatar())
                    .into(img_avatar);


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_client, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
         navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation_client);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(data != null && data.getData() !=null){

                imageUrl = data.getData();
                img_avatar.setImageURI(imageUrl);

                showDialogUpload(imageUrl);

            }

        }
    }

    private void showDialogUpload(Uri imageUrl) {

        AlertDialog.Builder builder = new AlertDialog.Builder(NavigationClientActivity.this);
        builder.setTitle("Actualizar Imagen")
                .setMessage("Seguro que quieres cambiar tu imagen?")
                .setNegativeButton("Cancelar",(dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("Subir",(dialogInterface,i)-> {
                    if (imageUrl != null)
                    {
                        waitingDialog.setMessage("Cargando");
                        waitingDialog.show();

                        String unique_name = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        StorageReference avatarFolder = storageReference.child("avatars/" + unique_name);

                        avatarFolder.putFile(this.imageUrl)
                                .addOnFailureListener(e -> {
                                    waitingDialog.dismiss();
                                    Snackbar.make(drawer, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }).addOnCompleteListener(task -> {
                            if (task.isSuccessful())
                            {
                                avatarFolder.getDownloadUrl().addOnSuccessListener(url -> {
                                    Map<String, Object> updateData = new HashMap<>();
                                    updateData.put("avatar", url.toString());

                                    UserUtils.updateUser(drawer, updateData);

                                });
                            }
                            waitingDialog.dismiss();

                        }).addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            waitingDialog.setMessage(new StringBuilder("Subiendo: ").append(progress).append("%"));

                        });

                    }

                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(R.color.colorAccent));

        });
        dialog.show();
    }
}
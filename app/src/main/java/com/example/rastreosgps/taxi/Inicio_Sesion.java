package com.example.rastreosgps.taxi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Inicio_Sesion# newInstance} factory method to
 * create an instance of this fragment.
 */

public class Inicio_Sesion extends Fragment {
  private SharedPreferences preferences;
    private SharedPreferences.Editor datos_Activity2;
  EditText Usuario;
 EditText Pass;
  Button LoginI;
 private String usuario="";
 private String pass="";
 private ProgressDialog progressDialog;
 private FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    DatabaseReference mDatabase;
    public Inicio_Sesion() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


       View view= inflater.inflate(R.layout.fragment_inicio__sesion, container, false);

       mAuth = FirebaseAuth.getInstance();
       mDatabase = FirebaseDatabase.getInstance().getReference();
       Usuario =(EditText) view.findViewById(R.id.Inicio_Correo);
       Pass =(EditText) view.findViewById(R.id.Inicio_Password);
       LoginI=(Button) view.findViewById(R.id.Inicio_Login);
       LoginI.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               usuario = Usuario.getText().toString();
               pass = Pass.getText().toString();

               if(!usuario.isEmpty() && !pass.isEmpty()){

                   progressDialog = ProgressDialog.show(getContext(), "Espera un poquito..",
                           "Estoy verificando tus datos.", true);
                   loginUser();
               }

               else{

                   Toast.makeText(getContext(), "Faltan datos por completar ", Toast.LENGTH_SHORT).show();
               }

           }
       });

        return view;
    }

    private void getUserInfo(){

        String userID = mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(userID).addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    Toast.makeText(getContext(), "Hola Mundo" , Toast.LENGTH_SHORT).show();

                    String nombre = dataSnapshot.child("Nombre").getValue(String.class);
                    String correo = dataSnapshot.child("Correo").getValue(String.class);
                    String numero = dataSnapshot.child("Numero").getValue(String.class);
                    String typeUser = dataSnapshot.child("typeUser").getValue(String.class);
                    preferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
                    datos_Activity2 = preferences.edit();
                    datos_Activity2.putString("Usuario", nombre);
                    datos_Activity2.putString("Correo", correo);
                    datos_Activity2.putString("Numero", numero);
                    datos_Activity2.commit();

                        if(typeUser.equals("cliente")){

                            Toast.makeText(getContext(), "Es cliente" , Toast.LENGTH_SHORT).show();
                            progressDialog.cancel();
                            getActivity().finish();
                            startActivity(new Intent(getContext(), MainActivityMaps.class));

                        }

                }


                else{

                    Toast.makeText(getContext(), "Credenciales invalidas" , Toast.LENGTH_SHORT).show();
                    progressDialog.cancel();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getContext(), "ocurrio un error: "+error , Toast.LENGTH_SHORT).show();
            }
        });

    }



    public void loginUser(){

    mAuth.signInWithEmailAndPassword(usuario, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override

        public void onComplete(@NonNull Task<AuthResult> task) {

            if(task.isSuccessful()) {
                getUserInfo();
            }
                else {

                Toast.makeText(getContext(), "Credenciales invalidas" , Toast.LENGTH_SHORT).show();
                progressDialog.cancel();

            }

        }
    });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView btnAtrasSesion = view.findViewById(R.id.btnAtrasSesion);
        btnAtrasSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.fragmentLogin);
            }
        });
    }

    }
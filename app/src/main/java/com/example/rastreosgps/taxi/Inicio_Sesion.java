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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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

        preferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
        datos_Activity2 = preferences.edit();
       View view= inflater.inflate(R.layout.fragment_inicio__sesion, container, false);

        mAuth = FirebaseAuth.getInstance();
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

    public void loginUser(){
    mAuth.signInWithEmailAndPassword(usuario, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()){

                datos_Activity2.putString("usuario",usuario);
                datos_Activity2.commit();
                Toast.makeText(getContext(), "La Usuario es correcto ", Toast.LENGTH_SHORT).show();

                getActivity().finish();
                startActivity(new Intent(getContext(), MainActivityMaps.class));

            }
                else {

                Toast.makeText(getContext(), "Credenciales invalidas" , Toast.LENGTH_SHORT).show();
            }

        }
    });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnAtrasSesion = view.findViewById(R.id.btnAtrasSesion);
        btnAtrasSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.fragmentLogin);
            }
        });
    }

    }
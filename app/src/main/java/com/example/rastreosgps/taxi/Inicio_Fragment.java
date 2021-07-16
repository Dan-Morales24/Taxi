package com.example.rastreosgps.taxi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment } subclass.
 */

public class Inicio_Fragment extends Fragment {

    private SharedPreferences preferences;
    private SharedPreferences.Editor datos_Activity2;

   public Inicio_Fragment(){
       //require empty public constructor
   }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        preferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
        if(preferences.contains("Usuario")){
            startActivity(new Intent(getContext(), MainActivityMaps.class));
            getActivity().finish();

        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull  View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btn1=view.findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.fragmentLogin);
            }
        });
    }
}
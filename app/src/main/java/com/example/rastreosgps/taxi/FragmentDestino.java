package com.example.rastreosgps.taxi;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.solver.widgets.WidgetContainer;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavAction;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;


import java.util.Arrays;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentDestino#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentDestino extends Fragment  {
    
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Object FragmentContainerView;
    double destlat ;
    double destLon;
     TrazarRuta trazarRuta;
     EditText editText;

    public FragmentDestino() {
        // Required empty public constructor
    }



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentDestino.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentDestino newInstance(String param1, String param2) {
        FragmentDestino fragment = new FragmentDestino();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_destino, container, false);
        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);




        // Initialize the SDK
        Places.initialize(getActivity().getApplicationContext(), "AIzaSyBp_PG1Db2LqFLDk5PSm1XO_fBtR-C3F3o");

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(getContext());


        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //Filtro para mostrar Locality, Sublocality, PostalCode,Country
        //autocompleteFragment.setTypeFilter(TypeFilter.REGIONS);

        autocompleteFragment.setLocationRestriction(RectangularBounds.newInstance(
                new LatLng(19.16313, -98.2809),
                new LatLng(19.6967, -98.04332)));
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME,Place.Field.ID, Place.Field.LAT_LNG));





        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + place.getLatLng());
                LatLng destinationLatLng = place.getLatLng();
                destlat = destinationLatLng.latitude;
                destLon = destinationLatLng.longitude;

                //    Toast.makeText(getContext(), "Latitud: " +destlat+ "Longitud:" +destLon, Toast.LENGTH_SHORT).show();
                editText = (EditText) view.findViewById(R.id.input);
                String dato = editText.getText().toString();

                Bundle bundle = new Bundle();
                bundle.putDouble("latitud", (destlat));
                bundle.putDouble("longitud",(destLon));
                bundle.putString("Destino",(place.getName()));
                bundle.putString("ejecutarMetodo",dato);
                Navigation.findNavController(view).navigate(R.id.fragmentMap, bundle);
                Navigation.findNavController(getView()).navigate(R.id.fragmentMap);
               // trazarRuta.enviarDatos(dato);


                //getActivity().onBackPressed();



            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });











    }










}
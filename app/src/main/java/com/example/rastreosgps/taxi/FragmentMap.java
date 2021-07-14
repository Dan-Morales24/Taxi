package com.example.rastreosgps.taxi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.Navigation;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


public class FragmentMap extends Fragment implements OnMapReadyCallback, DirectionFinderListener, NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences preferences;
    private SharedPreferences.Editor datos_Activity2;

    Button expand;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    private static final int RESULT_OK = -1 ;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    TextView mensaje2;
    MapView mMapView;
    View mView;
    View vista, confirmar;
    TextView Destino;
    Button enviar;
    private GoogleMap mGoogleMap;
    Double longitudOrigen, latitudOrigen;
    Boolean actualPosition = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //sharedPreference
        preferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
        datos_Activity2 = preferences.edit();



        // control del boton de retoceso
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else{
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        // We set the listener on the child fragmentManager
        getChildFragmentManager().setFragmentResultListener("key", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String key, @NonNull Bundle bundle) {


            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        getLocalization();
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //llamada al navigation view para mostrarlo
        expand = getView().findViewById(R.id.expandir);
        drawerLayout = getView().findViewById(R.id.drawer_layout);
        navigationView = getView().findViewById(R.id.nav_view);
        //colocar enfrente del fragmento el navigation view para poder controlarlo
        navigationView.bringToFront();
        //
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.home);
        vista = getActivity().findViewById(R.id.solicitar);
        confirmar = getActivity().findViewById(R.id.confirmar);
        confirmar.setVisibility(View.GONE);
        Places.initialize(getActivity().getApplicationContext(), "AIzaSyBp_PG1Db2LqFLDk5PSm1XO_fBtR-C3F3o");
        mMapView = (MapView) mView.findViewById(R.id.map_view);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

            enviar = (Button)getView().findViewById(R.id.enviar);
            confirmar = (ConstraintLayout)getView().findViewById(R.id.confirmar);
            mensaje2 = (TextView) getView().findViewById(R.id.destino);
            Destino = getView().findViewById(R.id.button_search);
            Destino.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                //   Navigation.findNavController(v).navigate(R.id.fragmentDestino);
                startAutocompleteActivity();
                }
           });

                    expand.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("WrongConstant")
                        @Override
                        public void onClick(View v) {
                            drawerLayout.openDrawer(Gravity.START);
                        }
                    });

       enviar.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               vista.setVisibility(View.GONE);
               confirmar.setVisibility(View.VISIBLE);
               expand.setVisibility(View.GONE);
           }
       });
    }

    public void mostrarDatos(){

        String usario = preferences.getString("userP","null");

    }


    public void cerrarSesion() {

        datos_Activity2.clear();
        datos_Activity2.commit();

        getActivity().finish();
        startActivity(new Intent(getContext(), MainActivity.class));



    }




    public void startAutocompleteActivity(){

        List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG,Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fieldList).build(getContext());
            startActivityForResult(intent,100);
       }
        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK ){

            Place place = Autocomplete.getPlaceFromIntent(data);
            Destino.setText("Destino: " + place.getAddress());
            LatLng destinationLatLng = place.getLatLng();
            double latitud = destinationLatLng.latitude;
            double longitud = destinationLatLng.longitude;
           // Toast.makeText(getContext(), "latitud : " +latitud+ " Longitud: "+longitud, Toast.LENGTH_SHORT).show();
            sendRequest(latitud,longitud);
            vista.setVisibility(View.VISIBLE);
            confirmar.setVisibility(View.GONE);

        }
    }

    private void getLocalization() {
            int permiso = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permiso == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (actualPosition) {
                    latitudOrigen = location.getLatitude();
                    longitudOrigen = location.getLongitude();
                    actualPosition = false;
                    LatLng miPosition = new LatLng(latitudOrigen, longitudOrigen);
                //    mGoogleMap.addMarker(new MarkerOptions().position(miPosition).title("Yo estoy aqui"));
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(latitudOrigen, longitudOrigen))
                            .zoom(20)
                            .build();
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    setLocation(location);
                }
            }
        });
    }



    public void setLocation (Location loc){
        if(loc.getLatitude()!=0.0 && loc.getLongitude()!=0.0){
            try {
                Geocoder geocoder = new Geocoder(getContext() , Locale.getDefault());
                List<Address> list  = geocoder.getFromLocation( loc.getLatitude(),loc.getLongitude(), 1);
                if (!list.isEmpty()){
                    Address dirCalle = list.get(0);
                    Toast.makeText(getContext(), "Estoy en este lugar : " +dirCalle.getAddressLine(0), Toast.LENGTH_SHORT).show();
                    mensaje2.setText("ubicacion: " + dirCalle.getAddressLine(0));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void sendRequest(double latitudDestino, double longitudDestino) {
    LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));
        double latitude = location.getLatitude();
        double longitud = location.getLongitude();
        //String lotions = location.getName();
        //  Toast.makeText(getContext(), "Estoy en : " +lotions, Toast.LENGTH_SHORT).show();
        try {
            new DirectionFinder(this, latitude, longitud, latitudDestino,longitudDestino).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(getContext(), "Espera un momento..",
                "Trazando ruta y calculando precios", true);
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        double tarifaBase=30;
        double tiempoMin;
        double distanciaKil;
        double costoxKilometro;
        double costoxMinuto;
        double CostoTotal;
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
             ((TextView) getView().findViewById(R.id.tiempo)).setText(route.duration.text);
             ((TextView) getView().findViewById(R.id.kilometros)).setText(route.distance.text);
                double tiempo = route.duration.value;
                double kilometros = route.distance.value;

                    tiempoMin = tiempo/60;
                    distanciaKil = kilometros/1000;
                    costoxKilometro = distanciaKil * 4.8;
                    costoxMinuto = tiempoMin * 1.8;
                    CostoTotal = costoxKilometro + costoxMinuto;
                    if(CostoTotal >30){
                        ((TextView) getView().findViewById(R.id.costo)).setText(""+CostoTotal+" Pesos");
                    }
                    else{
                        ((TextView) getView().findViewById(R.id.costo)).setText(""+tarifaBase+" Pesos");
                     }

                    Toast.makeText(getContext(), "tiempo : " +tiempo+ " Longitud: "+kilometros, Toast.LENGTH_SHORT).show();


                      originMarkers.add(mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.finish))
                              .title(route.startAddress)
                              .position(route.startLocation)));
                    destinationMarkers.add(mGoogleMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.finish))
                            .title(route.endAddress)
                            .position(route.endLocation)));
                    PolylineOptions polylineOptions = new PolylineOptions().
                            geodesic(true).
                            color(Color.BLACK).
                            width(10);

                          for (int i = 0; i < route.points.size(); i++)
                              polylineOptions.add(route.points.get(i));
                              polylinePaths.add(mGoogleMap.addPolyline(polylineOptions));
                          }
                        }

                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                          switch (item.getItemId()){

                              case R.id.home:
                                  Toast.makeText(getContext(), "inicio", Toast.LENGTH_SHORT).show();
                                  break;

                              case R.id.viajes:
                                  Toast.makeText(getContext(), "viajes realizados", Toast.LENGTH_SHORT).show();
                                  break;

                              case R.id.close:
                                  Toast.makeText(getContext(), "cerar sesion", Toast.LENGTH_SHORT).show();
                                 cerrarSesion();
                                  break;
                          }
                            return false;
                        }
                    }
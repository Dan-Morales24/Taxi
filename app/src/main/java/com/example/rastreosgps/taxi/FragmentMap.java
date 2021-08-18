package com.example.rastreosgps.taxi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import pl.droidsonroids.gif.GifImageView;


public class FragmentMap extends Fragment implements OnMapReadyCallback, DirectionFinderListener, NavigationView.OnNavigationItemSelectedListener,com.google.android.gms.location.LocationListener {

    private SharedPreferences preferences;
    private SharedPreferences.Editor datos_Activity2;
    TextView expand;
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
    View vista, confirmar,pedirTaxi;
    TextView Destino,textdestinopreg,buscando;
    Button enviar;
    Button Enviar_Peticion;
    ProgressBar progressBar;
    private GoogleMap mGoogleMap;
    Double longitudOrigen, latitudOrigen;
    Boolean actualPosition = true;
    Location mLastLocation;
    GifImageView car;
    ImageView correcto,planplus,planbasico;
    private Boolean requestbol = false;
    private Marker mDriveMarker;


    private Context mContext;

    // Initialise it from onAttach()
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        //sharedPreference
        preferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
        datos_Activity2 = preferences.edit();
        String nombre = preferences.getString("Usuario","null");
        String correo = preferences.getString("Correo","null");
        String numeroTelefono = preferences.getString("Numero","null");
        //////////////////

       // View navHeader = navigationView.getHeaderView(0);
       // nombreUsu = navHeader.findViewById(R.id.NombreUsuario);
       // nombreUsu.setText(nombre);
        //llamada al navigation view para mostrarlo
        planbasico = getView().findViewById(R.id.planBasico);
        planplus = getView().findViewById(R.id.planPlus);
        textdestinopreg = getView().findViewById(R.id.destinoPreg);
        buscando = getView().findViewById(R.id.Buscando);
        expand = getView().findViewById(R.id.expandir);
        progressBar = getView().findViewById(R.id.progressBar);
        car = getView().findViewById(R.id.gifcar);
        correcto = getView().findViewById(R.id.correctoimg);
        pedirTaxi = getView().findViewById(R.id.PedirTaxi);

        drawerLayout = getView().findViewById(R.id.drawer_layout);
        navigationView = getView().findViewById(R.id.nav_view);

        // inflar Header y pasar datos para mostrarlos
        View Cliente = navigationView.getHeaderView(0);
        ((TextView) Cliente.findViewById(R.id.NombreUsuario)).setText(nombre);
        ((TextView) Cliente.findViewById(R.id.NombreCorreo)).setText(correo);
        //////////////////////////////////////////////////////////


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
        pedirTaxi.setVisibility(View.GONE);
        expand.setVisibility(View.VISIBLE);
        Places.initialize(getActivity().getApplicationContext(), "AIzaSyBp_PG1Db2LqFLDk5PSm1XO_fBtR-C3F3o");
        mMapView = (MapView) mView.findViewById(R.id.map_view);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        Enviar_Peticion =(Button)getView().findViewById(R.id.EnviarPeticion);
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

            planbasico.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(getContext(), "Proximamente tipos de planes", Toast.LENGTH_SHORT).show();
                }
            });

            planplus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(getContext(), "Proximamente tipos de planes", Toast.LENGTH_SHORT).show();

                }
            });





            Enviar_Peticion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(requestbol){

                        geoQuery.removeAllListeners();
                        requestbol = false;
                        driverLocationRef.removeEventListener(driverLocationRefListener);

                        if(driverFoundID!=null){
                            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Drivers").child("customerRequestDrive").child(driverFoundID);
                            driverRef.setValue(true);
                            driverFoundID = null;
                        }

                        driverFound = false;
                        radius =1;
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                        GeoFire geoFire = new GeoFire(ref);
                        geoFire.removeLocation(userId);


                    }

                        else {


                        requestbol = true;
                        String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                        GeoFire geoFire = new GeoFire(ref);
                        geoFire.setLocation(UserId, new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                        getClosestDriver();

                        confirmar.setVisibility(View.VISIBLE);
                        pedirTaxi.setVisibility(View.VISIBLE);
                    }






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
               expand.setVisibility(View.GONE);
               vista.setVisibility(View.GONE);
               confirmar.setVisibility(View.VISIBLE);
               }
           });
        }

    private double radius = 1;
    private boolean driverFound = false;
    private String driverFoundID;
    GeoQuery geoQuery;


    /**

    private void getClosestDriver() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driverOnline");
        GeoFire geoFire = new GeoFire(driverLocation);
         geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestbol){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();

                                    driverFound = true;
                                    driverFoundID = dataSnapshot.getKey();

                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Drivers").child(driverFoundID).child("customerRequestDrive");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("customerRideId", customerId);

                                    driverRef.updateChildren(map);

                                Toast.makeText(getContext(), "encontro conductor conectado", Toast.LENGTH_SHORT).show();

                                getDriverLocation();
                            }


                        }



                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }


            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }


     */
/////

    private void getClosestDriver(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driverOnline");

        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestbol){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (driverFound){
                                    return;
                                }
                                    driverFound = true;
                                    driverFoundID = dataSnapshot.getKey();
                                   String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Drivers").child(driverFoundID).child("customerRequestDrive");
                                HashMap map = new HashMap();
                                    map.put("customerRideId", customerId);
                                  //  map.put("destination", destination);
                                  //  map.put("destinationLat", destinationLatLng.latitude);
                                  //  map.put("destinationLng", destinationLatLng.longitude);
                                   driverRef.updateChildren(map);
                                    getDriverLocation();
                                   // getDriverInfo();
                                   // getHasRideEnded();
                                   // mRequest.setText("Looking for Driver Location....");

                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


////


    @Override
    public void onCreateViewHolder(){


    }

    // nuevo


    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private Marker mDriverMarker;
    private void getDriverLocation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestbol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(mLastLocation.getLatitude());
                    loc1.setLongitude(mLastLocation.getLongitude());

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100){

                            Toast.makeText(mContext, "El conductor llego", Toast.LENGTH_SHORT).show();


                    }else{


                            Toast.makeText(mContext, "Encontramos al conductor", Toast.LENGTH_SHORT).show();

                    }



                    mDriverMarker = mGoogleMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(bitmapDescriptorFromVector(mContext, R.drawable.ic_car)));
                }

                else{

                    if(mDriverMarker!= null) {
                        mDriverMarker.remove();

                      
                            Toast.makeText(mContext, "se elimina el marker de conductor", Toast.LENGTH_SHORT).show();

                        }

                }

            }



            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }







    public void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
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
            Destino.setText(place.getAddress());
            LatLng destinationLatLng = place.getLatLng();
            double latitud = destinationLatLng.latitude;
            double longitud = destinationLatLng.longitude;
           // Toast.makeText(getContext(), "latitud : " +latitud+ " Longitud: "+longitud, Toast.LENGTH_SHORT).show();
            sendRequest(latitud,longitud);
            vista.setVisibility(View.VISIBLE);
            confirmar.setVisibility(View.GONE);
            pedirTaxi.setVisibility(View.GONE);
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
                    mLastLocation = location;
                    latitudOrigen = location.getLatitude();
                    longitudOrigen = location.getLongitude();
                    actualPosition = false;
                    LatLng miPosition = new LatLng(latitudOrigen, longitudOrigen);
                //    mGoogleMap.addMarker(new MarkerOptions().position(miPosition).title("Yo estoy aqui"));
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(latitudOrigen, longitudOrigen))
                            .zoom(16)
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
                  //  Toast.makeText(getContext(), "Estoy en este lugar : " +dirCalle.getAddressLine(0), Toast.LENGTH_SHORT).show();
                    mensaje2.setText(dirCalle.getAddressLine(0));
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
        double tarifaBase = 30;
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

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
             builder.include(route.startLocation);
             builder.include(route.endLocation);
             LatLngBounds bounds = builder.build();
             int width = getResources().getDisplayMetrics().widthPixels;
            int padding = (int) (width*0.2);

             CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,padding);
            mGoogleMap.animateCamera(cameraUpdate);
             //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom( route.endLocation,9));
           //  ((TextView) getView().findViewById(R.id.tiempo)).setText(route.duration.text);
           //  ((TextView) getView().findViewById(R.id.kilometros)).setText(route.distance.text);
                double tiempo = route.duration.value;
                double kilometros = route.distance.value;

                    tiempoMin = tiempo/60;
                    distanciaKil = kilometros/1000;
                    costoxKilometro = distanciaKil * 4.8;
                    costoxMinuto = tiempoMin * 1.8;
                    CostoTotal = costoxKilometro + costoxMinuto;
                    expand.setVisibility(View.VISIBLE);
                    DecimalFormat format = new DecimalFormat();
                    format.setMaximumFractionDigits(2);
                    if(CostoTotal >30){
                        ((TextView) getView().findViewById(R.id.costo)).setText("$"+format.format(CostoTotal));
                    }
                    else{
                        ((TextView) getView().findViewById(R.id.costo)).setText("$"+tarifaBase);
                     }


                   // Toast.makeText(getContext(), "tiempo : " +tiempo+ " Longitud: "+kilometros, Toast.LENGTH_SHORT).show();

                      originMarkers.add(mGoogleMap.addMarker(new MarkerOptions().icon(bitmapDescriptorFromVector(getActivity(), R.drawable.ic_from))
                              .title(route.startAddress)
                              .position(route.startLocation)));
                    destinationMarkers.add(mGoogleMap.addMarker(new MarkerOptions()
                            .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.ic_to))
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


                public BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
                    Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
                    vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
                    Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    vectorDrawable.draw(canvas);
                    return BitmapDescriptorFactory.fromBitmap(bitmap);
                }

    @Override
    public void onLocationChanged(Location location) {

    }
}
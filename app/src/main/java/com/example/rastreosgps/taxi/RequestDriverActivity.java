package com.example.rastreosgps.taxi;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.rastreosgps.taxi.Common.Common;
import com.example.rastreosgps.taxi.Model.DriverGeoModel;
import com.example.rastreosgps.taxi.Model.EventBus.DeclineRequestFromDriver;
import com.example.rastreosgps.taxi.Model.EventBus.DriverAcceptTripEvent;
import com.example.rastreosgps.taxi.Model.EventBus.SelectedPlaceEvent;
import com.example.rastreosgps.taxi.Model.TripPlanModel;
import com.example.rastreosgps.taxi.Remote.IGoogleAPI;
import com.example.rastreosgps.taxi.Remote.RetrofitClient;
import com.example.rastreosgps.taxi.Utils.UserUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.rastreosgps.taxi.databinding.ActivityRequestDriverActivityBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RequestDriverActivity extends FragmentActivity implements OnMapReadyCallback {

    TextView txt_origin;
    private Circle lastUserCircle;
    private long duration = 1000;
    private ValueAnimator lastPulseAnimator;
    private ValueAnimator animator;
    private static final int DESIRED_NUM_OF_SPINS = 5;
    private static final int DESIRED_SECONDS_PER_ONE_FULL_360_SPIN=40;
    //View
    @BindView(R.id.main_layout)
    RelativeLayout main_layout;
    @BindView(R.id.finding_your_ride_layout)
    CardView finding_your_ride_layout;
    @BindView(R.id.confirm_pickup_layout)
    CardView confirm_pickup_layout;
    @BindView(R.id.confirm_uber_layout)
    CardView confirm_uber_layout;
    @BindView(R.id.btn_confirm_uber)
    Button btn_confirm_uber;
    @BindView(R.id.btn_confirm_pickup)
    Button btn_confirm_pickup;
    @BindView(R.id.txt_address_pickup)
    TextView txt_address_pickup;
    @BindView(R.id.driver_info_layout)
    CardView driver_info_layout;
    @BindView(R.id.txt_driver_name)
    TextView txt_driver_name;
    @BindView(R.id.img_driver)
    ImageView img_driver;


    @BindView(R.id.fill_maps)
    View fill_maps;
    private DriverGeoModel lastDriverCall;

    @OnClick(R.id.btn_confirm_uber)
        void onConfirmUber(){
        confirm_uber_layout.setVisibility(View.GONE);
        confirm_pickup_layout.setVisibility(View.VISIBLE);
        setDataPickUp();
    }

    @OnClick(R.id.btn_confirm_pickup)
    void onPickupConfirm(){
    if(mMap == null) return;
    if(selectedPlaceEvent == null)return;
    //Limpiar el mapa
        mMap.clear();
        addMarkerWithPulseAnimation();
    }


    private GoogleMap mMap;
    private ActivityRequestDriverActivityBinding binding;
    private SelectedPlaceEvent selectedPlaceEvent;

    //routes
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleAPI iGoogleAPI;
    private Polyline blackPolyline,greypolyline;
    private PolylineOptions polylineOptions,blackpolylineOptions;
    private List<LatLng> polylineList;
    private Marker originMarker, destinationMarker;







    @Override
    protected void onStart() {
        super.onStart();
    if (!EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
        if(EventBus.getDefault().hasSubscriberForEvent(SelectedPlaceEvent.class))
            EventBus.getDefault().removeStickyEvent(SelectedPlaceEvent.class);

        if(EventBus.getDefault().hasSubscriberForEvent(DeclineRequestFromDriver.class))
            EventBus.getDefault().removeStickyEvent(DeclineRequestFromDriver.class);

        if(EventBus.getDefault().hasSubscriberForEvent(DriverAcceptTripEvent.class))
            EventBus.getDefault().removeStickyEvent(DriverAcceptTripEvent.class);

            EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        if(animator != null) animator.end();
        super.onDestroy();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onDriverAcceptEvent(DriverAcceptTripEvent event) {

        //Obtener informacion del viaje
        FirebaseDatabase.getInstance().getReference(Common.TRIP)
                .child(event.getTripIp())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Toast.makeText(RequestDriverActivity.this,"Conductor acepto",Toast.LENGTH_SHORT).show();

                        if(snapshot.exists()){

                            TripPlanModel tripPlanModel = snapshot.getValue(TripPlanModel.class);
                            mMap.clear();
                            fill_maps.setVisibility(View.GONE);
                            if(animator != null) animator.end();
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(mMap.getCameraPosition().target)
                                    .tilt(0f)
                                    .zoom(mMap.getCameraPosition().zoom)
                                    .build();
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            Glide.with(RequestDriverActivity.this)
                                    .load(tripPlanModel.getDriverModel().getAvatar())
                                    .into(img_driver);
                                    txt_driver_name.setText(tripPlanModel.getDriverModel().getFirstName());


                            confirm_pickup_layout.setVisibility(View.GONE);
                            confirm_uber_layout.setVisibility(View.GONE);
                            driver_info_layout.setVisibility(View.VISIBLE);
                            Toast.makeText(RequestDriverActivity.this,"Conductor acepto"+ tripPlanModel.getDriverModel().getFirstName(),Toast.LENGTH_SHORT).show();

                        }
                            else{

                            Snackbar.make(main_layout,getString(R.string.trip_not_found)+event.getTripIp(),Snackbar.LENGTH_SHORT).show();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Snackbar.make(main_layout,error.getMessage(),Snackbar.LENGTH_SHORT).show();

                    }
                });

    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onSelectedEvent(SelectedPlaceEvent event) { selectedPlaceEvent = event;   }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDeclineRequestEvent(DeclineRequestFromDriver event) {


        if(lastDriverCall != null){

            Common.driversFound.get(lastDriverCall.getKey()).setDecline(true);
            //Driver has Been decline request, just find new driver;
            findNearbyDriver(selectedPlaceEvent);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRequestDriverActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private void init(){

        ButterKnife.bind(this);
        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        drawPath(selectedPlaceEvent);
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_maps_style));
            if(!success)
                Toast.makeText(this,"Error al cargar el estilo de mapa, contacte con soporte", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();

        }


    }

    private void drawPath(SelectedPlaceEvent selectedPlaceEvent) {


        compositeDisposable.add(iGoogleAPI.getDirections("driving",
                "less_driving",
                selectedPlaceEvent.getOriginString(),selectedPlaceEvent.getDestinationString(),
                getString(R.string.google_maps_key))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(returnResult ->{
                    Log.d("API_RETURN", returnResult);

        //Request API


                   // Toast.makeText(this,"Origen: "+selectedPlaceEvent.getDestinationString(),Toast.LENGTH_SHORT).show();

                    try {
                        JSONObject jsonObject = new JSONObject(returnResult);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i=0; i<jsonArray.length();i++){

                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline  = poly.getString("points");
                            polylineList = Common.decodePoly(polyline);

                        }

                polylineOptions = new PolylineOptions();
                       polylineOptions.color(Color.GRAY);
                       polylineOptions.width(12);
                       polylineOptions.startCap(new SquareCap());
                       polylineOptions.jointType(JointType.ROUND);
                       polylineOptions.addAll(polylineList);
                       greypolyline = mMap.addPolyline(polylineOptions);

                       blackpolylineOptions = new PolylineOptions();
                        blackpolylineOptions.color(Color.BLACK);
                        blackpolylineOptions.width(12);
                        blackpolylineOptions.startCap(new SquareCap());
                        blackpolylineOptions.jointType(JointType.ROUND);
                        blackpolylineOptions.addAll(polylineList);
                        blackPolyline = mMap.addPolyline(blackpolylineOptions);

                        //Animator to Line
                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0,100);
                        valueAnimator.setDuration(1100);
                        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
                        valueAnimator.setInterpolator(new LinearInterpolator());
                        valueAnimator.addUpdateListener(value ->{

                            List<LatLng> points = greypolyline.getPoints();
                            int porcentValue = (int)value.getAnimatedValue();
                            int size = points.size();
                            int newPoints = (int)(size*(porcentValue/100.0f));
                            List<LatLng> p = points.subList(0,newPoints);
                            blackPolyline.setPoints(p);

                        });

                        valueAnimator.start();

                        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                .include(selectedPlaceEvent.getOrigin())
                                .include(selectedPlaceEvent.getDestination())
                                .build();


                        JSONObject object = jsonArray.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");
                        JSONObject legObjects =  legs.getJSONObject(0);
                        JSONObject time = legObjects.getJSONObject("duration");
                        String duration = time.getString("text");
                        String start_address = legObjects.getString("start_address");
                        String end_address = legObjects.getString("end_address");
                        addOriginMarker(duration,start_address);
                        addDestinationMarker(end_address);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,160));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom-1));





                    }catch (Exception e){
                        // aqui esta el errpr

                       Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }

                })
        );

    }



    private void addDestinationMarker(String end_address) {
        View view = getLayoutInflater().inflate(R.layout.destination_info_windows,null);
        TextView txt_destination = (TextView)view.findViewById(R.id.txt_destination);

        txt_destination.setText(Common.formatAdress(end_address));

        // Crear el icono marcador
        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvent.getDestination()));


    }

    private void addOriginMarker(String duration, String start_address) {

        View view = getLayoutInflater().inflate(R.layout.origin_info_windows,null);
        TextView txt_time = (TextView)view.findViewById(R.id.txt_time);
         txt_origin = (TextView)view.findViewById(R.id.txt_origin);
        txt_time.setText(Common.formatDuration(duration));
        txt_origin.setText(Common.formatAdress(start_address));

        // Crear el icono marcador
        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();

        originMarker = mMap.addMarker(new MarkerOptions()
        .icon(BitmapDescriptorFactory.fromBitmap(icon))
        .position(selectedPlaceEvent.getOrigin()));



    }

    private void setDataPickUp() {

        txt_address_pickup.setText(txt_origin != null ? txt_origin.getText() : "none");
        mMap.clear();
        //addpickupMarker
        addPickupMarker();

    }

    private void addPickupMarker()  {
        View view = getLayoutInflater().inflate(R.layout.pickup_info_windows,null);
        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(selectedPlaceEvent.getOrigin())
                .zoom(15)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        originMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvent.getOrigin()));


    }



    private void addMarkerWithPulseAnimation() {
    confirm_pickup_layout.setVisibility(View.GONE);
    fill_maps.setVisibility(View.VISIBLE);
    finding_your_ride_layout.setVisibility(View.VISIBLE);
    originMarker = mMap.addMarker(new MarkerOptions()
    .icon(BitmapDescriptorFactory.defaultMarker())
    .position(selectedPlaceEvent.getOrigin()));
    addPulsatingEffect(selectedPlaceEvent);

    }

    private void addPulsatingEffect(SelectedPlaceEvent selectedPlaceEvent) {
        if(lastPulseAnimator != null) lastPulseAnimator.cancel();
        if (lastUserCircle != null) lastUserCircle.setCenter(selectedPlaceEvent.getOrigin());

        lastPulseAnimator = Common.valueAnimate(duration,animation ->{

           if (lastUserCircle != null)
               lastUserCircle.setRadius((Float)animation.getAnimatedValue());
            else{
                lastUserCircle = mMap.addCircle(new CircleOptions()
                .center(selectedPlaceEvent.getOrigin())
                .radius((Float)animation.getAnimatedValue())
                .strokeColor(Color.WHITE)
                .fillColor(Color.parseColor("#33333333")));
           }

        });

        //efecto de giro de camara
       // startMapCameraSpinningAnimation(selectedPlaceEvent);
        findNearbyDriver(selectedPlaceEvent);

    }

    private void findNearbyDriver(SelectedPlaceEvent selectedPlaceEvent) {
        if(Common.driversFound.size() >0){

            float min_distance = 0; //distancia minima default 0
            DriverGeoModel foundDriver = null;
            Location currentRiderLocation = new Location("");
            currentRiderLocation.setLatitude(selectedPlaceEvent.getOrigin().latitude);
            currentRiderLocation.setLongitude(selectedPlaceEvent.getOrigin().longitude);
            for (String key:Common.driversFound.keySet()){

                Location driverLocation = new Location("");
                driverLocation.setLatitude(Common.driversFound.get(key).getGeoLocation().latitude);
                driverLocation.setLongitude(Common.driversFound.get(key).getGeoLocation().longitude);

           if(min_distance == 0){

               min_distance = driverLocation.distanceTo(currentRiderLocation);//First default min_distance

               if(!Common.driversFound.get(key).isDecline())
                  {
                    foundDriver=Common.driversFound.get(key);
                    break; // exit loop because we found driver

                  }
                    else

                        continue; // if already decline before, just skip and continue

                }
                else if(driverLocation.distanceTo(currentRiderLocation) < min_distance){
                //if have any driver smaller min__distance, just get it !
               min_distance = driverLocation.distanceTo(currentRiderLocation);
               if(!Common.driversFound.get(key).isDecline())
               {
                   foundDriver=Common.driversFound.get(key);
                   break; // exit loop because we found driver

               }
               else
                   continue; // if already decline before, just skip and continue

               }


            }
            if(foundDriver != null){

                UserUtils.sendRequestToDriver(this,main_layout,foundDriver,selectedPlaceEvent);
                lastDriverCall = foundDriver;
            }
                else{
                    Toast.makeText(this, getString(R.string.no_driver_accept_request),Toast.LENGTH_SHORT).show();
                   lastDriverCall = null;
                    finish();
            }

        }
            else{

               Snackbar.make(main_layout,"conductores no encontrados!",Snackbar.LENGTH_LONG).show();
                lastDriverCall = null;
                finish();
        }
    }

    private void startMapCameraSpinningAnimation(SelectedPlaceEvent selectedPlaceEvent) {
        if(animator != null)
            animator.cancel();
            animator = ValueAnimator.ofFloat(0,DESIRED_NUM_OF_SPINS*360);
            animator.setDuration(DESIRED_NUM_OF_SPINS*DESIRED_NUM_OF_SPINS*1000);
            animator.setInterpolator(new LinearInterpolator());
            animator.setStartDelay(100);
            animator.addUpdateListener(valueAnimator ->{
                Float newBearingValue = (Float) valueAnimator.getAnimatedValue();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(selectedPlaceEvent.getOrigin())
                .zoom(16f)
                .tilt(45f)
                .bearing(newBearingValue)
                .build()));


            });
            animator.start();

            findNearbyDriver(selectedPlaceEvent);



    }


}
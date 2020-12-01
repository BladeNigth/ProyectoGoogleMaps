package com.example.proyectogooglemaps;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    GasolineraController gc;
    LatLngBounds.Builder constructor = new LatLngBounds.Builder();
    LatLngBounds limites;
    Button centrar;
    boolean center = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        gc = new GasolineraController(this);
        centrar =(Button) findViewById(R.id.btncentar);
        centrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (center==true) {
                    try {
                        int ancho = getResources().getDisplayMetrics().widthPixels;
                        int alto = getResources().getDisplayMetrics().heightPixels;
                        int padding = (int) (alto * 0.25);
                        CameraUpdate centrarmarcadores = CameraUpdateFactory.newLatLngBounds(limites, ancho, alto, padding);
                        mMap.animateCamera(centrarmarcadores);
                    } catch (Exception e) {
                        Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }else if(center==false){
                    Toast.makeText(MapsActivity.this, "No hay gasolineras Agregadas", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        gasolineras(googleMap);
         MiUbicacion(googleMap);

    }
    public void gasolineras (GoogleMap googleMap){
        Cursor Vista = gc.allContadores();
        LatLng latitudes;
        if(Vista != null ){
            Vista.moveToFirst();
            /*                
            do{
                latitudes = new LatLng(Double.parseDouble(Vista.getString(5)), Double.parseDouble(Vista.getString(4)));
                mMap.addMarker(new MarkerOptions()
                        .position(latitudes)
                        .title(Vista.getString(0)+","+  Vista.getString(1))
                        .snippet(Vista.getString(2)+", "+ Vista.getString(3))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latitudes));
            }while (Vista.moveToNext());

             */

            try{
                if(TextUtils.isEmpty(Vista.getString(0))){
                    Toast.makeText(this, "vacio", Toast.LENGTH_SHORT).show();
                }else{
                    do{
                        latitudes = new LatLng(Double.parseDouble(Vista.getString(5)), Double.parseDouble(Vista.getString(4)));
                        mMap.addMarker(new MarkerOptions()
                                .position(latitudes)
                                .title(Vista.getString(0)+","+  Vista.getString(1))
                                .snippet(Vista.getString(2)+", "+ Vista.getString(3))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latitudes));
                        constructor.include(latitudes);
                    }while (Vista.moveToNext());
                     limites = constructor.build();
                     center = true;

                }
            }catch (Exception e){

            }
        }else{

        }
    }
    public void MiUbicacion(GoogleMap googleMap){
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));
        double latitu = location.getLatitude();
        double longitu = location.getLongitude();
        String Dirrecion  = "";
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(latitu,longitu, 1);
            if (!list.isEmpty()) {
                Address DirCalle = list.get(0);
                Dirrecion = DirCalle.getAddressLine(0);
            }
        } catch (IOException e) {
            e.getMessage();
        }
        LatLng a = new LatLng(latitu,longitu);
        mMap.addMarker(new MarkerOptions()
                .position(a)
                .title("Mi Ubicacion: ")
                .snippet(Dirrecion)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(a));
        CameraUpdateFactory.zoomIn();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(a,17.0f));
    }
}
package com.example.josemedinaruiz97.mygeo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int PERMISO_LOCATION = 1;
    private static final int RESOLVE_RESULT = 2;

    private FusedLocationProviderClient clienteLocalizacion;
    private GoogleMap googleMap;
    private LocationCallback callbackLocalizacion;
    private LocationRequest peticionLocalizacion;
    private LocationSettingsRequest ajustesPeticionLocalizacion;
    private SettingsClient ajustesCliente;
    private ArrayList<Localizacion> localizaciones =new ArrayList<>();
    private boolean ubicacionMostrada=false;
    private Location ubicacion;

    private boolean checkPermissions() {
        int estadoPermisos = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return estadoPermisos == PackageManager.PERMISSION_GRANTED;
    }
    private void dibujarObjetosMapa(double latitud,double longitud,boolean linea){
        LatLng coordenadas = new LatLng(latitud,longitud);
        this.googleMap.addMarker(new MarkerOptions().position(coordenadas).title("Mi Ubicacion"));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(coordenadas));
        this.googleMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        if(linea) {
            PolylineOptions p = new PolylineOptions();
            for (Localizacion localizacion : localizaciones) {
                p.add(new LatLng(localizacion.getLocalizacion().getLatitude(), localizacion.getLocalizacion().getLongitude()));
            }

            Polyline polyLinea = googleMap.addPolyline(p);
        }
    }
    private void ejercucionDeHebra(){
        if(!Db4o.comprobarConexion()) {
            Db4o.openDataBase(getExternalFilesDir(null) + "");
        }
        Thread t=new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Date d = new Date(getIntent().getExtras().getString("date"));
                    localizaciones = Db4o.consulta(d);
                }catch (Exception e){

                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (Exception e) {

        }

    }

    private void init() {
        ejercucionDeHebra();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if(checkPermissions()) {
            startService(new Intent(this, LocationService.class));
            startLocations();
        } else {
            requestPermissions();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESOLVE_RESULT:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.v(TAG, "Permiso ajustes localización");
                        startLocations();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.v(TAG, "Sin permiso ajustes localización");
                        break;
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        init();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (localizaciones.size() != 0 && ubicacionMostrada) {
            dibujarObjetosMapa(localizaciones.get(0).getLocalizacion().getLatitude(),
                    localizaciones.get(0).getLocalizacion().getLongitude(),true);
        }else if(ubicacion!=null){
            dibujarObjetosMapa(ubicacion.getLatitude(),ubicacion.getLongitude(),false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocations();
            }
        }
    }

    private void requestPermissions() {
        boolean solicitarPermiso = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (solicitarPermiso) {
            Log.v(TAG, "Explicación racional del permiso");
            showSnackbar(R.string.app_name, android.R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISO_LOCATION);
                }
            });
        } else {
            Log.v(TAG, "Solicitando permiso");
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISO_LOCATION);
        }
    }

    private void showSnackbar(final int idTexto, final int textoAccion,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(idTexto),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(textoAccion), listener).show();
    }

    @SuppressLint("MissingPermission")
    private void startLocations() {
        clienteLocalizacion = LocationServices.getFusedLocationProviderClient(this);
        ajustesCliente = LocationServices.getSettingsClient(this);
        clienteLocalizacion.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    Log.v("xyxyxy", "última localización: " + location.toString());
                } else {
                    Log.v(TAG, "no hay última localización");
                }
            }
        });
        callbackLocalizacion = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location localizacion = locationResult.getLastLocation();
                Log.v(TAG, localizacion.toString());
                Db4o.alta(localizacion);
                if(!ubicacionMostrada){
                    ubicacionMostrada=true;
                    ubicacion=localizacion;
                    onMapReady(googleMap);
                }
            }
        };
        peticionLocalizacion = new LocationRequest();
        peticionLocalizacion.setInterval(10000);
        peticionLocalizacion.setFastestInterval(5000);
        peticionLocalizacion.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(peticionLocalizacion);
        ajustesPeticionLocalizacion = builder.build();

        ajustesCliente.checkLocationSettings(ajustesPeticionLocalizacion)
            .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    Log.v(TAG, "Se cumplen todos los requisitos");
                    clienteLocalizacion.requestLocationUpdates(peticionLocalizacion, callbackLocalizacion,null);
                }
            })
            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.v(TAG, "Falta algún requisito, intento de adquisición");
                            try {
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(MapsActivity.this, RESOLVE_RESULT);
                            } catch (IntentSender.SendIntentException sie) {
                                Log.v(TAG, "No se puede adquirir.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Log.v(TAG, "Falta algún requisito, que no se puede adquirir.");
                    }
                }
            });
    }
}
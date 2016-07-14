package com.jvilam.foundapet.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jvilam.foundapet.R;
import com.jvilam.foundapet.entities.Pet;
import com.jvilam.foundapet.entities.Pets;
import com.jvilam.foundapet.helpers.BaseVolleyFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PetsMapActivity extends BaseVolleyFragment implements OnMapReadyCallback {

    private GoogleMap petsMap;
    private Pets pets;
    private Boolean patrolMode = false;
    private String state;
    private int idUser;
    private String userName;
    private int mapType;
    private boolean noGps = false;
    private boolean guest = false;

    private LatLng lastPos;
    private Map<Marker, Pet> allMarkersMap = new HashMap<>();

    private LocationListener locationListener;
    private LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pets_map);
        getParameters();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        petsMap = googleMap;
        petsMap.setMyLocationEnabled(true);

        // Se establece el tipo de mapa a mostrar en función de las preferencias. Normal por defecto.
        switch (mapType){
            case 0:
                petsMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case 1:
                petsMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case 2:
                petsMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            default:
                petsMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        petsMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPos, 14));

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                LatLng l = new LatLng(location.getLatitude(), location.getLongitude());
                petsMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, 14));
                state = getState(location);
                savePosition(location);
                if (ContextCompat.checkSelfPermission(PetsMapActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(PetsMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PetsMapActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
                if (patrolMode) {
                    sendUserPosition(location);
                    getUsersPositions();
                } else {
                    locationManager.removeUpdates(locationListener);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        if(patrolMode && noGps){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 5, locationListener);
        }else{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 25, locationListener);
        }
        if(patrolMode)
            getUsersPositions();


        addmarkers();

        petsMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });

        petsMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(allMarkersMap.containsKey(marker)){
                    Pet p = allMarkersMap.get(marker);
                    Intent intent = new Intent(getApplicationContext(), PetDescriptionActivity.class);
                    intent.putExtra("petInfo", p);
                    if(!guest) {
                        intent.putExtra("idUser", idUser);
                        intent.putExtra("userName", userName);
                    }
                    intent.putExtra("guest", guest);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Se sobreescribe el método para deshabilitar las actualizaciones de posición al salir de la pantalla.
     */
    @Override
    public void onBackPressed() {
        if (ContextCompat.checkSelfPermission(PetsMapActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(PetsMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PetsMapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        locationManager.removeUpdates(locationListener);
        super.onBackPressed();
    }

    /**
     * Método que inserta los marcadores de los animales
     */
    private void addmarkers() {

        if (pets != null) {
            for (int i = 0; i < pets.size(); i++) {
                Pet p = pets.getPet(i);
                Double latitude = Double.valueOf(p.getLatitude());
                Double longitude = Double.valueOf(p.getLongitude());
                String type = p.getType();
                String race = p.getRace();
                LatLng point = new LatLng(latitude, longitude);

                String foundOrLost;
                if (p.isFol()) {
                    foundOrLost = getResources().getString(R.string.found).concat(" - ");
                } else {
                    foundOrLost = getResources().getString(R.string.lost).concat(" - ");
                }
                Marker m;
                if (p.getIdUser() == idUser) {
                    m = petsMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.
                            defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(foundOrLost.concat(type.concat(" - ").concat(race))).snippet(getResources().getString(R.string.more_info)));
                } else {
                    m = petsMap.addMarker(new MarkerOptions().position(point).title(foundOrLost.concat(type.concat(" - ").concat(race))).snippet(getResources().getString(R.string.more_info)));
                }
                allMarkersMap.put(m, p);
            }
        }
    }

    /**
     * Método que inserta los marcadores de los usuarios cuando son recibidos por el servicio web.
     * @param markers JSONArray con la información de los usuario
     */
    private void addUsersMarkers(JSONArray markers){
        // Se limpia el mapa y se añaden los marcadores, para que no se dupliquen.
        petsMap.clear();
        allMarkersMap = new HashMap<>();
        addmarkers();
        for(int i = 0; i < markers.length(); i++){
            try {
                JSONObject marker = markers.getJSONObject(i);
                LatLng point = new LatLng(Double.valueOf(marker.getString("LATITUDE")), Double.valueOf(marker.getString("LONGITUDE")));
                petsMap.addMarker(new MarkerOptions().position(point).title(marker.getString("USERNAME")).icon(BitmapDescriptorFactory.fromResource(R.drawable.usermarker)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getParameters(){
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey("mascotas")){
                pets = (Pets) parameters.get("mascotas");
            }
            if (parameters.containsKey("idUser")){
                idUser = (int) parameters.get("idUser");
            }
            if (parameters.containsKey("userName")){
                userName = (String) parameters.get("userName");
            }
            if (parameters.containsKey("state")){
                state = (String) parameters.get("state");
            }
            if (parameters.containsKey("guest")) {
                guest = (boolean) parameters.get("guest");
            }
        }
        SharedPreferences prefs = getSharedPreferences("foundapet", Context.MODE_PRIVATE);
        if (prefs != null) {
            lastPos = new LatLng(Double.valueOf(prefs.getString("lat", "0")), Double.valueOf(prefs.getString("long", "0")));
            patrolMode = prefs.getBoolean("patrolMode", false);
            mapType = prefs.getInt("mapType", 0);
            noGps = prefs.getBoolean("noGps", false);
        }
    }

    private void savePosition(Location l){
        SharedPreferences prefs = getSharedPreferences("foundapet", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lat", String.valueOf(l.getLatitude()));
        editor.putString("long", String.valueOf(l.getLongitude()));
        editor.commit();
    }

    /**
     * Mmétodo que recupera las posiciones de los usuarios que tienen activado el modo patrulla
     */
    private void getUsersPositions() {
        String registerUrl = getResources().getString(R.string.url_get_users_positions);

        JSONObject params = new JSONObject();
        try {
            params.put("state", state);

            // Se crea la petición
            JsonArrayRequest jsonObjReq = new JsonArrayRequest(registerUrl, params,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            addUsersMarkers(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {}
            });
            // Se añade la petición a la cola
            addToQueue(jsonObjReq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mmétodo que actualiza la posición del usuario en la base de datos si está activo el modo patrulla
     */
    private void sendUserPosition(Location l) {
        String registerUrl = getResources().getString(R.string.url_send_user_position);

        JSONObject params = new JSONObject();
        try {

            params.put("idUser", idUser);
            params.put("state", state);
            params.put("lat", l.getLatitude());
            params.put("lon", l.getLongitude());

            // Se crea la petición
            JsonArrayRequest jsonObjReq = new JsonArrayRequest(registerUrl, params,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {}
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {}
            });
            // Se añade la petición a la cola
            addToQueue(jsonObjReq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que devuelve una cadena de texto que representa la provincia obtenida.
     * @param l La localización
     * @return  La cadena con la provincia
     */
    protected String getState(Location l){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        String s = null;
        try {
            addresses = geocoder.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
            s = addresses.get(0).getSubAdminArea().toUpperCase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }
}

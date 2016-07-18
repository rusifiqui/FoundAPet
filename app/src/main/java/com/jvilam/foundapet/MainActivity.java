package com.jvilam.foundapet;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.jvilam.foundapet.entities.Pet;
import com.jvilam.foundapet.entities.Pets;
import com.jvilam.foundapet.helpers.BaseVolleyActivity;
import com.jvilam.foundapet.views.NewPetActivity;
import com.jvilam.foundapet.views.PetsMapActivity;
//import com.purplebrain.adbuddiz.sdk.AdBuddiz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseVolleyActivity {

    private boolean doubleBackToExitPressedOnce = false;
    private String state;
    private int idUser;
    private String userName;
    private boolean patrolModeState = false;
    private boolean noGps = false;
    private boolean showAllPets = false;
    private boolean guest = false;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private ProgressDialog progress;
    private ProgressDialog progressLocating;

    private CheckBox patrolModeCheck;

    private Pets pts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Comprobamos que la aplicación tenga permisos para utilizar la ubicación y, en caso de no tenerlo, se solicita.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
        setContentView(R.layout.activity_main);

        // PUBLICIDAD
        //AdBuddiz.setPublisherKey(getResources().getString(R.string.key));
        // TODO Modo de prueba de publicidad. Comentar para subir a Producción
        //AdBuddiz.setTestModeActive();
        //AdBuddiz.cacheAds(this);

        ImageButton mapButton = (ImageButton) findViewById(R.id.imageButtonMap);
        ImageButton settingsButton = (ImageButton) findViewById(R.id.imageButtonSettings);
        ImageButton newPet = (ImageButton) findViewById(R.id.buttonNewPet);
        patrolModeCheck = (CheckBox) findViewById(R.id.checkBoxPatrolMode);
        TextView feedback = (TextView) findViewById(R.id.textViewFeedback);

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setTitle(R.string.loading);
        progress.setMessage(getResources().getString(R.string.loading_pets));
        progress.setCancelable(false);

        progressLocating = new ProgressDialog(this);
        progressLocating.setIndeterminate(true);
        progressLocating.setTitle(R.string.locating);
        progressLocating.setMessage(getResources().getString(R.string.locating_text));
        progressLocating.setCancelable(false);

        getParameters();

        if (feedback != null) {
            feedback.setText(Html.fromHtml("<a href=\"mailto:"+getString(R.string.email_address)+"?subject="+getString(R.string.email_subject)+"\" >"+ getString(R.string.feedback) +"</a>"));
            feedback.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if(guest) {
            patrolModeCheck.setClickable(false);
            patrolModeCheck.setEnabled(false);
            if (settingsButton != null) {
                settingsButton.setClickable(false);
                settingsButton.setEnabled(false);
            }
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                progressLocating.dismiss();
                //TODO Activar anuncios
                //AdBuddiz.showAd(MainActivity.this);
                // Se obtiene la provincia del usuario.
                state = getState(location);
                if(state != null && state.length() > 0){
                    // Comprobamos que la aplicación tenga permisos para utilizar la ubicación y, en caso de no tenerlo, se solicita.
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                1);
                    }
                    // Se eliminan las peticiones de solicitudes de localización.
                    locationManager.removeUpdates(locationListener);
                }
                Toast toast = Toast.makeText(getApplicationContext(), state, Toast.LENGTH_SHORT);
                toast.show();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };

        progressLocating.show();

        if(!noGps){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);

        // Listener para el botón de mapa
        if (mapButton != null) {
            mapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progress.show();
                    getPetsByState();
                }
            });
        }

        // Listener para el botón de ajustes
        if (settingsButton != null) {
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    intent.putExtra("idUser", idUser);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        }

        // Listener para el botón de nueva mascota
        if (newPet != null) {

                newPet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(guest){
                            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_guest_allowed), Toast.LENGTH_SHORT);
                            toast.show();
                        }else {
                            Intent intent = new Intent(getApplicationContext(), NewPetActivity.class);
                            intent.putExtra("idUser", idUser);
                            intent.putExtra("userName", userName);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }
                });
        }

        // Listener para el check del modo patrulla
        patrolModeCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = getSharedPreferences("foundapet", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("patrolMode",isChecked);
                editor.commit();
                patrolModeState = isChecked;
                updatePatrolModeState(patrolModeState);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            // Comprobamos que la aplicación tenga permisos para utilizar la ubicación y, en caso de no tenerlo, se solicita.
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }
            updatePatrolModeState(false);
            SharedPreferences prefs = getSharedPreferences("foundapet", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("patrolMode",false);
            editor.commit();
            // Se detienen las actualizaciones de localización.
            locationManager.removeUpdates(locationListener);
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.exit, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
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

    /**
     * Mmétodo que recupera las mascotas por provincia e inicia la actividad de mapa
     */
    private void getPetsByState() {
        String registerUrl;
        // Se comprueba si se deben mostrar todos los animales o solamente los de la provincia del usuario
        if(showAllPets){
            registerUrl = getResources().getString(R.string.url_get_all_pets);
        }else {
            registerUrl = getResources().getString(R.string.url_get_pets_by_state);
        }
        JSONObject params = new JSONObject();
        try {
            params.put("state", state);

            // Se crea la petición
            JsonArrayRequest jsonObjReq = new JsonArrayRequest(registerUrl, params,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray pets) {
                            // Se obtienen los comentarios de las mascotas de la provincia
                            pts = new Pets();
                            pts = getPets(pets);
                            Intent intent = new Intent(getApplicationContext(), PetsMapActivity.class);
                            intent.putExtra("mascotas", pts);
                            if(!guest) {
                                intent.putExtra("idUser", idUser);
                                intent.putExtra("userName", userName);
                            }
                            intent.putExtra("guest", guest);
                            intent.putExtra("state", state);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            progress.dismiss();
                            startActivity(intent);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // No hay mascotas
                    Intent intent = new Intent(getApplicationContext(), PetsMapActivity.class);
                    intent.putExtra("idUser", idUser);
                    intent.putExtra("userName", userName);
                    intent.putExtra("state", state);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    progress.dismiss();
                    startActivity(intent);
                }
            });
            // Se añade la petición a la cola
            addToQueue(jsonObjReq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mmétodo que actualiza en el servidor el estado del modo patrulla
     */
    private void updatePatrolModeState(boolean modeState) {
        String registerUrl = getResources().getString(R.string.url_update_patrol_mode_state);

        JSONObject params = new JSONObject();
        try {
            params.put("idUser", idUser);
            params.put("patrolState", modeState);

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

    private Pets getPets(JSONArray pets){
        Pets v = new Pets();
        for(int i = 0; i < pets.length(); i++){
            try {
                JSONObject a = pets.getJSONObject(i);
                Pet p = new Pet();
                p.setId(a.getInt("ID_PET"));
                p.setType(a.getString("TYPE"));
                p.setRace(a.getString("RACE"));
                p.setLatitude(a.getString("LATITUDE"));
                p.setLongitude(a.getString("LONGITUDE"));
                p.setDescription(a.getString("DESCRIPTION"));
                p.setStatus(1);
                p.setIdUser(a.getInt("ID_USER"));
                if(a.getInt("FOUND_OR_LOST") == 1){
                    p.setFol(true);
                }else{
                    p.setFol(false);
                }

                v.addPet(p);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return v;
    }

    /** Se elimina ya que, en caso se existir un gran volumen de comentarios, no caben en un JSON
     * Método que genera los comentarios para enviar al mapa.
     */
    /*private void makeComments(JSONArray com){
        Comments comments = new Comments();
        int actPet = 0;
        for(int i = 0; i < com.length(); i++){
            int p;
            try {
                Comment singleCom = new Comment();
                JSONObject c = com.getJSONObject(i);
                singleCom.setComment(c.getString("COMMENT"));
                singleCom.setUserName(c.getString("USER_NAME"));
                singleCom.setDate(c.getString("COMMENT_DATE"));
                singleCom.setId(c.getInt("ID_COMMENT"));
                p = c.getInt("ID_PET");
                if(actPet != p && actPet != 0){
                    commentsMap.put(actPet, comments);
                    actPet = p;
                    comments = new Comments();
                }else if (actPet != p){
                    actPet = p;
                }
                comments.addComment(singleCom);

                if((i == (com.length() - 1))){
                    commentsMap.put(p, comments);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }*/

    private void getParameters(){
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey("idUser")) {
                idUser = (int) parameters.get("idUser");
            }
            if (parameters.containsKey("userName")) {
                userName = (String) parameters.get("userName");
            }
            if (parameters.containsKey("guest")) {
                guest = (boolean) parameters.get("guest");
            }
        }
        SharedPreferences prefs = getSharedPreferences("foundapet", Context.MODE_PRIVATE);
        if (prefs != null) {
            patrolModeCheck.setChecked(prefs.getBoolean("patrolMode", false));
            noGps = prefs.getBoolean("noGps", false);
            showAllPets = prefs.getBoolean("allPets", false);
        }
    }
}

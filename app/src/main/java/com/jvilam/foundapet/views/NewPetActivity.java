package com.jvilam.foundapet.views;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jvilam.foundapet.MainActivity;
import com.jvilam.foundapet.R;
import com.jvilam.foundapet.helpers.BaseVolleyActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class NewPetActivity extends BaseVolleyActivity implements OnMapReadyCallback {

    private static final int SELECT_PHOTO = 100;
    private static final int REQUEST_CROP_ICON = 200;

    private LatLng lastPos;
    private LocationListener locationListener;
    private Bitmap image;
    private boolean pictureSet = false;
    private int idUser;

    private GoogleMap newPetMap;
    private Spinner spinnerType;
    private EditText race;
    private EditText description;
    private ImageView preview;
    private Switch petFound;

    private ProgressDialog progress;
    private String item;
    private LatLng selectedLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pet);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentPetLocation);
        mapFragment.getMapAsync(this);

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setTitle(R.string.loading);
        progress.setMessage(getResources().getString(R.string.creating_pet));
        progress.setCancelable(false);

        spinnerType = (Spinner) findViewById(R.id.spinnerType);
        race = (EditText) findViewById(R.id.editTextRace);
        description = (EditText) findViewById(R.id.editTextComments);
        Button send = (Button) findViewById(R.id.buttonSave);
        ImageButton picture = (ImageButton) findViewById(R.id.buttonPicture);
        preview = (ImageView) findViewById(R.id.imageViewPreview);
        petFound = (Switch) findViewById(R.id.switchFoundPet);

        getParameters();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.pet_types, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerType.setAdapter(adapter);

        // Listener para eliminar el hint del spinner de tipo de especie
        spinnerType.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                removeHint();
                return false;
            }
        });

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}

        });

        // Listener para el botón enviar
        if (send != null){
            send.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(validateFields()) {
                        progress.show();
                        String state = getState(selectedLocation);
                        sendNewPet(state);
                    }
                }
            });
        }

        if (picture != null){
            picture.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    // Validación de permisos
                    if (ContextCompat.checkSelfPermission(NewPetActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ActivityCompat.requestPermissions(NewPetActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                    }
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){

                    final Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setData(imageReturnedIntent.getData());
                    intent.putExtra("outputX", 400);
                    intent.putExtra("outputY", 400);
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("scale", true);
                    intent.putExtra("noFaceDetection", true);
                    intent.putExtra("output", imageReturnedIntent.getData());
                    startActivityForResult(intent, REQUEST_CROP_ICON);
                }
                break;
            case REQUEST_CROP_ICON:
                try {
                    if (imageReturnedIntent != null){
                        Uri selectedImage = imageReturnedIntent.getData();
                        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        image = BitmapFactory.decodeStream(imageStream);
                        preview.setImageBitmap(decodeUri(selectedImage));
                        pictureSet = true;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        newPetMap = googleMap;

        if (ContextCompat.checkSelfPermission(NewPetActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(NewPetActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NewPetActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        newPetMap.setMyLocationEnabled(true);
        newPetMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPos, 14));

        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                LatLng l = new LatLng(location.getLatitude(), location.getLongitude());
                newPetMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, 14));
                savePosition(location);
                if (ContextCompat.checkSelfPermission(NewPetActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(NewPetActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(NewPetActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
                locationManager.removeUpdates(locationListener);
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        newPetMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                selectedLocation = point;
                newPetMap.clear();
                newPetMap.addMarker(new MarkerOptions().position(point));
                selectedLocation = new LatLng(point.latitude, point.longitude);
            }
        });
    }

    private void removeHint(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.pet_types_no_hint, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerType.setAdapter(adapter);

    }

    /**
     * Método que llama al servicio web del servidor para dar de alta una nueva mascota
     * @param state La provincia en la que se encuentra el usuario
     */
    private void sendNewPet(String state){
        String registerUrl = getResources().getString(R.string.url_new_pet);

        JSONObject params = new JSONObject();
        try {
            params.put("type", item);
            params.put("race", race.getText().toString());
            params.put("desc", description.getText().toString());
            params.put("lat", selectedLocation.latitude);
            params.put("long", selectedLocation.longitude);
            params.put("state", state);
            params.put("img", convertBitmapToString(image));
            params.put("idusr", idUser);
            params.put("lost", petFound.isChecked());

            // Se crea la petición
            JsonArrayRequest jsonObjReq = new JsonArrayRequest(registerUrl, params,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                JSONObject a = response.getJSONObject(0);
                                String resultCode = a.getString("resultCode");
                                int result = Integer.valueOf(resultCode);
                                if (result == 0) {
                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.pet_created, Toast.LENGTH_SHORT);
                                    toast.show();
                                    finishActivity();
                                } else {
                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.user_creation_error, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            progress.dismiss();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progress.dismiss();
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.user_creation_error, Toast.LENGTH_SHORT);
                    toast.show();
                }
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
    protected String getState(LatLng l){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        String s = null;
        try {
            addresses = geocoder.getFromLocation(l.latitude, l.longitude, 1);
            s = addresses.get(0).getSubAdminArea().toUpperCase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * Método que valida el valor de los campos antes de llamar al servicio para dar de alta una mascota
     * @return true -> OK, false -> KO
     */
    private boolean validateFields(){
        if(spinnerType.getSelectedItem().toString().equals(getResources().getString(R.string.spinner_not_valid))){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.no_type, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        if(race.getText() == null ||race.getText().toString().length() == 0){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.no_race, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        if(description.getText() == null ||description.getText().toString().length() == 0){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.no_desc, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        if(selectedLocation == null){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.no_location, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        if(!pictureSet){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.no_pic, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        return true;
    }

    /**
     * Método que recupera parámetros de la aplicación
     */
    private void getParameters(){

        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey("idUser")) {
                idUser = (int) parameters.get("idUser");
            }
        }
        SharedPreferences prefs = getSharedPreferences("foundapet", Context.MODE_PRIVATE);
        if (prefs != null) {
            lastPos = new LatLng(Double.valueOf(prefs.getString("lat", "0")), Double.valueOf(prefs.getString("long", "0")));
        }

    }

    /**
     * Método que almacena la localización
     * @param l La localización
     */
    private void savePosition(Location l){
        SharedPreferences prefs = getSharedPreferences("foundapet", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lat", String.valueOf(l.getLatitude()));
        editor.putString("long", String.valueOf(l.getLongitude()));
        editor.commit();
    }

    /**
     * Método que decodifica una URI y devuelve la imagen como un Bitmap
     * @param selectedImage la dirección
     * @return La imágen
     * @throws FileNotFoundException
     */
    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 400;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap myBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

        if (myBitmap.getWidth() < myBitmap.getHeight()){
            try {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return myBitmap;
    }

    /**
     * Método que convierte un objeto de tipo Bitmap a una cadena de caracteres codificada en Base 64
     * @param bmp La imagen
     * @return La cadena
     */
    public String convertBitmapToString(Bitmap bmp){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 90, stream); //compress to which format you want.
        byte[] byte_arr = stream.toByteArray();
        return Base64.encodeToString(byte_arr, Base64.DEFAULT);

    }

    /**
     * Método encargado de finalizar la actividad.
     */
    private void finishActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("idUser", idUser);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

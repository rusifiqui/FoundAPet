package com.jvilam.foundapet;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.jvilam.foundapet.helpers.BaseVolleyActivity;
import com.jvilam.foundapet.helpers.Sha1Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jvilam on 20/05/2016.
 * Actividad principal para gestionar el acceso a la aplicación.
 * @since 20/05/2016
 * @version 1.0
 */
public class LoginActivity extends BaseVolleyActivity {

    private EditText user;
    private EditText pass;
    private CheckBox remember;

    private ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user = (EditText) findViewById(R.id.editTextUser);
        pass = (EditText) findViewById(R.id.editTextPassword);
        Button access = (Button) findViewById(R.id.buttonLogin);
        Button newUser = (Button) findViewById(R.id.buttonNewUser);
        Button guest = (Button) findViewById(R.id.buttonGuest);
        remember = (CheckBox) findViewById(R.id.checkBoxRemeberLogin);

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setTitle(R.string.connect_title);
        progress.setMessage(getResources().getString(R.string.connect_message));
        progress.setCancelable(false);

        // Comprobamos que la aplicación tenga permisos para utilizar la ubicación y, en caso de no tenerlo, se solicita.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }

        // Comprobamos si existe configuración
        getConfiguration();

        // Listener para el botón de login
        if (access != null) {
            access.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(user.getText().length() > 0 && pass.getText().length() > 0){
                        // Se comprueba si se ha marcado el check para almacenar las credenciales. En caso
                        // de estar marcado, se guardan el usuario y la contraseña en las preferencias.
                        if(remember.isChecked())
                            saveConfiguration();
                        progress.show();
                        login();
                    }else{
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.credentials_needed, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });
        }

        // Listener para el botón de nuevo usuario
        if (newUser != null) {
            newUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), NewUserActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        }

        if (guest != null) {
            guest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("guest", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }
            });
        }

        // Listener para el check de recordar usuario y contraseña
        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    deteleConfiguration();
                }
            }
        });

    }

    /**
     * Método que recupera la configuración del usuario y la contraseña
     */
    private void getConfiguration(){
        SharedPreferences prefs = getSharedPreferences("foundapet", Context.MODE_PRIVATE);
        user.setText(prefs.getString("user", ""));
        pass.setText(prefs.getString("pass", ""));
        if(user.getText().length() > 0 && pass.getText().length() > 0){
            remember.setChecked(true);
        }
    }

    private void saveConfiguration(){
        SharedPreferences prefs = getSharedPreferences("foundapet", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user", user.getText().toString());
        editor.putString("pass", pass.getText().toString());
        editor.commit();
    }

    private void deteleConfiguration(){
        SharedPreferences prefs = getSharedPreferences("foundapet", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("user");
        editor.remove("pass");
        editor.commit();
    }

    /**
     * Método que realiza el login del usuario
     */
    private void login(){
        String loginUrl = getResources().getString(R.string.url_login);
        String password = null;

        // Se obtiene la contraseña cifrada
        try {
            password = Sha1Helper.encript(pass.getText().toString());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if(password != null) {
            JSONObject params = new JSONObject();
            try {
                params.put("user", user.getText().toString());
                params.put("pass", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Se crea la petición
            JsonArrayRequest jsonObjReq = new JsonArrayRequest(loginUrl, params,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                JSONObject a = response.getJSONObject(0);
                                String as = a.getString("COUNT");
                                int idUser = a.getInt("ID_USER");
                                String userName = a.getString("USERNAME");

                                if(Integer.valueOf(as) != 0){
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.putExtra("idUser", idUser);
                                    intent.putExtra("userName", userName);
                                    intent.putExtra("guest", false);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    LoginActivity.this.finish();
                                }else{
                                    Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrong_user), Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrong_user), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                            progress.dismiss();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progress.dismiss();
                    Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrong_user), Toast.LENGTH_SHORT);
                    toast.show();
                    onConnectionFailed(error.toString());
                }
            });

            // Se añade la petición a la cola
            addToQueue(jsonObjReq);
        }
    }


}

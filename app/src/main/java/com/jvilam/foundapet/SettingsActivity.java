package com.jvilam.foundapet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.jvilam.foundapet.helpers.BaseVolleyActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends BaseVolleyActivity {

    private Spinner mapType;
    private CheckBox noGps;
    private CheckBox showAllPets;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setTitle(R.string.loading);
        progress.setMessage(getResources().getString(R.string.deleteing_user));
        progress.setCancelable(false);

        mapType = (Spinner) findViewById(R.id.spinnermapType);
        noGps = (CheckBox) findViewById(R.id.checkBoxNoGps);
        showAllPets = (CheckBox) findViewById(R.id.checkBoxShowAllPets);
        Button deleteUserButton = (Button) findViewById(R.id.buttonDeleteUser);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.map_types, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mapType.setAdapter(adapter);

        getPreferences();

        prefs = getSharedPreferences("foundapet", Context.MODE_PRIVATE);
        editor = prefs.edit();

        mapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("mapType", position);
                editor.commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}

        });
        noGps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("noGps", isChecked);
                editor.commit();
            }
        });

        showAllPets.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("allPets", isChecked);
                editor.commit();
            }
        });

        if (deleteUserButton != null) {
            deleteUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteUserDialog();
                }
            });
        }
    }

    /**
     * Método que recupera valores almacenados
     */
    private void getPreferences(){
        SharedPreferences prefs = getSharedPreferences("foundapet", Context.MODE_PRIVATE);
        if (prefs != null) {
            if(prefs.contains("mapType")){
                mapType.setSelection(prefs.getInt("mapType", 0));
            }
            if(prefs.contains("noGps")){
                noGps.setChecked(prefs.getBoolean("noGps", false));
            }
            if(prefs.contains("allPets")){
                showAllPets.setChecked(prefs.getBoolean("allPets", false));
            }
        }
    }

    /**
     * Método que solicita confirmación antes de eliminar un usuario.
     */
    private void deleteUserDialog(){
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setMessage(R.string.delete_confirmation)
                .setTitle(R.string.attention)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progress.show();
                        int userId;
                        Bundle parameters = getIntent().getExtras();
                        if (parameters != null) {
                            if (parameters.containsKey("idUser")) {
                                userId = (int) parameters.get("idUser");
                                deleteUser(userId);
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog confirmDialog = builder.create();
        confirmDialog.show();
    }

    /**
     * Mmétodo que elimina un usuario
     */
    private void deleteUser(int idUser){
        String registerUrl = getResources().getString(R.string.url_delete_user);

        JSONObject params = new JSONObject();
        try {
            params.put("idUser", idUser);

            // Se crea la petición
            JsonArrayRequest jsonObjReq = new JsonArrayRequest(registerUrl, params,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray pets) {
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            progress.dismiss();
                            startActivity(intent);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
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

}

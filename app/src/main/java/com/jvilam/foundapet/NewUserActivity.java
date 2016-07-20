package com.jvilam.foundapet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
 * Actividad encargada del registro de nuevos usuarios.
 * @author Enrique Vila
 */
public class NewUserActivity extends BaseVolleyActivity {

    EditText fName;
    EditText lName;
    EditText user;
    EditText email;
    EditText pass;
    EditText verifyPass;
    Button register;

    ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        fName = (EditText) findViewById(R.id.editTextFirstName);
        lName = (EditText) findViewById(R.id.editTextLastname);
        user = (EditText) findViewById(R.id.editTextUser);
        email = (EditText) findViewById(R.id.editTextEmail);
        pass = (EditText) findViewById(R.id.editTextPassword);
        verifyPass = (EditText) findViewById(R.id.editTextRepeatPassword);
        register = (Button) findViewById(R.id.buttonRegister);

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setTitle(R.string.connect_title);
        progress.setMessage(getResources().getString(R.string.connect_message));
        progress.setCancelable(false);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             if(validateFields()){
                 progress.show();
                 registerUser();
             }
            }
        });
    }

    /**
     * Método encargado de validar los campos de la actividad
     * @return true si los datos introducidos son correctos, false en caso contrario
     */
    private boolean validateFields(){
        if(fName.length() == 0 || lName.length() == 0 || user.length() == 0 ||
                email.length() == 0 || pass.length() == 0 || verifyPass.length() == 0){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.all_fields_required, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        if(user.getText().toString().contains(" ")){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.no_space_in_login, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        if(pass.length() < 6){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.short_pass, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        if(pass.getText().toString().contains(" ")){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.no_space_in_pass, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        if(!pass.getText().toString().equals(verifyPass.getText().toString())){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.pass_dont_match, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        if(!email.getText().toString().contains("@") || email.length() < 6){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.not_valid_email, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        return true;
    }

    /**
     * Método que se encarga de llamar al servicio web encargado de generar el nuevo usuario.
     */
    private void registerUser() {
        String registerUrl = getResources().getString(R.string.url_register);
        String encriptedPassword = null;
        try {
            encriptedPassword = Sha1Helper.encript(pass.getText().toString());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (encriptedPassword != null) {
            JSONObject params = new JSONObject();
            try {
                params.put("user", user.getText().toString());
                params.put("pass", encriptedPassword);
                params.put("fname", fName.getText().toString());
                params.put("lname", lName.getText().toString());
                params.put("email", email.getText().toString());

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
                                        Toast toast = Toast.makeText(getApplicationContext(), R.string.user_created, Toast.LENGTH_SHORT);
                                        toast.show();
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        progress.dismiss();
                                        startActivity(intent);
                                    } else {
                                        if (result == 1) {
                                            Toast toast = Toast.makeText(getApplicationContext(), R.string.user_exists, Toast.LENGTH_SHORT);
                                            toast.show();
                                        } else {
                                            Toast toast = Toast.makeText(getApplicationContext(), R.string.user_creation_error, Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
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
    }
}

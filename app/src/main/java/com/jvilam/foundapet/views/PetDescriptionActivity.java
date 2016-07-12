package com.jvilam.foundapet.views;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.jvilam.foundapet.MainActivity;
import com.jvilam.foundapet.R;
import com.jvilam.foundapet.entities.Comment;
import com.jvilam.foundapet.entities.Comments;
import com.jvilam.foundapet.entities.Pet;
import com.jvilam.foundapet.helpers.BaseVolleyActivity;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;


/**
 * Clase que muestra la información de las mascotas
 */
public class PetDescriptionActivity extends BaseVolleyActivity {

    private Pet pet;

    private TextView race;
    private TextView desc;
    private TextView image;
    private ImageView img;
    private Comments petComments;
    private ImageButton comments;

    private int idUser;
    private String userName;

    private Bitmap decodedImg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_description);

        // PUBLICIDAD
        AdBuddiz.setPublisherKey(getResources().getString(R.string.key));
        // TODO Modo de prueba de publicidad. Comentar para subir a Producción
        //AdBuddiz.setTestModeActive();
        AdBuddiz.cacheAds(this);
        AdBuddiz.showAd(this);

        TextView type = (TextView) findViewById(R.id.textViewType);
        race = (TextView) findViewById(R.id.textViewRace);
        desc = (TextView) findViewById(R.id.textViewDescription);
        image = (TextView) findViewById(R.id.textViewLoadingImage);
        img = (ImageView) findViewById(R.id.imageViewPet);
        ImageButton share = (ImageButton) findViewById(R.id.imageButtonShare);
        ImageButton rescued = (ImageButton) findViewById(R.id.imageButtonPetRescued);
        comments = (ImageButton) findViewById(R.id.imageButtonComments);

        if (comments != null) {
            comments.setEnabled(false);
        }
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeLayoutPetDesc);

        getParameters();
        getComments();

        if(idUser != pet.getIdUser()){
            if (layout != null) {
                layout.removeView(rescued);
            }
        }

        if (type != null) {
            type.setText(pet.getType());
        }
        race.setText(pet.getRace());
        desc.setText(pet.getDescription());

        // Listener del botón compartir
        if (share != null) {
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Validación de permisos
                    if (ContextCompat.checkSelfPermission(PetDescriptionActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PetDescriptionActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                    Bitmap b = decodedImg;
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/jpeg");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), b, race.getText().toString(), null);
                    Uri imageUri = Uri.parse(path);
                    share.putExtra(Intent.EXTRA_STREAM, imageUri);
                    share.putExtra(Intent.EXTRA_TEXT, desc.getText().toString());
                    startActivity(Intent.createChooser(share, "Select"));
                }
            });
        }

        // Listener del botón rescatado
        if (rescued != null) {
            rescued.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(PetDescriptionActivity.this);

                    builder.setMessage(R.string.rescue_warning)
                            .setTitle(R.string.attention)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    markPetAsRescued();
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
            });
        }

        // Listener para el botón de comentarios
        if (comments != null) {
            comments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), CommentsActivity.class);
                    intent.putExtra("idUser", idUser);
                    intent.putExtra("userName", userName);
                    intent.putExtra("idPet", pet.getId());
                    intent.putExtra("comments", petComments);
                    startActivity(intent);
                }
            });
        }
    }

    private void getParameters(){
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey("petInfo")) {
                pet = (Pet) parameters.get("petInfo");
            }
            if (parameters.containsKey("idUser")) {
                idUser = (int) parameters.get("idUser");
            }
            if (parameters.containsKey("userName")) {
                userName = (String) parameters.get("userName");
            }
            if(parameters.containsKey("comments")){
                petComments = (Comments) parameters.get("comments");
            }
        }
    }

    /**
     * Método encargado de incluir la imagen recuperada del servidor en la actividad
     * @param imgEncoded La imagen codificada en Base 64
     */
    private void setImage(String imgEncoded){
        byte[] decodedString = Base64.decode(imgEncoded, Base64.DEFAULT);
        decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        if(decodedImg != null){
            img.setImageBitmap(decodedImg);
            image.setText(R.string.image);
        }
    }

    /**
     * Método que se encarga de recuperar la imagen de la mascota
     */
    private void getPetPicture() {
        String registerUrl = getResources().getString(R.string.url_get_pet_image);

        JSONObject params = new JSONObject();
        try {
            params.put("id", pet.getId());
            // Se crea la petición
            JsonArrayRequest jsonObjReq = new JsonArrayRequest(registerUrl, params,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            if(response != null){
                                try {
                                    JSONObject p = response.getJSONObject(0);
                                    setImage(p.getString("IMG"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
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

    private void markPetAsRescued(){
        String registerUrl = getResources().getString(R.string.url_mark_pet_as_rescued);

        JSONObject params = new JSONObject();
        try {
            params.put("id", pet.getId());
            // Se crea la petición
            JsonArrayRequest jsonObjReq = new JsonArrayRequest(registerUrl, params,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            finishActivity();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    finishActivity();
                }
            });
            // Se añade la petición a la cola
            addToQueue(jsonObjReq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getComments() {
        petComments = new Comments();
        String registerUrl = getResources().getString(R.string.url_get_comments);
        JSONObject params = new JSONObject();

        try {
            params.put("idPet", pet.getId());

            // Se crea la petición
            JsonArrayRequest jsonObjReq = new JsonArrayRequest(registerUrl, params,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray com) {
                            // Recuperamos los comentarios y los almacenamos en la variable petComments.
                            for(int i = 0; i < com.length(); i++){
                                try {
                                    Comment singleCom = new Comment();
                                    JSONObject c = com.getJSONObject(i);
                                    singleCom.setComment(c.getString("COMMENT"));
                                    singleCom.setUserName(c.getString("USER_NAME"));
                                    singleCom.setDate(c.getString("COMMENT_DATE"));
                                    singleCom.setId(c.getInt("ID_COMMENT"));
                                    petComments.addComment(singleCom);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            comments.setEnabled(true);
                            getPetPicture();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    comments.setEnabled(true);
                    getPetPicture();
                }
            });
            // Se añade la petición a la cola
            addToQueue(jsonObjReq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void finishActivity(){
        Toast toast = Toast.makeText(getApplicationContext(), R.string.pet_rescued, Toast.LENGTH_SHORT);
        toast.show();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("idUser", idUser);
        intent.putExtra("userName", userName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

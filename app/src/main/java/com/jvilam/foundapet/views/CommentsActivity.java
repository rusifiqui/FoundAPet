package com.jvilam.foundapet.views;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.jvilam.foundapet.MainActivity;
import com.jvilam.foundapet.R;
import com.jvilam.foundapet.entities.Comment;
import com.jvilam.foundapet.entities.Comments;
import com.jvilam.foundapet.helpers.BaseVolleyActivity;
import com.jvilam.foundapet.listTools.CommentAdapter;
import com.jvilam.foundapet.listTools.CommentItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Actividad empleada para mostrar los comentarios de cada animal.
 * @author Enrique Vila
 */
public class CommentsActivity extends BaseVolleyActivity {

    private String userName;
    private int idUser;
    private int idPet;
    private EditText commentText;
    private ProgressDialog progress;
    private Comments petComments;

    private boolean guest = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        getParameters();
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layoutComments);
        Button comment = (Button) findViewById(R.id.buttonNewComment);
        commentText = (EditText) findViewById(R.id.editTextComment);
        ListView lComments = (ListView) findViewById(R.id.listViewComments);
        CommentAdapter cAdapt = new CommentAdapter(this, getComments());
        if (lComments != null) {
            lComments.setAdapter(cAdapt);
        }

        if (lComments != null) {
            lComments.setFocusable(true);
            lComments.requestFocus();
        }

        if(guest){
            if (layout != null) {
                layout.removeView(commentText);
                layout.removeView(comment);
            }
        }

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setTitle(R.string.connect_comment);
        progress.setMessage(getResources().getString(R.string.connect_comment_message));
        progress.setCancelable(false);



        if (comment != null) {
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(commentText.getText().length() > 0){
                        progress.show();
                        sendComment();
                    }else{
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.comment_required, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });
        }
    }

    /**
     * Método para recuperar los parámetros empleados por la actividad.
     */
    private void getParameters(){
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey("idUser")) {
                idUser = (int) parameters.get("idUser");
            }
            if (parameters.containsKey("userName")) {
                userName = (String) parameters.get("userName");
            }
            if (parameters.containsKey("idPet")) {
                idPet = (int) parameters.get("idPet");
            }
            if(parameters.containsKey("comments")){
                petComments = (Comments) parameters.get("comments");
            }
            if(parameters.containsKey("guest")){
                guest = (boolean) parameters.get("guest");
            }
        }
    }

    /**
     * Método que llama al servicio web para almacenar el nuevo comentario
     */
    private void sendComment(){
        String loginUrl = getResources().getString(R.string.url_comment);
        String com = commentText.getText().toString().replaceAll("\n", ".");

        JSONObject params = new JSONObject();
        try {
            params.put("idPet", idPet);
            params.put("idUser", idUser);
            params.put("userName", userName);
            params.put("comment", com);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Se crea la petición
        JsonArrayRequest jsonObjReq = new JsonArrayRequest(loginUrl, params,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progress.dismiss();
                        finishActivity();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                onConnectionFailed(error.toString());
            }
        });

        // Se añade la petición a la cola
        addToQueue(jsonObjReq);
    }

    /**
     * Métofo encargado de añadir los comentarios
     * @return los comentarios
     */
    private ArrayList<CommentItem> getComments(){
        ArrayList<CommentItem> items = new ArrayList<>();
        if(petComments != null && petComments.size() > 0) {
            for (int i = 0; i < petComments.size(); i++) {
                Comment c = petComments.get(i);
                items.add(new CommentItem(getDate(c.getDate()) + " - " + c.getUserName(), c.getComment()));
            }
        }else{
            items.add(new CommentItem(getResources().getString(R.string.no_comments_title), getResources().getString(R.string.no_comments)));
        }
        return items;
    }

    /**
     * Método encargado de finalizar la actividad
     */
    private void finishActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("idUser", idUser);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Método para convertir el formato de la fecha almacenada en BBDD
     * @param date la fecha
     * @return la fecha en formato correcto
     */
    private String getDate(String date){
        String y = date.substring(0, date.indexOf("-"));
        String m = date.substring(date.indexOf("-")+1, date.lastIndexOf("-"));
        String d = date.substring(date.lastIndexOf("-")+1, date.length());
        return d.concat("/").concat(m).concat("/").concat(y);
    }
}

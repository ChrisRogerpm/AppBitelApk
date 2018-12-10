package com.dev.christian.app.Pkg_Fragment;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dev.christian.app.Pkg_Activity.MainActivity;
import com.dev.christian.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SesionFragment extends Fragment implements View.OnClickListener {

    EditText editText_dni, editText_pwd;
    Button button_sesion;

    String URL_LOGIN = MainActivity.HOST + "/api/IniciarSesion";

    public SesionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sesion, container, false);
        editText_dni = (EditText) v.findViewById(R.id.txt_dni);
        editText_pwd = (EditText) v.findViewById(R.id.txt_password);
        button_sesion = (Button) v.findViewById(R.id.btn_iniciar_sesion);

        button_sesion.setOnClickListener(this);
        return v;
    }

    private void IniciarSesion() {

        final String dni = editText_dni.getText().toString().trim();
        final String password = editText_pwd.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        String id_usuario = "";
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            id_usuario = jsonResponse.getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if(id_usuario.isEmpty()){
                            Toast.makeText(getActivity(), "Los datos ingresados no coinciden con nuestros registros, vuelva a intentarlo", Toast.LENGTH_SHORT).show();
                        }else{
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("InicioSesion", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("Logeado", true);
                            editor.putString("id_usuario", id_usuario);
                            editor.apply();
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), error+"", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("dni", dni);
                params.put("password", password);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    @Override
    public void onClick(View v) {
        if(v == button_sesion){
            IniciarSesion();
        }
    }
}

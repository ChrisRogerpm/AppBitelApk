package com.dev.christian.app.Pkg_Fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
public class ProfileFragment extends Fragment implements View.OnClickListener {

    Button button_cerrar_sesion, button_resetear;
    String URL_RESET = MainActivity.HOST + "/api/ResetearPassword";

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        button_cerrar_sesion = (Button) v.findViewById(R.id.btn_logout);
        button_resetear = (Button) v.findViewById(R.id.btnResetarPassword);
        button_cerrar_sesion.setOnClickListener(this);
        button_resetear.setOnClickListener(this);
        return v;
    }

    public void CerrarSesion() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Esta usted seguro de cerrar sesión?");
        alertDialogBuilder.setPositiveButton("Si",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        SharedPreferences preferences = getActivity().getSharedPreferences("InicioSesion", Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = preferences.edit();

                        editor.putBoolean("Logeado", false);

                        editor.putString("id_usuario", "");

                        editor.apply();

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        //Showing the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void ResetearPasswordConfirm() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Esta usted seguro de Resetear su contraseña?");
        alertDialogBuilder.setPositiveButton("Si",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        int usuario_id = ObtenerUsuarioLogeadoId();
                        ResetearPassword(usuario_id);
                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        //Showing the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void ResetearPassword(final int id_usuario) {

        final String usuario_id = String.valueOf(id_usuario);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_RESET,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase("success")) {
                            SharedPreferences preferences = getActivity().getSharedPreferences("InicioSesion", Context.MODE_PRIVATE);

                            SharedPreferences.Editor editor = preferences.edit();

                            editor.putBoolean("Logeado", false);

                            editor.putString("id_usuario", "");

                            editor.apply();

                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), error + "", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("usuario_id", usuario_id);
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

    public int ObtenerUsuarioLogeadoId() {
        int id = 0;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("InicioSesion", Context.MODE_PRIVATE);
        id = Integer.parseInt(sharedPreferences.getString("id_usuario", "0"));
        return id;
    }

    @Override
    public void onClick(View v) {
        if (v == button_cerrar_sesion) {
            CerrarSesion();
        }
        if (v == button_resetear) {
            ResetearPasswordConfirm();
        }
    }
}

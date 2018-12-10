package com.dev.christian.app.Pkg_Fragment;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dev.christian.app.Pkg_Activity.MainActivity;
import com.dev.christian.app.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArchivoFragment extends Fragment implements View.OnClickListener, LocationListener {

    EditText editText_nombre_archivo;
    Button btnGenarar, btnGPS;
    public double gps_latitud;
    public double gps_longitud;
    private LocationManager locationManager;
    private LocationListener listener;
    ProgressDialog progressDialog;
    String URL_GENERAR_ARCHIVO = MainActivity.HOST + "/api/GenerarGPS";

    public ArchivoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_archivo, container, false);

        GPSEnable();

        editText_nombre_archivo = (EditText) v.findViewById(R.id.txtNombreArchivo);
        btnGenarar = (Button) v.findViewById(R.id.btnArchivo_GPS);
        btnGPS = (Button) v.findViewById(R.id.btnGPS);

        btnGenarar.setEnabled(false);
        btnGenarar.setOnClickListener(this);
        btnGPS.setOnClickListener(this);
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        ConfigurarPermisosSDK23_GPS();

        return v;
    }

    public void GPSEnable() {
        try {
            int gpsSignal = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);

            if (gpsSignal == 0) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void ConfigurarPermisosSDK23_GPS() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
        }
    }


    private void GenerarArchivoGPS(final String IdUsuario, final String NombreArchivo, final String LatitudGPS, final String LongitudGPS) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Generando Archivo GPS");
        progressDialog.setMessage("Cargando...");
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_GENERAR_ARCHIVO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        boolean respuesta = false;
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            respuesta = jsonResponse.getBoolean("respuesta");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (respuesta) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(getActivity(), "Se ha generado el archivo con exito", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Ah ocurrido un error inesperado, Mensaje Servidor", Toast.LENGTH_SHORT).show();
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
                params.put("Id", IdUsuario);
                params.put("NombreArchivo", NombreArchivo);
                params.put("Latitud", LatitudGPS);
                params.put("Longitud", LongitudGPS);
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
        requestQueue.add(stringRequest);
    }

    public int ObtenerUsuarioLogeadoId() {
        int id = 0;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("InicioSesion", Context.MODE_PRIVATE);
        id = Integer.parseInt(sharedPreferences.getString("id_usuario", "0"));
        return id;
    }

    public void ConfirmacionEnvio() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Esta seguro de generar el archivo GPS?");
        alertDialogBuilder.setPositiveButton("Si",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String idusu = String.valueOf(ObtenerUsuarioLogeadoId());
                        String NombreArchivo = editText_nombre_archivo.getText().toString();
                        String Latitud = String.valueOf(gps_latitud);
                        String Longitud = String.valueOf(gps_longitud);
                        GenerarArchivoGPS(idusu, NombreArchivo, Latitud, Longitud);
                        //Toast.makeText(getActivity(), idusu + " - " + NombreArchivo + " - " + Latitud + Longitud, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onLocationChanged(Location location) {
        gps_latitud = location.getLatitude();
        gps_longitud = location.getLongitude();
        progressDialog.dismiss();
        btnGenarar.setEnabled(true);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onClick(View view) {
        if (view == btnGenarar) {
            ConfirmacionEnvio();
        } else if (view == btnGPS) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Obteniendo ubicación GPS");
            progressDialog.setMessage("Cargando...");
            progressDialog.show();
        }
    }
}

package com.dev.christian.app.Pkg_Fragment;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;
import static android.support.v4.provider.FontsContractCompat.FontRequestCallback.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScannerFragment extends Fragment implements View.OnClickListener,LocationListener {

    private Button btn_scanner, btn_cargar, btn_img;
    private EditText editText_sucursal,editText_monto;
    private CheckBox checkBox_imagen;
    private Bitmap bitmap;
    private Uri filePath;
    String contenido_qr, formato_qr;
    String imagen = "";
    public double gps_latitud;
    public double gps_longitud;
    private LocationManager locationManager;
    private LocationListener listener;
    ProgressDialog progressDialog;
    private int PICK_IMAGE_REQUEST = 1;
    private String URL_RECARGA = MainActivity.HOST+"/api/EnviarPago";

    public ScannerFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_scanner, container, false);

        GPSEnable();

        btn_scanner = (Button) v.findViewById(R.id.btn_escaner);
        btn_cargar = (Button) v.findViewById(R.id.btn_recargar);
        btn_img = (Button) v.findViewById(R.id.btn_imagen);
        checkBox_imagen = (CheckBox) v.findViewById(R.id.chk_imagen);
        editText_monto = (EditText) v.findViewById(R.id.txt_monto_saldo);
        editText_sucursal = (EditText) v.findViewById(R.id.txt_sucursal);

        btn_scanner.setOnClickListener(this);
        btn_cargar.setOnClickListener(this);
        btn_img.setOnClickListener(this);
        checkBox_imagen.setEnabled(false);
        editText_sucursal.setEnabled(false);
        btn_cargar.setEnabled(false);

        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        ConfigurarPermisosSDK23_GPS();

        return v;
    }

    public void GPSEnable(){
        try {
            int gpsSignal = Settings.Secure.getInt(getActivity().getContentResolver(),Settings.Secure.LOCATION_MODE);

            if (gpsSignal == 0){
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        GPSEnable();
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

    private void MostrarVentanaImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                imagen = ObtenerStringImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            checkBox_imagen.setChecked(true);
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_CANCELED) {
            checkBox_imagen.setChecked(false);
            imagen = "";
        }

        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            contenido_qr = scanningResult.getContents();
            formato_qr = scanningResult.getFormatName();
            editText_sucursal.setText(ObtenerSucursalId(contenido_qr));
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,this);

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Obteniendo ubicación GPS");
            progressDialog.setMessage("Cargando...");
            progressDialog.show();
            //progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        }
        if(requestCode == IntentIntegrator.REQUEST_CODE && resultCode == Activity.RESULT_CANCELED){

        }
    }

    public String ObtenerStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public String  ObtenerSucursalId(String sucursal_id){
        String[] id_sucursal = sucursal_id.split("-");
        return id_sucursal[1];
    }

    public boolean SesionActiva() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("InicioSesion", Context.MODE_PRIVATE);
        boolean loggin = false;
        loggin = sharedPreferences.getBoolean("Logeado", false);
        return loggin;
    }

    public int ObtenerUsuarioLogeadoId(){
        int id = 0;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("InicioSesion", Context.MODE_PRIVATE);
        id = Integer.parseInt(sharedPreferences.getString("id_usuario","0"));
        return id;
    }

    @Override
    public void onClick(View v) {
        if (v == btn_scanner) {
            if (SesionActiva()){
                IntentIntegrator integrator = new IntentIntegrator(this.getActivity()).forSupportFragment(this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setCameraId(0);
                integrator.initiateScan();
            }else{
                Toast.makeText(getActivity(), "Inicie Sesión antes de realizar una acción", Toast.LENGTH_SHORT).show();
            }
        } else if (v == btn_cargar) {
            if (SesionActiva()){
                ConfirmacionEnvio();
            }else{
                Toast.makeText(getActivity(), "Inicie Sesión antes de realizar una acción", Toast.LENGTH_SHORT).show();
            }
        }else if(v == btn_img){
            if (SesionActiva()){
                MostrarVentanaImage();
            }else{
                Toast.makeText(getActivity(), "Inicie Sesión antes de realizar una acción", Toast.LENGTH_SHORT).show();
            }

        }

    }

    public void ConfirmacionEnvio(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Esta usted seguro de Enviar el Pago?");
        alertDialogBuilder.setPositiveButton("Si",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        int idusu = ObtenerUsuarioLogeadoId();
                        double monto_ = Double.parseDouble(editText_monto.getText().toString());
                        String id_sucursal_ = ObtenerSucursalId(contenido_qr);
                        EnviarDatoWebServices(idusu,monto_,gps_latitud,gps_longitud,id_sucursal_,imagen);
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

    private void EnviarDatoWebServices(final int usuario_id,final double monto, final double latitud, final double longitud, final String id_sucursal, final String foto_adjunta) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_RECARGA,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("success")){
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            Toast.makeText(getActivity(), "Se ha subido con exito", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getActivity(), "Ah ocurrido un error inesperado, revise nuevamente", Toast.LENGTH_SHORT).show();
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
                params.put("id_usuario", String.valueOf(usuario_id));
                params.put("monto", String.valueOf(monto));
                params.put("latitud", String.valueOf(latitud));
                params.put("longitud", String.valueOf(longitud));
                params.put("sucursal_id", String.valueOf(id_sucursal));
                params.put("foto",String.valueOf(foto_adjunta));
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
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
//                10000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }


    @Override
    public void onLocationChanged(Location location) {
        if (btn_cargar.isEnabled()){
            gps_latitud = location.getLatitude();
            gps_longitud = location.getLongitude();
            progressDialog.dismiss();
        }else{
            gps_latitud = location.getLatitude();
            gps_longitud = location.getLongitude();
            btn_cargar.setEnabled(true);
            progressDialog.dismiss();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

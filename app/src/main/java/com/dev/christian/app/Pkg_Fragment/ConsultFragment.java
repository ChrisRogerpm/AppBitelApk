package com.dev.christian.app.Pkg_Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
public class ConsultFragment extends Fragment implements View.OnClickListener {


    EditText editText_codigo;
    TextView textViewNombre, textViewSaldo, textViewFecha;
    Button button_consultar;
    public String URL_CONSULTA = MainActivity.HOST + "/api/BuscarCodigoPDV";

    public ConsultFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_consult, container, false);

        editText_codigo = (EditText) v.findViewById(R.id.txtCodigopdv);
        button_consultar = (Button) v.findViewById(R.id.btnConsultarCodigo);
        textViewNombre = (TextView) v.findViewById(R.id.txtpdv);
        textViewSaldo = (TextView) v.findViewById(R.id.txtsaldo);
        textViewFecha = (TextView) v.findViewById(R.id.txtfecha);
        button_consultar.setOnClickListener(this);
        return v;
    }

    public int ObtenerUsuarioLogeadoId() {
        int id = 0;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("InicioSesion", Context.MODE_PRIVATE);
        id = Integer.parseInt(sharedPreferences.getString("id_usuario", "0"));
        return id;
    }

    private void BuscarPDV() {

        final String codigpdv = editText_codigo.getText().toString().trim();
        final String IdUsuario = String.valueOf(ObtenerUsuarioLogeadoId());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CONSULTA,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        

                        String Nombre_PDV;
                        String Saldo;
                        String Fecha;
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            Nombre_PDV = jsonResponse.getString("nombre_punto_venta");
                            Saldo = jsonResponse.getString("recarga");
                            Fecha = jsonResponse.getString("updated_at");
                            textViewNombre.setText(Nombre_PDV);
                            textViewSaldo.setText(Saldo);
                            textViewFecha.setText(Fecha);
                        } catch (JSONException e) {
                            e.printStackTrace();
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
                params.put("codigo_pdv", codigpdv);
                params.put("IdUsuario", IdUsuario);
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
        if (v == button_consultar) {
            BuscarPDV();
        }

    }
}

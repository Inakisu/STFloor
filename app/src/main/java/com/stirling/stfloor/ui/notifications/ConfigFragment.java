package com.stirling.stfloor.ui.notifications;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;


import com.stirling.stfloor.R;
import com.stirling.stfloor.Utils.Constants;
import com.stirling.stfloor.Utils.ElasticSearchAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Credentials;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class ConfigFragment extends Fragment {

    private Retrofit retrofit;
    private ElasticSearchAPI searchAPI;
    private String macDispositivo;
    private String mElasticSearchPassword = Constants.elasticPassword;
    private String queryJson = "";
    public  boolean detener = false;
    private JSONObject jsonObject;
    private Spinner spinnerDispConfiguracion;


    private Button botonGuardar;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_config, container, false);
        /*final TextView textView = root.findViewById(R.id.text_notifications);
        notificationsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        return root;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstaceState) {
        super.onViewCreated(view, savedInstaceState);

        //Inicializamos la API de Elasticsearch
        inicializarAPI();

        //Inicializamos variables
        botonGuardar = (Button) view.findViewById(R.id.botonGuardar);
        spinnerDispConfiguracion = (Spinner) view.findViewById(R.id.spinnerDispConf);

        /*spinnerDispConfiguracion.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Obtenemos la dirección MAC del dispositivo que ha seleccionado
                //en el spinner
            }
        });*/
    }

    /**
     * Este método recoge los parámetros de configuración establecidos y los envía
     */
    public void enviarConfiguracion(){
        HashMap<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Authorization", Credentials.basic("android",
                mElasticSearchPassword));
        try {
            queryJson = "{\n" +
                    "  \"idMac\":\""+ macA +"\",\n" +
                    "  \"nombreHab\":\""+ macA +"\",\n" +
                    "  \"correousu\":\""+ correo +"\",\n" +
                    "  \"dueno\":true\n" +
                    "}";
            jsonObject = new JSONObject(queryJson);
        }catch (JSONException jerr){
            Log.d("Error: ", jerr.toString());
        }
        //Creamos el body con el JSON
        RequestBody body = RequestBody.create(okhttp3.MediaType
                .parse("application/json; charset=utf-8"),(jsonObject.toString()));
        //Realizamos la llamada mediante la API
        Call<RespuestaU> call = searchAPI.postCazuela(headerMap, body);
        call.enqueue(new Callback<RespuestaU>() {
            @Override
            public void onResponse(Call<RespuestaU> call, Response<RespuestaU> response) {
                String jsonResponse = "";
                try{
                    Log.d(TAG, "onResponse addcazuela: server response: " +
                            response.toString());
                    //Si la respuesta es satisfactoria
                    if(response.isSuccessful()){
                        Log.d(TAG, "repsonseBody addcazuela: "+ response.body().toString());
                        Log.d(TAG, " --onResponse addcazuela: la response: "+response.body()
                                .toString());
                    }else{
                        jsonResponse = response.errorBody().string(); //error response body
                    }
                    Log.d(TAG, "onResponse add cazuela: ok ");

                }catch (NullPointerException e){
                    Log.e(TAG, "onResponse addcazuela: NullPointerException: " + e.getMessage() );
                }
                catch (IndexOutOfBoundsException e){
                    Log.e(TAG, "onResponse addcazuela: IndexOutOfBoundsException: " +
                            e.getMessage() );
                }
                catch (IOException e){
                    Log.e(TAG, "onResponse addcazuela: IOException: " + e.getMessage() );
                }
            }

            @Override
            public void onFailure(Call<RespuestaU> call, Throwable t) {

            }
        });
    }

    private void inicializarAPI(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_ELASTICSEARCH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        searchAPI = retrofit.create(ElasticSearchAPI.class);

    }
}
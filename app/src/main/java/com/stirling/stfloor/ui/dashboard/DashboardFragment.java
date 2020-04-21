package com.stirling.stfloor.ui.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.stirling.stfloor.BluetoothActivity;
import com.stirling.stfloor.Models.HitsLists.HitsListD;
import com.stirling.stfloor.Models.HitsObjects.HitsObjectD;
import com.stirling.stfloor.Models.POJOs.Dispositivo;
import com.stirling.stfloor.Models.gson2pojo.Aggregations;
import com.stirling.stfloor.Models.gson2pojo.Example;
import com.stirling.stfloor.Models.gson2pojo.Hit;
import com.stirling.stfloor.Models.gson2pojo.Hits;
import com.stirling.stfloor.Models.gson2pojo.MyAgg;
import com.stirling.stfloor.R;
import com.stirling.stfloor.Utils.Constants;
import com.stirling.stfloor.Utils.ElasticSearchAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Credentials;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class DashboardFragment extends Fragment {

    private Retrofit retrofit;
    private ElasticSearchAPI searchAPI;
    private String macDispositivo;
    private String mElasticSearchPassword = Constants.elasticPassword;
    private String queryJson = "";
    public  boolean detener;
    private JSONObject jsonObject;
    private FloatingActionButton btnAnadirDispositivo;
    private FloatingActionButton btnPrueba;

    private ArrayAdapter<String> spinnerAdapter;
    private ArrayList<Dispositivo> mDispositivo; // Lista donde se almacenarán las respuestas de la query de las cazuelas
    private ArrayList<String> nombreDisps;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);

        return root;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstaceState){
        super.onViewCreated(view, savedInstaceState);

        //Inicializamos la API de Elasticsearch
        inicializarAPI();

        //Inicializamos variables

        //Obtenemos la lista de dispositivos y la guardamos en SharedPreferences
        actualizarListaDispositivos();

        //Extraemos de la lista de dispositivos los nombres de éstos y los introducimos en una lista
        /*for(int i = 0; i<mDispositivo.size(); i++){
            String nombreDisp = mDispositivo.get(i).getNombreHab(); //todo
        }*/
        //Introducimos datos de la lista obtenida en el spinner
        spinnerAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_item,
                nombreDisps);

        //Proceso para actualizar información cada segundo
        /*final Handler handler = new Handler();
        new Runnable(){
            @Override
            public void run(){
                if(!detener){
                    actualizarValores(macDispositivo);
                }
            }
        };*/

        btnPrueba = (FloatingActionButton) view.findViewById(R.id.botonPrueba);
        btnAnadirDispositivo = (FloatingActionButton) view.findViewById(R.id.anadirDispFloatingButton);
        //listener para el botón Añadir Dispositivo, nueva activity

        btnAnadirDispositivo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Abrimos activity de búsqueda de dispositivos BLE para sincronización
                Intent intent = new Intent(getActivity(), BluetoothActivity.class);
                startActivity(intent);
            }
        });
        btnPrueba.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Cambiar a fragment visualización

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

    private void actualizarValores(String mac){
        //Generamos un authentication header para identificarnos contra Elasticsearch
        HashMap<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Authorization", Credentials.basic("android",
                mElasticSearchPassword));
        try{
            queryJson = "{\n" +
                    "  \"query\":{ \n" +
                    "    \"bool\":{\n" +
                    "      \"must\": [\n" +
                    "        {\"match\": {\n" +
                    "          \"idMac\": \"" + mac + "\"\n" +
                    "          }\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"aggs\": {\n" +
                    "    \"myAgg\": {\n" +
                    "      \"top_hits\": {\n" +
                    "        \"size\": 2,\n" +
                    "        \"sort\": [\n" +
                    "          {\n" +
                    "            \"timestamp\":{\n" +
                    "              \"order\": \"desc\"\n" +
                    "            }\n" +
                    "          }]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            jsonObject = new JSONObject(queryJson);
        }catch (JSONException jerr){
            Log.d("Error: ", jerr.toString());
        }
        // Creamos el Body con el JSON
        RequestBody body = RequestBody.create(okhttp3.MediaType.
                parse("application/json; charset=utf-8"), (jsonObject.toString()));
        //Realizamos la llamada mediante la API
        Call<Example> call= searchAPI.searchMedicion(headerMap, body);
        call.enqueue(new Callback<Example>(){
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                Example example;
                Aggregations aggregations;
                MyAgg myAgg;
                Hits hits = new Hits();
                Hit hit = new Hit();
                String jsonResponse = "";
                try{
                    Log.d(TAG, "onResponse: server response: " + response.toString());
                    //Si la respuesta es satisfactoria
                    if(response.isSuccessful()){
                        Log.d(TAG, "repsonseBody: "+ response.body().toString());
                        example = response.body();
                        aggregations = example.getAggregations();
                        myAgg = aggregations.getMyAgg();
                        hits = myAgg.getHits();
                        Log.d(TAG, " -----------onResponse: la response: "+response.body()
                                .toString());
                    }else{
                        jsonResponse = response.errorBody().string(); //error response body
                    }
                    Log.d(TAG, "onResponse: hits: " + hits.getHits().toString());
                    for(int i = 0; i < hits.getHits().size(); i++){                                 // esto por qué está aquí?
                        Log.d(TAG, "onResponse: data: " + hits.getHits().get(i)
                                .getSource().toString());
//                        mMedicion.add(hits.getHits().get(i).getSource());                           //añadimos último valor al array de mediciones anteriores
                    }

                }catch (NullPointerException e){
                    Log.e(TAG, "onResponse: NullPointerException: " + e.getMessage() );
                }
                catch (IndexOutOfBoundsException e){
                    Log.e(TAG, "onResponse: IndexOutOfBoundsException: " + e.getMessage() );
                }
                catch (IOException e){
                    Log.e(TAG, "onResponse: IOException: " + e.getMessage() );
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {

            }

        });

    }
    private void actualizarListaDispositivos(){
        //Generamos un authentication header para identificarnos contra Elasticsearch
        HashMap<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Authorization", Credentials.basic("android",
                mElasticSearchPassword));
        try{
            queryJson = "{\n" +
                        "  \"query\": {\n" +
                        "    \"bool\": {\n" +
                        "      \"must\": [\n" +
                        "        {\"match_all\": {\n" +
                        "        }}\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  }" +
                        "}";
            jsonObject = new JSONObject(queryJson);
        }catch (JSONException jerr){
            Log.d("Error: ", jerr.toString());
        }
        // Creamos el Body con el JSON
        RequestBody body = RequestBody.create(okhttp3.MediaType.
                parse("application/json; charset=utf-8"), (jsonObject.toString()));
        //Realizamos la llamada mediante la API
        Call<HitsObjectD> call= searchAPI.searchDispositivo(headerMap, body);
        call.enqueue(new Callback<HitsObjectD>() {
            @Override
            public void onResponse(Call<HitsObjectD> call, Response<HitsObjectD> response) {
                Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();//prueba
                HitsListD hitsListD = new HitsListD();
                String jsonResponse = "";
                try{
                    Log.d(TAG, "onResponse: server response: " + response.toString());
                    //Si la respuesta es satisfactoria
                    if(response.isSuccessful()){
                        Log.d(TAG, "repsonseBody: "+ response.body().toString());
                        hitsListD = response.body().getHits();
                        Log.d(TAG, " -----------onResponse: la response: "+response.body()
                                .toString());
                    }else{
                        jsonResponse = response.errorBody().string(); //error response body
                        Log.e("ErrES:", jsonResponse);
                    }
                    for(int i = 0; i < hitsListD.getDispositivoIndex().size(); i++){
                        Log.d(TAG, "onResponse: data: " + hitsListD.getDispositivoIndex().get(i)
                                .getDispositivo().toString());
                        mDispositivo.add(hitsListD.getDispositivoIndex().get(i).getDispositivo());
                    }
                    saveArrayList(mDispositivo, "navprefs");

                }catch (NullPointerException e){
                    Log.e(TAG, "onResponse: NullPointerException: " + e.getMessage() );
                }
                catch (IndexOutOfBoundsException e){
                    Log.e(TAG, "onResponse: IndexOutOfBoundsException: " + e.getMessage() );
                }
                catch (IOException e){
                    Log.e(TAG, "onResponse: IOException: " + e.getMessage() );
                }
            }

            @Override
            public void onFailure(Call<HitsObjectD> call, Throwable t) {
                Toast.makeText(getContext(), "Failure", Toast.LENGTH_SHORT).show();//prueba
                Log.e("onFailure: ", t.toString());
            }

        });
    }
    public void saveArrayList(ArrayList<Dispositivo> list, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }
}
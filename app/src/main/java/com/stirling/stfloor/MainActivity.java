package com.stirling.stfloor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.gson.Gson;
import com.stirling.stfloor.Models.HitsLists.HitsListD;
import com.stirling.stfloor.Models.HitsObjects.HitsObjectD;
import com.stirling.stfloor.Models.POJOs.Dispositivo;
import com.stirling.stfloor.Utils.Constants;
import com.stirling.stfloor.Utils.ElasticSearchAPI;
import com.stirling.stfloor.ui.dashboard.DashboardFragment;
import com.stirling.stfloor.ui.home.HomeFragment;
import com.stirling.stfloor.ui.visualizacion.VisualizacionFragment;

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


public class MainActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private ElasticSearchAPI searchAPI;
    private String macDispositivo;
    private String mElasticSearchPassword = Constants.elasticPassword;
    private String queryJson = "";
    private JSONObject jsonObject;

    private ArrayList<Dispositivo> mDispositivo; // Lista donde se almacenarán las respuestas de la query de las cazuelas

    private DashboardFragment dashboardFragment;
    private HomeFragment homeFragment;
    private VisualizacionFragment visualizacionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //no controlo el navcontroller
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        //Inicializamos la API de Elasticsearch
        /*inicializarAPI();

        //Inicializamos variables
        mDispositivo = new ArrayList<Dispositivo>(); //Lista de dispositivos que hay en la BD

        //Obtenemos lista de dispositivos
        obtenerDispositivos();*/

    }

    /**
     * Obtenemos listado de dispositivos desde la BD para guardarlos en arrayList
     */
    /*public void obtenerDispositivos(){
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
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();//prueba
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
                    //borramos para evitar duplicados de peticiones anteriores
                    mDispositivo.clear();
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
                Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_SHORT).show();//prueba
                Log.e("onFailure: ", t.toString());
            }

        });
    }*/

    /**
     * Método para guardar en SharedPreferences un Arraylist
     * En este caso, se utilizará para almacenar la lista de dispositivos obtenidos
     * @param list
     * @param key
     */
    /*public void saveArrayList(ArrayList<Dispositivo> list, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        //eliminamos datos anteriores
        editor.clear().commit();
        //añadimos
        editor.putString(key, json);
        editor.apply();
    }*/

    /**
     * Se inicializa la API de Elasticsearch en la URL especificada
     */
    /*private void inicializarAPI(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_ELASTICSEARCH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        searchAPI = retrofit.create(ElasticSearchAPI.class);
    }*/

}

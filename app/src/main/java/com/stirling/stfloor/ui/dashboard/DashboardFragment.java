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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.HeatDataEntry;
import com.anychart.charts.HeatMap;
import com.anychart.enums.SelectionMode;
import com.anychart.graphics.vector.SolidFill;
import com.anychart.palettes.RangeColors;
import com.anychart.scales.LinearColor;
import com.google.gson.Gson;
import com.stirling.stfloor.BluetoothActivity;
import com.stirling.stfloor.MainActivity;
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
import java.util.List;

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
    public  boolean detener = false;
    private JSONObject jsonObject;
    private FloatingActionButton btnAnadirDispositivo;
    private FloatingActionButton btnPrueba;
    private Spinner spinnerDispositivos;
    private boolean primeraVez = true;
    private Dispositivo[] dispositivo;

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
        mDispositivo = new ArrayList<Dispositivo>(); //Lista de dispositivos que hay en la BD
        nombreDisps = new ArrayList<>();
        spinnerDispositivos = (Spinner) view.findViewById(R.id.spinnerDispDash);

        //Obtener lista de dispositivos desde sharedPreferences

        //Inicializamos anychartview
        AnyChartView anyChartView = view.findViewById(R.id.any_chart_view);
        //anyChartView.setProgressBar(view.findViewById());
        HeatMap tempMap = AnyChart.heatMap();
        tempMap.stroke("1 #fff");
        tempMap.hovered()
                .stroke("6 #fff")
                .fill(new SolidFill("#545f69", 1d))
                .labels("{ fontColor: '#fff' }");

        tempMap.interactivity().selectionMode(SelectionMode.NONE);

        tempMap.title().enabled(true);
        tempMap.title()
                .text("Risk Matrix in Project Server")
                .padding(0d, 0d, 20d, 0d);

        tempMap.labels().enabled(true);
        tempMap.labels()
                .minFontSize(14d)
                .format("function() {\n" +
                        "      var namesList = [\"Low\", \"Medium\", \"High\", \"Extreme\"];\n" +
                        "      return namesList[this.heat];\n" +
                        "    }");

        tempMap.yAxis(0).stroke(null);
        tempMap.yAxis(0).labels().padding(0d, 15d, 0d, 0d);
        tempMap.yAxis(0).ticks(false);
        tempMap.xAxis(0).stroke(null);
        tempMap.xAxis(0).ticks(false);

        tempMap.tooltip().title().useHtml(true);
        tempMap.tooltip()
                .useHtml(true)
                .titleFormat("function() {\n" +
                        "      var namesList = [\"Low\", \"Medium\", \"High\", \"Extreme\"];\n" +
                        "      return '<b>' + namesList[this.heat] + '</b> Residual Risk';\n" +
                        "    }")
                .format("function () {\n" +
                        "       return '<span style=\"color: #CECECE\">Likelihood: </span>' + this.x + '<br/>' +\n" +
                        "           '<span style=\"color: #CECECE\">Consequence: </span>' + this.y;\n" +
                        "   }");

        List<DataEntry> data = new ArrayList<>();
       /* data.add(new CustomHeatDataEntry("Rare", "Insignificant", 0, "#90caf9"));
        data.add(new CustomHeatDataEntry("Rare", "Minor", 0, "#90caf9"));
        data.add(new CustomHeatDataEntry("Rare", "Moderate", 0, "#90caf9"));
        data.add(new CustomHeatDataEntry("Rare", "Major", 0, "#90caf9"));
        data.add(new CustomHeatDataEntry("Rare", "Extreme", 0, "#90caf9"));
        data.add(new CustomHeatDataEntry("Unlikely", "Insignificant", 0, "#90caf9"));
        data.add(new CustomHeatDataEntry("Unlikely", "Minor", 0, "#90caf9"));
        data.add(new CustomHeatDataEntry("Unlikely", "Moderate", 0, "#90caf9"));
        data.add(new CustomHeatDataEntry("Unlikely", "Major", 1, "#ffb74d"));
        data.add(new CustomHeatDataEntry("Unlikely", "Extreme", 1, "#ffb74d"));
        data.add(new CustomHeatDataEntry("Possible", "Insignificant", 0, "#90caf9"));
        data.add(new CustomHeatDataEntry("Possible", "Minor", 0, "#90caf9"));
        data.add(new CustomHeatDataEntry("Possible", "Moderate", 1, "#ffb74d"));
        data.add(new CustomHeatDataEntry("Possible", "Major", 1, "#ffb74d"));
        data.add(new CustomHeatDataEntry("Possible", "Extreme", 1, "#ffb74d"));
        data.add(new CustomHeatDataEntry("Likely", "Insignificant", 0, "#90caf9"));
        data.add(new CustomHeatDataEntry("Likely", "Minor", 1, "#ffb74d"));
        data.add(new CustomHeatDataEntry("Likely", "Moderate", 1, "#ffb74d"));
        data.add(new CustomHeatDataEntry("Likely", "Major", 2, "#ef6c00"));
        data.add(new CustomHeatDataEntry("Likely", "Extreme", 2, "#ef6c00"));
        data.add(new CustomHeatDataEntry("Almost\\nCertain", "Insignificant", 0, "#90caf9"));
        data.add(new CustomHeatDataEntry("Almost\\nCertain", "Minor", 1, "#ffb74d"));
        data.add(new CustomHeatDataEntry("Almost\\nCertain", "Moderate", 1, "#ffb74d"));
        data.add(new CustomHeatDataEntry("Almost\\nCertain", "Major", 2, "#ef6c00"));
        data.add(new CustomHeatDataEntry("Almost\\nCertain", "Extreme", 3, "#d84315"));*/

//        data.add(new CustomHeatDataEntry("1", "1", 20, "#90caf9"));
//        data.add(new CustomHeatDataEntry("1", "2", 21 ,"#90caf9"));
//        data.add(new CustomHeatDataEntry("2", "1", 19,"#ffb74d"));
//        data.add(new CustomHeatDataEntry("2", "2", 22 ,"#90caf9"));

        data.add(new CustomHeatDataEntry("1", "1", 30));
        data.add(new CustomHeatDataEntry("1", "2", 21));
        data.add(new CustomHeatDataEntry("2", "1", 19));
        data.add(new CustomHeatDataEntry("2", "2", 22));
        data.add(new CustomHeatDataEntry("3", "1", 25));
        data.add(new CustomHeatDataEntry("3", "2", 29));

        LinearColor colorTemps = LinearColor.instantiate();
        String[] colores = new String[2];
        colores[0] = "#00ccff";
        colores[1] = "#ffcc00";
        colorTemps.colors(colores);

        //asignamos los datos a la tabla
        tempMap.data(data);
        //asignamos la escala de color lineal a la tabla
        tempMap.colorScale(String.valueOf(colorTemps));

        anyChartView.setChart(tempMap);

        spinnerDispositivos.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Se obtiene la posición del elemento seleccionado en el spinner
                int pos = spinnerDispositivos.getSelectedItemPosition();
                //Obtenemos la dirección MAC del dispositivo correspondiente a esa posición
                //en el arrayList de dispositivos obtenidos.
                macDispositivo = dispositivo[pos].getIdMac();
                Log.d("Dash: ", "Dir. MAC del disp. selecc.: " + macDispositivo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        //Proceso para actualizar información cada segundo en segundo plano
        final Handler handler = new Handler();
        new Runnable(){
            @Override
            public void run(){
                if(!detener){
                    //actualizarValores(macDispositivo);
                }
            }
        };

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

    @Override
    public void onPause() {
        super.onPause();
//        detener = true;
//        mCazuela.clear();
        primeraVez = true;
    }
    @Override
    public void onResume() {
        super.onResume();
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.detach(this).attach(this).commit();
        if(primeraVez){ //para que al añadir un nuevo dispositivo desde otra activity se actualice
//            Log.e("ACT", "===================22222222222222===============");
            obtenerDispsSharedPrefs();
            actualizarListaDispositivos();
        }
        detener = false;
    }

    /**
     * Método para entrada datos al heatmap de AnyChart
     */
    private class CustomHeatDataEntry extends HeatDataEntry {
        CustomHeatDataEntry(String x, String y, Integer heat){//, String fill) {
            super(x, y, heat);
            //setValue("fill", fill);
        }
    }

    private void inicializarAPI(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_ELASTICSEARCH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        searchAPI = retrofit.create(ElasticSearchAPI.class);
    }

    /**
     * Ejecuta el método de MainActivity que solicita la lista de dispositivos a la base de datos
     */
    private void actualizarLista(){
        ((MainActivity) getActivity()).obtenerDispositivos();
    }

    /**
     * Método que obtiene la lista de dispositivos desde SharedPreferences
     */
    private void obtenerDispsSharedPrefs(){ //Estoy hay que probarlo muy fuerte en el debugger
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String jsonString = prefs.getString("navprefs", null);
        Gson gson = new Gson();
        //A un array
        dispositivo = gson.fromJson( jsonString, Dispositivo[].class );
        System.out.println("Obt SPrefs Conf: " + gson.toJson( dispositivo ) );
        for(int j = 0; j<dispositivo.length; j++){
            String no = dispositivo[j].getNombreHab();
            nombreDisps.add(no);
            System.out.println("Nombre hab prueba: " + no);
        }
        //Introducimos datos de la lista obtenida en el spinner
        spinnerAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_item,
                nombreDisps);
        //Asignamos al spinner el adapter
        spinnerDispositivos.setAdapter(spinnerAdapter);
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
        /*//Generamos un authentication header para identificarnos contra Elasticsearch
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

                    //Extraemos de la lista de dispositivos los nombres de éstos y los introducimos en una lista
                    nombreDisps = new ArrayList<String>();
                    for(int i = 0; i<mDispositivo.size(); i++){
                        nombreDisps.add(mDispositivo.get(i).getNombreHab()); //todo
                    }*/
                    //Introducimos datos de la lista obtenida en el spinner
                    spinnerAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_item,
                            nombreDisps);
                    //Asignamos al spinner el adapter
                    spinnerDispositivos.setAdapter(spinnerAdapter);

                /*}catch (NullPointerException e){
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

        });*/
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
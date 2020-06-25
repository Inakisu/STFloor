package com.stirling.stfloor.ui.configuration;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import com.stirling.stfloor.BluetoothActivity;
import com.stirling.stfloor.Models.HitsLists.HitsListD;
import com.stirling.stfloor.Models.HitsObjects.HitsObjectD;
import com.stirling.stfloor.Models.POJOs.Dispositivo;
import com.stirling.stfloor.Models.POJOs.RespuestaB;
import com.stirling.stfloor.Models.POJOs.RespuestaU;
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

public class ConfigFragment extends Fragment {

    private Retrofit retrofit;
    private ElasticSearchAPI searchAPI;
    private String macDispositivo, nombreDispositivo;
    private int consignaDispositivo;
    private int strTOU, strREL, strPROX, strPREL;
    private String mElasticSearchPassword = Constants.elasticPassword;
    private String queryJson = "";
    public  boolean detener = false;
    private JSONObject jsonObject;
    private ArrayAdapter<String> spinnerAdapter;
    private ArrayList<Dispositivo> mDispositivo; // Lista donde se almacenarán las respuestas de la query de las cazuelas
    public ArrayList<String> nombreDisps;

    private FloatingActionButton btnAnadir;
    private EditText nomHabit, textConsigna, textTOU, textREL, textPROX, textPREL;
    private Spinner spinnerDispConfiguracion;
    private Button botonGuardar;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_config, container, false);

        return root;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstaceState) {
        super.onViewCreated(view, savedInstaceState);

        //Inicializamos la API de Elasticsearch
        inicializarAPI();

        //Inicializamos variables
        botonGuardar = (Button) view.findViewById(R.id.botonGuardar);
        btnAnadir = (FloatingActionButton) view.findViewById(R.id.anadirDispFloatingButton);
        nombreDisps = new ArrayList<>(); //arrayListo con los nombres de los disps. para mostrar en spinner
        mDispositivo = new ArrayList<Dispositivo>(); //Lista de dispositivos que hay en la BD
        spinnerDispConfiguracion = (Spinner) view.findViewById(R.id.spinnerDispConf);
        textConsigna = (EditText) view.findViewById(R.id.tempConsigna);
        nomHabit = (EditText) view.findViewById(R.id.nomHabCuadro);
        textTOU = (EditText) view.findViewById(R.id.textTOU);
        textREL = (EditText) view.findViewById(R.id.txtREL);
        textPREL = (EditText) view.findViewById(R.id.textPREL);
        textPROX = (EditText) view.findViewById(R.id.textPROX);

        //Obtenemos los dispositivos almacenados en SharedPreferences
//        obtenerDesdeSharedPrefs();
        if(!mDispositivo.isEmpty()){
            mDispositivo.clear();
        }
        if(!nombreDisps.isEmpty()){
            nombreDisps.clear();
        }
        obtenerDispositivos();

        spinnerDispConfiguracion.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Se obtiene la posición del elemento seleccionado en el spinner
                int pos = spinnerDispConfiguracion.getSelectedItemPosition();
                //Obtenemos la dirección MAC del dispositivo correspondiente a esa posición
                //en el arrayList de dispositivos obtenidos.
                macDispositivo = mDispositivo.get(pos).getIdMac();
                nombreDispositivo = mDispositivo.get(pos).getNombreHab();
                strTOU = mDispositivo.get(pos).getStrTOU();
                strREL = mDispositivo.get(pos).getStrREL();
                strPROX = mDispositivo.get(pos).getStrPROX();
                strPREL = mDispositivo.get(pos).getStrPREL();
                consignaDispositivo = mDispositivo.get(pos).gettConsigna();
                Log.d("Conf: ", "Dir. MAC del disp. selecc.: " + macDispositivo);

                //Update configuration fragment EditText fields
                textConsigna.setText(Integer.toString(consignaDispositivo));
                nomHabit.setText(nombreDispositivo);
                textTOU.setText(Integer.toString(strTOU));
                textREL.setText(Integer.toString(strREL));
                textPROX.setText(Integer.toString(strPROX));
                textPREL.setText(Integer.toString(strPREL));
//                textTOU.setText(String.valueOf(strTOU));
//                textREL.setText(String.valueOf(strREL));
//                textPROX.setText(String.valueOf(strPROX));
//                textPREL.setText(String.valueOf(strPREL));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        btnAnadir.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Abrimos activity de búsqueda de dispositivos BLE para sincronización
                Intent intent = new Intent(getActivity(), BluetoothActivity.class);
                startActivity(intent);
            }
        });

        botonGuardar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                /*//recogemos información introducida
                String textConsNuevo = textConsigna.getText().toString();
                String textSensNuevo = textSensibilidad.getText().toString();
                String textNomNuevo = nomHabit.getText().toString();*/
                //eliminamos la entrada de ese dispositivo
                borrarDispositivo(macDispositivo);

                if(!mDispositivo.isEmpty()){
                    mDispositivo.clear();
                }
                if(!nombreDisps.isEmpty()){
                    nombreDisps.clear();
                }
            }
        });
    }

    /**
     * Este método borrará la entrada del dispositivo especificado en el index stf_dispositivo
     */
    public void borrarDispositivo(String mac){
        HashMap<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Authorization", Credentials.basic("android",
                mElasticSearchPassword));
        try {
            queryJson = "{\n" +
                        " \"query\":{ \n" +
                        "    \"bool\":{\n" +
                        "      \"must\": [\n" +
                        "        {\"match\": {\n" +
                        "          \"idMac\": \"" + mac + "\"\n" +
                        "          }\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  }\n" +
                        "}";
            jsonObject = new JSONObject(queryJson);
        }catch (JSONException jerr){
            Log.d("Error: ", jerr.toString());
        }
        //Creamos el body con el JSON
        RequestBody body = RequestBody.create(okhttp3.MediaType
                .parse("application/json; charset=utf-8"),(jsonObject.toString()));
        //Realizamos la llamada mediante la API
        Call<RespuestaB> call = searchAPI.deleteDispByQuery(headerMap, body);
        call.enqueue(new Callback<RespuestaB>() {
            @Override
            public void onResponse(Call<RespuestaB> call, Response<RespuestaB> response) {
                String jsonResponse = "";
                try{
                    Log.d(TAG, "onResponse borrarDispositivo");
                    //Si la respuesta es satisfactoria
                    if(response.isSuccessful()){
                        Log.d(TAG, "onResponse borrarCazuela: Exitoso! Ole los caracole!: "+
                                response.body().toString());
                    }else{
                        jsonResponse = response.errorBody().string(); //error response body
                        Log.d(TAG, "onResponse borrarCazuela: NO successful..: "+
                                response.body().toString());
                    }
                    //recogemos información introducida
                    String textConsNuevo = textConsigna.getText().toString();
                    String textNomNuevo = nomHabit.getText().toString();
                    String textTOUnuevo = textTOU.getText().toString();
                    String textRELnuevo = textREL.getText().toString();
                    String textPROXnuevo = textPROX.getText().toString();
                    String textPRELnuevo = textPREL.getText().toString();
                    enviarConfiguracion(macDispositivo, textNomNuevo, textConsNuevo, textTOUnuevo,
                            textRELnuevo, textPROXnuevo, textPRELnuevo);

                }catch (NullPointerException e){
                    Log.e(TAG, "onResponse borrarDispositivo: NullPointerException: " +
                            e.getMessage() );
                }
                catch (IndexOutOfBoundsException e){
                    Log.e(TAG, "onResponse borrarDispositivo: IndexOutOfBoundsException: " +
                            e.getMessage() );
                }
                catch (IOException e){
                    Log.e(TAG, "onResponse borrarDispositivo: IOException: " +
                            e.getMessage() );
                }
            }

            @Override
            public void onFailure(Call<RespuestaB> call, Throwable t) {

            }
        });
    }

    /**
     * Obtenemos listado de dispositivos desde la BD para guardarlos en arrayList
     */
    public void obtenerDispositivos(){
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
                Toast.makeText(getActivity().getApplicationContext(), "Success",
                        Toast.LENGTH_SHORT).show();//prueba
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
                    //spinnerAdapter.clear(); //añadido el 1junio2020 15:38
                    for(int i = 0; i < hitsListD.getDispositivoIndex().size(); i++){
                        Log.d(TAG, "onResponse: data: " + hitsListD.getDispositivoIndex()
                                .get(i).getDispositivo().toString());
                        mDispositivo.add(hitsListD.getDispositivoIndex().get(i).getDispositivo());
                        nombreDisps.add(hitsListD.getDispositivoIndex().get(i)
                                .getDispositivo().getNombreHab());
                    }

                    spinnerAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                            android.R.layout.simple_spinner_item, nombreDisps);
                    //Notificamos al adapter de los cambios sucedidos
                    spinnerAdapter.notifyDataSetChanged();
                    //Asignamos al spinner el adapter
                    spinnerDispConfiguracion.setAdapter(spinnerAdapter);

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
                Toast.makeText(getActivity().getApplicationContext(), "Failure", Toast.LENGTH_SHORT).show();//prueba
                Log.e("onFailure: ", t.toString());
            }

        });
    }

    /**
     * Este método recoge los parámetros de configuración establecidos y los envía
     *
     * @param mac dirección MAC del dispositivo a modificar
     * @param nombreHab Nuevo nombre de habitación
     * @param tCons nuevo valor de temperatura de consigna deseada
     * @param sensTOU nuevo valor de configuración de sensibilidad
     */
    public void enviarConfiguracion(String mac, String nombreHab, String tCons, String sensTOU,
                                    String sensREL, String sensPROX, String sensPREL){
        HashMap<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Authorization", Credentials.basic("android",
                mElasticSearchPassword));
        try {
            queryJson = "{\n" +
                    "  \"idMac\":\""+ mac +"\",\n" +
                    "  \"nombreHab\":\""+ nombreHab +"\",\n" +
                    "  \"tConsigna\":\""+ tCons +"\",\n" +
                    "  \"TOU_THRESH\":\""+ sensTOU +"\",\n" +
                    "  \"REL_THRESH\":\""+ sensREL +"\",\n" +
                    "  \"PROX_THRESH\":\""+ sensPROX +"\",\n" +
                    "  \"PREL_THRESH\":\""+ sensPREL + "\"\n" +
                    "}";
            jsonObject = new JSONObject(queryJson);
        }catch (JSONException jerr){
            Log.d("Error: ", jerr.toString());
        }
        //Creamos el body con el JSON
        RequestBody body = RequestBody.create(okhttp3.MediaType
                .parse("application/json; charset=utf-8"),(jsonObject.toString()));
        //Realizamos la llamada mediante la API
        Call<RespuestaU> call = searchAPI.postDispReg(headerMap, body);
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
                    obtenerDispositivos();

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

    /**
     * Se obtiene desde sharedPreferences la lista de dispositivos obtenida en MainActivity
     */
    /*private void obtenerDesdeSharedPrefs(){
        //Vaciamos el arrayList nombreDisps
        nombreDisps.clear();
        //Vaciamos el array de dispositivos (necesario???)
        if(dispositivo != null){
            dispositivo = new Dispositivo[dispositivo.length];
        }
        //Obtenemos desde sharedPreferences
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
        //Introducimos los nombres al spinner
        spinnerAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_item,
                nombreDisps);
        //Asignamos al spinner el adapter
        spinnerDispConfiguracion.setAdapter(spinnerAdapter);
    }*/

    /**
     * Ejecuta el método de MainActivity que solicita la lista de dispositivos a la base de datos
     */
    /*private void actualizarLista(){
        ((MainActivity) getActivity()).obtenerDispositivos();
    }*/

    /**
     * Inicialización retrofit y API de Elasticsearch
     */
    private void inicializarAPI(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_ELASTICSEARCH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        searchAPI = retrofit.create(ElasticSearchAPI.class);

    }
}
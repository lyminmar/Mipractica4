package com.example.mipractica4;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    boolean primera_vez = true;
    String TAG="AVISO";  //me indica como esta mi programa
    //VARIABLES QUE VOY A SACAR JSON
    int Temp;
    int Precip;
    String Municipio;
    String fecha;

    String api_key = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJseW1pbm1hckB0ZWxlY28udXB2LmVzIiwianRpIjoiZjFmNzA0MWEtODYzNi00MDFhLTk1OGEtMWFmOTliM2I0NTQ5IiwiaXNzIjoiQUVNRVQiLCJpYXQiOjE1NjA0MzI2ODUsInVzZXJJZCI6ImYxZjcwNDFhLTg2MzYtNDAxYS05NThhLTFhZjk5YjNiNDU0OSIsInJvbGUiOiIifQ._UD_LPgvEGaQqzP07HxZPWUDhjUv7THmJny0VUxMhHs";
    String cod_Mun="49148"; //peleasdeabajo

    TextView Temperatura, Municip, Precipitacion, Fecha;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Temperatura= (TextView)findViewById(R.id.Temperatura);
        Municip= (TextView)findViewById(R.id.Municip);
        Precipitacion= (TextView)findViewById(R.id.Precipitacion);
        Fecha= (TextView)findViewById(R.id.Fecha);

        String uri ="https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/diaria/"+cod_Mun+"?api_key="+api_key+"";
        TareaAsincrona tarea_asinc = new TareaAsincrona();
        tarea_asinc.execute(uri);


        // Creación de tarea asíncrona
        // Ejecución de hilo de tarea asíncrona
    }



    class TareaAsincrona extends AsyncTask <String,String,String> {


        @Override
        protected String doInBackground(String[] uri) {
            // Llamada a petición API-REST con la URI o URL indicada en el método
            // .execute. Por último, retorno del string entregado por la llamada
            // a la API-REST

            String JsonQ = API_REST(uri[0]);

            if (JsonQ!=null)
                Log.d(TAG, JsonQ);
            else
                Log.d(TAG, "JsonQ = No hay JSON");

            return JsonQ;
        }

        @Override
        protected void onPostExecute(String respuesta) {

            if (respuesta!=null) {
                try {

                    if (primera_vez) {
                        primera_vez = false;

                        // Obtención de la propiedad "datos" del JSON

                        JSONObject objeto = new JSONObject(respuesta);
                        String datos = objeto.getString("datos");

                        // Creación de una nuevo objeto de TareaAsincrona
                        // Ejecución del hilo correspondiente

                        TareaAsincrona tarea_asinc2 = new TareaAsincrona();
                        tarea_asinc2.execute(datos);

                    } else { // segunda vez: recogida de respuesta de la segunda llamada

                        // Obtencion de las propiedades oportunas del JSON recibido
                        // Aquí ya se puede acceder a la UI, ya que estamos en el hilo
                        // convencional de ejecución, y por tanto ya se puede modificar
                        // el contenido de los TextView que contienen los valores de los datos.


                        JSONArray iniciojson = new JSONArray(respuesta);

                        Temp = iniciojson.getJSONObject(0).getJSONObject("prediccion").getJSONArray("dia").getJSONObject(1).getJSONObject("temperatura").getJSONArray("dato").getJSONObject(1).getInt("value");
                        Temperatura.setText(""+Temp);

                        Municipio=iniciojson.getJSONObject(0).getString("nombre");
                        Municip.setText(Municipio);

                        fecha=iniciojson.getJSONObject(0).getJSONObject("prediccion").getJSONArray("dia").getJSONObject(1).getString("fecha");
                        Fecha.setText(fecha);

                        Precip = iniciojson.getJSONObject(0).getJSONObject("prediccion").getJSONArray("dia").getJSONObject(1).getJSONArray("probPrecipitacion").getJSONObject(1).getInt("value");
                        Precipitacion.setText(""+Precip);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Problemas decodificando JSON");
                }
            }

        } // onPostExecute


    }//  TareaAsincrona




    /** La peticion del argumento es recogida y devuelta por el método API_REST.
     Si hay algun problema se retorna null */
    public String API_REST(String uri){

        StringBuffer response = null;

        try {
            URL url = new URL(uri);
            Log.d(TAG, "URL: " + uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Detalles de HTTP
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Codigo de respuesta: " + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String output;
                response = new StringBuffer();

                while ((output = in.readLine()) != null) {
                    response.append(output);
                }
                in.close();
            } else {
                Log.d(TAG, "responseCode: " + responseCode);
                return null; // retorna null anticipadamente si hay algun problema
            }
        } catch(Exception e) { // Posibles excepciones: MalformedURLException, IOException y ProtocolException
            e.printStackTrace();
            Log.d(TAG, "Error conexión HTTP:" + e.toString());
            return null; // retorna null anticipadamente si hay algun problema
        }

        return new String(response); // de StringBuffer -response- pasamos a String

    } // API_REST


} // MainActivity
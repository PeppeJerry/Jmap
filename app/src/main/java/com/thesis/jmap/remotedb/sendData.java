package com.thesis.jmap.remotedb;

import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.thesis.jmap.localdb.Dot;
import com.thesis.jmap.localdb.databasedots;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

public class sendData implements Runnable {
    private databasedots database;
    private Handler h = new Handler();

    // Tempi per il recupero delle informazioni
    public static final int SEND_DOTS_MINUTE = 2;
    public static final int MAX_RANDOM_MS = 5;

    public sendData(databasedots database) {
        this.database = database;
    }

    @Override
    public void run() {
        while(true) {
            syncStoredData.run();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    Runnable syncStoredData = new Runnable() {
        @Override
        public void run() {

            String error = null;
            JSONObject data;
            HttpURLConnection con = null;

            if (database.dotDao().num_rows() != 0) {
                // Prendo dal db locale lo uuid più anziano, successivamente prendo max 5000 punti con quello uuid
                String uuid = database.dotDao().getLatestUuidDot();
                String model = Build.MODEL;
                List<Dot> dots = database.dotDao().getQueuedots(uuid, 5000);

                // Converto la lista in un array con sintassi JSON
                Gson gson = new Gson();
                String dotsJson = gson.toJson(dots);

                // Rimuovo le informazioni ripetute non necessarie  (Ridondanti per un JSON)
                dotsJson = dotsJson.replace("\"uuid\":\"" + uuid + "\",", "");
                dotsJson = dotsJson.replace("\"uuid\":\"" + uuid + "\"", "");


                try {
                    // Genero il JSONObject che verranno inviati al server come dati
                    data = new JSONObject();
                    data.put("uuid", uuid);
                    data.put("model", model);
                    data.put("dots", new JSONArray(dotsJson));

                    // Inizializzo i parametri per la connessione - Metodo POST, tipo JSON
                    URL url = new URL("https://jmap.altervista.org/index.php");
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.connect();

                    // Instauro lo stream per l'invio dei dati con invio di una stringa in formato JSON 'data.toString()'
                    DataOutputStream localDataOutputStream = new DataOutputStream(con.getOutputStream());
                    localDataOutputStream.writeBytes(data.toString());
                    localDataOutputStream.flush();
                    localDataOutputStream.close();

                    // Verifico che la connessione abbia esito positivo
                    if(con.getResponseCode()!=200)
                        throw new IOException("Code is not 200");

                    // Instauro lo stream di recezione dati dal server per eventuali risposte
                    BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String line;
                    String response = "";
                    while ((line = rd.readLine()) != null) {
                        response+=line;
                    }
                    rd.close();

                    // Verifico che la risposta in ingresso è un JSON ed elimino tutti i punti inviati al db remoto
                    try{
                        data = new JSONObject(response);
                        database.dotDao().syncData(data.getString("uuid"),data.getLong("time"));
                    }
                    catch(JSONException e){
                        error = "Response Not a JSON";
                    }

                    // Per sviluppi futuri la variabile "error" potrebbe essere utilizzata
                    // per inviare file log di errori così da migliorare l'applicazione
                } catch (JSONException e) {
                    error = "JSON Creation Error\n"+e.toString();
                } catch (MalformedURLException e) {
                    error = "URL Malformed\n"+e.toString();
                } catch (ProtocolException e) {
                    error = "Protocol Error\n"+e.toString();
                } catch (IOException e) {
                    error = "I/O con Error\n"+e.toString();
                } catch (NullPointerException e) {
                    error = "Connection Variable Null\n"+e.toString();
                } catch (Exception e) {
                    error = "Not Defined Error\n"+e.toString();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    if(error != null){
                        Log.i("data error", error);
                    }
                }
            }
        }
    };
}
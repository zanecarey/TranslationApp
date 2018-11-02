package com.example.zane.translationapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    EditText translateEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onTranslateClick(View view) {

        EditText translateEditText = (EditText) findViewById(R.id.editText);

        if(!isEmpty(translateEditText)) {
            Toast.makeText(this, "Getting Translation", Toast.LENGTH_LONG).show();

            new SaveFeed().execute();
        } else {
            Toast.makeText(this, "Enter phrase to translate", Toast.LENGTH_SHORT).show();
        }

    }


    protected boolean isEmpty(EditText editText) {

        return editText.getText().toString().trim().length() == 0;

    }

    class SaveFeed extends AsyncTask<Void, Void, Void> {

        String jsonString = "";

        String result = "";
        String textViewString ="";

        JSONObject jObject;

        JSONArray jArray;

        @Override
        protected Void doInBackground(Void... voids) {

            EditText translateEditText = (EditText) findViewById(R.id.editText);

            String wordsToTranslate = translateEditText.getText().toString();

            wordsToTranslate = wordsToTranslate.replace(" ", "+");
            URL url = null;
            StringBuffer response = new StringBuffer();

            try {
                url = new URL("http://newjustin.com/translateit.php?action=translations&english_words=" + wordsToTranslate);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(false);
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

                int status = urlConnection.getResponseCode();
                if(status != 200) {
                    throw new IOException("Post failed with error code" + status);
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    while((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }

                result = response.toString();
                try {
                    jObject = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    jArray = jObject.getJSONArray("translations");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                outputTranslations(jArray);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            TextView translationTextView = (TextView) findViewById(R.id.translationTextView);

            translationTextView.setText(textViewString);
        }

        protected void outputTranslations(JSONArray jsonArray) {
            String[] languages = {"arabic", "chinese", "danish", "dutch", "french", "german",
                    "italian", "portuguese", "russian", "spanish"};

            try{
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject obj =
                            jsonArray.getJSONObject(i);

                    textViewString = textViewString + languages[i] + " : " + obj.getString(languages[i]) + "\n";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

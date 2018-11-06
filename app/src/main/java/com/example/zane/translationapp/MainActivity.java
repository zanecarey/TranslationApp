package com.example.zane.translationapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    EditText translateEditText;
    private Locale currentSpokenLang = Locale.US;


    private Locale locSpanish = new Locale("es", "MX");
    private Locale locRussian = new Locale("ru", "RU");
    private Locale locPortuguese = new Locale("pt", "BR");
    private Locale locDutch = new Locale("nl", "NL");
    private Locale[] languages = {locDutch, Locale.FRENCH, Locale.GERMAN, Locale.ITALIAN,
                                    locPortuguese, locRussian, locSpanish};


    private TextToSpeech textToSpeech;

    private Spinner languageSpinner;

    private int spinnerIndex = 0;

    private String[] arrayOfTranslations;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        languageSpinner = (Spinner) findViewById(R.id.language_spinner);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSpokenLang = languages[i];

                spinnerIndex = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    protected void onDestroy() {
        if(textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
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

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(currentSpokenLang);

            if(result == TextToSpeech.LANG_MISSING_DATA || result ==TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language Not Supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Text to Speech Failed", Toast.LENGTH_SHORT).show();

        }
    }

    public void readTheText(View view) {
        textToSpeech.setLanguage(currentSpokenLang);

        if(arrayOfTranslations.length >= 9) {
            textToSpeech.speak(arrayOfTranslations[spinnerIndex+4],
                    TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Toast.makeText(this, "Translate Text First", Toast.LENGTH_SHORT).show();
        }
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

            translationTextView.setMovementMethod(new ScrollingMovementMethod());

            String stringOfTranslations = textViewString.replaceAll("\\w+\\s:", "#");

            arrayOfTranslations = stringOfTranslations.split("#");

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
    public void ExceptSpeechInput(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_input_phase));

        try{
            startActivityForResult(intent, 100);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.stt_not_supported_message), Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if((requestCode == 100 ) && (data != null) && (resultCode == RESULT_OK)) {
            ArrayList<String> spokenText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            EditText wordsEntered = (EditText) findViewById(R.id.editText);

            wordsEntered.setText(spokenText.get(0));
        }
    }
}

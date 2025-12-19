package elixer.com.imagine;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONObject;
import java.net.URLEncoder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    TextView txtResult;
    EditText editInput;
    ImageView imgView;
    Button btnTranslate, btnImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResult = findViewById(R.id.txtTranslationResult);
        editInput = findViewById(R.id.editInput);
        imgView = findViewById(R.id.imgView);
        btnTranslate = findViewById(R.id.btnTranslate);
        btnImage = findViewById(R.id.btnLoadImage);

        // --- BUTON TRADUCERE ---
        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textDeTradus = editInput.getText().toString();

                if (!textDeTradus.isEmpty()) {

                    new TranslatorTask().execute(textDeTradus);
                } else {
                    txtResult.setText("Te rog scrie ceva mai întâi!");
                }
            }
        });

        // --- BUTON IMAGINE ---
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ImageTask().execute("https://dog.ceo/api/breeds/image/random");
            }
        });
    }


    private class TranslatorTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient();

            // Textul primit de la buton este în params[0]
            String textOriginal = params[0];

            try {

                String textCodat = URLEncoder.encode(textOriginal, "UTF-8");

                // URL-ul pentru MyMemory e
                String url = "https://api.mymemory.translated.net/get?q=" + textCodat + "&langpair=en|ro";

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    // Extragem traducerea
                    return jsonObject.getJSONObject("responseData").getString("translatedText");
                } else {
                    return "Eroare Server: " + response.code();
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Eroare: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            txtResult.setText(result);
        }
    }

    private class ImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            OkHttpClient client = new OkHttpClient();
            try {
                Request requestJson = new Request.Builder().url(urls[0]).build();
                Response responseJson = client.newCall(requestJson).execute();
                String jsonText = responseJson.body().string();
                JSONObject jsonObject = new JSONObject(jsonText);
                String urlPoza = jsonObject.getString("message");

                Request requestPoza = new Request.Builder().url(urlPoza).build();
                Response responsePoza = client.newCall(requestPoza).execute();
                return BitmapFactory.decodeStream(responsePoza.body().byteStream());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) imgView.setImageBitmap(result);
        }
    }
}
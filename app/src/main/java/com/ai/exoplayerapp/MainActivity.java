package com.ai.exoplayerapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import com.ai.exoplayerapp.databinding.ActivityMain2Binding;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private ActivityMain2Binding binding;
    private ProgressDialog pd;
    private ExoPlayer player;
    private String videoUrl = "https://a8g4i9g5y.ssl.hwcdn.net/files/a8wn4hw/vi/ba/d2/10366588/idrm/stream.mpd";
    private String licenseUrl = "https://wv-keyos.licensekeyserver.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String ID = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String url = "https://rapi.ifood.tv/drm.php?appId=4&siteId=427&auth-token=1212551&version=sv6.0&deviceId="+ID+"&deviceFwVer=4.99";
         new JsonTask().execute(url);
    }

    private void setUpPlayer(String jwt) {

        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
        UUID drmSchemeUuid = UUID.fromString((C.WIDEVINE_UUID).toString());
        MediaSourceFactory factory =  new DashMediaSource.Factory(dataSourceFactory);
        HashMap<String, String> map = new HashMap<>();
        map.put("Authorization", jwt);
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(videoUrl)
                .setDrmConfiguration(
                        new MediaItem.DrmConfiguration.Builder(drmSchemeUuid)
                                .setLicenseUri(licenseUrl)
                                .setMultiSession(false)
                                .setLicenseRequestHeaders(map)
                                .build())
                .build();

        MediaSource mediaSource =factory
                        .createMediaSource(mediaItem);

        player = new ExoPlayer.Builder(this).build();

        player.setMediaSource(mediaSource);
        binding.playerView.setPlayer(player);
        player.prepare();
        player.setPlayWhenReady(true);

    }


    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                String line = "";
                String token = "";

                while ((line = reader.readLine()) != null) {
                    JSONObject jsonObject = new JSONObject(line);
                    token = jsonObject.getString("customdata");
                }

                return token;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
          setUpPlayer(result);
        }
    }

}
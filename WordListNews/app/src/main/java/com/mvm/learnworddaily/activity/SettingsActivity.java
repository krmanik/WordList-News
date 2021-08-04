package com.mvm.learnworddaily.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mvm.learnworddaily.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    Spinner searchEngine;
    public static final String shared_prefs = "sharedPrefs";
    SharedPreferences sharedPreferences;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getString(R.string.test_ads_id));

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        sharedPreferences = getSharedPreferences(shared_prefs, MODE_PRIVATE);

        searchEngine = findViewById(R.id.search_engine);

        ArrayAdapter<CharSequence> searchArrayAdapter = ArrayAdapter.createFromResource(this, R.array.search_engine_array, android.R.layout.simple_spinner_item);
        searchArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchEngine.setAdapter(searchArrayAdapter);
        searchEngine.setOnItemSelectedListener(this);

        String search = sharedPreferences.getString("search", "Google");
        int searchPosition = searchArrayAdapter.getPosition(search);
        searchEngine.setSelection(searchPosition);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == searchEngine.getId()) {
            String search = parent.getItemAtPosition(position).toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("search", search);
            editor.apply();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void resetPrefs() {

    }

    public void savePrefs() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
    }
}

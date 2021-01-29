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
    Spinner spinnerPrimaryLang, spinnerSecondaryLang, searchEngine;

    EditText urlEditText, scrollPageValue;

    Button resetBtn, saveBtn;

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

        spinnerPrimaryLang = findViewById(R.id.primary_lang);
        spinnerSecondaryLang = findViewById(R.id.secondary_lang);
        urlEditText = findViewById(R.id.wed_url_settings);
        resetBtn = findViewById(R.id.reset_btn_settings);
        saveBtn = findViewById(R.id.save_btn_settings);
        scrollPageValue = findViewById(R.id.scroll_page);
        searchEngine = findViewById(R.id.search_engine);

        String url = sharedPreferences.getString("url", "");
        urlEditText.setText(url);

        int scroll = sharedPreferences.getInt("scroll", 0);
        scrollPageValue.setText(String.valueOf(scroll));

        ArrayAdapter<CharSequence> primaryLangArrayAdapter = ArrayAdapter.createFromResource(this, R.array.lang_array, android.R.layout.simple_spinner_item);
        primaryLangArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrimaryLang.setAdapter(primaryLangArrayAdapter);
        spinnerPrimaryLang.setOnItemSelectedListener(this);

        String primaryLang = sharedPreferences.getString("primaryLang", "English");
        int spinnerPrimaryPosition = primaryLangArrayAdapter.getPosition(primaryLang);
        spinnerPrimaryLang.setSelection(spinnerPrimaryPosition);


        ArrayAdapter<CharSequence> secondaryLangArrayAdapter = ArrayAdapter.createFromResource(this, R.array.lang_array, android.R.layout.simple_spinner_item);
        secondaryLangArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSecondaryLang.setAdapter(secondaryLangArrayAdapter);
        spinnerSecondaryLang.setOnItemSelectedListener(this);

        String secondaryLang = sharedPreferences.getString("secondaryLang", "English");
        int spinnerSecondaryPosition = primaryLangArrayAdapter.getPosition(secondaryLang);
        spinnerSecondaryLang.setSelection(spinnerSecondaryPosition);



        ArrayAdapter<CharSequence> searchArrayAdapter = ArrayAdapter.createFromResource(this, R.array.search_engine_array, android.R.layout.simple_spinner_item);
        searchArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchEngine.setAdapter(searchArrayAdapter);
        searchEngine.setOnItemSelectedListener(this);

        String search = sharedPreferences.getString("search", "Google");
        int searchPosition = searchArrayAdapter.getPosition(search);
        searchEngine.setSelection(searchPosition);

        resetBtn.setOnClickListener(v -> {
            resetPrefs();
        });

        saveBtn.setOnClickListener(v -> {
            savePrefs();
        });

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == spinnerPrimaryLang.getId()) {
            String lang = parent.getItemAtPosition(position).toString();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("primaryLang", lang);
            editor.apply();
        }

        if (parent.getId() == spinnerSecondaryLang.getId()) {
            String lang = parent.getItemAtPosition(position).toString();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("secondaryLang", lang);
            editor.apply();
        }

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
        String url = "https://v2.glosbe.com/en/en/{}";

        scrollPageValue.setText(String.valueOf(0));
        urlEditText.setText(url);

        spinnerPrimaryLang.setSelection(0);
        spinnerSecondaryLang.setSelection(0);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("primaryLang", "English");
        editor.putString("secondaryLang", "English");

        editor.putString("url", url);
        editor.apply();
    }

    public void savePrefs() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String url = urlEditText.getText().toString();
        int scroll = Integer.parseInt(scrollPageValue.getText().toString());

        String primary = sharedPreferences.getString("primaryLang", "English");
        String secondary = sharedPreferences.getString("secondaryLang", "English");

        if (url.isEmpty() || url.startsWith("https://v2.glosbe.com")) {
            url = "https://v2.glosbe.com/" + getLangCode(secondary) + "/" + getLangCode(primary) + "/{}";
        }

        urlEditText.setText(url);

        editor.putString("primaryLang", primary);
        editor.putString("secondaryLang", secondary);
        editor.putInt("scroll", scroll);
        editor.putString("url", url);
        editor.apply();

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
    }

    public String getLangCode(String lang) {

        String retLang;

        switch (lang) {
            case "English":
                retLang = "en";
                break;

            case "Hindi":
                retLang = "hi";
                break;

            case "Russian":
                retLang = "ru";
                break;

            case "Korean":
                retLang = "ko";
                break;

            case "Japanese":
                retLang = "ja";
                break;

            case "Chinese":
                retLang = "zh";
                break;

            default:
                retLang = "en";
        }
        return retLang;
    }
}

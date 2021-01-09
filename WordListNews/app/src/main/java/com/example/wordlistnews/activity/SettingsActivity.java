package com.example.wordlistnews.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wordlistnews.R;

import java.lang.reflect.Array;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    Spinner spinnerPrimaryLang, spinnerSecondaryLang;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        spinnerPrimaryLang = findViewById(R.id.primary_lang);
        spinnerSecondaryLang = findViewById(R.id.secondary_lang);

        ArrayAdapter<CharSequence> primaryLangArrayAdapter = ArrayAdapter.createFromResource(this, R.array.lang_array, android.R.layout.simple_spinner_item);
        primaryLangArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrimaryLang.setAdapter(primaryLangArrayAdapter);
        spinnerPrimaryLang.setOnItemSelectedListener(this);


        ArrayAdapter<CharSequence> secondaryLangArrayAdapter = ArrayAdapter.createFromResource(this, R.array.lang_array, android.R.layout.simple_spinner_item);
        secondaryLangArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSecondaryLang.setAdapter(secondaryLangArrayAdapter);
        spinnerSecondaryLang.setOnItemSelectedListener(this);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == spinnerPrimaryLang.getId()) {
            String lang = parent.getItemAtPosition(position).toString();
            Toast.makeText(parent.getContext(), lang, Toast.LENGTH_SHORT).show();
        }

        if (parent.getId() == spinnerSecondaryLang.getId()) {
            String lang = parent.getItemAtPosition(position).toString();
            Toast.makeText(parent.getContext(), lang, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

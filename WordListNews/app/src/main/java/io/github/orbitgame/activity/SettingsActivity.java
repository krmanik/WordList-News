package io.github.orbitgame.activity;

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

import io.github.orbitgame.R;


public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner searchEngine, theme;
    public static final String shared_prefs = "sharedPrefs";
    SharedPreferences sharedPreferences;
    EditText height, transparency;
    Button saveButton, resetButton;

    ArrayAdapter<CharSequence> themeArrayAdapter, searchArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        transparency = findViewById(R.id.dev_tools_transparency);
        height = findViewById(R.id.dev_tools_height);

        sharedPreferences = getSharedPreferences(shared_prefs, MODE_PRIVATE);

        searchEngine = findViewById(R.id.search_engine);
        theme = findViewById(R.id.dev_tools_theme);

        searchArrayAdapter = ArrayAdapter.createFromResource(this, R.array.search_engine_array, android.R.layout.simple_spinner_item);
        searchArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchEngine.setAdapter(searchArrayAdapter);
        searchEngine.setOnItemSelectedListener(this);

        String search = sharedPreferences.getString("search", "Google");
        int searchPosition = searchArrayAdapter.getPosition(search);
        searchEngine.setSelection(searchPosition);


        themeArrayAdapter = ArrayAdapter.createFromResource(this, R.array.dev_tools_theme_array, android.R.layout.simple_spinner_item);
        themeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        theme.setAdapter(themeArrayAdapter);
        theme.setOnItemSelectedListener(this);

        String themeText = sharedPreferences.getString("theme", "Monokai Pro");
        int themePosition = themeArrayAdapter.getPosition(themeText);
        theme.setSelection(themePosition);


        int h = sharedPreferences.getInt("height", 70);
        height.setText(h + "");
        float t = sharedPreferences.getFloat("transparency", 1);
        transparency.setText(t + "");

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });

        saveButton = findViewById(R.id.save_btn_settings);
        saveButton.setOnClickListener(v -> {
            savePrefs();
        });


        resetButton = findViewById(R.id.reset_btn_settings);
        resetButton.setOnClickListener(v -> {
            resetPrefs();
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (parent.getId() == searchEngine.getId()) {
            String search = parent.getItemAtPosition(position).toString();
            editor.putString("search", search);
            editor.apply();
        } else if (parent.getId() == theme.getId()) {
            String themeText = parent.getItemAtPosition(position).toString();
            editor.putString("theme", themeText);
            editor.apply();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void resetPrefs() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("height", 70);
        editor.putFloat("transparency", 1);
        editor.putString("theme", "Light");
        editor.putString("search", "Google");
        editor.apply();

        height.setText(70+"");
        transparency.setText(1+"");


        String search = sharedPreferences.getString("search", "Google");
        int searchPosition = searchArrayAdapter.getPosition(search);
        searchEngine.setSelection(searchPosition);

        String themeText = sharedPreferences.getString("theme", "Monokai Pro");
        int themePosition = themeArrayAdapter.getPosition(themeText);
        theme.setSelection(themePosition);
    }

    public void savePrefs() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int hText = 70;
        try {
           hText  = Integer.parseInt(height.getText().toString());
        } catch (Exception e) {
            hText = 70;
        }

        if (hText >= 5 && hText <= 100) {
            editor.putInt("height", hText);
        } else {
            editor.putInt("height", 70);
        }

        float transparencyText;
        try {
            transparencyText = Float.parseFloat(transparency.getText().toString());
        } catch (Exception e) {
            transparencyText = 70;
        }

        if (transparencyText >= 0.1 && transparencyText <= 1) {
            editor.putFloat("transparency", transparencyText);
        } else {
            editor.putFloat("transparency", 1);
        }
        editor.apply();
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
    }
}

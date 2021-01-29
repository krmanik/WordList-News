package com.mvm.learnworddaily.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mvm.learnworddaily.R;
import com.mvm.learnworddaily.dbhelper.WordDBHelper;
import com.mvm.learnworddaily.adapter.WordListAdapter;
import com.mvm.learnworddaily.model.WordDataModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;

public class VocabActivity extends AppCompatActivity {

    WordDBHelper wordDbHelper;
    private List<WordDataModel> wordDataModelList = new ArrayList<WordDataModel>();

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocab);
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



        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });

        wordDbHelper = new WordDBHelper(this);

        RecyclerView wordListRecyclerView = (RecyclerView) findViewById(R.id.word_list_recycler_view);
        wordListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        wordListRecyclerView.addItemDecoration(dividerItemDecoration);

        Cursor cursor = wordDbHelper.getWordList();

        String word;
        String meanings;

        WordDataModel wordDataModel;

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                word = cursor.getString(0);
                meanings = cursor.getString(1);
                wordDataModel = new WordDataModel(word, meanings);
                wordDataModelList.add(wordDataModel);
            }

            wordListRecyclerView.setAdapter(new WordListAdapter(wordDataModelList));
        }

    }

}

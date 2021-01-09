package com.example.wordlistnews.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlistnews.R;
import com.example.wordlistnews.dbhelper.WordDBHelper;
import com.example.wordlistnews.adapter.WordListAdapter;
import com.example.wordlistnews.model.WordDataModel;

import java.util.ArrayList;
import java.util.List;

public class VocabActivity extends AppCompatActivity {

    WordDBHelper wordDbHelper;
    private List<WordDataModel> wordDataModelList = new ArrayList<WordDataModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocab);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

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

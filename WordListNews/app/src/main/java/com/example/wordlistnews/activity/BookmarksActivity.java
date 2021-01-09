package com.example.wordlistnews.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlistnews.MainActivity;
import com.example.wordlistnews.dbhelper.BookmarksDBHelper;
import com.example.wordlistnews.R;
import com.example.wordlistnews.adapter.BookmarksAdapter;
import com.example.wordlistnews.model.BookmarkDataModel;

import java.util.ArrayList;
import java.util.List;

public class BookmarksActivity extends AppCompatActivity implements BookmarksAdapter.OnBookmarkClickListener{

    BookmarksDBHelper bookmarksDBHelper;
    List<BookmarkDataModel> bookmarkDataModels = new ArrayList<BookmarkDataModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });

        bookmarksDBHelper = new BookmarksDBHelper(this);

        RecyclerView bookmarksRecyclerView = (RecyclerView) findViewById(R.id.bookmarks_recycler_view);
        bookmarksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        bookmarksRecyclerView.addItemDecoration(dividerItemDecoration);

        Cursor cursor = bookmarksDBHelper.getBookmarks();

        String title;
        String url;

        BookmarkDataModel bookmarkDataModel;

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                title = cursor.getString(0);
                url = cursor.getString(1);
                bookmarkDataModel = new BookmarkDataModel(title, url);
                bookmarkDataModels.add(bookmarkDataModel);
            }

            bookmarksRecyclerView.setAdapter(new BookmarksAdapter(bookmarkDataModels,  this));
        }

    }

    @Override
    public void onBookmarkClick(int position) {
        BookmarkDataModel bookmarkDataModel = bookmarkDataModels.get(position);

        Toast.makeText(this, bookmarkDataModel.getTitle(), Toast.LENGTH_SHORT).show();

        Log.i("Bookmark", bookmarkDataModel.getTitle()+bookmarkDataModel.getUrl());

        Intent openUrl = new Intent(BookmarksActivity.this, MainActivity.class);

        String url = bookmarkDataModel.getUrl();
        openUrl.putExtra("URL", url);
        startActivity(openUrl);
        finish();
    }
}

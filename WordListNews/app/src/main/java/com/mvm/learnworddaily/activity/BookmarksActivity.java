package com.mvm.learnworddaily.activity;

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

import com.mvm.learnworddaily.MainActivity;
import com.mvm.learnworddaily.dbhelper.BookmarksDBHelper;
import com.mvm.learnworddaily.R;
import com.mvm.learnworddaily.adapter.BookmarksAdapter;
import com.mvm.learnworddaily.model.BookmarkDataModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;

public class BookmarksActivity extends AppCompatActivity implements BookmarksAdapter.OnBookmarkClickListener{

    BookmarksDBHelper bookmarksDBHelper;
    List<BookmarkDataModel> bookmarkDataModels = new ArrayList<BookmarkDataModel>();

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
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

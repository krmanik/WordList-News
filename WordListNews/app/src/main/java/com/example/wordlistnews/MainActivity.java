package com.example.wordlistnews;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wordlistnews.activity.BookmarksActivity;
import com.example.wordlistnews.activity.HelpActivity;
import com.example.wordlistnews.activity.SettingsActivity;
import com.example.wordlistnews.activity.VocabActivity;
import com.example.wordlistnews.dbhelper.BookmarksDBHelper;
import com.example.wordlistnews.dbhelper.WordDBHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    WebView webView, wordWebView;
    ImageButton actionHome, actionMore, actionPopupActivate, actionClearEditText;
    Button actionBack, actionForward, actionRefresh, actionBookmark, actionOpenInBrowser;
    LinearLayout homeButtonLayout, settingsButtonLayout, bookmarkButtonLayout, helpButtonLayout, vocabButtonLayout, buttonTopLinearLayout;
    ProgressBar progressBar, popupProgressbar;
    Dialog wordDialog;
    Dialog optionMenuDialog;
    ImageButton pronounceWord, saveWordBtn;
    TextView wordTextView;
    boolean popupActive = true;
    boolean webpageLoading = false;

    int saveWordClickCount = 0;

    private TextToSpeech mTTS;
    int mTTSResult;

    String homePage = "file:///android_asset/home.html";

    WordDBHelper wordDbHelper;
    BookmarksDBHelper bookmarksDBHelper;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // activity_main
        editText = findViewById(R.id.edit_text);
        editText.clearFocus();
        webView = findViewById(R.id.webView);
        actionHome = findViewById(R.id.action_home);
        actionMore = findViewById(R.id.action_more);
        actionClearEditText = findViewById(R.id.action_clear_edit_text);
        actionPopupActivate = findViewById(R.id.action_activate_popup);
        progressBar = findViewById(R.id.progressbar);

        editText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search();
            }
            return false;
        });

        actionHome.setOnClickListener(v -> {
            goHomeScreen();
        });

        actionMore.setOnClickListener(v -> {
            showMorePopup();
        });

        actionClearEditText.setOnClickListener(v -> {
            editText.setText("");
        });

        actionPopupActivate.setOnClickListener(v -> {
            popupActive = !popupActive;
            if (popupActive) {
                actionPopupActivate.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_baseline_translate_active_24));
            } else {
                actionPopupActivate.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_baseline_translate_24));
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.addJavascriptInterface(new JSInterface(this), "JSInterface");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))
            { WebView.setWebContentsDebuggingEnabled(true); }
        }

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
                if (progressBar.getProgress() == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    downloadDialog(url, userAgent, contentDisposition, mimetype);
                } else {
                    String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    ActivityCompat.requestPermissions(this, permission, 1);
                }
            } else {
                downloadDialog(url, userAgent, contentDisposition, mimetype);
            }
        });
        webView.setWebViewClient(new AppWebViewClient());
        webView.loadUrl(homePage);


        wordDbHelper = new WordDBHelper(this);
        bookmarksDBHelper = new BookmarksDBHelper(this);

        optionMenuDialog = new Dialog(this);
        optionMenuDialog.setContentView(R.layout.custom_action_menu);
        actionRefresh = optionMenuDialog.findViewById(R.id.action_refresh);


        wordDialog = new Dialog(this);
        wordDialog.setContentView(R.layout.word_popup);
        saveWordBtn = wordDialog.findViewById(R.id.save_word);

        showWordPopup();


        String url = getIntent().getStringExtra("URL");
        if (url != null) {
            if (url.startsWith("https://")) {
                webView.loadUrl(url);
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (editText.isFocused()) {
            editText.clearFocus();

        }

        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
        webView.pauseTimers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        webView.resumeTimers();
    }


    private void goHomeScreen() {
        editText.clearFocus();

        webView.stopLoading();
        webView.loadUrl(homePage);
    }

    private void showMorePopup() {
        Window window = optionMenuDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.TOP | Gravity.RIGHT;
        window.setAttributes(wlp);

        actionBack = optionMenuDialog.findViewById(R.id.action_back);
        actionForward = optionMenuDialog.findViewById(R.id.action_forward);
        actionBookmark = optionMenuDialog.findViewById(R.id.action_bookmark);
        actionOpenInBrowser = optionMenuDialog.findViewById(R.id.action_open_in_browser);

        homeButtonLayout = optionMenuDialog.findViewById(R.id.home_button_ll);
        settingsButtonLayout = optionMenuDialog.findViewById(R.id.setting_button_ll);
        bookmarkButtonLayout = optionMenuDialog.findViewById(R.id.bookmark_button_ll);
        vocabButtonLayout = optionMenuDialog.findViewById(R.id.vocab_button_ll);
        helpButtonLayout = optionMenuDialog.findViewById(R.id.help_button_ll);

        if (webView.canGoBack()) {
            actionBack.setEnabled(true);
            actionBack.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24));
        } else {
            actionBack.setEnabled(false);
            actionBack.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_black_24));
        }

        if (webView.canGoForward()) {
            actionForward.setEnabled(true);
            actionForward.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_forward_24));
        } else {
            actionForward.setEnabled(false);
            actionForward.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_forward_black_24));
        }

        if (webView.getUrl().startsWith("file:///")) {
            homeButtonLayout.setVisibility(View.GONE);
        } else {
            homeButtonLayout.setVisibility(View.VISIBLE);
        }

        actionBack.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            }
            optionMenuDialog.dismiss();
        });

        actionForward.setOnClickListener(v -> {
            if (webView.canGoForward()) {
                webView.goForward();
            }
            optionMenuDialog.dismiss();
        });

        actionRefresh.setOnClickListener(v -> {
            if (webpageLoading) {
                webView.stopLoading();
            } else {
                webView.reload();
            }
            optionMenuDialog.dismiss();
        });

        actionBookmark.setOnClickListener(v -> {
            if (webView.getUrl().startsWith("file:///")) {
                return;
            }
            saveBookmarks();
            optionMenuDialog.dismiss();
        });

        actionOpenInBrowser.setOnClickListener(v ->{
            if (webView.getUrl().startsWith("file:///")) {
                return;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl()));
            startActivity(browserIntent);
        });


        homeButtonLayout.setOnClickListener(v -> {
            goHomeScreen();
            optionMenuDialog.dismiss();
        });

        settingsButtonLayout.setOnClickListener(v -> {
            viewSettingScreen();
            optionMenuDialog.dismiss();
        });

        bookmarkButtonLayout.setOnClickListener(v -> {
            viewBookmarkScreen();
            optionMenuDialog.dismiss();
        });

        vocabButtonLayout.setOnClickListener(v -> {
            viewVocabScreen();
            optionMenuDialog.dismiss();
        });

        helpButtonLayout.setOnClickListener(v -> {
            viewHelpScreen();
            optionMenuDialog.dismiss();
        });

        optionMenuDialog.show();
    }

    public void viewSettingScreen() {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }

    public void viewBookmarkScreen() {
        Intent intent = new Intent(this, BookmarksActivity.class);
        this.startActivity (intent);
    }

    public void viewVocabScreen() {
        Intent intent = new Intent(this, VocabActivity.class);
        this.startActivity(intent);
    }

    public void viewHelpScreen() {
        Intent intent = new Intent(this, HelpActivity.class);
        this.startActivity(intent);
    }

    public void saveBookmarks() {
        String url = webView.getUrl();
        String title = webView.getTitle();
        Log.i("Info::", url+title);

        if (bookmarksDBHelper.isUrlExists(url)) {
            Toast.makeText(this, "Url already bookmarked.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = bookmarksDBHelper.insertBookmark(title, url);

        if (inserted) {
            Toast.makeText(this, "Url bookmarked", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    public void showWordPopup() {
        // popup words with webview
        wordTextView = wordDialog.findViewById(R.id.popup_text_view);
        popupProgressbar = wordDialog.findViewById(R.id.popup_progressbar);

        pronounceWord = wordDialog.findViewById(R.id.pronounce_word);

        pronounceWord.setOnClickListener(v -> {
            textToSpeech();
        });

        saveWordBtn.setOnClickListener(v -> {
            saveWordToList();
        });

        wordWebView = wordDialog.findViewById(R.id.popup_webView);
        wordWebView.getSettings().setJavaScriptEnabled(true);
        wordWebView.getSettings().setLoadsImagesAutomatically(true);
        wordWebView.addJavascriptInterface(new JSInterface(this), "JSInterface");

        wordWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                popupProgressbar.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);

                if (popupProgressbar.getProgress() == 100) {
                    popupProgressbar.setVisibility(View.GONE);
                }
            }
        });
        wordWebView.setWebViewClient(new PopUpWebViewClient());

        Window wordWindow = wordDialog.getWindow();
        WindowManager.LayoutParams wmLp = wordWindow.getAttributes();
        wmLp.gravity = Gravity.TOP;
        wmLp.width = getScreenWidth();
        wmLp.height = (int) (getScreenHeight() * 0.7);
        wordWindow.setAttributes(wmLp);
    }

    public void textToSpeech() {
        mTTS = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // prefs lang change
                mTTSResult = mTTS.setLanguage(Locale.US);
                if (mTTSResult == TextToSpeech.LANG_MISSING_DATA
                        || mTTSResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.i("TTS", "Missing");
                }

                String word = wordTextView.getText().toString();
                if (!mTTS.isSpeaking()) {
                    mTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
                }


            } else {
                Log.i("TTS", "Init fail");
            }
        });
    }

    public void saveWordToList() {
        wordWebView.loadUrl("javascript:(getMeaning())");
    }

    private void downloadDialog(String url, String userAgent, String contentDisposition, String mimetype) {
        String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download")
                .setMessage("Do you want to save "+fileName)
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, which) -> {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    String cookie = CookieManager.getInstance().getCookie(url);

                    request.addRequestHeader("Cookie", cookie);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

                    DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    downloadManager.enqueue(request);

                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.cancel();
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void search() {
        String text = editText.getText().toString();
        searchOrLoadUrl(text);
    }

    private void searchOrLoadUrl(String text) {

        editText.clearFocus();


        if (TextUtils.isEmpty(text)) {
            return;
        } else if (Patterns.WEB_URL.matcher(text.toLowerCase()).matches()) {
            if (text.contains("http://") || text.contains("https://")) {
                webView.loadUrl(text);
            } else {
                webView.loadUrl("https://" + text);
            }
        } else {
            webView.loadUrl("https://www.google.com/search?q="+text);
        }
        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (this.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public boolean isAlpha(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }


    // main activity web view client
    public class AppWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            wordDialog.dismiss();
            editText.setText(url);
            editText.clearFocus();

            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (url.startsWith("file:///android_asset")) {
                url = "";
            }

            progressBar.setVisibility(View.VISIBLE);
            editText.setText(url);
            editText.clearFocus();

            webpageLoading = true;
            actionRefresh.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_close_white_24));

            wordDialog.dismiss();

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            String jsCode = readJsFromAssets("popup1.js");

            if (!url.startsWith("file:///android_asset")) {
                webView.loadUrl("javascript:(function(){ " + jsCode + " })();");
            }

            webpageLoading = false;
            actionRefresh.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_refresh_24));

            super.onPageFinished(view, url);
        }
    }

    // popup word web view client
    private class PopUpWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            popupProgressbar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            String jsCode = readJsFromAssets("addCodeToPopup.js");

            if (!url.startsWith("file:///android_asset")) {
                wordWebView.loadUrl("javascript:(function(){ " + jsCode + " })();");
            }

            saveWordBtn.setVisibility(View.VISIBLE);
            saveWordClickCount = 0;


            // prefs auto scroll
            Handler lHandler = new Handler();
            lHandler.postDelayed(() -> wordWebView.scrollTo(0, 400), 200);
            super.onPageFinished(view, url);
        }
    }

    // javascript interface to call java function from android
    private class JSInterface {
        Context mContext;

        JSInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void viewMeaningPopup(String words) {
            // settings here language
            if (TextUtils.isEmpty(words)) {
                return;
            }

            if (popupActive) {
                runOnUiThread(() -> {
                    // get pref set height, lang
                    String url = "https://v2.glosbe.com/en/hi/" + words;
                    if (isAlpha(words)) {
                        wordWebView.loadUrl(url);
                        wordTextView.setText(words);
                    }
                    wordDialog.show();
                });
            }
        }

        // interface for popup word web view
        @JavascriptInterface
        public void getWordData(String jsonData) {

            saveWordClickCount += 1;

            runOnUiThread(() -> {

                try {
                    JSONArray jsonArray = new JSONArray(jsonData);

                    String word = wordTextView.getText().toString();
                    StringBuilder meanings = new StringBuilder();

                    if (jsonArray.length() == 0) {
                        Toast.makeText(mContext, "Meanings data empty", Toast.LENGTH_LONG).show();
                        return;
                    }

                    for (int i=0; i < jsonArray.length(); i++) {
                        meanings.append(jsonArray.get(i).toString());

                        if (i != jsonArray.length()-1) {
                            meanings.append(", ");
                        }
                    }

                    // when page loaded
                    Log.i("Words::meanings", word+"::"+meanings.toString());

                    if (wordDbHelper.isWordExists(word)) {
                        Toast.makeText(mContext, "Word exists, click again to update", Toast.LENGTH_LONG).show();
                    } else {
                        boolean inserted = wordDbHelper.insertWord(word, meanings.toString());
                        if (inserted) {
                            Toast.makeText(mContext, "Word saved", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "Error saving the word", Toast.LENGTH_LONG).show();
                        }
                    }

                    if (saveWordClickCount >= 2) {
                        boolean updated = wordDbHelper.updateWord(word, meanings.toString());
                        Toast.makeText(mContext, "Word updated", Toast.LENGTH_LONG).show();
                        saveWordClickCount = 0;
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });


        }

    }

    private String readJsFromAssets(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = null;
        try {
            String jsFile = "js/" + fileName;
            reader = new BufferedReader(new InputStreamReader(getAssets().open(jsFile)));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                stringBuilder.append(mLine);
            }
        } catch (IOException e) {
            Log.e("Error::onPageFinished", e.getLocalizedMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("Error::onPageFinished", e.getLocalizedMessage());
                }
            }
        }
        if (stringBuilder == null) {
            stringBuilder.append("");
        }
        return stringBuilder.toString();
    }

}
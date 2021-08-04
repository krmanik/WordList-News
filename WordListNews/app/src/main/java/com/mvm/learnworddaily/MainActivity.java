package com.mvm.learnworddaily;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
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
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.mvm.learnworddaily.activity.BookmarksActivity;
import com.mvm.learnworddaily.activity.HelpActivity;
import com.mvm.learnworddaily.activity.SettingsActivity;
import com.mvm.learnworddaily.dbhelper.BookmarksDBHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import static com.mvm.learnworddaily.activity.SettingsActivity.shared_prefs;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    WebView webView;
    ImageButton actionHome, actionMore, showDevTools, actionClearEditText;
    Button actionBack, actionForward, actionRefresh, actionBookmark, actionOpenInBrowser;
    LinearLayout homeButtonLayout, settingsButtonLayout, bookmarkButtonLayout, helpButtonLayout;
    ProgressBar progressBar;
    Dialog optionMenuDialog;
    boolean webpageLoading = false;

    String homePage = "file:///android_asset/home.html";

    BookmarksDBHelper bookmarksDBHelper;

    SharedPreferences sharedPreferences;

    private AdView mAdView;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
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


        // activity_main
        editText = findViewById(R.id.edit_text);
        editText.clearFocus();
        webView = findViewById(R.id.webView);
        actionHome = findViewById(R.id.action_home);
        actionMore = findViewById(R.id.action_more);
        actionClearEditText = findViewById(R.id.action_clear_edit_text);
        showDevTools = findViewById(R.id.show_dev_tools);
        progressBar = findViewById(R.id.progressbar);

        sharedPreferences = getSharedPreferences(shared_prefs, MODE_PRIVATE);

        editText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search();
            }
            return false;
        });

        actionHome.setOnClickListener(v -> goHomeScreen());

        actionMore.setOnClickListener(v -> showMorePopup());

        actionClearEditText.setOnClickListener(v -> editText.setText(""));

        showDevTools.setOnClickListener(v -> {


            webView.loadUrl("javascript:(function() {" +
                    "eruda.init();\n" +
                    "\n" +
                    "eruda._entryBtn.hide();\n" +
                    "eruda._devTools._$el[0].style.height = \""+ 80 +"%\";\n" +
                    "\n" +
                    "if (eruda.get()._isShow) {\n" +
                    "   eruda.hide();\n" +
                    "} else {\n" +
                    "   eruda.show();\n" +
                    "}" +
                    "})()");


        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

        bookmarksDBHelper = new BookmarksDBHelper(this);

        optionMenuDialog = new Dialog(this);
        optionMenuDialog.setContentView(R.layout.custom_action_menu);
        actionRefresh = optionMenuDialog.findViewById(R.id.action_refresh);

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
        wlp.gravity = Gravity.TOP | Gravity.END;
        window.setAttributes(wlp);

        actionBack = optionMenuDialog.findViewById(R.id.action_back);
        actionForward = optionMenuDialog.findViewById(R.id.action_forward);
        actionBookmark = optionMenuDialog.findViewById(R.id.action_bookmark);
        actionOpenInBrowser = optionMenuDialog.findViewById(R.id.action_open_in_browser);

        homeButtonLayout = optionMenuDialog.findViewById(R.id.home_button_ll);
        settingsButtonLayout = optionMenuDialog.findViewById(R.id.setting_button_ll);
        bookmarkButtonLayout = optionMenuDialog.findViewById(R.id.bookmark_button_ll);
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

        actionOpenInBrowser.setOnClickListener(v -> {
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
        this.startActivity(intent);
    }

    public void viewHelpScreen() {
        Intent intent = new Intent(this, HelpActivity.class);
        this.startActivity(intent);
    }

    public void saveBookmarks() {
        String url = webView.getUrl();
        String title = webView.getTitle();
        Log.i("Info::", url + title);

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

    private void downloadDialog(String url, String userAgent, String contentDisposition, String mimetype) {
        String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download")
                .setMessage("Do you want to save " + fileName)
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
                .setNegativeButton("No", (dialog, which) -> dialog.cancel());

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

            String search = sharedPreferences.getString("search", "Google");
            String searchEngine = getSearchEngine(search);
            webView.loadUrl(searchEngine + text);
        }
        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (this.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }


    // main activity web view client
    public class AppWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
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

            injectJS("VM.js");

            progressBar.setVisibility(View.VISIBLE);
            editText.setText(url);
            editText.clearFocus();
            webpageLoading = true;
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            webpageLoading = false;
            actionRefresh.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_refresh_24));
            super.onPageFinished(view, url);
        }
    }

    public String getSearchEngine(String search) {
        String url;
        switch (search) {
            case "Baidu":
                url = "https://www.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=baidu&wd=";
                break;

            case "Wikipedia":
                url = "https://en.wikipedia.org/w/index.php?search=google";
                break;

            case "Bing":
                url = "https://www.bing.com/search?q=";
                break;

            case "DuckDuckGo":
                url = "https://duckduckgo.com/?q=";
                break;

            case "Yandex":
                url = "https://yandex.com/search/?text=";
                break;

            default:
                url = "https://www.google.com/search?q=";
                break;
        }
        return url;
    }

    private void injectJS(String jsFile) {
        try {
            InputStream inputStream = getAssets().open("js/" + jsFile);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            webView.loadUrl("javascript:(function() {" +
                    "globalThis.isShownDevTools = false;" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var script = document.createElement('script');" +
                    "script.type = 'text/javascript';" +
                    "script.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(script)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
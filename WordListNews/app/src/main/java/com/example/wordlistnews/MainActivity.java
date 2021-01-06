package com.example.wordlistnews;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
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

import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    WebView webView, wordWebView;
    ImageButton actionSearch, actionHome, actionMore;
    ProgressBar progressBar, popupProgressbar;
    Dialog wordDialog;
    TextView wordTextView;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText = findViewById(R.id.edit_text);
        editText.clearFocus();
        webView = findViewById(R.id.webView);
        actionSearch = findViewById(R.id.action_search);
        actionHome = findViewById(R.id.action_home);
        actionMore = findViewById(R.id.action_more);
        progressBar = findViewById(R.id.progressbar);

        wordDialog = new Dialog(this);
        wordDialog.setContentView(R.layout.word_popup);
        wordTextView = wordDialog.findViewById(R.id.popup_text_view);
        wordTextView = wordDialog.findViewById(R.id.popup_text_view);
        popupProgressbar = wordDialog.findViewById(R.id.popup_progressbar);

        wordWebView = wordDialog.findViewById(R.id.popup_webView);
        wordWebView.getSettings().setJavaScriptEnabled(true);
        wordWebView.getSettings().setLoadsImagesAutomatically(true);
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

        actionSearch.setOnClickListener(v -> {
            search();
        });

        editText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search();
            }
            return false;
        });

        actionHome.setOnClickListener(v -> {
            goHomeScreen();
            editText.clearFocus();
        });

        actionMore.setOnClickListener(v -> {
            showMorePopup();
            editText.clearFocus();
        });

        Window wordWindow = wordDialog.getWindow();
        WindowManager.LayoutParams wmLp = wordWindow.getAttributes();
        wmLp.gravity = Gravity.TOP;
        wmLp.width = getScreenWidth();
        wmLp.height = (int) (getScreenHeight() * 0.7);
        wordWindow.setAttributes(wmLp);

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
    }

    private void goHomeScreen() {
    }

    private void showMorePopup() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_action_menu);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.TOP | Gravity.RIGHT;
        window.setAttributes(wlp);
        dialog.show();
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

    public void search() {
        String text = editText.getText().toString();
        searchOrLoadUrl(text);
        editText.clearFocus();
    }

    private void searchOrLoadUrl(String text) {

        if (TextUtils.isEmpty(text)) {
            editText.clearFocus();
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
            progressBar.setVisibility(View.VISIBLE);
            editText.setText(url);
            editText.clearFocus();
            wordDialog.dismiss();
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(getAssets().open("js/popup1.js")));
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

            webView.loadUrl("javascript:(function() { " + stringBuilder + "})()");
            super.onPageFinished(view, url);
        }
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private class JSInterface {
        Context mContext;

        JSInterface(Context c) {
            mContext = c;
        }

        @android.webkit.JavascriptInterface
        public void viewMeaningPopup(String words) {
            // settings here language
            if (TextUtils.isEmpty(words)) {
                return;
            }

            runOnUiThread(() -> {

                // get pref set height

                String url = "https://v2.glosbe.com/en/hi/" + words;


                if (isAlpha(words)) {
                    wordWebView.loadUrl(url);
                }
                wordTextView.setText(words);
                wordDialog.show();

            });


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

    private class PopUpWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            popupProgressbar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // prefs auto scroll
            Handler lHandler = new Handler();
            lHandler.postDelayed(() -> wordWebView.scrollTo(0, 400), 200);
            super.onPageFinished(view, url);
        }
    }
}
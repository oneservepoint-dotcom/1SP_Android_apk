package com.oneservepoint.employee;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.CookieManager;
import android.view.View;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebResourceRequest;

public class MainActivity extends Activity {
    private WebView webView;
    private LinearLayout bottomBar;
    private TextView homeTab, leadsTab, accountTab;
    private static final String HOME_URL = "https://oneservepoint.com";
    private final int navy = Color.rgb(8, 38, 74);
    private final int green = Color.rgb(91, 158, 18);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(navy);
        getWindow().setNavigationBarColor(navy);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        FrameLayout frame = new FrameLayout(this);
        webView = new WebView(this);
        frame.addView(webView, new FrameLayout.LayoutParams(-1, -1));

        bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER);
        bottomBar.setBackgroundColor(Color.WHITE);
        bottomBar.setElevation(dp(8));
        bottomBar.setVisibility(View.GONE);

        homeTab = makeTab("⌂", "Home");
        leadsTab = makeTab("▤", "Saved Leads");
        accountTab = makeTab("●", "My Account");
        bottomBar.addView(homeTab, new LinearLayout.LayoutParams(0, dp(60), 1));
        bottomBar.addView(leadsTab, new LinearLayout.LayoutParams(0, dp(60), 1));
        bottomBar.addView(accountTab, new LinearLayout.LayoutParams(0, dp(60), 1));

        root.addView(frame, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(bottomBar, new LinearLayout.LayoutParams(-1, dp(60)));
        setContentView(root);

        homeTab.setOnClickListener(v -> {
            select(homeTab);
            webView.evaluateJavascript(
                "if(window.homePage){homePage.style.display='block';}" +
                "if(window.applyPage){applyPage.style.display='none';}" +
                "if(window.partnerPage){partnerPage.style.display='none';}" +
                "window.scrollTo(0,0);", null);
        });

        leadsTab.setOnClickListener(v -> {
            select(leadsTab);
            webView.evaluateJavascript(
                "(function(){var w=document.getElementById('employeeWorkspace');" +
                "if(w)w.scrollIntoView({behavior:'smooth'});" +
                "var h=[].slice.call(document.querySelectorAll('h1,h2,h3')).find(e=>/my leads/i.test(e.innerText||''));" +
                "if(h)h.scrollIntoView({behavior:'smooth',block:'start'});" +
                "else {var t=document.getElementById('myEmployeeLeadsRows');if(t)t.scrollIntoView({behavior:'smooth'});}" +
                "})()", null);
        });

        accountTab.setOnClickListener(v -> {
            select(accountTab);
            webView.evaluateJavascript(
                "(function(){var w=document.getElementById('employeeWorkspace');" +
                "if(w)w.scrollIntoView({behavior:'smooth'});" +
                "var h=document.getElementById('employeeSignedInAs');" +
                "if(h)h.scrollIntoView({behavior:'smooth',block:'center'});" +
                "})()", null);
            Toast.makeText(this, "My Account: Employee ID / Email and Logout are shown in your employee profile area.", Toast.LENGTH_LONG).show();
        });

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(webView, true);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setAllowFileAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setUserAgentString(settings.getUserAgentString() + " 1SPAndroidApp/1.2");

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) { return false; }
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                updateLoginTabs();
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) updateLoginTabs();
            }
        });

        if (state != null) webView.restoreState(state);
        else webView.loadUrl(HOME_URL);
    }

    private TextView makeTab(String symbol, String label) {
        TextView tv = new TextView(this);
        tv.setText(symbol + "\n" + label);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(navy);
        return tv;
    }

    private void select(TextView chosen) {
        for (TextView t : new TextView[]{homeTab, leadsTab, accountTab})
            t.setTextColor(t == chosen ? green : navy);
    }

    private void updateLoginTabs() {
        if (webView == null) return;
        String js = "(function(){var e=document.getElementById('employeeWorkspace');" +
            "if(!e)return 'no';var s=getComputedStyle(e);" +
            "return (s.display!=='none' && s.visibility!=='hidden')?'yes':'no';})()";
        webView.evaluateJavascript(js, value -> {
            boolean loggedIn = "\"yes\"".equals(value);
            bottomBar.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
            if (loggedIn) select(homeTab);
        });
    }

    private int dp(int n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        if (webView != null) webView.saveState(out);
        super.onSaveInstanceState(out);
    }

    @Override protected void onDestroy() {
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}

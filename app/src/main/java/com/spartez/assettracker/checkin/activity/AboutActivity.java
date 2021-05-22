package com.spartez.assettracker.checkin.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.webkit.WebView;
import android.widget.TextView;

import com.spartez.assettracker.checkin.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView copyright = (TextView) findViewById(R.id.about_copyright_link);
        copyright.setText(Html.fromHtml(getString(R.string.drawer_copyright)));
        copyright.setMovementMethod(LinkMovementMethod.getInstance());

        WebView license = (WebView) findViewById(R.id.about_license);
        license.loadDataWithBaseURL(null, "<html><body>" + getString(R.string.about_license) + "</body></html>", "text/html", "utf-8", null);
        license.setBackgroundColor(Color.TRANSPARENT);

        TextView version = (TextView) findViewById(R.id.about_version);
        try {
            PackageInfo pInfo;
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText(String.format(getString(R.string.app_version_text), pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            version.setText(String.format(getString(R.string.app_version_text), "unknown"));
        }
    }
}

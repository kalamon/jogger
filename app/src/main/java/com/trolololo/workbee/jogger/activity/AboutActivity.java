package com.trolololo.workbee.jogger.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.trolololo.workbee.jogger.R;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView copyright = findViewById(R.id.about_copyright_link);
        copyright.setText(Html.fromHtml(getString(R.string.drawer_copyright), FROM_HTML_MODE_COMPACT));
        copyright.setMovementMethod(LinkMovementMethod.getInstance());

        WebView license = findViewById(R.id.about_license);
        license.loadDataWithBaseURL(null, "<html><body>" + getString(R.string.about_license) + "</body></html>", "text/html", "utf-8", null);
        license.setBackgroundColor(Color.TRANSPARENT);

        TextView version = findViewById(R.id.about_version);
        try {
            PackageInfo pInfo;
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText(String.format(getString(R.string.app_version_text), pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            version.setText(String.format(getString(R.string.app_version_text), "unknown"));
        }
    }
}

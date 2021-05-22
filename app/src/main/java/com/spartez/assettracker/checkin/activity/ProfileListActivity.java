package com.spartez.assettracker.checkin.activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.spartez.assettracker.checkin.R;
import com.spartez.assettracker.checkin.adapter.ProfileAdapter;
import com.spartez.assettracker.checkin.analytics.AnalyticsService;
import com.spartez.assettracker.checkin.domain.Profile;

import java.util.List;

public class ProfileListActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Profile selectedProfile = null;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);
        setTitle(R.string.title_activity_profile_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionsMenu addProfileMenu = (FloatingActionsMenu) findViewById(R.id.action_add_profile_menu);

        final View menuBackground = findViewById(R.id.add_profile_menu_background);
        menuBackground.setVisibility(View.GONE);
        menuBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProfileMenu.collapse();
            }
        });
        addProfileMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                menuBackground.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                menuBackground.setVisibility(View.GONE);
            }
        });
        final FloatingActionButton addDateProfile = (FloatingActionButton) findViewById(R.id.action_add_date_field_profile);
        addDateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProfileMenu.collapse();
                addNewProfile(true);
            }
        });
        final FloatingActionButton addStoredOperationProfile = (FloatingActionButton) findViewById(R.id.action_add_stored_op_profile);
        addStoredOperationProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProfileMenu.collapse();
                addNewProfile(false);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                TextView tv = (TextView) findViewById(R.id.copyright_link);
                tv.setText(Html.fromHtml(getString(R.string.drawer_copyright)));
                tv.setMovementMethod(LinkMovementMethod.getInstance());

                super.onDrawerOpened(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ((TextView) findViewById(R.id.add_profile_text)).setText(Html.fromHtml(getString(R.string.add_profile_description)));

        int profileCount = setupListView();

        AnalyticsService.getInstance().initialize(this);
        AnalyticsService.getInstance().trackAppStarted(profileCount);
    }

    private int setupListView() {
        ListView listView = (ListView) findViewById(R.id.profileList);
        List<Profile> profiles = Profile.loadAll(getApplicationContext());
        final ProfileAdapter adapter = new ProfileAdapter(this, profiles);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onProfileLongClick(adapter, position);
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onProfileClick(adapter, position);
            }
        });
        showOrHideProfileList(listView, profiles);
        return profiles.size();
    }

    private void showOrHideProfileList(ListView listView, List<Profile> profiles) {
        if (profiles.size() > 0) {
            listView.setVisibility(View.VISIBLE);
            findViewById(R.id.add_profile_text).setVisibility(View.GONE);
            findViewById(R.id.long_press_for_ops).setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.GONE);
            findViewById(R.id.add_profile_text).setVisibility(View.VISIBLE);
            findViewById(R.id.long_press_for_ops).setVisibility(View.GONE);
        }
    }

    @Override
    public void onRestart() {
        selectedProfile = null;
        setupListView();
        setMenuVisibility();
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_profile_list, menu);
        setMenuVisibility();

        return true;
    }

    private void setMenuVisibility() {
        MenuItem item1 = menu.findItem(R.id.action_scan);
        MenuItem item2 = menu.findItem(R.id.action_copy);
        MenuItem item3 = menu.findItem(R.id.action_delete);
        item1.setVisible(selectedProfile != null);
        item2.setVisible(selectedProfile != null);
        item3.setVisible(selectedProfile != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_scan: {
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra(Profile.class.getCanonicalName(), selectedProfile);
                startActivity(intent);
                return true;
            }
            case R.id.action_copy: {
                Profile copy = Profile.clone(selectedProfile);
                openProfileEditor(copy);
                return true;
            }
            case R.id.action_delete: {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                deleteSelectedProfile();
                                setMenuVisibility();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //"No" button clicked
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setMessage(R.string.confirm_delete)
                        .setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener)
                        .show();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }


    private void deleteSelectedProfile() {
        List<Profile> profiles = Profile.remove(getApplicationContext(), selectedProfile);
        selectedProfile = null;
        ListView listView = (ListView) findViewById(R.id.profileList);
        ProfileAdapter adapter = (ProfileAdapter) listView.getAdapter();
        adapter.setProfiles(profiles);
        adapter.notifyDataSetChanged();
        showOrHideProfileList(listView, profiles);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_feedback) {
            try {
                String body = "Enter your Question, Enquiry or Feedback below:\n\n\n";
                Intent mail = new Intent(Intent.ACTION_SEND);
                mail.setType("message/rfc822");
                mail.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@spartez.com"});
                mail.putExtra(Intent.EXTRA_SUBJECT, "[Asset Tracker] Android Asset Checks Feedback");
                mail.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(mail);
                return true;
            } catch (ActivityNotFoundException e) {
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
                dlgAlert.setMessage(R.string.cannot_send_email);
                dlgAlert.setPositiveButton(R.string.close, null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void addNewProfile(boolean legacyDateFieldProfile) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(Profile.class.getCanonicalName() + ".legacy", legacyDateFieldProfile);
        startActivity(intent);
    }

    private void onProfileLongClick(ProfileAdapter adapter, int position) {
        if (position < 0) {
            if (selectedProfile != null) {
                selectedProfile.setSelected(false);
                selectedProfile = null;
            }
        } else {
            Profile profile = (Profile) adapter.getItem(position);
            if (selectedProfile != null && profile == selectedProfile) {
                selectedProfile.setSelected(false);
                selectedProfile = null;
            } else {
                if (selectedProfile != null) {
                    selectedProfile.setSelected(false);
                }
                selectedProfile = profile;
                selectedProfile.setSelected(true);
            }
        }
        invalidateOptionsMenu();
        adapter.notifyDataSetChanged();
    }

    private void onProfileClick(ProfileAdapter adapter, int position) {
        openProfileEditor((Profile) adapter.getItem(position));
    }

    private void openProfileEditor(Profile profile) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(Profile.class.getCanonicalName(), profile);
        startActivity(intent);
    }
}

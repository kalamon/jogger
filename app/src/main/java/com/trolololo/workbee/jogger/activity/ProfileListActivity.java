package com.trolololo.workbee.jogger.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.adapter.ProfileAdapter;
import com.trolololo.workbee.jogger.domain.Profile;

import java.util.List;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        final FloatingActionsMenu addProfileMenu = findViewById(R.id.action_add_profile_menu);

//        final View menuBackground = findViewById(R.id.add_profile_menu_background);
//        menuBackground.setVisibility(View.GONE);
//        menuBackground.setOnClickListener(v -> addProfileMenu.collapse());
//        addProfileMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
//            @Override
//            public void onMenuExpanded() {
//                menuBackground.setVisibility(View.VISIBLE);
//            }

//            @Override
//            public void onMenuCollapsed() {
//                menuBackground.setVisibility(View.GONE);
//            }
//        });
        final FloatingActionButton addStoredOperationProfile = findViewById(R.id.action_add_duet_machine);
        addStoredOperationProfile.setOnClickListener(v -> {
//            addProfileMenu.collapse();
            addNewProfile(false);
        });

        DrawerLayout drawer = findViewById(R.id.main_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                TextView tv = findViewById(R.id.copyright_link);
                tv.setText(Html.fromHtml(getString(R.string.drawer_copyright), FROM_HTML_MODE_COMPACT));
                tv.setMovementMethod(LinkMovementMethod.getInstance());

                super.onDrawerOpened(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ((TextView) findViewById(R.id.add_profile_text)).setText(Html.fromHtml(getString(R.string.add_profile_description), FROM_HTML_MODE_COMPACT));

        Profile lastOpenProfile = setupListView();
        if (lastOpenProfile != null) {
            openProfile(lastOpenProfile);
        }
    }

    private Profile setupListView() {
        ListView listView = findViewById(R.id.profileList);
        List<Profile> profiles = Profile.loadAll(getApplicationContext());
        final ProfileAdapter adapter = new ProfileAdapter(this, profiles);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            onProfileLongClick(adapter, position);
            return true;
        });
        listView.setOnItemClickListener((parent, view, position, id) -> onProfileClick(adapter, position));
        showOrHideProfileList(listView, profiles);

        return Profile.getLastOpenProfile(this, profiles);
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
        Profile lastOpenProfile = setupListView();
        setMenuVisibility();
        if (lastOpenProfile != null) {
            openProfile(lastOpenProfile);
        }
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.main_layout);
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
        MenuItem item1 = menu.findItem(R.id.action_edit);
        MenuItem item2 = menu.findItem(R.id.action_delete);
        item1.setVisible(selectedProfile != null);
        item2.setVisible(selectedProfile != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            openProfileEditor(selectedProfile);
            return true;
        } else if (id == R.id.action_delete) {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteSelectedProfile();
                        setMenuVisibility();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //"No" button clicked
                        break;
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

        return super.onOptionsItemSelected(item);
    }


    private void deleteSelectedProfile() {
        List<Profile> profiles = Profile.remove(getApplicationContext(), selectedProfile);
        selectedProfile = null;
        ListView listView = findViewById(R.id.profileList);
        ProfileAdapter adapter = (ProfileAdapter) listView.getAdapter();
        adapter.setProfiles(profiles);
        adapter.notifyDataSetChanged();
        Profile.setLastOpenProfile(this, null);
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
        }

        DrawerLayout drawer = findViewById(R.id.main_layout);
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
        Profile profile = (Profile) adapter.getItem(position);
        openProfile(profile);
    }

    private void openProfile(Profile profile) {
        Intent intent = new Intent(this, JogActivity.class);
        intent.putExtra(Profile.class.getCanonicalName(), profile);
        startActivity(intent);
    }

    private void openProfileEditor(Profile profile) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(Profile.class.getCanonicalName(), profile);
        startActivity(intent);
    }
}

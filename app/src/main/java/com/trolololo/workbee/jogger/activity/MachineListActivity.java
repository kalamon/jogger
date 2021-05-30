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
import com.trolololo.workbee.jogger.adapter.MachineAdapter;
import com.trolololo.workbee.jogger.domain.Machine;

import java.util.List;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

public class MachineListActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Machine selectedMachine = null;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_list);
        setTitle(R.string.title_activity_profile_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton addMachine = findViewById(R.id.action_add_duet_machine);
        addMachine.setOnClickListener(v -> {
            addNewMachine();
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

        ((TextView) findViewById(R.id.add_profile_text)).setText(Html.fromHtml(getString(R.string.add_machine_description), FROM_HTML_MODE_COMPACT));

        Machine lastOpenMachine = setupListView();
        if (lastOpenMachine != null) {
            openProfile(lastOpenMachine);
        }
    }

    private Machine setupListView() {
        ListView listView = findViewById(R.id.machineList);
        List<Machine> machines = Machine.loadAll(getApplicationContext());
        final MachineAdapter adapter = new MachineAdapter(this, machines);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            onProfileLongClick(adapter, position);
            return true;
        });
        listView.setOnItemClickListener((parent, view, position, id) -> onProfileClick(adapter, position));
        showOrHideProfileList(listView, machines);

        return Machine.getLastOpenProfile(this, machines);
    }

    private void showOrHideProfileList(ListView listView, List<Machine> machines) {
        if (machines.size() > 0) {
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
        selectedMachine = null;
        Machine lastOpenMachine = setupListView();
        setMenuVisibility();
        if (lastOpenMachine != null) {
            openProfile(lastOpenMachine);
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
        getMenuInflater().inflate(R.menu.menu_machine_list, menu);
        setMenuVisibility();

        return true;
    }

    private void setMenuVisibility() {
        MenuItem item1 = menu.findItem(R.id.action_edit);
        MenuItem item2 = menu.findItem(R.id.action_delete);
        item1.setVisible(selectedMachine != null);
        item2.setVisible(selectedMachine != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            openProfileEditor(selectedMachine);
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
        List<Machine> machines = Machine.remove(getApplicationContext(), selectedMachine);
        selectedMachine = null;
        ListView listView = findViewById(R.id.machineList);
        MachineAdapter adapter = (MachineAdapter) listView.getAdapter();
        adapter.setMachines(machines);
        adapter.notifyDataSetChanged();
        Machine.setLastOpenProfile(this, null);
        showOrHideProfileList(listView, machines);
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

    private void addNewMachine() {
        Intent intent = new Intent(this, MachineActivity.class);
        startActivity(intent);
    }

    private void onProfileLongClick(MachineAdapter adapter, int position) {
        if (position < 0) {
            if (selectedMachine != null) {
                selectedMachine.setSelected(false);
                selectedMachine = null;
            }
        } else {
            Machine machine = (Machine) adapter.getItem(position);
            if (selectedMachine != null && machine == selectedMachine) {
                selectedMachine.setSelected(false);
                selectedMachine = null;
            } else {
                if (selectedMachine != null) {
                    selectedMachine.setSelected(false);
                }
                selectedMachine = machine;
                selectedMachine.setSelected(true);
            }
        }
        invalidateOptionsMenu();
        adapter.notifyDataSetChanged();
    }

    private void onProfileClick(MachineAdapter adapter, int position) {
        Machine machine = (Machine) adapter.getItem(position);
        openProfile(machine);
    }

    private void openProfile(Machine machine) {
        Intent intent = new Intent(this, JogActivity.class);
        intent.putExtra(Machine.class.getCanonicalName(), machine);
        startActivity(intent);
    }

    private void openProfileEditor(Machine machine) {
        Intent intent = new Intent(this, MachineActivity.class);
        intent.putExtra(Machine.class.getCanonicalName(), machine);
        startActivity(intent);
    }
}

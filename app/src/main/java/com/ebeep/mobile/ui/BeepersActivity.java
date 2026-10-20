package com.ebeep.mobile.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.Beeper;
import com.ebeep.mobile.data.DatabaseHelper;
import com.ebeep.mobile.data.Session;
import com.ebeep.mobile.net.BeeperService;

import java.util.ArrayList;

/** Screen 4: My Beepers, the list of beepers the user connected. */
public class BeepersActivity extends BaseActivity implements BeeperAdapter.OnBeeperClickListener {

    private RecyclerView listBeepers;
    private TextView txtEmpty;

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Toast.makeText(this, R.string.beepers_notifications_denied, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beepers);
        SystemBars.apply(this, findViewById(R.id.root));
        setUpToolbar(R.string.beepers_title, false);

        listBeepers = findViewById(R.id.listBeepers);
        txtEmpty = findViewById(R.id.txtEmpty);
        listBeepers.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btnAddBeeper).setOnClickListener(v ->
                startActivity(new Intent(this, AddBeeperActivity.class)));

        askForNotificationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload, so a new, renamed or removed beeper shows up when we come back
        loadBeepers();
    }

    private void loadBeepers() {
        DatabaseHelper database = new DatabaseHelper(this);
        ArrayList<Beeper> beepers = database.getBeepers(Session.getUserId(this));
        database.close();

        listBeepers.setAdapter(new BeeperAdapter(beepers, this));
        txtEmpty.setVisibility(beepers.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    // ---- Menu ----

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_beepers, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuSettings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.menuLogout) {
            confirmLogOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ---- List clicks ----

    @Override
    public void onBeeperClick(Beeper beeper) {
        Session.setCurrentBeeper(this, beeper.getId());
        startActivity(new Intent(this, BeeperHomeActivity.class));
    }

    @Override
    public void onBeeperLongClick(Beeper beeper) {
        String[] options = {
                getString(R.string.beepers_option_rename),
                getString(R.string.beepers_option_remove)
        };
        new AlertDialog.Builder(this)
                .setTitle(beeper.getNickname())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(beeper);
                    } else {
                        confirmRemove(beeper);
                    }
                })
                .show();
    }

    private void showRenameDialog(Beeper beeper) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_rename, null);
        EditText editNewName = dialogView.findViewById(R.id.editNewName);
        editNewName.setText(beeper.getNickname());

        new AlertDialog.Builder(this)
                .setTitle(R.string.beepers_rename_title)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String newName = editNewName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        renameBeeper(beeper.getId(), newName);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void renameBeeper(long beeperRowId, String newName) {
        DatabaseHelper database = new DatabaseHelper(this);
        database.renameBeeper(beeperRowId, newName);
        database.close();
        loadBeepers();
    }

    private void confirmRemove(Beeper beeper) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.beepers_remove_title)
                .setMessage(getString(R.string.beepers_remove_message, beeper.getNickname()))
                .setPositiveButton(R.string.yes, (dialog, which) -> removeBeeper(beeper))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void removeBeeper(Beeper beeper) {
        // The service must not keep a connection to a beeper that no longer exists
        if (beeper.getId() == Session.getCurrentBeeper(this)) {
            BeeperService.stop(this);
        }
        DatabaseHelper database = new DatabaseHelper(this);
        database.deleteBeeper(beeper.getId());
        database.close();
        loadBeepers();
    }
}

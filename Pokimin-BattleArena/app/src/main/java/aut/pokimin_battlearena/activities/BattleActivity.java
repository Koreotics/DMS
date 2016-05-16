package aut.pokimin_battlearena.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import java.io.Serializable;

import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.fragments.BattleFragment;
import aut.pokimin_battlearena.fragments.ResultFragment;
import aut.pokimin_battlearena.dialogs.SearchDialog;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class BattleActivity extends Activity implements Serializable {

    SearchDialog search;
    BroadcastReceiver receiver;
    BluetoothAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        if (savedInstanceState == null) {

            FragmentManager     manager     = this.getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            switch (getIntent().getIntExtra("fragmentID", 0)) {
                case (0): // search
                    search = new SearchDialog();
                    search.show(manager, "Search");

                    break;
                case (1): // battle
                    BattleFragment battle = new BattleFragment();
                    transaction.add(R.id.fragment_battle, battle);
                    transaction.commit();
                    break;
                case (2): // result
                    ResultFragment result = new ResultFragment();
                    transaction.replace(R.id.battle_fragment, result);
                    break;
            }

            transaction.addToBackStack(null);
            transaction.commit();
        }


    }

    public void bluetooth() {

        // check if device supports bluetooth
        adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {
            search.setMessage("This device does not support bluetooth. Get an upgrade mate");
        } else {
            if (receiver == null) {
                receiver = new BluetoothReceiver();
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(receiver, filter);

            if (!adapter.isEnabled()) {
                search.setMessage("Bluetooth is disabled. Please enable to proceed.");
                // TODO: enable bluetooth
                final Button bluetoothButton = search.getBluetoothButton();
                bluetoothButton.setVisibility(View.VISIBLE);
                bluetoothButton.setOnClickListener( new AdapterView.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.enable();
                        bluetoothButton.setVisibility(View.GONE);
                    }
                });
            } else {
                search.setMessage("Bluetooth is enabled");
            }
        }
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (receiver != null) { unregisterReceiver(receiver); }
    }


    public class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int newState
                        = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (newState) {
                    case BluetoothAdapter.STATE_OFF :
                        search.setMessage("Bluetooth is disabled");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON :
                        search.setMessage("Enabling bluetooth...");
                        break;
                    case BluetoothAdapter.STATE_ON :
                        search.setMessage("Bluetooth has been enabled");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF :
                        search.setMessage("Disabling bluetooth...");
                        break;
                }
            }
            else if (intent.getAction().equals
                    (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
            {  int newScanMode = intent.getIntExtra
                    (BluetoothAdapter.EXTRA_SCAN_MODE, -1);
                switch (newScanMode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        search.setMessage("Bluetooth is connected and is discoverable");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE :
                        search.setMessage("Bluetooth is connected");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE :
                        search.setMessage("Bluetooth is not connected or discoverable");
                        break;
                }
            }
        }
    }
}

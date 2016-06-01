package aut.pokimin_battlearena.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import aut.pokimin_battlearena.Objects.Player;
import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.activities.BattleActivity;
import aut.pokimin_battlearena.activities.MainActivity;
import aut.pokimin_battlearena.services.BluetoothClient;
import aut.pokimin_battlearena.services.BluetoothNode;
import aut.pokimin_battlearena.services.BluetoothServer;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class SearchDialog extends DialogFragment implements AdapterView.OnClickListener {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // FIELDS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    BattleActivity activity;
    Thread clientThread;
    Thread serverThread;
    BluetoothNode clientNode;
    BluetoothNode serverNode;

    // text view field
    private TextView message;

    // button fields
    private Button search;
    private Button server;
    private Button cancel;

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // DIALOG FRAGMENT
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // building the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // set the layout of the dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_search, null);
        builder.setView(view);

        // retrieving views from layout
        message   = (TextView) view.findViewById(R.id.search_message);
        search    = (Button) view.findViewById(R.id.search_button);
        server    = (Button) view.findViewById(R.id.server_button);
        cancel    = (Button) view.findViewById(R.id.cancel_button);

        // setting listeners to buttons
        search.setOnClickListener(this);
        server.setOnClickListener(this);
        cancel.setOnClickListener(this);

        // create the dialog
        return builder.create();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // MUTATOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void setMessage(String message) { this.message.setText(message); }

    public void showServerButton() { this.server.setVisibility(View.VISIBLE); }
    public void showSearchButton() { this.search.setVisibility(View.VISIBLE); }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // ACCESSOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public TextView getMessageView() { return message; }
    public Button getServerButton()  { return server; }
    public Button getSearchButton()  { return search; }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // ON CLICK LISTENER
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public void onClick(View view) {
        activity = (BattleActivity) getActivity();

        if (view == search) {

            if (activity.configBluetooth()) {
                search.setVisibility(View.GONE);

                BluetoothNode client = new BluetoothClient(this.getActivity());
                client.registerActivity(activity);

                clientThread = new Thread(client);
                clientThread.start();
            }
        } else if (view == cancel) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);

            if (serverNode != null) { serverNode.stop(); }
            if (clientNode != null) { clientNode.stop(); }

        } else if (view == server) {

            message.setText("You have become a server. Awaiting challenger...");
            search.setVisibility(View.GONE);

            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(intent);

            BluetoothNode server = new BluetoothServer(this.getActivity());
            server.registerActivity(activity);
            serverThread = new Thread(server);
            serverThread.start();

            if (clientNode != null) { clientNode.stop(); }
        }
    }
}

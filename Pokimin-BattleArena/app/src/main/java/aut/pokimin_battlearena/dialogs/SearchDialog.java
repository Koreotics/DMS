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

import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.activities.BattleActivity;
import aut.pokimin_battlearena.activities.MainActivity;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class SearchDialog extends DialogFragment {

    private TextView message;

    private Button search;
    private Button bluetooth;
    private Button server;
    private Button cancel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_search, null);
        builder.setView(view);

        message = (TextView) view.findViewById(R.id.search_message);

        search = (Button) view.findViewById(R.id.search_button);
        search.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                BattleActivity activity = (BattleActivity) getActivity();
                activity.bluetooth();
            }
        });

        bluetooth = (Button) view.findViewById(R.id.bluetooth_button);

        server = (Button) view.findViewById(R.id.server_button);

        cancel = (Button) view.findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        return builder.create();
    }

    public void setMessage(String message) { this.message.setText(message); }
    public Button getBluetoothButton() { return bluetooth; }
    public Button   getSearchButton() { return search; }
    public Button   getServerButton() { return server; }
}

package aut.pokimin_battlearena.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.activities.MainActivity;
import aut.pokimin_battlearena.fragments.MainMenuFragment;
import aut.pokimin_battlearena.services.DatabaseController;


/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class RegisterDialog extends DialogFragment implements AdapterView.OnClickListener {

    private Bundle bundle;

    private EditText username;
    private Button   register;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_register, null);
        builder.setView(view);

        bundle = savedInstanceState;

        username = (EditText) view.findViewById(R.id.player_name_input);
        register = (Button)   view.findViewById(R.id.register_button);
        register.setOnClickListener(this);

        return builder.create();
    }

    @Override
    public void onClick(View v) {
        String name = username.getText().toString();

        DatabaseController database = (DatabaseController) getArguments().getSerializable("database");
        database.createPlayer(name);

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }
}

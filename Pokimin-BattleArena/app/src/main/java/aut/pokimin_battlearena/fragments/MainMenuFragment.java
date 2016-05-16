package aut.pokimin_battlearena.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.activities.BattleActivity;
import aut.pokimin_battlearena.activities.MainActivity;
import aut.pokimin_battlearena.activities.MinionActivity;
import aut.pokimin_battlearena.activities.PlayerActivity;


/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class MainMenuFragment extends ListFragment implements AdapterView.OnItemClickListener {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // FIELD
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    OnFragmentInteractionListener mListener;

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // LIST FRAGMENT
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, viewGroup, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set string values for list
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.menu_list,
                android.R.layout.simple_list_item_1);
        setListAdapter(adapter);

        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Activity activity = getActivity();
        Intent intent = new Intent(getActivity(), MainActivity.class);

        // Creating content for intent
        switch (position) {
            case 0: intent = new Intent(activity, PlayerActivity.class); break;
            case 1: intent = new Intent(activity, MinionActivity.class); break;
            case 2:
                intent.putExtra("fragmentID", 0);
                intent = new Intent(activity, BattleActivity.class);
                break;
        }

        startActivity(intent);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CLASSES
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public interface OnFragmentInteractionListener {
        void onMainMenuFragmentInteraction(int position);
    }
}

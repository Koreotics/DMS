package aut.pokimin_battlearena.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Player;
import aut.pokimin_battlearena.Objects.Skill;
import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.activities.BattleActivity;
import aut.pokimin_battlearena.services.BluetoothNode;
import aut.pokimin_battlearena.utils.MovesAdapter;


/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class BattleFragment extends Fragment {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // FIELDS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    OnFragmentInteractionListener mListener;

    BluetoothNode bluetoothNode;
    BattleActivity battleActivity;

    TextView message;
    TextView opponent;
    TextView player;

    GridView moveSet;

    ProgressBar opponentHealth;
    ProgressBar playerHealth;

    Button test;

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // FRAGMENT
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle savedInstanceState) {

        battleActivity = (BattleActivity) getActivity();
        bluetoothNode = battleActivity.getBluetoothNode();

        View view = inflater.inflate(R.layout.fragment_battle, viewGroup, false);

        // retrieving views from layout
        message  = (TextView) view.findViewById(R.id.battle_response_message);
        opponent = (TextView) view.findViewById(R.id.battle_opponent_name);
        player   = (TextView) view.findViewById(R.id.battle_player_name);
        moveSet  = (GridView) view.findViewById(R.id.move_sets);
        opponentHealth = (ProgressBar) view.findViewById(R.id.opponent_hp);
        playerHealth   = (ProgressBar) view.findViewById(R.id.player_hp);

        playerHealth.setMax(battleActivity.getPlayer().getActiveMonster().getHealth());
        test = (Button) view.findViewById(R.id.testButton);
        test.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View v) {
                //when test button is pressed
                bluetoothNode.forward("test");
            }
        });

        final BattleActivity activity = (BattleActivity) getActivity();
        Player player1 = activity.getPlayer();
        Monster minion = player1.getActiveMonster();

        ArrayList<Skill> minionMoves = new ArrayList<>();

        minionMoves.add(minion.getSkill1());
        minionMoves.add(minion.getSkill2());
        minionMoves.add(minion.getSkill3());
        minionMoves.add(minion.getSkill4());

        // setting player information
        player.setText(minion.getName());
        playerHealth.setProgress(minion.getHealth());

        // setting moves within moveSet
        Skill[] skills =  minionMoves.toArray(new Skill[minionMoves.size()]);
        final MovesAdapter adapter = new MovesAdapter(view.getContext(), skills);
        moveSet.setAdapter(adapter);
        moveSet.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Skill selectedMove = (Skill) adapter.getItem(position);
                activity.sendActiveSkill(selectedMove);
            }
        });

        return view;
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

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // ACCESSOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void setMessage(String message)         { this.message.setText(message); }
    public void setOpponentName(String player)     { this.opponent.setText(player); }
    public void setPlayerName(String player)       { this.player.setText(player); }
    public void setOpponentHealth(Monster monster) { this.opponentHealth.setProgress(monster.getHealth()); }
    public void setPlayerHealth(Monster monster)   { this.playerHealth.setProgress(monster.getHealth()); }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // UTILITY
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void checkHealth() {

        Intent intent = null;

        if (opponentHealth.getProgress() <= 0) {
            intent = new Intent(getActivity(), BattleActivity.class);
            intent.putExtra("result", opponent.getText());
        } else if (playerHealth.getProgress() <= 0) {
            intent = new Intent(getActivity(), BattleActivity.class);
            intent.putExtra("result", player.getText());
        }

        if (intent != null) {
            intent.putExtra("fragmentID", BattleActivity.RESULT_FRAGMENT);
            startActivity(intent);
        }

    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CLASS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public interface OnFragmentInteractionListener {
        void onBattleFragmentInteraction();
    }


}

package aut.pokimin_battlearena.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import aut.pokimin_battlearena.Objects.Skill;
import aut.pokimin_battlearena.R;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class MovesAdapter extends BaseAdapter {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // FIELDS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private Context context;
    private final Skill[] moveSet;

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public MovesAdapter(Context context, Skill[] moveSet) {
        this.context = context;
        this.moveSet = moveSet;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // ACCESSORS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override public int    getCount()              { return moveSet.length; }
    @Override public long   getItemId(int position) { return position; }
    @Override public Object getItem(int position)   { return moveSet[position]; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_move_set, null);

            TextView moveName  = (TextView) view.findViewById(R.id.battle_move_name);
            TextView moveType  = (TextView) view.findViewById(R.id.battle_move_type);
            TextView moveCount = (TextView) view.findViewById(R.id.battle_move_count);

            moveName.setText(moveSet[position].getName());
            moveType.setText(moveSet[position].getType());
            moveCount.setText(moveSet[position].getMaxPP()+"");
        } else { view = (View) convertView; }

        return view;
    }
}

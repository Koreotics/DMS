package aut.pokimin_battlearena.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.Serializable;

import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Skill;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class DatabaseController extends SQLiteOpenHelper implements Serializable{

    public static final String DATABASE_NAME = "Pokimin.db";


    public DatabaseController(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v("test", "success1");
        String createSkillTable = "CREATE TABLE Skills (" +
                "Name           text primary key, " +
                "Type           text, " +
                "Multiply       real," +
                "MaxPP          integer," +
                "Speed          integer);";

        //Creates table that Holds monster details at lvl 1
        String createMonsterTable = "CREATE TABLE Monsters (" +
                "Name           text primary key, " +
                "Element        text," +
                "Health         integer," +
                "Defence        real," +
                "Attack         integer," +
                "Speed          integer," +
                "Skill1         text," +
                "Skill2         text," +
                "Skill3         text," +
                "Skill4         text," +
                "FOREIGN KEY(Skill1) REFERENCES Skills(Name)," +
                "FOREIGN KEY(Skill2) REFERENCES Skills(Name)," +
                "FOREIGN KEY(Skill3) REFERENCES Skills(Name)," +
                "FOREIGN KEY(Skill4) REFERENCES Skills(Name));";

        String createPartyTable = "CREATE TABLE Party (" +
                "Name           text primary key, " +
                "Level          integer, " +
                "Exp            integer," +
                "Health         integer," +
                "Defence        real," +
                "Attack         integer," +
                "Speed          integer," +
                "FOREIGN KEY(Name) REFERENCES Monsters(Name));";

        String createPlayerTable = "CREATE TABLE Player (" +
                "Name           text  primary key, " +
                "ActiveMonster  text," +
                "FOREIGN KEY(ActiveMonster) REFERENCES Party(Name));";

        db.execSQL(createSkillTable);
        db.execSQL(createMonsterTable);
        db.execSQL(createPartyTable);
        db.execSQL(createPlayerTable);

        prepopulateTables(db);
        // inserts item


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v("monster", "22");
        // sql statements as String
        String dropMonstersTable = "drop table if exists Monsters";
        String dropSkillsTable = "drop table if exists Skills";
        String dropPlayerTable = "drop table if exists Player";
        String dropPartyTable = "drop table if exists Party";

        // passes statements to sql
        db.execSQL(dropMonstersTable);
        db.execSQL(dropSkillsTable);
        db.execSQL(dropPlayerTable);
        db.execSQL(dropPartyTable);


        // recreate tables
        onCreate(db);
    }

    public void prepopulateTables(SQLiteDatabase db){


        //------------------------------------------------------------------------------------------
        //Populate Skills table
        //------------------------------------------------------------------------------------------
        ContentValues values = new ContentValues();
        values.put("Name", "Tackle");
        values.put("Type", "damage");
        values.put("multiply", 1.2);
        values.put("MaxPP", 20);
        values.put("Speed", 10);
        db.insert("Skills", null, values);

        values.clear();
        values.put("Name", "Ember");
        values.put("Type", "damage");
        values.put("multiply", 1.5);
        values.put("MaxPP", 5);
        values.put("Speed", 15);
        db.insert("Skills", null, values);

        values.clear();
        values.put("Name", "Protect");
        values.put("Type", "protect");
        values.put("multiply", 0);
        values.put("MaxPP", 4);
        values.put("Speed", 20);
        db.insert("Skills", null, values);

        values.clear();
        values.put("Name", "Harden");
        values.put("Type", "defence");
        values.put("multiply", 1.2);
        values.put("MaxPP", 5);
        values.put("Speed", 18);
        db.insert("Skills", null, values);

        Log.v("test", "success");

        //------------------------------------------------------------------------------------------
        //Populate Monster table
        //------------------------------------------------------------------------------------------

        values.clear();
        values.put("Name", "Blop");
        values.put("Element", "Fire");
        values.put("Health", 20);
        values.put("Attack", 3);
        values.put("Defence", 0.1);
        values.put("Speed", 1);
        values.put("Skill1", "Tackle");
        values.put("Skill2", "Ember");
        values.put("Skill3", "Harden");
        values.put("Skill4", "Protect");

        db.insert("Monsters", null, values);

        initParty(db);
    }

    //------------------------------------------------------------------------------------------
    //Monster Related methods
    //------------------------------------------------------------------------------------------

    //Sets monsters skills and element
    public void setMonsterStandardInfo(Context context, String name, Monster monster){

        String skillName1, skillName2, skillName3, skillName4;

        // access database
        SQLiteDatabase db = this.getWritableDatabase();

        // gets monster row
        String selectQuery = "SELECT element, Skill1, Skill2, Skill3, Skill4 FROM Monsters Where name Like \'" +name+ "\'";
        Cursor cursor = db.rawQuery(selectQuery, null);

        // get skill names and monster element from monter
        if (cursor.moveToFirst()) {
                monster.setElement(cursor.getString(cursor.getColumnIndex("Element")));
                skillName1 = cursor.getString(cursor.getColumnIndex("Skill1"));
                skillName2 = cursor.getString(cursor.getColumnIndex("Skill2"));
                skillName3 = cursor.getString(cursor.getColumnIndex("Skill3"));
                skillName4 = cursor.getString(cursor.getColumnIndex("Skill4"));

                monster.setSkill1(new Skill(context, skillName1));
                monster.setSkill2(new Skill(context, skillName2));
                monster.setSkill3(new Skill(context, skillName3));
                monster.setSkill4(new Skill(context, skillName4));
            Log.v("monster", "10");
        }

    }
    //sets monsters current level/exp/health/attack/speed
    public void setMonsterCurrentStats( String name, Monster monster){

        SQLiteDatabase db = this.getWritableDatabase();
        // gets monster row
        String selectQuery = "SELECT Level, Exp, Health, Defence, Attack, Speed FROM Party Where name Like \'" +name+ "\'";
        Cursor cursor2 = db.rawQuery(selectQuery, null);
        Log.v("monster", "222");
        // get skill names and monster element from monster
        if (cursor2.moveToFirst()) {
            Log.v("monster", "3");
            monster.setLevel(cursor2.getInt(cursor2.getColumnIndex("Level")));
            monster.setExp(cursor2.getInt(cursor2.getColumnIndex("Exp")));
            monster.setHealth(cursor2.getInt(cursor2.getColumnIndex("Health")));
            monster.setDefence(cursor2.getDouble(cursor2.getColumnIndex("Defence")));
            monster.setAttack(cursor2.getInt(cursor2.getColumnIndex("Attack")));
            monster.setSpeed(cursor2.getInt(cursor2.getColumnIndex("Speed")));

        }
    }

    public void setSkillInfo(String name, Skill skill){
        // access database
        SQLiteDatabase db = this.getWritableDatabase();

        // gets monster row
        String selectQuery = "SELECT * FROM Skills Where name Like \'" +name+ "\'";
        Cursor cursor = db.rawQuery(selectQuery, null);

        //
        if (cursor.moveToFirst()) {
            skill.setType(cursor.getString(cursor.getColumnIndex("Type")));
            skill.setMultiply(cursor.getDouble(cursor.getColumnIndex("Multiply")));
            skill.setMaxPP(cursor.getInt(cursor.getColumnIndex("MaxPP")));
            skill.setSpeed(cursor.getInt(cursor.getColumnIndex("Speed")));

            cursor.close();
        }
        Log.v("skill", "2");

    }

    //------------------------------------------------------------------------------------------
    //Player Related methods not tested yet
    //------------------------------------------------------------------------------------------


    public void createPlayer(String name){

        // access database
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Name", name);

        // gets monster row
        String selectQuery = "SELECT * FROM Monsters ";
        Cursor cursor = db.rawQuery(selectQuery, null);



        if (cursor.moveToFirst()) {
                values.put("ActiveMonster", cursor.getString(cursor.getColumnIndex("Name")));

            }

        db.insert("Player", null, values);
    }

    public boolean isPlayerExist() {

        // access database
        SQLiteDatabase db = this.getWritableDatabase();

        // check if record exists in player table
        String query = "SELECT * FROM player";

        Cursor cursor = db.rawQuery(query, null);

        return cursor.moveToFirst();
    }

    public String getPlayerName() {

        String name = "";

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM player";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex("Name"));
            cursor.close();
        }

        return name;
    }
    public void setActiveMonster(String playerName, String monsterName){
        // access database
        SQLiteDatabase db = this.getWritableDatabase();

        // updates quantity for row with parameter name
        ContentValues values = new ContentValues();
        values.put("ActiveMonster", monsterName);


        db.update("Player", values, "Name=?", new String[]{playerName});
        // gets monster row
//        String updateQuery = "UPDATE Player SET ActiveMonster=\'" +monsterName+ "\' where Name=" +playerName;
//        db.execSQL(updateQuery);

        Log.v("skill", "2");
    }

    public String getActiveMonsterName(String playerName){
        // access database
        SQLiteDatabase db = this.getWritableDatabase();
        String activeMon = "";
        // gets monster row
        String selectQuery = "SELECT * FROM Player Where name Like \'" +playerName+ "\'";
        Cursor cursor = db.rawQuery(selectQuery, null);

        //
        if (cursor.moveToFirst()) {
            activeMon = cursor.getString(cursor.getColumnIndex("ActiveMonster"));
            cursor.close();
        }
        return activeMon;
    }

    //------------------------------------------------------------------------------------------
    //Party Related methods
    //------------------------------------------------------------------------------------------

    //Called when tables are first created only, transfers monster data into party table
    public void initParty(SQLiteDatabase db){


        // gets monster row
        String selectQuery = "SELECT * FROM Monsters ";
        Cursor cursor = db.rawQuery(selectQuery, null);
        ContentValues values = new ContentValues();


        if (cursor.moveToFirst()) {
            do{
                values.put("Name", cursor.getString(cursor.getColumnIndex("Name")));
                values.put("Level", 1);
                values.put("Exp", 0);
                values.put("Health", cursor.getInt(cursor.getColumnIndex("Health")));
                values.put("Defence", cursor.getDouble(cursor.getColumnIndex("Defence")));
                values.put("Attack", cursor.getInt(cursor.getColumnIndex("Attack")));
                values.put("Speed", cursor.getInt(cursor.getColumnIndex("Speed")));


        } while (cursor.moveToNext());
            db.insert("Party", null, values);
        }
        else{Log.v("Fail", "party table init failed");}

    }

    public void updateMonsterInfo(Monster monster){

        // Level up automatically if xp reaches a certain threshold
        int threshold = 100;

        if(monster.getExp() >= threshold){
            monster.setExp      (monster.getExp()-threshold);
            monster.setAttack   (monster.getLevel()*5);
            monster.setDefence  (monster.getLevel());
            monster.setHealth   (monster.getLevel()*5+(10));

            monster.setLevel    (monster.getLevel()+1);
        }

        // access database
        SQLiteDatabase db = this.getWritableDatabase();

        // updates quantity for row with parameter name
        ContentValues values = new ContentValues();
        values.put("Level", monster.getLevel());
        values.put("Exp", monster.getExp());
        values.put("Health", monster.getHealth());
        values.put("Defence", monster.getDefence());
        values.put("Attack", monster.getAttack());
        values.put("Speed", monster.getSpeed());

        db.update("Party", values, "Name=?", new String[]{monster.getName()});
    }

}

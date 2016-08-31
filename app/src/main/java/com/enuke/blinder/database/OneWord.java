package com.enuke.blinder.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.enuke.blinder.Entity.AvatarEntity;
import com.enuke.blinder.Entity.OneWordEntity;
import com.xabber.android.data.DatabaseManager;
import com.xabber.android.data.entity.AbstractEntityTable;

import java.util.ArrayList;

/**
 * Created by amrit on 10/3/15.
 */


public class OneWord extends AbstractEntityTable {


    private static final class Fields implements AbstractEntityTable.Fields {


        public static final String ONE_WORD_ID = "onewordid";
        public static final String ONE_WORD = "oneword";
        public static final String ONE_WORD_GENDER = "onewordgender";

        private Fields() {
        }


    }


    private static final String NAME = "oneword";
    private final static OneWord instance;
    static {
        instance = new OneWord(DatabaseManager.getInstance());
        DatabaseManager.getInstance().addTable(instance);
    }    private static final String[] PROJECTION = new String[]{Fields.ONE_WORD_ID,
            Fields.ONE_WORD,Fields.ONE_WORD_GENDER};
    private final DatabaseManager databaseManager;
    private final Object insertNewLoginLock;
    private SQLiteStatement insertNewLoginStatement;

    private OneWord(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        insertNewLoginStatement = null;
        insertNewLoginLock = new Object();
    }

    public static OneWord getInstance() {
        return instance;
    }

    @Override
    public void create(SQLiteDatabase db) {
        String sql;

        sql = "CREATE TABLE " + NAME + " (" + Fields.ONE_WORD_ID
                + " INTEGER PRIMARY KEY,"
                + Fields.ONE_WORD + " TEXT,"+Fields.ONE_WORD_GENDER+" TEXT);";
        DatabaseManager.execSQL(db, sql);
        sql = "CREATE INDEX " + NAME + "_list ON " + NAME + " ("
                + Fields.ONE_WORD_ID
                + " ASC)";
        DatabaseManager.execSQL(db, sql);
    }

    @Override
    protected String getTableName() {
        return NAME;
    }

    @Override
    protected String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public void migrate(SQLiteDatabase db, int toVersion) {
        super.migrate(db, toVersion);
        String sql;
        switch (toVersion) {

            default:
                break;
        }
    }

    /**
     * Save new user to the database.
     *
     * @return Assigned id.
     */
    public long add(int oneWordId,String oneWord, String oneWordGender) {


        synchronized (insertNewLoginLock) {
            if (insertNewLoginStatement == null) {
                SQLiteDatabase db = databaseManager.getWritableDatabase();
                insertNewLoginStatement = db.compileStatement("INSERT INTO "
                        + NAME + " (" + Fields.ONE_WORD_ID + ", " + Fields.ONE_WORD + ", " + Fields.ONE_WORD_GENDER + ") VALUES "
                        + "(?,?,?);");
            }
            insertNewLoginStatement.bindLong(1, oneWordId);
            insertNewLoginStatement.bindString(2, oneWord);
            insertNewLoginStatement.bindString(3, oneWordGender);
            return insertNewLoginStatement.executeInsert();
        }

    }

    //return true if there is any user present
    public boolean hasUser(int user) {
        SQLiteDatabase db = databaseManager.getWritableDatabase();
        Cursor cr = db.query(NAME, null, Fields.USER + " = ? ", new String[]{String.valueOf(user)},
                null, null, null);

        return ((cr.getCount() > 0)) ? true : false;
    }

    //Update the Other Details if its already in database
    public long updateCard(int oneWordId, String oneWord ,String oneWordGender) {
        SQLiteDatabase db = databaseManager.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Fields.ONE_WORD_ID, oneWordId);
        values.put(Fields.ONE_WORD, oneWord);
        values.put(Fields.ONE_WORD_GENDER, oneWordGender);


        return db.update(NAME, values, Fields.USER + " = ?",
                new String[]{String.valueOf(oneWordId)});
    }



    public ArrayList<AvatarEntity> getAllOneWords() {
        ArrayList<AvatarEntity> array_list = new ArrayList<AvatarEntity>();
        SQLiteDatabase db = databaseManager.getReadableDatabase();
        Cursor cursor = db.query(NAME, null, null, null,
                null, null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while(cursor.isAfterLast() == false) {
                String id = cursor.getString(cursor.getColumnIndex(Fields.ONE_WORD_ID));
                String word = cursor.getString(cursor.getColumnIndex(Fields.ONE_WORD));
                AvatarEntity entity = new AvatarEntity();
                entity.setAvatarId(id);
                entity.setAvatarName(word);
                array_list.add(entity);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return array_list;
    }
    public ArrayList<OneWordEntity> getAllSingleWords() {
        ArrayList<OneWordEntity> array_list = new ArrayList<OneWordEntity>();
        SQLiteDatabase db = databaseManager.getReadableDatabase();
        Cursor cursor = db.query(NAME, null, null, null,
                null, null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while(cursor.isAfterLast() == false) {
                String id = cursor.getString(cursor.getColumnIndex(Fields.ONE_WORD_ID));
                String word = cursor.getString(cursor.getColumnIndex(Fields.ONE_WORD));
                String gender = cursor.getString(cursor.getColumnIndex(Fields.ONE_WORD_GENDER));
                OneWordEntity entity = new OneWordEntity();
                entity.setAvatarId(id);
                entity.setAvatarName(word);
                entity.setAvatargender(gender);
                array_list.add(entity);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return array_list;
    }


    public long insertOneWordRecordIfNotExist(String id, String word, String gender){
        boolean isExist = false;
        ArrayList<AvatarEntity> onewords = getAllOneWords();
        for(int i=0; i<onewords.size(); i++) {
            if(onewords.get(i).getAvatarId().equalsIgnoreCase(id)) {
                isExist = true;
                break;
            }
        }

        if(!isExist) {
            SQLiteDatabase db = databaseManager.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            contentValues.put(Fields.ONE_WORD_ID, id);
            contentValues.put(Fields.ONE_WORD, word);
            contentValues.put(Fields.ONE_WORD_GENDER, gender);

            long rowid = db.insert(NAME, null, contentValues);

            db.close();

            return rowid;
        }

        return 0;
    }


    public String getOneWordId(String name) {
        String wordId = "";
        ArrayList<AvatarEntity> onewords = getAllOneWords();
        for(int i=0; i<onewords.size(); i++) {
            if(onewords.get(i).getAvatarName().equalsIgnoreCase(name)) {
                wordId = onewords.get(i).getAvatarId();
                break;
            }
        }
        return wordId;
    }

    public String getOneWord(String id) {
        String name = "";
        ArrayList<AvatarEntity> onewords = getAllOneWords();
        for(int i=0; i<onewords.size(); i++) {
            if(onewords.get(i).getAvatarId().equalsIgnoreCase(id)) {
                name = onewords.get(i).getAvatarName();
                break;
            }
        }
        return name;
    }


}
package com.enuke.blinder.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.enuke.blinder.Entity.UserMatchEntity;
import com.xabber.android.data.DatabaseManager;
import com.xabber.android.data.entity.AbstractEntityTable;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by enuke on 3/10/15.
 */
public  class UserMatchTable extends AbstractEntityTable {


    public static final class Fields implements AbstractEntityTable.Fields {


        public static final String USER_COLUMN_ID = "userid";
        // Shortlisted table fields
        public static final String USER_COLUMN_MATCHID = "matchid";
        public static final String USER_COLUMN_SHORTLISTED = "userisshortlisted";
        public static final String USER_COLUMN_VIEWED = "userisviewed";
        public static final String USER_COLUMN_MESSAGE_SENT = "userismessagesent";
        public static final String USER_COLUMN_XMPP_CHAT_STARTED = "userischatstarted";
        public static final String USER_COLUMN_FLAG = "userflag";
        public static final String USER_COLUMN_DATE = "userdate";

        private Fields() {
        }


    }


    public static final String NAME = "usermatchtable";
    private final static UserMatchTable instance;
    static {
        instance = new UserMatchTable(DatabaseManager.getInstance());
        DatabaseManager.getInstance().addTable(instance);
    }    private static final String[] PROJECTION = new String[]{Fields.USER_COLUMN_ID,
            Fields.USER_COLUMN_MATCHID,Fields.USER_COLUMN_SHORTLISTED,Fields.USER_COLUMN_VIEWED,
            Fields.USER_COLUMN_MESSAGE_SENT,Fields.USER_COLUMN_XMPP_CHAT_STARTED,Fields.USER_COLUMN_FLAG,Fields.USER_COLUMN_DATE};
    private final DatabaseManager databaseManager;
    private final Object insertNewLoginLock;
    private SQLiteStatement insertNewLoginStatement;

    private UserMatchTable(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        insertNewLoginStatement = null;
        insertNewLoginLock = new Object();
    }

    public static UserMatchTable getInstance() {
        return instance;
    }

    @Override
    public void create(SQLiteDatabase db) {
        String sql;
        sql = "CREATE TABLE " + NAME + " (" + Fields.USER_COLUMN_ID + " INTEGER PRIMARY KEY,"
                + Fields.USER_COLUMN_MATCHID + " INTEGER," + Fields.USER_COLUMN_SHORTLISTED  + " TEXT,"
                + Fields.USER_COLUMN_VIEWED  + " TEXT,"+ Fields.USER_COLUMN_MESSAGE_SENT  + " TEXT,"
                + Fields.USER_COLUMN_XMPP_CHAT_STARTED  + " TEXT,"+ Fields.USER_COLUMN_FLAG  + " TEXT,"
                + Fields.USER_COLUMN_DATE  + " TEXT);";
        DatabaseManager.execSQL(db, sql);
        sql = "CREATE INDEX " + NAME + "_list ON " + NAME + " ("
                + Fields.USER_COLUMN_ID
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
   /* public long add(int userId,String cardPosition) {


            synchronized (insertNewLoginLock) {
                if (insertNewLoginStatement == null) {
                    SQLiteDatabase db = databaseManager.getWritableDatabase();
                    insertNewLoginStatement = db.compileStatement("INSERT INTO "
                            + NAME + " (" + Fields.USER_COLUMN_ID + ", " + Fields.USER_CARD_POSITION + ") VALUES "
                            + "(?,?);");
                }
                insertNewLoginStatement.bindLong(1, userId);
                insertNewLoginStatement.bindString(2, cardPosition);



                return insertNewLoginStatement.executeInsert();
            }

    }*/

    //return true if there is any user present
    public boolean hasUser(int user) {
        SQLiteDatabase db = databaseManager.getWritableDatabase();
        Cursor cr = db.query(NAME, null, Fields.USER + " = ? ", new String[]{String.valueOf(user)},
                null, null, null);

        return ((cr.getCount() > 0)) ? true : false;
    }

    //Update the Other Details if its already in database
   /* public long updateCard(int userId, String cardPosition) {
        SQLiteDatabase db = databaseManager.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Fields.USER_COLUMN_ID, userId);
        values.put(Fields.USER_CARD_POSITION, cardPosition);


        return db.update(NAME, values, Fields.USER + " = ?",
                new String[]{String.valueOf(userId)});
    }*/


    public String getIsProfileViewed(String userId) {
        String isViewed = "0";
        SQLiteDatabase db = databaseManager.getReadableDatabase();
        Cursor cursor = db.query(NAME, null, Fields.USER_COLUMN_ID + " = ? ", new String[]{String.valueOf(userId)},
                null, null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while(cursor.isAfterLast() == false) {
                isViewed = cursor.getString(cursor.getColumnIndex(Fields.USER_COLUMN_VIEWED));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return isViewed;
    }



    public void updateUserXmppChatStartedFlag(Context context, String userId) {
        UserMatchEntity user = UserTable.getInstance().getUserMatch(userId);
        String xmppChatFlag = user.getUserisxmppchatstarted();
        if(xmppChatFlag.equalsIgnoreCase("0") || xmppChatFlag.equalsIgnoreCase("") || xmppChatFlag.equalsIgnoreCase("null")) {
            ContentValues values = new ContentValues();
            values.put(Fields.USER_COLUMN_XMPP_CHAT_STARTED, "1");
            try {
                updateShortlistedUserRecord(userId, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void updateUserMessageSentFlag(Context context, String userId) {
        UserMatchEntity user = UserTable.getInstance().getUserMatch(userId);
        String messageSentFlag = user.getUserismessagesent();
        if(messageSentFlag.equalsIgnoreCase("0") || messageSentFlag.equalsIgnoreCase("") || messageSentFlag.equalsIgnoreCase("null")) {
            ContentValues values = new ContentValues();
            values.put(Fields.USER_COLUMN_MESSAGE_SENT, "1");
            try {
                updateShortlistedUserRecord(userId, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getIsMessageSent(String userId) {
        String isMessageSent = "0";
        SQLiteDatabase db = databaseManager.getReadableDatabase();
        Cursor cursor = db.query(NAME, null, Fields.USER_COLUMN_ID + " = ? ", new String[]{String.valueOf(userId)},
                null, null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while(cursor.isAfterLast() == false) {
                isMessageSent = cursor.getString(cursor.getColumnIndex(Fields.USER_COLUMN_MESSAGE_SENT));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return isMessageSent;
    }

    public String getIsXmppChatStarted(String userId) {
        String isXmppChatStarted = "0";
        SQLiteDatabase db = databaseManager.getReadableDatabase();
        Cursor cursor = db.query(NAME, null, Fields.USER_COLUMN_ID + " = ? ", new String[]{String.valueOf(userId)},
                null, null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while(cursor.isAfterLast() == false) {
                isXmppChatStarted = cursor.getString(cursor.getColumnIndex(Fields.USER_COLUMN_XMPP_CHAT_STARTED));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return isXmppChatStarted;
    }




    /**
     * Insert shortlisted user record
     * @param contentValues
     * @return
     * @throws Exception
     */
    public long insertShortlistedUserRecord(ContentValues contentValues) throws Exception{
        SQLiteDatabase db = databaseManager.getWritableDatabase();
        long rowid = db.insert(NAME, null, contentValues);

        db.close();

        return rowid;
    }

    /**
     * Update shortlisted user record
     * @param id
     * @param contentValues
     * @return
     * @throws Exception
     */
    public int updateShortlistedUserRecord (String id, ContentValues contentValues) throws Exception{
        int noOfRows = 0;
        if(TextUtils.isEmpty(id)) {
            contentValues.put(Fields.USER_COLUMN_DATE, String.valueOf(Calendar.getInstance().getTimeInMillis()));
            insertShortlistedUserRecord(contentValues);
        } else {
            SQLiteDatabase db = databaseManager.getWritableDatabase();
            noOfRows = db.update(NAME, contentValues, Fields.USER_COLUMN_ID + " = ? ", new String[] {id} );
            if(noOfRows == 0) {
                // No record found
                System.out.println("user inserted id: " + id);
                contentValues.put(Fields.USER_COLUMN_DATE, String.valueOf(Calendar.getInstance().getTimeInMillis()));
                long rowid = db.insert(NAME, null, contentValues);
            } else {
                System.out.println("user updated id: " + id);
            }
            db.close();
        }
        return noOfRows;
    }

    public void updateMatchedUserRecord(Context context) {
        ArrayList<UserMatchEntity> users = UserTable.getInstance().getAllViewedMatches(context, "MATCHED");
        for(int i=0; i<users.size(); i++) {
            UserMatchEntity user = users.get(i);
            String userId = user.getUserid();

            ContentValues values = new ContentValues();
            values.put(Fields.USER_COLUMN_FLAG, "");

            try {
                updateShortlistedUserRecord(userId, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
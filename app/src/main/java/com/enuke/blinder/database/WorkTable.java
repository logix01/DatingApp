package com.enuke.blinder.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.enuke.blinder.Entity.WorkEntity;
import com.xabber.android.data.DatabaseManager;
import com.xabber.android.data.entity.AbstractEntityTable;

import java.util.ArrayList;

/**
 * Created by nitesh on 10/3/15.
 */

public class WorkTable extends AbstractEntityTable {


    private static final class Fields implements AbstractEntityTable.Fields {


        public static final String WORK_ID = "workid";
        public static final String USER_ID = "userid";
        public static final String WORK_DESCRIPTION = "workdescription";
        public static final String WORK_EMPLOYER = "workemployer";
        public static final String WORK_LOCATION = "worklocation";


        private Fields() {
        }
    }

    private static final String NAME = "worktable";
    private final static WorkTable instance;
    static {
        instance = new WorkTable(DatabaseManager.getInstance());
        DatabaseManager.getInstance().addTable(instance);
    }    private static final String[] PROJECTION = new String[]{Fields.WORK_ID,Fields.USER_ID,Fields.WORK_DESCRIPTION,Fields.WORK_LOCATION,Fields.WORK_EMPLOYER,};
    private final DatabaseManager databaseManager;
    private final Object insertNewLoginLock;
    private SQLiteStatement insertNewLoginStatement;

    private WorkTable(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        insertNewLoginStatement = null;
        insertNewLoginLock = new Object();
    }

    public static WorkTable getInstance() {
        return instance;
    }

    @Override
    public void create(SQLiteDatabase db) {
        String sql;
        sql = "CREATE TABLE " + NAME + " (" + Fields.WORK_ID+ " INTEGER PRIMARY KEY," + Fields.USER_ID + " INTEGER,"
                +Fields.WORK_DESCRIPTION+" TEXT,"+Fields.WORK_LOCATION+" TEXT,"+Fields.WORK_EMPLOYER+" TEXT);";
        DatabaseManager.execSQL(db, sql);
        sql = "CREATE INDEX " + NAME + "_list ON " + NAME + " ("
                + Fields.WORK_ID
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
    public long add(int wordId,int userId, String workDescription, String workEmployer, String workLocation) {


        synchronized (insertNewLoginLock) {
            if (insertNewLoginStatement == null) {
                SQLiteDatabase db = databaseManager.getWritableDatabase();
                insertNewLoginStatement = db.compileStatement("INSERT INTO "
                        + NAME + " (" + Fields.WORK_ID + ", " + Fields.USER_ID + ", "
                        + Fields.WORK_DESCRIPTION + "," + Fields.WORK_LOCATION + ","
                        + Fields.WORK_EMPLOYER + ") VALUES "
                        + "(?,?,?,?,?);");
            }
            insertNewLoginStatement.bindLong(1, wordId);
            insertNewLoginStatement.bindLong(2, userId);
            insertNewLoginStatement.bindString(3, workDescription);
            insertNewLoginStatement.bindString(4, workLocation);
            insertNewLoginStatement.bindString(5, workEmployer);



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
    public long updateWork(int workId, int userId ,String workDescription, String workLocation, String workEmployer) {
        SQLiteDatabase db = databaseManager.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Fields.WORK_ID, workId);
        values.put(Fields.USER_ID, userId);
        values.put(Fields.WORK_DESCRIPTION, workDescription);
        values.put(Fields.WORK_LOCATION, workLocation);
        values.put(Fields.WORK_EMPLOYER, workEmployer);


        return db.update(NAME, values, Fields.USER_ID + " = ?",
                new String[]{String.valueOf(workId)});
    }

//////////////////////////////////////////////////////////

    public ArrayList<WorkEntity> getAllWorkOfUser(String userId) {
        ArrayList<WorkEntity> array_list = new ArrayList<WorkEntity>();
        SQLiteDatabase db = databaseManager.getReadableDatabase();

        Cursor cursor = db.query(NAME, null, Fields.USER_ID + " = ? ", new String[]{String.valueOf(userId)},
                null, null, null);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while(cursor.isAfterLast() == false) {
                String id = cursor.getString(cursor.getColumnIndex(Fields.WORK_ID));
                String userid = cursor.getString(cursor.getColumnIndex(Fields.USER_ID));
                String description = cursor.getString(cursor.getColumnIndex(Fields.WORK_DESCRIPTION));
                String employer = cursor.getString(cursor.getColumnIndex(Fields.WORK_EMPLOYER));
                String location = cursor.getString(cursor.getColumnIndex(Fields.WORK_LOCATION));

                WorkEntity entity = new WorkEntity();
                entity.setWorkid(id);
                entity.setUserid(userid);
                entity.setWorkdescription(description);
                entity.setWorkemployer(employer);
                entity.setWorklocation(location);
                array_list.add(entity);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return array_list;
    }


    public long insertUserWorkRecord(ContentValues contentValues) {
        SQLiteDatabase db = databaseManager.getWritableDatabase();
        long rowid = db.insert(NAME, null, contentValues);
        db.close();
        return rowid;
    }

    public int updateUserWorkRecord (String id, ContentValues contentValues) {
        int noOfRows = 0;
        if(TextUtils.isEmpty(id)) {
            insertUserWorkRecord(contentValues);
        } else {
            SQLiteDatabase db = databaseManager.getWritableDatabase();
            noOfRows = db.update(NAME, contentValues, Fields.WORK_ID + " = ? ", new String[] {id} );
            if(noOfRows == 0) {
                // No record found
                System.out.println("user work inserted id: " + id);
                long rowid = db.insert(NAME, null, contentValues);
            } else {
                System.out.println("user work updated id: " + id);
            }
            db.close();
        }
        return noOfRows;
    }


}
package com.enuke.blinder.database;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.enuke.blinder.Entity.EducationEntity;
import com.xabber.android.data.DatabaseManager;
import com.xabber.android.data.entity.AbstractEntityTable;

import org.xbill.DNS.Update;

import java.util.ArrayList;


/**
 * Created by enuke on 3/10/15.
 */
public class EducationTable extends AbstractEntityTable {


    private static final class Fields implements AbstractEntityTable.Fields {


        public static final String USER_COLUMN_ID = "userid";
        // Education table fields
        public static final String EDUCATION_COLUMN_ID = "educationid";
        public static final String EDUCATION_COLUMN_NAME = "educationname";
        public static final String EDUCATION_COLUMN_TYPE = "educationtype";
        public static final String EDUCATION_COLUMN_YEAR = "educationyear";

        private Fields() {
        }


    }


    private static final String NAME = "educationtable";
    private final static EducationTable instance;
    static {
        instance = new EducationTable(DatabaseManager.getInstance());
        DatabaseManager.getInstance().addTable(instance);
    }    private static final String[] PROJECTION = new String[]{Fields.EDUCATION_COLUMN_ID,Fields.USER_COLUMN_ID,
            Fields.EDUCATION_COLUMN_NAME,Fields.EDUCATION_COLUMN_TYPE,Fields.EDUCATION_COLUMN_YEAR,};
    private final DatabaseManager databaseManager;
    private final Object insertNewLoginLock;
    private SQLiteStatement insertNewLoginStatement;

    private EducationTable(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        insertNewLoginStatement = null;
        insertNewLoginLock = new Object();
    }

    public static EducationTable getInstance() {
        return instance;
    }

    @Override
    public void create(SQLiteDatabase db) {
        String sql;
        sql = "CREATE TABLE " + NAME + " (" + Fields.EDUCATION_COLUMN_ID
                + " INTEGER PRIMARY KEY,"
                + Fields.USER_COLUMN_ID + " INTEGER,"
                + Fields.EDUCATION_COLUMN_NAME + " TEXT,"
                + Fields.EDUCATION_COLUMN_TYPE + " TEXT,"
                + Fields.EDUCATION_COLUMN_YEAR + " TEXT);";
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

   /* *//**
     * Save new user to the database.
     *
     * @return Assigned id.
     */
    public long add(EducationEntity educationEntity) {


        int educationId = Integer.parseInt(educationEntity.getEducationid());
        int userId = Integer.parseInt(educationEntity.getUserid());
        String educationName = educationEntity.getEducationname();
        String educationType = educationEntity.getEducationtype();
        String educationYear = educationEntity.getEducationyear();


        //Insert if new user
        if (!hasUser(educationId)) {
            synchronized (insertNewLoginLock) {
                if (insertNewLoginStatement == null) {
                    SQLiteDatabase db = databaseManager.getWritableDatabase();
                    insertNewLoginStatement = db.compileStatement("INSERT INTO "
                            + NAME + " (" + Fields.EDUCATION_COLUMN_ID + ", " + Fields.USER_COLUMN_ID + ", " + Fields.EDUCATION_COLUMN_NAME + ", "
                            + Fields.EDUCATION_COLUMN_TYPE + ", " + Fields.EDUCATION_COLUMN_YEAR + ") VALUES "
                            + "(?,?);");
                }
                insertNewLoginStatement.bindLong(1, educationId);
                insertNewLoginStatement.bindLong(2, userId);
                insertNewLoginStatement.bindString(3, educationName);
                insertNewLoginStatement.bindString(4, educationType);
                insertNewLoginStatement.bindString(5, educationYear);
                return insertNewLoginStatement.executeInsert();
            }
        }
        else {
            return updateEducation(educationId,userId,educationName,educationType,educationYear);
        }

    }

    //return true if there is any user present
    public boolean hasUser(int educationId) {
        SQLiteDatabase db = databaseManager.getWritableDatabase();
        Cursor cr = db.query(NAME, null, Fields.EDUCATION_COLUMN_ID + " = ? ", new String[]{String.valueOf(educationId)},
                null, null, null);

        return ((cr.getCount() > 0)) ? true : false;
    }

    //Update the Other Details if its already in database
    public long updateEducation(int educationId, int userId,String educationName,String educationType,String educationYear) {
        SQLiteDatabase db = databaseManager.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Fields.EDUCATION_COLUMN_ID, educationId);
        values.put(Fields.USER_COLUMN_ID, userId);
        values.put(Fields.EDUCATION_COLUMN_NAME, educationName);
        values.put(Fields.EDUCATION_COLUMN_TYPE, educationType);
        values.put(Fields.EDUCATION_COLUMN_YEAR, educationYear);


        return db.update(NAME, values, Fields.EDUCATION_COLUMN_ID + " = ?",
                new String[]{String.valueOf(educationId)});
    }




    /**
     * Get all the work details of particular user
     * @param userId
     * @return
     */
    public ArrayList<EducationEntity> getAllEducationOfUser(String userId) {
        ArrayList<EducationEntity> array_list = new ArrayList<EducationEntity>();
        SQLiteDatabase db = databaseManager.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT * FROM  " + NAME + " WHERE " + Fields.USER_COLUMN_ID + "='" + userId + "'", null );
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while(cursor.isAfterLast() == false) {
                String id = cursor.getString(cursor.getColumnIndex(Fields.EDUCATION_COLUMN_ID));
                String userid = cursor.getString(cursor.getColumnIndex(Fields.USER_COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(Fields.EDUCATION_COLUMN_NAME));
                String type = cursor.getString(cursor.getColumnIndex(Fields.EDUCATION_COLUMN_TYPE));
                String year = cursor.getString(cursor.getColumnIndex(Fields.EDUCATION_COLUMN_YEAR));

                EducationEntity entity = new EducationEntity();
                entity.setEducationid(id);
                entity.setUserid(userid);
                entity.setEducationname(name);
                entity.setEducationtype(type);
                entity.setEducationyear(year);
                array_list.add(entity);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return array_list;
    }




   /* *//**
     * Insert user work record
     * @param contentValues
     * @return
     * @throws Exception
     */
    public long insertUserEducationRecord(ContentValues contentValues) throws Exception{
        SQLiteDatabase db = databaseManager.getWritableDatabase();
        long rowid = db.insert(NAME, null, contentValues);

        db.close();

        return rowid;
    }

    /**
     * Update user work record
     * @param id
     * @param contentValues
     * @return
     * @throws Exception
     */
    public int updateUserEducationRecord (String id, ContentValues contentValues) throws Exception{
        int noOfRows = 0;
        if(TextUtils.isEmpty(id)) {
            insertUserEducationRecord(contentValues);
        } else {
            SQLiteDatabase db = databaseManager.getWritableDatabase();
            noOfRows = db.update(NAME, contentValues, Fields.EDUCATION_COLUMN_ID + " = ? ", new String[] {id} );
            if(noOfRows == 0) {
                // No record found
                System.out.println("user education inserted id: " + id);
                long rowid = db.insert(NAME, null, contentValues);
            } else {
                System.out.println("user education updated id: " + id);
            }
            db.close();
        }
        return noOfRows;
    }

}
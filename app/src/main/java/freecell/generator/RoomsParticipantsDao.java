package freecell.generator;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import freecell.generator.RoomsParticipants;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table ROOMS_PARTICIPANTS.
*/
public class RoomsParticipantsDao extends AbstractDao<RoomsParticipants, Long> {

    public static final String TABLENAME = "ROOMS_PARTICIPANTS";

    /**
     * Properties of entity RoomsParticipants.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Chat_id = new Property(1, Long.class, "chat_id", false, "CHAT_ID");
        public final static Property Account = new Property(2, String.class, "account", false, "ACCOUNT");
    };


    public RoomsParticipantsDao(DaoConfig config) {
        super(config);
    }
    
    public RoomsParticipantsDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'ROOMS_PARTICIPANTS' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'CHAT_ID' INTEGER," + // 1: chat_id
                "'ACCOUNT' TEXT);"); // 2: account
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'ROOMS_PARTICIPANTS'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, RoomsParticipants entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long chat_id = entity.getChat_id();
        if (chat_id != null) {
            stmt.bindLong(2, chat_id);
        }
 
        String account = entity.getAccount();
        if (account != null) {
            stmt.bindString(3, account);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public RoomsParticipants readEntity(Cursor cursor, int offset) {
        RoomsParticipants entity = new RoomsParticipants( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // chat_id
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2) // account
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, RoomsParticipants entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setChat_id(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setAccount(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(RoomsParticipants entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(RoomsParticipants entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}

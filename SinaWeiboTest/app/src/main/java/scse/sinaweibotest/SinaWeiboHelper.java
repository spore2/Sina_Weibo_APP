package scse.sinaweibotest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by HP233 on 2017/10/14.
 */

public class SinaWeiboHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "UserInfo";
    private static final int DB_VERSION = 1;

    public SinaWeiboHelper(Context context){
        super(context, SinaWeiboHelper.DB_NAME, null, SinaWeiboHelper.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE USERINFO ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                    + "UID TEXT, "
                    + "TEL TEXT, "
                    + "ACCOUNT TEXT"
                    + "PWD TEXT"
                    + "LASTUSED NUMERIC");
    }

    /*@Override
    public int update(String table, ContentValues values, String whereClause, String [] whereArgs){


    }*/

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}

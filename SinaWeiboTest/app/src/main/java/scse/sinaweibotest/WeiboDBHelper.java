package scse.sinaweibotest;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by HP233 on 2018/1/5.
 */

public class WeiboDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "WeiboDataBase";

    public WeiboDBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //创建账号表格，保存的信息很简单，登陆的账号名和对应的TOKEN
        String sqlStatement = "CREATE TABLE IF NOT EXISTS account (" +
                "accountId integer primary key autoincrement," +
                "accountName text," +
                "token text)";
        db.execSQL(sqlStatement);

        //创建草稿表格，保存用户的草稿，保存的信息为账号Id和对应的草稿内容
        sqlStatement = "CREATE TABLE IF NOT EXISTS draft(" +
                "draftId integer primary key autoincrement," +
                "accountId text," +
                "content text)";
        db.execSQL(sqlStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //TODO
    }

    /*public void addAccount(String accountName, String token){
        SQLiteDatabase db = getWritableDatabase();
        String sqlStatement = "INSERT INTO account (accountName, token)" +
                "VALUES" +
                "(?, ?),";
        db.execSQL(sqlStatement, new Object[]{accountName, token});
    }*/
    public void addAccount(String accountName, Oauth2AccessToken token){
        SQLiteDatabase db = getWritableDatabase();

        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(arrayOutputStream);
            objectOutputStream.writeObject(token);
            objectOutputStream.flush();
            byte data[] = arrayOutputStream.toByteArray();
            objectOutputStream.close();
            arrayOutputStream.close();
            String sqlStatement = "INSERT INTO account (accountName, token)" +
                    "VALUES" +
                    "(?, ?),";
            db.execSQL(sqlStatement, new Object[]{accountName, data});
            //Dbhelper dbhelper = Dbhelper.getInstens(context);
            //SQLiteDatabase database = dbhelper.getWritableDatabase();
            //database.execSQL("insert into classtable (classtabledata) values(?)", new Object[] { data });
            //database.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void deleteAccount(int accountId){
        SQLiteDatabase db = getWritableDatabase();
        String sqlStatement = "DELETE FROM account WHERE accountId = ?";
        db.execSQL(sqlStatement, new Object[]{accountId});
    }

    public void addDraft(String accountId, String content){
        SQLiteDatabase db = getWritableDatabase();
        String sqlStatement = "INSERT INTO draft (accountId, content)" +
                "VALUES" +
                "(?, ?)";
        db.execSQL(sqlStatement, new Object[]{accountId, content});
    }

    public void deleteDraft(int draftId){
        SQLiteDatabase db = getWritableDatabase();
        String sqlStatement = "DELETE FROM draft WHERE draftId = ?";
        db.execSQL(sqlStatement, new Object[]{draftId});
    }

    public SparseArray<String> inquireDraft(String accountId){
        int draftId;
        String draftContent;
        //HashMap<Integer, String> accountList = new HashMap<>();
        SparseArray<String> draftList = new SparseArray();
        SQLiteDatabase db = getReadableDatabase();
        //String sqlStatement = "SELECT * FROM account";
        String [] args = {String.valueOf(accountId)};
        Cursor cursor = db.query("draft", null, "accountId = ?", args, null, null, null);

        while(cursor.moveToNext()){
            draftId = cursor.getInt(0);
            draftContent = cursor.getString(2);
            draftList.append(draftId, draftContent);
        }
        cursor.close();
        return draftList;
    }

    public SparseArray<nameAndToken> inquireAccount(){
        int accountId;
        String accountName;
        Oauth2AccessToken accountToken = null;
        nameAndToken nat;
        SparseArray<nameAndToken> accountList = new SparseArray<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("account", null, null, null, null, null, null);
        while(cursor.moveToNext()){
            //accountToken = cursor.getString(2);
            accountId = cursor.getInt(0);
            accountName = cursor.getString(1);
            //byte data[] = cursor.getBlob(cursor.getColumnIndex("token"));
            byte data[] = cursor.getBlob(2);
            ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
            try {
                ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream);
                accountToken= (Oauth2AccessToken) inputStream.readObject();
                inputStream.close();
                arrayInputStream.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            nat = new nameAndToken(accountName, accountToken);
            accountList.append(accountId, nat);
        }
        cursor.close();
        return accountList;
    }
}

class nameAndToken{
    private String name;
    private Oauth2AccessToken token;

    public nameAndToken(String name, Oauth2AccessToken token){
        this.name = name;
        this.token = token;
    }

    public String getName(){
        return this.name;
    }

    public Oauth2AccessToken getToken(){
        return this.token;
    }
}
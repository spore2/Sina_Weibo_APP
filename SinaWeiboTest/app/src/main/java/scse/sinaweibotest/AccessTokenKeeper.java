package scse.sinaweibotest;

import android.content.Context;
import android.content.SharedPreferences;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

/**
 * Created by HP233 on 2017/10/7.
 */

public class AccessTokenKeeper {

    private static final String PREFERNCES_NAME = "com_weibo_sdk_android";
    private static final String KEY_UID = "uid";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_EXPIRES_IN = "expires_in";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    public static void writeAccessToken(Context context, Oauth2AccessToken token){

        if(context == null || token == null) return;
        //一种用来储存少量数据的类 可通过activity的getSharedPreferences方法获得
        SharedPreferences pref = context.getSharedPreferences(AccessTokenKeeper.PREFERNCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(AccessTokenKeeper.KEY_UID, token.getUid());
        editor.putString(AccessTokenKeeper.KEY_ACCESS_TOKEN, token.getToken());
        editor.putString(AccessTokenKeeper.KEY_REFRESH_TOKEN, token.getRefreshToken());
        editor.putLong(AccessTokenKeeper.KEY_EXPIRES_IN, token.getExpiresTime());
        editor.commit();
    }

    public static Oauth2AccessToken readAccessToken(Context context){

        if(context == null) return null;

        Oauth2AccessToken token = new Oauth2AccessToken();
        SharedPreferences pref = context.getSharedPreferences(AccessTokenKeeper.PREFERNCES_NAME, Context.MODE_APPEND);
        token.setUid(pref.getString(AccessTokenKeeper.KEY_UID, ""));
        token.setToken(pref.getString(AccessTokenKeeper.KEY_ACCESS_TOKEN, ""));
        token.setRefreshToken(pref.getString(AccessTokenKeeper.KEY_ACCESS_TOKEN, ""));
        token.setExpiresTime(pref.getLong(AccessTokenKeeper.KEY_EXPIRES_IN, 0));

        return token;
    }

    public static void clear(Context context){

        if(context == null) return;

        SharedPreferences pref = context.getSharedPreferences(AccessTokenKeeper.PREFERNCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}

package scse.sinaweibotest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

import java.util.ArrayList;
import java.util.HashMap;

import static scse.sinaweibotest.MainActivity.token;

/**
 * Created by Administrator on 2018/1/5.
 */

public class FirstLoginActivity extends Activity {
    private AuthInfo mAuthInfo;
    private SsoHandler mSsoHandler;
    private Oauth2AccessToken mAccessToken;
    private Spinner mSpinner;

    public void onCreate(Bundle savedInstandceState){
        super.onCreate(savedInstandceState);
        setContentView(R.layout.activity_firstlogin);

        this.mSpinner = findViewById(R.id.accountList);

        ArrayList<HashMap<String, Object>> accounts = new ArrayList<>();
        WeiboDBHelper dbHelper = new WeiboDBHelper(this);
        SparseArray<nameAndToken> result = dbHelper.inquireAccount();
        for(int i = 0; i < result.size(); ++i){
            nameAndToken temp = result.valueAt(i);
            HashMap<String, Object> info = new HashMap<>();
            info.put("account", temp.getName());
            info.put("token", temp.getToken());
            accounts.add(info);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, accounts, R.layout.fragment_account_list_item, new String[]{"name"}, new int[]{R.id.accountName});

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Spinner spinner = (Spinner)adapterView;
                HashMap<String, Object> hm = (HashMap<String, Object>)spinner.getItemAtPosition(i);
                AccessTokenKeeper.writeAccessToken(FirstLoginActivity.this, (Oauth2AccessToken)(hm.get("token")));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(FirstLoginActivity.this, mAuthInfo);
        //Button loginBtn = findViewById(R.id.webLogin);
        ImageButton loginBtnImage = findViewById(R.id.webLoginImage);
        loginBtnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSsoHandler.authorizeWeb(new AuthListener());
            }
        });
    }

    private void reserveAccountToken(Oauth2AccessToken token){
        WeiboDBHelper dbHelper = new WeiboDBHelper(this);
        dbHelper.addAccount(token.getUid(), token);
    }

    private class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values){
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            reserveAccountToken(mAccessToken);
            if(mAccessToken.isSessionValid()){
                //updateTokenView(false);
                AccessTokenKeeper.writeAccessToken(FirstLoginActivity.this, mAccessToken);
                Toast.makeText(FirstLoginActivity.this, "授权成功", Toast.LENGTH_SHORT).show();
                token = values.getString("access_token");
                /*Intent intent = new Intent(mContext, ReadWeiboActivity.class);
                startActivity(intent);*/

            }else{
                String code = values.getString("code");
                String message = "授权失败";
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(FirstLoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onWeiboException(WeiboException we){

        }

        @Override
        public void onCancel(){

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
        if (this.mSsoHandler != null) {
            this.mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
}

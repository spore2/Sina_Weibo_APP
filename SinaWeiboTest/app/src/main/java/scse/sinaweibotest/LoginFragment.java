package scse.sinaweibotest;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

/**
 * Created by HP233 on 2017/10/16.
 */

public class LoginFragment extends Fragment {

    private Activity mContext;
    private Button loginBtn;
    private AuthInfo mAuthInfo;
    private SsoHandler mSsoHandler;
    private Oauth2AccessToken mAccessToken;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mContext = (Activity)context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View res = inflater.inflate(R.layout.fragment_login, container, false);

        mAuthInfo = new AuthInfo(mContext, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(mContext, mAuthInfo);

        loginBtn = res.findViewById(R.id.login);
        if(loginBtn == null) System.out.println("button is null");
        else {
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSsoHandler.authorizeWeb(new AuthListener());
                }
            });
        }
        return res;
    }

    private class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values){
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if(mAccessToken.isSessionValid()){
                //updateTokenView(false);
                AccessTokenKeeper.writeAccessToken(mContext, mAccessToken);
                Toast.makeText(mContext, "授权成功", Toast.LENGTH_SHORT).show();
                MainActivity.token = values.getString("access_token");
                /*Intent intent = new Intent(mContext, ReadWeiboActivity.class);
                startActivity(intent);*/

            }else{
                String code = values.getString("code");
                String message = "授权失败";
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onWeiboException(WeiboException we){

        }

        @Override
        public void onCancel(){

        }
    }

    private void updateTokenView(boolean hasExisted){
        /*String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                new java.util.Date(mAccessToken.getExpiresTime()));
        String format = "Token：%1$s \\n有效期：%2$s";
        this.mTokenText.setText(String.format(format, mAccessToken.getToken(), date));
        String message = String.format(format, mAccessToken.getToken(), date);
        if(hasExisted){
            message = "Token 仍在有效期内，无需再次登录。" + "\n" + message;
        }
        this.mTokenText.setText(message);*/
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

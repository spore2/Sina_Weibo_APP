package scse.sinaweibotest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by HP233 on 2017/10/8.
 */

public class ReadWeiboActivity extends AppCompatActivity {

    private static final String TAG = ReadWeiboActivity.class.getName();
    //public static String PATH = "https://api.weibo.com/2/statuses/share.json";
    public static String PATH = "https://api.weibo.com/oauth2/authorize";

    private EditText mEditText;

    private Handler handler = new Handler(){

        public void handleMyMessage(Message msg){
            TextView response = (TextView)findViewById(R.id.response);
            response.setText(msg.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_weibo);

        mEditText = (EditText) findViewById(R.id.content);
        String draftContent;
        Intent intent = getIntent();
        if((draftContent = intent.getStringExtra("draftcontent")) != null) {
            //EditText content = (EditText)findViewById(R.id.content);
            mEditText.setText(draftContent);
            System.out.println(draftContent);
        }else{
            System.out.print("nothing");
        }
        Button submitButton = (Button)findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //EditText content = (EditText) findViewById(R.id.content);
                //final String sendContent = content.getText().toString();
                final String sendContent = mEditText.getText().toString();
                //System.out.println();
                //ReadWeiboActivity.this.sendRequest();
                new Thread(new Runnable(){

                    @Override
                    public void run(){
                        String url = "https://api.weibo.com/2/statuses/share.json?";
                        //String url = "https://api.weibo.com/oauth2/authorize";

                        Map<String, String> params = new HashMap<>();
                        //params.put("authorization", AccessTokenKeeper.readAccessToken(ReadWeiboActivity.this).toString());
                        //System.out.println("token value: " + AccessTokenKeeper.readAccessToken(ReadWeiboActivity.this).getToken());

                        params.put("access_token", AccessTokenKeeper.readAccessToken(ReadWeiboActivity.this).getToken());
                        params.put("status", sendContent + Constants.REDIRECT_URL);
                        //params.put("REDIRECT_URL", "https://api.weibo.com/oauth2/default.html");
                        //System.out.println(AccessTokenKeeper.readAccessToken(ReadWeiboActivity.this).toString());

                        String result = HttpUtil.submitPostData(url, params, "UTF-8");
                        Message msg = new Message();

                        ReadWeiboActivity.this.handler.handleMessage(new Message());
                        //System.out.println(result);
                        //Toast.makeText(ReadWeiboActivity.this, result, Toast.LENGTH_LONG);
                    }
                }).start();
            }
        });
    }

    @Override
    public void onBackPressed(){
        //判断编辑框中是否还有内容，若有，弹出保存草稿的提示
        final String weiboContent = mEditText.getText().toString();
        if(!weiboContent.equals("")){
            new AlertDialog.Builder(this).
                    setTitle("Notification").
                    setMessage("need to reserve draft?").
                    setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //保存当前微博内容至草稿箱
                    System.out.println("test for draft");
                    WeiboDBHelper dbHelper = new WeiboDBHelper(ReadWeiboActivity.this);
                    SharedPreferences pref = ReadWeiboActivity.this.getSharedPreferences("com_weibo_sdk_android", Context.MODE_APPEND);
                    String userId = pref.getString("uid", null);
                    dbHelper.addDraft(userId, weiboContent);
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }else{
            super.onBackPressed();
        }
    }

    private String getNowDateTime(String format){
        if(format==""){
            format="yyyy-MM-dd HH:mm:ss";
        }
        Date now = new Date();
        SimpleDateFormat df = new SimpleDateFormat(format);//设置日期格式
        return df.format(now); // new Date()为获取当前系统时间
    }

    public void onClickSubmitButton(View view){
        EditText content = (EditText)findViewById(R.id.content);
        Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(ReadWeiboActivity.this);
        StatusesAPI sapi = new StatusesAPI(ReadWeiboActivity.this, Constants.APP_KEY, token);

        sapi.update(content.getText().toString(), "0.0", "0.0", new MyRequestListener());

    }

    private class MyRequestListener implements RequestListener{

        @Override
        public void onComplete(String feedBack){
            System.out.println("request complete");
            Toast.makeText(ReadWeiboActivity.this, feedBack, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e){
            Log.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(ReadWeiboActivity.this, info.toString(), Toast.LENGTH_LONG).show();

        }

    }
}

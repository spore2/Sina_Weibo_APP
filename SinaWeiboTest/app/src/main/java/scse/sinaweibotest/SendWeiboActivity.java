package scse.sinaweibotest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;

/**
 * Created by HP233 on 2017/10/24.
 */

public class SendWeiboActivity extends AppCompatActivity {

    private static final int PHOTO_REQUEST_GALLERY = 2;
    //private String imagePath;
    private Bitmap sendImage;
    private EditText mEditText;
    /*private static final String READ_EXTERNAL_STORAGE = "READ_EXTERNAL_STORAGE";
    private static final String WRITE_EXTERNAL_STORAGE = "WRITE_EXTERNAL_STORAGE";
    private static final String READ_INTERNAL_STORAGE = "READ_INTERNAL_STORAGE";
    private static final String WRITE_INTERNAL_STORAGE = "WRITE_INTERNAL_STORAGE";*/
    private RuntimePermissionHelper mPermissionHelper;
    //private RequestListener mListener;
    //表情显示相关
    /* EmotionMainFragment emotionMainFragment;
    private EmotionKeyboard mEmotionKeyboard;
    private View mEmotionLayout;//表情布局*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_weibo);

        mEditText = (EditText) findViewById(R.id.sendContent);
        String draftContent;
        Intent intent = getIntent();
        if((draftContent = intent.getStringExtra("draftContent")) != null) {
            //EditText content = (EditText)findViewById(R.id.content);
            mEditText.setText(draftContent);
            mEditText.setSelection(draftContent.length());
            //System.out.println(draftContent);
        }

        mPermissionHelper = new RuntimePermissionHelper(this);
        initTopToolbar();
        initBottomToolbar();

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
                            WeiboDBHelper dbHelper = new WeiboDBHelper(SendWeiboActivity.this);
                            SharedPreferences pref = SendWeiboActivity.this.getSharedPreferences("com_weibo_sdk_android", Context.MODE_APPEND);
                            String userId = pref.getString("uid", null);
                            dbHelper.addDraft(userId, weiboContent);
                            finish();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                    //TODO
                }
            }).show();
        }else{
            super.onBackPressed();
        }
    }

    private void initTopToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item_sendContent: {
                        EditText content = (EditText) findViewById(R.id.sendContent);
                        String sendContent = content.getText().toString() + Constants.REDIRECT_URL;
                        StatusesAPI sapi = new StatusesAPI(SendWeiboActivity.this,
                                Constants.APP_KEY,
                                AccessTokenKeeper.readAccessToken(SendWeiboActivity.this));
                        GridLayout gridLayout = (GridLayout) findViewById(R.id.imageGrid);
                        if(gridLayout.getChildCount() != 0){
                            sapi.shareImage(sendContent, sendImage, null, null, new SendWeiboRequestListener());
                        }else{
                            sapi.shareText(sendContent, null, null, new SendWeiboRequestListener());
                        }
                    }
                    break;
                }
                return true;
            }
        });
    }

    /*public void initEmotionMainFragment(){
        //构建传递参数
        Bundle bundle = new Bundle();
        //绑定主内容编辑框
        bundle.putBoolean(EmotionMainFragment.BIND_TO_EDITTEXT,false);
        //隐藏控件
        bundle.putBoolean(EmotionMainFragment.HIDE_BAR_EDITTEXT_AND_BTN,true);
        //替换fragment
        //创建修改实例
        //emotionMainFragment = EmotionMainFragment.newInstance(EmotionMainFragment.class, bundle);
        //emotionMainFragment.bindToContentView(et_emotion);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in thefragment_container view with this fragment,
        // and add the transaction to the backstack
        transaction.replace(R.id.fl_emotionview_main,emotionMainFragment);
        transaction.addToBackStack(null);
        //提交修改
        transaction.commit();
    }*/

    private void initBottomToolbar(){
        ImageView images = (ImageView) findViewById(R.id.imageSelect);
        ImageView topics = (ImageView) findViewById(R.id.topics);
        ImageView at = (ImageView) findViewById(R.id.at);
        ImageView emoji = (ImageView) findViewById(R.id.emoji);
        ImageView draft = (ImageView) findViewById(R.id.draft);

        images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean permission = mPermissionHelper.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(!permission){
                    mPermissionHelper.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 12);
                }else{
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
                }
            }
        });

        topics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SendWeiboActivity.this, "unimplemented!", Toast.LENGTH_LONG);
            }
        });

        emoji.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

            }
        });

        //设置表情显示面板
        //initEmotionMainFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == PHOTO_REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null){
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            if(c.moveToFirst()){
                //System.out.println("move to first success!!");
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                String imagePath = c.getString(columnIndex);
                loadImageIntoGrid(imagePath);
            }
            c.close();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadImageIntoGrid(String imagePath){

        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        GridLayout gridLayout = (GridLayout)findViewById(R.id.imageGrid);
        int count = gridLayout.getChildCount();
        GridLayout.LayoutParams params = null;
        ImageView image = new ImageView(SendWeiboActivity.this);

        if(count == 0){
            final BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inJustDecodeBounds = true;
            options.inSampleSize = 4;
            this.sendImage = BitmapFactory.decodeFile(imagePath, options);
        }
        image.setImageBitmap(bm);
        GridLayout.Spec rowSpec = GridLayout.spec(count / gridLayout.getColumnCount());
        GridLayout.Spec columnSpec = GridLayout.spec(count % gridLayout.getColumnCount());
        params = new GridLayout.LayoutParams(rowSpec, columnSpec);
        params.width = 220;
        params.height = 220;
        gridLayout.addView(image, params);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case 12:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //如果请求成功，则进行相应的操作，比如我们这里打开一个新的Activity
                    //按说只要设置相应的组件被选中就能再次触发onClick方法，但是第一次测试发现并不行...
                    //To DO
                } else {
                    //如果请求失败
                    mPermissionHelper.startAppSettings();
                }
                break;
        }
    }

    private class SendWeiboRequestListener implements RequestListener {

        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                //LogUtil.i(TAG, response);
                if (response.startsWith("{\"statuses\"")) {
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statuses = StatusList.parse(response);
                    if (statuses != null && statuses.total_number > 0) {
                        Toast.makeText(SendWeiboActivity.this,
                                "获取微博信息流成功, 条数: " + statuses.statusList.size(),
                                Toast.LENGTH_LONG).show();
                    }
                } else if (response.startsWith("{\"created_at\"")) {
                    // 调用 Status#parse 解析字符串成微博对象
                    Status status = Status.parse(response);
                    Toast.makeText(SendWeiboActivity.this,
                            "发送一送微博成功, id = " + status.id,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SendWeiboActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e){
            //System.out.println(e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(SendWeiboActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_write_weibo, menu);
        return true;
    }
}
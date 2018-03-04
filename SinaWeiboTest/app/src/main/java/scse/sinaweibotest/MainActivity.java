package scse.sinaweibotest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

public class MainActivity extends Activity {

    public Toolbar toolBar;

    public static String token;
    private Oauth2AccessToken mAccessToken;
    private SsoHandler mSsoHandler;
    private AuthInfo mAuthInfo;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private BottomNavigationBar mBottomNavigationBar;
    private Fragment mainFragments [] = new Fragment [4];
    private FragmentManager mFragmentManager;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mAuthInfo = new AuthInfo(MainActivity.this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        this.mSsoHandler = new SsoHandler(MainActivity.this, mAuthInfo);
        //this.mTokenText = (TextView)findViewById(R.id.that);

        mFragmentManager = getFragmentManager();
        toolBar = findViewById(R.id.toolbar);
        mFab = findViewById(R.id.floatingActionButton);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SendWeiboActivity.class);
                startActivity(intent);
            }
        });

        initDraw();
        initBottomNavigationBar();
        initMenu();

        //mFab.setOnClickListener(writeWeibo);

        this.mAccessToken = AccessTokenKeeper.readAccessToken(MainActivity.this);
        if(this.mAccessToken.getExpiresTime() == 0){
            System.out.println("token is null");
            Intent intent = new Intent(this, FirstLoginActivity.class);
            startActivity(intent);
        }else{
            System.out.println("get token valid");
            if(this.mAccessToken.isSessionValid()) updateTokenView(true);
        }
        /*ImageView iv = new ImageView(MainActivity.this);
        iv.setImageIcon(new Icon());*/
    }


    private void initDraw(){
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mNavigationView = findViewById(R.id.drawerNavigation);
        /*mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                TestFragment tf = (TestFragment)mainFragments[1];
                tf.changeContent(item.getTitle().toString());
                mDrawerLayout.closeDrawer(mNavigationView);
                return true;
            }
        });*/
    }

    private void initBottomNavigationBar(){

        mBottomNavigationBar = findViewById(R.id.bottomBar);
        mBottomNavigationBar.setMode(BottomNavigationBar.MODE_SHIFTING);
        mBottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_RIPPLE);
        mBottomNavigationBar
                .setActiveColor(R.color.colorPrimary)
                .setInActiveColor("#FFFFFF")
                .setBarBackgroundColor("#ECECEC");
        mBottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.homepage, "Browser"))
                .addItem(new BottomNavigationItem(R.drawable.ic_music_note_white_24dp, "Home"))
                .addItem(new BottomNavigationItem(R.drawable.ic_tv_white_24dp, "Draft"))
                .initialise();
        mBottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                Toolbar tb = findViewById(R.id.toolbar);
                if(mainFragments[position] == null){
                    switch(position){
                        case 0:{
                            mainFragments[0] = new HomeFragment();
                            tb.setTitle(R.string.home_fragment_name);
                            //mFab.setImageResource(R.drawable.floatwhite);
                            //System.out.println("homefragment");
                        } break;
                        case 1: {
                            mainFragments[1] = new TestFragment();
                            tb.setTitle(R.string.person_fragment_name);
                            //mFab.setImageResource(R.drawable.floatwhite);
                            //System.out.println("personfragment");
                        } break;
                        case 2: {
                            mainFragments[2] = new DraftFragment();
                            tb.setTitle(R.string.draft_fragment_draft);
                            //mFab.setImageResource(R.drawable.deletedrafts);
                            //System.out.println("draftfragment");
                        } break;
                    }
                    ft.add(R.id.frame, mainFragments[position]);
                }else{
                    switch(position){
                        case 0:{
                            //mainFragments[0] = new HomeFragment();
                            tb.setTitle(R.string.home_fragment_name);
                            //mFab.setImageResource(R.drawable.floatwhite);
                            //System.out.println("homefragment");
                        } break;
                        case 1: {
                            //mainFragments[1] = new TestFragment();
                            tb.setTitle(R.string.person_fragment_name);
                            //mFab.setImageResource(R.drawable.floatwhite);
                            //System.out.println("personfragment");
                        } break;
                        case 2: {
                            //mainFragments[2] = new DraftFragment();
                            tb.setTitle(R.string.draft_fragment_draft);
                            //mFab.setImageResource(R.drawable.deletedrafts);
                            //System.out.println("draftfragment");
                        } break;
                    }
                    ft.show(mainFragments[position]);
                }
                ft.commit();
            }

            @Override
            public void onTabUnselected(int position) {
                if(mainFragments[position] != null){
                    FragmentTransaction ft = mFragmentManager.beginTransaction();
                    ft.hide(mainFragments[position]);
                    ft.commit();
                }
            }

            @Override
            public void onTabReselected(int position) {

            }
        });
        mBottomNavigationBar.selectTab(0);
    }

    private void initMenu(){
        /*ImageView iv = new ImageView(MainActivity.this);
        iv.setImageIcon(new Icon());*/
        Menu menu = mNavigationView.getMenu();
        MenuItem mi_favorite = menu.getItem(0);
        MenuItem mi_draft = menu.getItem(1);
        MenuItem mi_setting = menu.getItem(2);
        try {
            mi_favorite.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Toast.makeText(MainActivity.this,
                            "微博官方没有提供对应API，暂无法实现",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
            });

            mi_draft.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    return false;
                }
            });

            mi_setting.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Intent intent = new Intent(MainActivity.this, ThemeSwitchActivity.class);
                    startActivity(intent);
                    return false;
                }
            });

        }catch(Exception e){
            System.out.println(e.toString());
        }
    }

    /*private void initFragment(){
        Fragment homeFragment = new TestFragment();
        mFragmentManager = getFragmentManager();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        mainFragments[1] = homeFragment;
        ft.add(R.id.frame, homeFragment);
        ft.commit();
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
        if (this.mSsoHandler != null) {
            this.mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }

    }

    public DrawerLayout getDrawerLayout(){
        return this.mDrawerLayout;
    }

    public Fragment [] getFragments(){
        return this.mainFragments;
    }

    private void logIn(String account, String password){

    }

    private class AuthListener implements WeiboAuthListener{

        @Override
        public void onComplete(Bundle values){
            MainActivity.this.mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            //System.out.println("before parse: " + values);
            //String phoneNum = mAccessToken.getPhoneNum();
            if(mAccessToken.isSessionValid()){
                updateTokenView(false);
                AccessTokenKeeper.writeAccessToken(MainActivity.this, mAccessToken);
                Toast.makeText(MainActivity.this, "授权成功", Toast.LENGTH_SHORT).show();
                MainActivity.token = values.getString("access_token");
                //System.out.println("value of token: " + token);

                /*Intent intent = new Intent(MainActivity.this, ReadWeiboActivity.class);
                startActivity(intent);*/

            }else{
                String code = values.getString("code");
                String message = "授权失败";
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
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
        /*buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean flag = true;
                EditText account = (EditText)findViewById(R.id.userId);
                EditText password = (EditText)findViewById(R.id.password);
                SinaWeiboHelper swh;
                SQLiteDatabase db;
                Cursor userCursor = null;
                //MainActivity.this.mSsoHandler.authorizeWeb(new AuthListener());
                try{
                    swh = new SinaWeiboHelper(MainActivity.this);
                    db = swh.getWritableDatabase();
                    userCursor = db.query("USERINFO", new String [] {"ACCOUNT", "PWD"}, "LASTUSED = ?", new String [] {"true"}, null, null, null);
                }catch(SQLiteException e){
                    Toast toast = Toast.makeText(MainActivity.this, "Database unavaliable", Toast.LENGTH_LONG);
                    toast.show();
                }
                if(userCursor.moveToFirst()){
                    account.setText(userCursor.getString(3));
                    password.setText(userCursor.getString(4));
                }
                new Thread(new Runnable(){

                    @Override
                    public void run(){
                        //String url = "https://api.weibo.com/oauth2/authorize?client_id=92981932&redirect_uri=https://api.weibo.com/oauth2/default.html";
                        String url = "https://api.weibo.com/oauth2/authorize?";
                        String result = HttpUtil.logIn(url, "13121211028", "19980303hupeng");
                        System.out.println(result);
                    }
                }).start();
                MainActivity.this.mSsoHandler.authorizeWeb(new AuthListener());
            }
        });*/

        /*writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ReadWeiboActivity.class);
                startActivity(intent);
            }
        });
        viewButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, API_StatusActivity.class);
                startActivity(intent);
            }
        });
        webButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){
                String url = "https://www.baidu.com";
                AuthInfo ai = new AuthInfo(MainActivity.this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
                AuthRequestParam req = new AuthRequestParam(MainActivity.this);
                req.setAuthListener(new AuthListener(){
                });
                req.setUrl(url);
                req.setAuthInfo(ai);
                Bundle data = req.createRequestParamBundle();
                Intent intent = new Intent(MainActivity.this, WebViewAcitvity.class);
                startActivity(intent);
            }
        });*/
}

class BottomBarBehavior extends CoordinatorLayout.Behavior <BottomNavigationBar> {

    public BottomBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //确定所提供的子视图是否有另一个特定的同级视图作为布局从属。
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, BottomNavigationBar child, View dependency) {
        //这个方法是说明这个子控件是依赖AppBarLayout的
        return dependency instanceof AppBarLayout;
    }

    //用于响应从属布局的变化
    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, BottomNavigationBar child, View dependency) {

        float translationY = Math.abs(dependency.getTop());//获取更随布局的顶部位置
        //System.out.println("bottom appbarlayout move: " + translationY);

        child.setTranslationY(translationY);
        return true;
    }
}

class FloatingActionButtonBehavior extends CoordinatorLayout.Behavior <FloatingActionButton> {

    private MainActivity mContext;

    public FloatingActionButtonBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (MainActivity)context;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency){

        return dependency instanceof Toolbar;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {

        //float translationY = Math.abs(dependency.getTop());//获取更随布局的顶部位置
        //child.setTranslationY(translationY);

        float currentTop = dependency.getTop();
        float percent = currentTop / mContext.toolBar.getHeight();

        if(currentTop != 0){
            if(child.getVisibility() == View.GONE) child.setVisibility(View.VISIBLE);
            child.setScaleX(percent);
            child.setScaleY(percent);
        }else{
            child.setVisibility(View.GONE);
        }

        return true;
    }
}
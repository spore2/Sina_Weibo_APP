package scse.sinaweibotest;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.ArrayList;

public class API_StatusActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    /** UI 元素：ListView */
    private ListView mListView;
    /** 填充在ListView上的String */
    private ArrayList<String> mListContent = new ArrayList<>();
    private ArrayList<Status> mListStatus = new ArrayList<>();
    private ArrayAdapter adapter;
    /** 当前 Token 信息 */
    private Oauth2AccessToken mAccessToken;
    /** 用于获取微博信息流等操作的API */
    private StatusesAPI mStatusesAPI;
    /** 用户信息接口 */
 //   private UsersAPI mUsersAPI;

    /**微博动态信息*/
    private TextView weibo_view;
    private ListView mStatusList;
    private boolean isRefresh = true;
    private long nowLastStatusID=0;

    /**
     * @see {@link Activity#onCreate}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(API_StatusActivity.this);
        String toTheme = shp.getString("theme_which","GreyTheme");
        setTheme(API_StatusActivity.this.getResources().getIdentifier(toTheme,"style",Constants.PACKAGE_NAME));
        setContentView(R.layout.activity_api_status);

        mListView = (ListView)findViewById(R.id.statusList);


        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if(!mListView.canScrollVertically(1)&&i==0){
                    isRefresh=false;
                    LoadStatus();
                }
                else isRefresh = true;
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent intent = new Intent(API_StatusActivity.this, API_CommentActivity.class);
                intent.putExtra("content", mListContent.get(position));
                intent.putExtra("weiboId", mListStatus.get(position).id);
                //Status mstatus = mListStatus.get(position);
                //intent.putExtra("Status", mstatus);
                startActivity(intent);
            }
        });

        String USER_ID = "USER_ID";
        ((TextView)findViewById(R.id.showUserName)).setText(USER_ID);

        ImageButton refreshBtn = (ImageButton)findViewById(R.id.refreshButton);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadStatus();
            }
        });

        // 获取当前已保存过的 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        // 对statusAPI实例化
        mStatusesAPI = new StatusesAPI(this, Constants.APP_KEY, mAccessToken);

        LoadStatus();

    }

    /**
     * 微博 OpenAPI 回调接口。
     */
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i(TAG, response);
                if (response.startsWith("{\"statuses\"")) {
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statuses = StatusList.parse(response);
                    if (statuses != null && statuses.total_number > 0) {
                        Toast.makeText(API_StatusActivity.this,
                                "获取微博信息流成功, 条数: " + statuses.statusList.size(),
                                Toast.LENGTH_LONG).show();
                        toListView(statuses.statusList,isRefresh);
                       }
                } else {
                    Toast.makeText(API_StatusActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(API_StatusActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };

    /**
     * 将StatusList转换为StringList中的数据
     * 已经确保了输入非null
     */
    private void toListView(ArrayList<Status> statusList, boolean isRefresh){
        //获取当前滚动条位置
        int x = mListView.getScrollX();
        int y = mListView.getScrollY();
        //如果是刷新，那么清空当前的list（而不是append）
        if(isRefresh){
            mListContent.clear();
            mListStatus.clear();
        }
        for(int i=0;i<statusList.size();i++){
            Status status = statusList.get(i);
            String string =
                    status.created_at + " \n@" +
                    status.user.screen_name + " :\n" +
                    status.text + "\n";
            mListContent.add(string);
            mListStatus.add(status);
        }
        nowLastStatusID = Long.parseLong(statusList.get(statusList.size()-1).id);
        adapter = new ArrayAdapter<String>(
                this,android.R.layout.simple_list_item_1,mListContent);
        mListView.setAdapter(adapter);
        //移动到之前的滚动条位置，可能表现会很差www
        mListView.scrollTo(x,y);
    }

    /**
    *  读取微博动态
    */
    private void LoadStatus(){

        Toast.makeText(API_StatusActivity.this,"Weibo Contents Updating",
                Toast.LENGTH_LONG).show();
        if (mAccessToken != null && mAccessToken.isSessionValid()) {
            /**
             * 获取当前登录用户及其所关注用户的最新微博。
             *
             * @param since_id    若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0
             * @param max_id      若指定此参数，则返回ID小于或等于max_id的微博，默认为0。
             * @param count       单页返回的记录条数，默认为50。
             * @param page        返回结果的页码，默认为1。
             * @param base_app    是否只获取当前应用的数据。false为否（所有数据），true为是（仅当前应用），默认为false。
             * @param featureType 过滤类型ID，0：全部、1：原创、2：图片、3：视频、4：音乐，默认为0。
             *                    <li>{@link #FEATURE_ALL}
             *                    <li>{@link #FEATURE_ORIGINAL}
             *                    <li>{@link #FEATURE_PICTURE}
             *                    <li>{@link #FEATURE_VIDEO}
             *                    <li>{@link #FEATURE_MUSICE}
             * @param trim_user   返回值中user字段开关，false：返回完整user字段、true：user字段仅返回user_id，默认为false。
             * @param listener    异步请求回调接口
             */
            if(isRefresh)mStatusesAPI.friendsTimeline(0L, 0L, 10, 1, false, 0, false, mListener);
            else mStatusesAPI.friendsTimeline(0L, nowLastStatusID, 10, 1, false, 0, false, mListener);
            //Func : 获取 @当前用户 的最新微博
            //mStatusesAPI.mentions(0L, 0L, 10, 1, 0, 0, 0, false, mListener);

        } else {
            Toast.makeText(API_StatusActivity.this,"access token is empty",
                    Toast.LENGTH_LONG).show();
        }

    }


}

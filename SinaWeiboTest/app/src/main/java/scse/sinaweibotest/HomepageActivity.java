package scse.sinaweibotest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.ArrayList;

/*
todo 这个应该可以整合进HomeFragment中
     目前和HomeFragment的区别就是，这个在最上面多了用户信息的显示
     本来下面显示的微博应该是该用户的微博，但是API调用不了
     甚至连获取当前授权用户的微博的API都调用不了……
*/
public class HomepageActivity extends AppCompatActivity {

    private static final String TAG = "HomepageActivity";

    private User user;
    /** UI 元素：ListView */
    private EasyRecyclerView mListView;
    private StatusAdapter adapter = new StatusAdapter(this);
    /** 当前 Token 信息 */
    private Oauth2AccessToken mAccessToken;
    /** 用于获取微博信息流等操作的API */
    private StatusesAPI mStatusesAPI;
private boolean once = true;
    /** 用户信息接口 */
    //   private UsersAPI mUsersAPI;

    /**微博动态信息*/
    private long nowLastStatusID=1;


    /**
     * @see {@link Activity#onCreate}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if(once){
            once = false;
            SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(HomepageActivity.this);
            String toTheme = shp.getString("theme_which","GreyTheme");
            System.out.println(">>>"+toTheme);
            System.out.println(">>>"+HomepageActivity.this.getResources().getIdentifier(toTheme,"style",Constants.PACKAGE_NAME));
            setTheme(HomepageActivity.this.getResources().getIdentifier(toTheme,"style",Constants.PACKAGE_NAME));
            recreate();

        }
        setContentView(R.layout.fragment_home);
try {
    // 获取当前已保存过的 Token
    mAccessToken = AccessTokenKeeper.readAccessToken(this);
    // 对statusAPI实例化
    mStatusesAPI = new StatusesAPI(this, Constants.APP_KEY, mAccessToken);
    //这里的user是从StatusViewHolder中传递过来的
    user = (User) getIntent().getSerializableExtra("User");

    /** 显示User信息*/
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    ImageView homepageUserIcon = (ImageView) findViewById(R.id.HomepageUserIcon);

    Glide.with(this)
            .load(user.avatar_large)
//                .placeholder(R.drawable.placeholderUserIcon)
//                .bitmapTransform(new CropCircleTransformation(getContext()))
            .into(homepageUserIcon);

    TextView homepageUserName = (TextView) findViewById(R.id.HomepageUserName);
    homepageUserName.setText(user.screen_name);

    //后期应该需要改成Glide显示
    ImageView homepageUserGender = (ImageView) findViewById(R.id.HomepageUserGender);
    if (user.gender.equals("m"))
        homepageUserGender.setBackgroundResource(R.drawable.gender_male);
    else if (user.gender.equals("f"))
        homepageUserGender.setBackgroundResource(R.drawable.gender_female);
    else
        homepageUserGender.setBackgroundResource(R.drawable.gender_unknown);

        /*
        String gender = user.gender.equals("m") ? "gender_male" :
                        user.gender.equals("f") ? "gender_female" :
                                                  "gender_unknown" ;

         try {

            Glide.with(this)
                    .load(Constants.GLIDE_DRAWABLE_PATH + gender)
//                .placeholder(R.drawable.placeholderUserIcon)
//                .bitmapTransform(new CropCircleTransformation(getContext()))
                    .into(homepageUserGender);


        } catch (Exception e){
            System.out.println("ERROR " + e);
        }*/

    TextView homepageVerifiedReason = (TextView) findViewById(R.id.HomepageVerifiedReason);
    if (user.verified_reason.length() > 1) {
        homepageVerifiedReason.setText(user.verified_reason);
    } else {
        homepageVerifiedReason.setVisibility(View.GONE);
    }

    TextView homepageUserDescription = (TextView) findViewById(R.id.HomepageUserDescription);
    if (user.description.length() > 1) {
        homepageUserDescription.setText(user.description);
    } else {
        homepageUserDescription.setVisibility(View.GONE);
    }

    TextView homepageUserLocation = (TextView) findViewById(R.id.HomepageUserLocation);
    if (user.location.length() > 1) {
        homepageUserLocation.setText(user.location);
    } else {
        homepageUserLocation.setVisibility(View.GONE);
    }

    TextView homepageUserFollowingCount = (TextView) findViewById(R.id.HomepageUserFollowingCount);
    homepageUserFollowingCount.setText(String.valueOf("关注数·" + user.friends_count));

    TextView homepageUserFollowerCount = (TextView) findViewById(R.id.HomepageUserFollowerCount);
    homepageUserFollowerCount.setText(String.valueOf("粉丝数·" + user.followers_count));

    // 获取不到点赞数 姑且用收藏数代替
    TextView homepageUserLikesCount = (TextView) findViewById(R.id.HomepageUserLikesCount);
    homepageUserLikesCount.setText(String.valueOf("收藏数·" + user.favourites_count));

    TextView homepageUserStatusCount = (TextView) findViewById(R.id.HomepageUserStatusCount);
    homepageUserStatusCount.setText(String.valueOf("微博数·" + user.statuses_count));
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    mListView = (EasyRecyclerView) findViewById(R.id.HomepageRecyclerView);
    mListView.setLayoutManager(new LinearLayoutManager(this));
    mListView.setAdapter(adapter);


    adapter.setMore(R.layout.view_more, new RecyclerArrayAdapter.OnMoreListener() {
        @Override
        public void onMoreShow() {
            //     isRefresh = false;
            LoadStatus(false);
        }

        @Override
        public void onMoreClick() {

        }
    });
    adapter.setNoMore(R.layout.view_nomore);
    adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            Intent commentIntent = new Intent(HomepageActivity.this, API_CommentActivity.class);
            commentIntent.putExtra("Status", adapter.getItem(position));
            startActivity(commentIntent);
        }
    });


    // 上拉刷新 可能会有bug
    mListView.setRefreshListener(new android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter.clear();
                    LoadStatus(true);
                }
            }, 10);
        }
    });
/*
        DividerDecoration itemDecoration = new DividerDecoration(Color.GRAY, 5, 0, 0);//颜色 & 高度 & 左边距 & 右边距
        itemDecoration.setDrawLastItem(true);//有时候你不想让最后一个item有分割线,默认true.
        itemDecoration.setDrawHeaderFooter(false);//是否对Header于Footer有效,默认false.
        mListView.addItemDecoration(itemDecoration);
*/

    LoadStatus(true);
}catch(Exception e){
    System.out.println(e.toString());
}
    }

    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i(TAG, response);
                if (response.startsWith("{\"statuses\"")) {
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statuses = StatusList.parse(response);
                    if (statuses != null && statuses.total_number > 0) {
  //                      Toast.makeText(HomepageActivity.this,
    //                            "获取微博信息流成功, 条数: " + statuses.statusList.size(),
      //                          Toast.LENGTH_LONG).show();
                        toListView(statuses.statusList);
                    }
                } else {
                    Toast.makeText(HomepageActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(HomepageActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };

    private void toListView(ArrayList<Status> statusList){
        for(int i=0;i<statusList.size();i++){
            Status status = statusList.get(i);
            if(status.user.id.equals(user.id)){
                adapter.add(status);
            }

        }

        //  mListView.setAdapter(adapter);

        nowLastStatusID = Long.parseLong(statusList.get(statusList.size()-1).id);

//        if(count<=10)LoadStatus(false);
    }

    private void LoadStatus(boolean isRefresh){

//        Toast.makeText(this, "Weibo Contents Updating",
//                Toast.LENGTH_LONG).show();
        if (mAccessToken != null && mAccessToken.isSessionValid()) {
            /**
             * 获取最新的提到登录用户的微博列表，即@我的微博。
             *
             * @param since_id      若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0。
             * @param max_id        若指定此参数，则返回ID小于或等于max_id的微博，默认为0。
             * @param count         单页返回的记录条数，默认为50。
             * @param page          返回结果的页码，默认为1。
             * @param authorType    作者筛选类型，0：全部、1：我关注的人、2：陌生人 ,默认为0。可为以下几种 :
             *                      <li>{@link #AUTHOR_FILTER_ALL}
             *                      <li>{@link #AUTHOR_FILTER_ATTENTIONS}
             *                      <li>{@link #AUTHOR_FILTER_STRANGER}
             * @param sourceType    来源筛选类型，0：全部、1：来自微博的评论、2：来自微群的评论，默认为0。可为以下几种 :
             *                      <li>{@link #SRC_FILTER_ALL}
             *                      <li>{@link #SRC_FILTER_WEIBO}
             *                      <li>{@link #SRC_FILTER_WEIQUN}
             * @param filterType    原创筛选类型，0：全部微博、1：原创的微博，默认为0。 可为以下几种 :
             *                      <li>{@link #TYPE_FILTER_ALL}
             *                      <li>{@link #TYPE_FILTER_ORIGAL}
             * @param trim_user     返回值中user字段开关，false：返回完整user字段、true：user字段仅返回user_id，默认为false
             * @param listener      异步请求回调接口
             */




            if(isRefresh)mStatusesAPI.friendsTimeline(0L, 0L, 10, 1, false, 0, false, mListener);
            else mStatusesAPI.friendsTimeline(0L, nowLastStatusID-1, 10, 1, false, 0, false, mListener);
//            if(isRefresh)mStatusesAPI.mentions(0L, 0L, 10, 1, 0, 0, 0, false, mListener);
  //          else mStatusesAPI.mentions(0L, nowLastStatusID-1, 10, 1, 0, 0, 0, false, mListener);
            //Func : 获取 @当前用户 的最新微博
            //mStatusesAPI.mentions(0L, 0L, 10, 1, 0, 0, 0, false, mListener);


        } else {
            Toast.makeText(this,"access token is empty",
                    Toast.LENGTH_LONG).show();
        }

    }


}

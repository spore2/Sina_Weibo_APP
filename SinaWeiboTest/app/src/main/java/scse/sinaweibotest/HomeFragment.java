package scse.sinaweibotest;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


public class HomeFragment extends Fragment {

    private Activity mActivity;

    /** UI 元素：ListView */
    private EasyRecyclerView mListView;
    private StatusAdapter adapter = new StatusAdapter(mActivity);
    /** 当前 Token 信息 */
    private Oauth2AccessToken mAccessToken;
    /** 用于获取微博信息流等操作的API */
    private StatusesAPI mStatusesAPI;

    /** 用户信息接口 */
    //   private UsersAPI mUsersAPI;

    /**微博动态信息*/
    private long nowLastStatusID=1;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mActivity = (Activity)context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String toTheme = shp.getString("theme_which","GreyTheme");
        final Context contextThemeWrapper = new ContextThemeWrapper(
                getActivity(), getResources().getIdentifier(toTheme,"style",Constants.PACKAGE_NAME));
        LayoutInflater localInflater = inflater
                .cloneInContext(contextThemeWrapper);

        View res = localInflater.inflate(R.layout.fragment_home, container, false);
        try {
            RelativeLayout rl = res.findViewById(R.id.HomepageInfo);
            rl.setVisibility(View.GONE);

            mListView = res.findViewById(R.id.HomepageRecyclerView);
            mListView.setLayoutManager(new LinearLayoutManager(mActivity));
            mListView.setAdapter(adapter);

            // 获取当前已保存过的 Token
            mAccessToken = AccessTokenKeeper.readAccessToken(mActivity);

            mStatusesAPI = new StatusesAPI(mActivity, Constants.APP_KEY, mAccessToken);

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
                    try {
                        Intent commentIntent = new Intent(mActivity, API_CommentActivity.class);
                        commentIntent.putExtra("Status", adapter.getItem(position));
                        startActivity(commentIntent);
                    } catch (Exception e) {
                        System.out.println("ERROR " + e.toString());
                    }
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

            DividerDecoration itemDecoration = new DividerDecoration(Color.GRAY, 5, 0,0);//颜色 & 高度 & 左边距 & 右边距
            itemDecoration.setDrawLastItem(true);//有时候你不想让最后一个item有分割线,默认true.
            itemDecoration.setDrawHeaderFooter(false);//是否对Header于Footer有效,默认false.
            mListView.addItemDecoration(itemDecoration);

            LoadStatus(true);
        }catch(Exception e){
            System.out.println(e.toString());
        }
        return res;
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
                        Toast.makeText(mActivity,
                                "获取微博信息流成功, 条数: " + statuses.statusList.size(),
                                Toast.LENGTH_LONG).show();
                        toListView(statuses.statusList);
                    }
                } else {
                    Toast.makeText(mActivity, response, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(mActivity, info.toString(), Toast.LENGTH_LONG).show();
        }
    };

    private void toListView(ArrayList<Status> statusList){

        for(int i=0;i<statusList.size();i++){
            Status status = statusList.get(i);
            adapter.add(status);
        }

        //  mListView.setAdapter(adapter);

        nowLastStatusID = Long.parseLong(statusList.get(statusList.size()-1).id);

    }

    private void LoadStatus(boolean isRefresh){

        Toast.makeText(mActivity, "Weibo Contents Updating",
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
            //    if(isRefresh)mStatusesAPI.userTimeline(user.screen_name, 0L, 0L, 10, 1, false, 0, false, mListener);
            //  else mStatusesAPI.userTimeline(user.screen_name, 0L, nowLastStatusID-1, 10, 1, false, 0, false, mListener);
            if(isRefresh)mStatusesAPI.friendsTimeline(0L, 0L, 50, 1, false, 0, false, mListener);
            else mStatusesAPI.friendsTimeline(0L, nowLastStatusID-1, 50, 1, false, 0, false, mListener);
            //Func : 获取 @当前用户 的最新微博
            //mStatusesAPI.mentions(0L, 0L, 10, 1, 0, 0, 0, false, mListener);


        } else {
            Toast.makeText(mActivity,"access token is empty",
                    Toast.LENGTH_LONG).show();
        }

    }


}

package scse.sinaweibotest;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.CommentList;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.utils.LogUtil;

public class API_CommentActivity extends AppCompatActivity {
    private static final String TAG = "API_CommentActivity";
    private static final int getCommentByWeiboID = 0;
    private static final int getCommentByLoginUserID = 1;

    private Status status;

    /** UI 元素：ListView */
    private EasyRecyclerView mCommentListView;
    private EasyRecyclerView mStatusView;
    private StatusAdapter adapterStatus = new StatusAdapter(this);
    private CommentAdapter adapter = new CommentAdapter(this);


    /** 功能列表 */
    private String[] mFuncList;
    /** 当前 Token 信息 */
    private Oauth2AccessToken mAccessToken;
    /** 微博评论接口 */
    private CommentsAPI mCommentsAPI;

    /**微博动态信息*/
    private long nowLastCommentID=1;

    /**
     * @see {@link Activity#onCreate}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(API_CommentActivity.this);
            String toTheme = shp.getString("theme_which","GreyTheme");
            setTheme(API_CommentActivity.this.getResources().getIdentifier(toTheme,"style",Constants.PACKAGE_NAME));

            setContentView(R.layout.activity_content_comment);
            View view = findViewById(R.id.commentViewALL);
            view.setFocusable(true);
            view.setClickable(true);
            view.setLongClickable(true);

            StatusViewHolder.inComment = true;

            // 获取当前已保存过的 Token
            mAccessToken = AccessTokenKeeper.readAccessToken(this);
            // 获取微博评论信息接口
            mCommentsAPI = new CommentsAPI(this, Constants.APP_KEY, mAccessToken);

            // 初始化评论列表
            mCommentListView = (EasyRecyclerView) findViewById(R.id.CommentListRecyclerView);
            mCommentListView.setLayoutManager(new LinearLayoutManager(this));
            mCommentListView.setAdapter(adapter);

            adapter.setMore(R.layout.view_more, new RecyclerArrayAdapter.OnMoreListener() {
                @Override
                public void onMoreShow() {
                    //     isRefresh = false;
                    LoadComment(getCommentByWeiboID, false);
                }

                @Override
                public void onMoreClick() {

                }
            });

            adapter.setNoMore(R.layout.view_nomore);

            // 刷新相关
            mCommentListView.setRefreshListener(new android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mCommentListView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            adapter.clear();
                            LoadComment(getCommentByWeiboID, true);
                        }
                    }, 10);
                }
            });

            // 分割线相关
            DividerDecoration itemDecoration = new DividerDecoration(Color.GRAY, 5, 0, 0);//颜色 & 高度 & 左边距 & 右边距
            itemDecoration.setDrawLastItem(true);//有时候你不想让最后一个item有分割线,默认true.
            itemDecoration.setDrawHeaderFooter(false);//是否对Header于Footer有效,默认false.
            mCommentListView.addItemDecoration(itemDecoration);

            // 初始化Status显示，并接收来自上一个Activity传入的Status，
            // 出于偷懒和统一的考虑，这里虽然只有一个元素，仍然使用了EasyRecyclerView
            mStatusView = (EasyRecyclerView) findViewById(R.id.StatusContentInComment);
            mStatusView.setLayoutManager(new LinearLayoutManager(this));
            mStatusView.setAdapter(adapterStatus);
            status = (Status) getIntent().getSerializableExtra("Status");
            adapterStatus.add(status);

            // 获取评论信息
            LoadComment(getCommentByWeiboID, true);
            /**
             * 设定上，每次mListener接收到response后会自动执行toListView，但如果出错或导致程序闪退
             * 如果闪退，或由于mListContent为空
             */
        /*
        //todo 评论 做成嵌入微博内容元件的button
        SentComment("CommentText", 123, true);
        //TODO 回复评论 做成嵌入评论内容元件的button OR 做成 对应ViewHolder的OnClickListener
        ReplyComment(123, 123, "ReplyText", true);
        //TODO 删除评论 做成嵌入评论内容元件的button 注意只能删除自己的评论
        //其实以上接口就不一定能用
        DeleteComment(123);
        */
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        StatusViewHolder.inComment=false;
    }

    /**
     * 微博 OpenAPI 回调接口。
     * TODO 补全发表评论、回复评论、删除评论的内容
     * * 无法从 发表评论、回复评论、删除评论 时的response来区分操作?
     * * * Plan A 可以定义全局变量来对此进行判断 在发送请求时赋值
     */
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {

            LogUtil.i(TAG, response);
            //我不确定这里是否能类比API_StatusActivity，待测试
            if (response.startsWith("{\"comments\"")) {
                // 调用 StatusList#parse 解析字符串成微博列表对象
                CommentList comments = CommentList.parse(response);
                if(comments != null && comments.total_number > 0){
                    Toast.makeText(API_CommentActivity.this,
                            "获取评论成功, 条数: " + comments.commentList.size(),
                            Toast.LENGTH_LONG).show();
                    toListView(comments);
                }
            } else if (response.startsWith("{\"created_at\"")) {
                ;
            } else {
                Toast.makeText(API_CommentActivity.this, response, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
        }
    };

    //todo 现在只支持时序显示
    //StatusID现在被定义为了全局变量
    private void LoadComment(int type, boolean isRefresh){
        Toast.makeText(this, "Comment Contents Updating",
                Toast.LENGTH_LONG).show();
        switch(type){
            case getCommentByWeiboID :
                /**
                 * 根据微博ID返回某条微博的评论列表。
                 *
                 *   @param id         需要查询的微博ID。
                 * @param since_id   若指定此参数，则返回ID比since_id大的评论（即比since_id时间晚的评论），默认为0。
                 * @param max_id     若指定此参数，则返回ID小于或等于max_id的评论，默认为0。
                 * @param count      单页返回的记录条数，默认为50
                 * @param page       返回结果的页码，默认为1。
                 *  @param authorType 作者筛选类型，0：全部、1：我关注的人、2：陌生人 ,默认为0。可为以下几种 :
                 *                   <li>{@link #AUTHOR_FILTER_ALL}
                 *                   <li>{@link #AUTHOR_FILTER_ATTENTIONS}
                 *                   <li>{@link #AUTHOR_FILTER_STRANGER}
                 * @param listener   异步请求回调接口
                 */

                if(isRefresh)mCommentsAPI.show(Long.valueOf(status.id), 0, 0, 10, 1, 0, mListener);
                else mCommentsAPI.show(Long.valueOf(status.id), 0,nowLastCommentID-1, 10, 1, 0, mListener);
                break;
        }
    }

    private void toListView(CommentList comments){

        for(int i=0;i<comments.commentList.size();i++){
            Comment comment = comments.commentList.get(i);
            adapter.add(comment);
        }

        nowLastCommentID = Long.parseLong(comments.commentList.get(comments.commentList.size()-1).id);

    }

    private void SentComment(String CommentText, long Statusid, boolean comment_ori){
        /**
         * 对一条微博进行评论。
         *
         * @param comment     评论内容，内容不超过140个汉字。
         * @param id          需要评论的微博ID。
         * @param comment_ori 当评论转发微博时，是否评论给原微博
         * @param listener    异步请求回调接口
         */
        mCommentsAPI.create(CommentText, Statusid, comment_ori, mListener);
    }

    private void DeleteComment(long CommentID){
        /**
         * 删除一条评论。
         *
         * @param cid      要删除的评论ID，只能删除登录用户自己发布的评论。
         * @param listener 异步请求回调接口
         */
        mCommentsAPI.destroy(CommentID, mListener);
    }

    private void ReplyComment(long ReplyCommentID, long StatusID, String Text, boolean comment_ori){
        /**
         * 回复一条评论。
         *
         * @param cid             需要回复的评论ID
         * @param id              需要评论的微博ID
         * @param comment         回复评论内容，内容不超过140个汉字
         * @param without_mention 回复中是否自动加入“回复@用户名”，true：是、false：否，默认为false
         * @param comment_ori     当评论转发微博时，是否评论给原微博，false：否、true：是，默认为false
         * @param listener        异步请求回调接口
         */
        mCommentsAPI.reply(ReplyCommentID, StatusID, Text, true, comment_ori, mListener);
    }
}

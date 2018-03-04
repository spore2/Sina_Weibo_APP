package scse.sinaweibotest;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.sina.weibo.sdk.openapi.models.Comment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommentViewHolder extends BaseViewHolder<Comment> {
    private ImageView commentUserIcon;
    private TextView commentUserName;
    private TextView commentCreateAt;
    private TextView commentText;
    private RelativeLayout commentReply;
    private TextView commentReplyNameText;

    public CommentViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_comment);
        commentUserIcon = $(R.id.CommentUserICON);
        commentUserName = $(R.id.CommentUserName);
        commentCreateAt = $(R.id.CommentCreateAt);
        commentText = $(R.id.CommentText);
        commentReply = $(R.id.CommentReply);
        commentReplyNameText = $(R.id.CommentReplyNameText);

    }

    @Override
    public void setData(final Comment comment){
        //加载头像
        Glide.with(getContext())
                .load(comment.user.avatar_large)
//                .placeholder(R.drawable.placeholderUserIcon)
//                .bitmapTransform(new CropCircleTransformation(getContext()))
                .into(commentUserIcon);
        //加载用户名
        commentUserName.setText(comment.user.screen_name);
        //加载创建时间
        String createdAt;
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        DateFormat df2 = new SimpleDateFormat("yyyy-MMM-dd", Locale.CHINA);

        Date date = new Date();
        try {
            date = df.parse(comment.created_at);
        } catch (ParseException e) {
            ;//date = now
        }

        long timeDiff = new Date().getTime()-date.getTime();

        long minute = 60 * 1000;
        long hour = 60 * minute;
        long day = 24 * hour;

        if(timeDiff < minute){
            createdAt = "刚刚";
        } else if(timeDiff < hour){
            createdAt = Long.toString(timeDiff/minute) + "分钟前";
        } else if(timeDiff < day){
            createdAt = Long.toString(timeDiff/hour) + "小时前";
        } else if(timeDiff < 2*day){
            createdAt = "昨天";
        } else {
            createdAt = df2.format(date);
        }

        commentCreateAt.setText(createdAt);
        //加载正文
        commentText.setText(comment.text);

        //todo 获取评论的点赞数 没有api
        //todo 只能获取某条评论所回复的评论，不能获取评论的回复
        //接上 这就导致了阅读评论的体验很差很差

        //加载被评论评论
        if(comment.reply_comment!=null){
            //加载正文
            String NameText = "@"
                    + comment.reply_comment.user.screen_name
                    + " : "
                    + comment.reply_comment.text;
            commentReplyNameText.setText(NameText);
        }
        else commentReply.setVisibility(View.GONE);
    }

}

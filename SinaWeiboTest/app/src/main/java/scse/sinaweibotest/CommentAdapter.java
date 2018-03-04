package scse.sinaweibotest;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.sina.weibo.sdk.openapi.models.Comment;

public class CommentAdapter extends RecyclerArrayAdapter<Comment> {
    public CommentAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommentViewHolder(parent);
    }
}
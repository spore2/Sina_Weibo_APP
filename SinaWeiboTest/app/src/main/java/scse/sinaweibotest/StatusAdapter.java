package scse.sinaweibotest;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.sina.weibo.sdk.openapi.models.Status;

public class StatusAdapter extends RecyclerArrayAdapter<Status> {
    public StatusAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new StatusViewHolder(parent);
    }
}
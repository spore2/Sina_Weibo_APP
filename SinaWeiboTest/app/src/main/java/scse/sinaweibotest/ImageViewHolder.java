package scse.sinaweibotest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

public class ImageViewHolder extends BaseViewHolder<StatusImage> {
    private ImageView statusImage;

    public ImageViewHolder(ViewGroup parent) {
        super(new ImageView(parent.getContext()));
        statusImage = (ImageView) itemView;
        statusImage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        statusImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    @Override
    public void setData(StatusImage data) {
        ViewGroup.LayoutParams params = statusImage.getLayoutParams();
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean no_pic_mode = shp.getBoolean("no_picture_mode",false);

        if(no_pic_mode)return;

        //todo 目前图片显示太模糊 还有就是要做点开显示大图 目前这个瀑布流显示也不是很好看
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();

        params.height = dm.widthPixels/4; // height
        statusImage.setLayoutParams(params);
        //imgPicture.setImageResource(R.mipmap.ic_launcher);
        Glide.with(getContext())
                .load(data.getSrc()+"?imageView2/0/w/"+ dm.widthPixels/4) // width
                .into(statusImage);
    }
}

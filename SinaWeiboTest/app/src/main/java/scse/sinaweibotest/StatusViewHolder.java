package scse.sinaweibotest;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.sina.weibo.sdk.openapi.models.Status;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusViewHolder extends BaseViewHolder<Status> {
    private ViewGroup thisParent;
    public static boolean inComment = false;

    private ImageButton statusUserIcon;
    private TextView statusUserName;
    private TextView statusCreateAt;
    private TextView statusSource;
    private TextView statusText;
   // private TextView statusImage;
   private EasyRecyclerView statusNineImage;
    private RelativeLayout statusRetweeted;
    private TextView statusRetweetedNameText;
 //   private TextView statusRetweetedImage;
    private EasyRecyclerView statusRetweetedImage;

    private ImageButton statusShareButton;
    private ImageButton statusLikeButton;
    private ImageButton statusCommentButton;
    private ImageButton statusRetweetedButton;
    private ImageButton statusHideStatusButton;

    public StatusViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_status);
        thisParent = parent;

        statusUserIcon = $(R.id.StatusUserICON);
        statusUserName = $(R.id.StatusUserName);
        statusCreateAt = $(R.id.StatusCreateAt);
        statusSource = $(R.id.StatusSource);
        statusText = $(R.id.StatusText);
        //statusImage = $(R.id.StatusImage);
        statusNineImage = $(R.id.StatusNineImage);


        statusRetweeted = $(R.id.StatusRetweeted);
        statusRetweetedNameText = $(R.id.StatusRetweetedNameText);
        statusRetweetedImage = $(R.id.StatusRetweetedImage);
        statusShareButton = $(R.id.StatusShareButton);
        statusLikeButton = $(R.id.StatusLikeButton);
        statusCommentButton = $(R.id.StatusCommentButton);
        statusRetweetedButton = $(R.id.StatusRetweetedButton);
        statusHideStatusButton = $(R.id.HideStatus);
    }

    @Override
    public void setData(final Status status){
        try {
            //加载头像
            Glide.with(getContext())
                    .load(status.user.avatar_large)
//                .placeholder(R.drawable.placeholderUserIcon)
//                .bitmapTransform(new CropCircleTransformation(getContext()))
                    .into(statusUserIcon);
            statusUserIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent homepageIntent = new Intent(getContext(), HomepageActivity.class);
                        homepageIntent.putExtra("User", status.user);
                        getContext().startActivity(homepageIntent);
                    } catch (Exception e) {
                        System.out.println("ERROR " + e.toString());
                    }
                }
            });
            //加载用户名
            statusUserName.setText(status.user.name);
            //加载创建时间
            String createdAt;
            DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
            DateFormat df2 = new SimpleDateFormat("yyyy-MMM-dd", Locale.CHINA);

            Date date = new Date();
            try {
                date = df.parse(status.created_at);
            } catch (ParseException e) {
                ;//date = now
            }

            long timeDiff = new Date().getTime() - date.getTime();

            long minute = 60 * 1000;
            long hour = 60 * minute;
            long day = 24 * hour;

            if (timeDiff < minute) {
                createdAt = "刚刚";
            } else if (timeDiff < hour) {
                createdAt = Long.toString(timeDiff / minute) + "分钟前";
            } else if (timeDiff < day) {
                createdAt = Long.toString(timeDiff / hour) + "小时前";
            } else if (timeDiff < 2 * day) {
                createdAt = "昨天";
            } else {
                createdAt = df2.format(date);
            }

            statusCreateAt.setText(createdAt);
            //加载来源
            Pattern pattern = Pattern.compile(">.+<");
            Matcher m = pattern.matcher(status.source);
            if (m.find()) statusSource.setText(m.group(0).substring(1, m.group(0).length() - 1));
            else statusSource.setText(status.source);
            //加载正文
            statusText.setText(status.text);
            statusText.setText(formatWeiBoContent(getContext(), statusText.getText().toString(), statusText, status));

            if (status.pic_urls != null) {
                ImageAdapter adapter = new ImageAdapter(getContext());
                statusNineImage.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
                statusNineImage.setAdapter(adapter);
                for (int i = 0; i < status.pic_urls.size(); i++) {
                    adapter.add(new StatusImage(status.pic_urls.get(i)));
                }
            } else statusNineImage.setVisibility(View.GONE);

            //加载被转发微博
            if (status.retweeted_status != null) {
                //加载正文
                String NameText = "@"
                        + status.retweeted_status.user.screen_name
                        + " : "
                        + status.retweeted_status.text;
                statusRetweetedNameText.setText(NameText);
                statusRetweetedNameText.setText(formatWeiBoContent(getContext(), statusRetweetedNameText.getText().toString(), statusRetweetedNameText, status));

                //加载图片
                if (status.retweeted_status.pic_urls != null) {
                    ImageAdapter adapter2 = new ImageAdapter(getContext());
                    statusRetweetedImage.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
                    statusRetweetedImage.setAdapter(adapter2);
                    for (int i = 0; i < status.retweeted_status.pic_urls.size(); i++) {
                        adapter2.add(new StatusImage(status.retweeted_status.pic_urls.get(i)));
                    }
                } else statusRetweetedImage.setVisibility(View.GONE);
            } else statusRetweeted.setVisibility(View.GONE);

            // 加载ImageButton
            Glide.with(getContext())
                    .load("android.resource://scse.sinaweibotest/drawable/hide")
                    .into(statusHideStatusButton);
            Glide.with(getContext())
                    .load("android.resource://scse.sinaweibotest/drawable/comment")
                    .into(statusCommentButton);
            Glide.with(getContext())
                    .load("android.resource://scse.sinaweibotest/drawable/share")
                    .into(statusShareButton);
            Glide.with(getContext())
                    .load("android.resource://scse.sinaweibotest/drawable/retweeted")
                    .into(statusRetweetedButton);
            Glide.with(getContext())
                    .load("android.resource://scse.sinaweibotest/drawable/like")
                    .into(statusLikeButton);

            statusHideStatusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inComment)
                        thisParent.setVisibility(View.GONE);
                    //         inComment = true;
                }
            });

            statusCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("该功能尚未完善")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();


                    // TODO: 2017/10/23 评论
                }

            });
            statusLikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("哦不，新浪微博没有开放相关API！")
                            .setPositiveButton("确定", null)
                            .setNegativeButton("取消", null)
                            .show();
                    // TODO: 2017/10/23 赞
                }
            });
            statusRetweetedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("哦不，新浪微博没有开放相关API！")
                            .setPositiveButton("确定", null)
                            .setNegativeButton("取消", null)
                            .show();
                    // TODO: 2017/10/23 转发
                }
            });
            statusShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    // TODO: 2017/10/23  分享
                }
            });


        }catch (Exception e){
            System.out.println(e.toString());
        }
    }


    /**
     * 格式化微博文本
     *
     * @param context 上下文
     * @param source 源文本
     * @param textView 目标 TextView
     * @return SpannableStringBuilder
     */
    public static SpannableStringBuilder formatWeiBoContent(final Context context, String source, TextView textView,final Status status) {

        // 获取到 TextView 的文字大小，后面的 ImageSpan 需要用到该值
        int textSize = (int) textView.getTextSize();

        // 若要部分 SpannableString 可点击，需要如下设置
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        // 将要格式化的 String 构建成一个 SpannableStringBuilder
        SpannableStringBuilder value = new SpannableStringBuilder(source);

        // 使用正则匹配话题
        Linkify.addLinks(value, Constants.PATTERN_TOPIC, Constants.SCHEME_TOPIC);
        // 使用正则匹配链接
        Linkify.addLinks(value, Constants.PATTERN_URL, Constants.SCHEME_URL);
        // 使用正则匹配@用户
        Linkify.addLinks(value, Constants.PATTERN_AT, Constants.SCHEME_AT);

        // 自定义的匹配部分的点击效果
        MyClickableSpan clickSpan;

        // 获取上面到所有 addLinks 后的匹配部分(这里一个匹配项被封装成了一个 URLSpan 对象)
        URLSpan[] urlSpans = value.getSpans(0, value.length(), URLSpan.class);

        // 遍历所有的 URLSpan
        for (final URLSpan urlSpan : urlSpans) {
            // 点击匹配部分效果
            clickSpan = new MyClickableSpan() {
                @Override
                public void onClick(View view) {
                    String webURL = urlSpan.getURL();
                    if(webURL.contains("url:")){
                        webURL = webURL.replace("url:","");
                        Intent webIntent = new Intent();
                        webIntent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(webURL);
                        webIntent.setData(content_url);
                        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(webIntent);
                    } else if(webURL.contains("at:")){
                        webURL = webURL.replace("at:","");
                        Toast.makeText(context, "您戳了"+webURL+"一下", Toast.LENGTH_LONG).show();
                    }
                }
            };
            // 话题
            if (urlSpan.getURL().startsWith(Constants.SCHEME_TOPIC)) {
                int start = value.getSpanStart(urlSpan);
                int end = value.getSpanEnd(urlSpan);
                value.removeSpan(urlSpan);
                // 格式化话题部分文本
                value.setSpan(clickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            // @用户
            if (urlSpan.getURL().startsWith(Constants.SCHEME_AT)) {
                int start = value.getSpanStart(urlSpan);
                int end = value.getSpanEnd(urlSpan);
                value.removeSpan(urlSpan);
                // 格式化@用户部分文本
                value.setSpan(clickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            // 链接
            if (urlSpan.getURL().startsWith(Constants.SCHEME_URL)) {
                int start = value.getSpanStart(urlSpan);
                int end = value.getSpanEnd(urlSpan);
                value.removeSpan(urlSpan);
                SpannableStringBuilder urlSpannableString = getUrlTextSpannableString(context, urlSpan.getURL(), textSize);
                value.replace(start, end, urlSpannableString);
                // 格式化链接部分文本
                value.setSpan(clickSpan, start, start + urlSpannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        // 表情需要单独格式化
        Matcher emotionMatcher = Constants.PATTERN_EMOTION.matcher(value);
        while (emotionMatcher.find()) {
            String emotion = emotionMatcher.group();
            int start = emotionMatcher.start();
            int end = emotionMatcher.end();

            int resId = EmotionUtils.getImageByName(emotion);
            if (resId != -1) { // 表情匹配
                Drawable drawable = context.getResources().getDrawable(resId,null);
                drawable.setBounds(0, 0, (int) (textSize * 1.3), (int) (textSize * 1.3));
                // 自定义的 VerticalImageSpan ，可解决默认的 ImageSpan 不垂直居中的问题
                VerticalImageSpan imageSpan = new VerticalImageSpan(drawable);
                value.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        }

        return value;
    }
    private static SpannableStringBuilder getUrlTextSpannableString(Context context, String source, int size) {
        SpannableStringBuilder builder = new SpannableStringBuilder(source);
        String prefix = " ";
        builder.replace(0, prefix.length(), prefix);

        Drawable drawable = context.getResources().getDrawable(R.drawable.status_link,null);
        drawable.setBounds(0, 0, size, size);

        builder.setSpan(new VerticalImageSpan(drawable), prefix.length(), source.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(" 网页链接");
        return builder;
    }
}



class MyClickableSpan extends ClickableSpan {

    @Override
    public void onClick(View view) {
        ;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(0xff03A9F4);
        ds.setUnderlineText(false);
    }

}

class VerticalImageSpan extends ImageSpan {

    public VerticalImageSpan(Drawable drawable) {
        super(drawable);
    }

    /**
     * update the text line height
     */
    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end,
                       Paint.FontMetricsInt fontMetricsInt) {
        Drawable drawable = getDrawable();
        Rect rect = drawable.getBounds();
        if (fontMetricsInt != null) {
            Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
            int fontHeight = fmPaint.descent - fmPaint.ascent;
            int drHeight = rect.bottom - rect.top;
            int centerY = fmPaint.ascent + fontHeight / 2;

            fontMetricsInt.ascent = centerY - drHeight / 2;
            fontMetricsInt.top = fontMetricsInt.ascent;
            fontMetricsInt.bottom = centerY + drHeight / 2;
            fontMetricsInt.descent = fontMetricsInt.bottom;
        }
        return rect.right;
    }

    /**
     * see detail message in android.text.TextLine
     *
     * @param canvas the canvas, can be null if not rendering
     * @param text the text to be draw
     * @param start the text start position
     * @param end the text end position
     * @param x  the edge of the replacement closest to the leading margin
     * @param top the top of the line
     * @param y  the baseline
     * @param bottom the bottom of the line
     * @param paint the work paint
     */
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {

        Drawable drawable = getDrawable();
        canvas.save();
        Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
        int fontHeight = fmPaint.descent - fmPaint.ascent;
        int centerY = y + fmPaint.descent - fontHeight / 2;
        int transY = centerY - (drawable.getBounds().bottom - drawable.getBounds().top) / 2;
        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.restore();
    }

}

@SuppressWarnings("serial")
class EmotionUtils implements Serializable {

    public static Map<String, Integer> emojiMap;

    static {
        emojiMap = new HashMap<String, Integer>();


        emojiMap.put("[奥特曼]", R.drawable.d_aoteman);
        emojiMap.put("[宝宝]", R.drawable.d_baobao);
        emojiMap.put("[并不简单]", R.drawable.d_bingbujiandan);
        emojiMap.put("[吃瓜]", R.drawable.d_chigua);
        emojiMap.put("[二哈]", R.drawable.d_erha);
        emojiMap.put("[防毒面具]", R.drawable.d_fangdumianju);
        emojiMap.put("[费解]", R.drawable.d_feijie);
        emojiMap.put("[肥皂]", R.drawable.d_feizao);
        emojiMap.put("[跪了]", R.drawable.d_guile);
        emojiMap.put("[嘿嘿嘿]", R.drawable.d_heiheihei);
        emojiMap.put("[坏笑]", R.drawable.d_huaixiao);
        emojiMap.put("[骷髅]", R.drawable.d_kulou);
        emojiMap.put("[浪]", R.drawable.d_lang);
        emojiMap.put("[男孩儿]", R.drawable.d_nanhaier);
        emojiMap.put("[神兽]", R.drawable.d_shenshou);
        emojiMap.put("[摊手]", R.drawable.d_tanshou);
        emojiMap.put("[舔]", R.drawable.d_tian);
        emojiMap.put("[旅行]", R.drawable.d_travel);
        emojiMap.put("[捂]", R.drawable.d_wu);
        emojiMap.put("[星星眼]", R.drawable.d_xingxingyan);
        emojiMap.put("[允悲]", R.drawable.d_yunbei);
        emojiMap.put("[呵呵]", R.drawable.d_zuiyou);

        emojiMap.put("[呵呵]", R.drawable.d_hehe);
        emojiMap.put("[嘻嘻]", R.drawable.d_xixi);
        emojiMap.put("[哈哈]", R.drawable.d_haha);
        emojiMap.put("[爱你]", R.drawable.d_aini);
        emojiMap.put("[挖鼻屎]", R.drawable.d_wabishi);
        emojiMap.put("[吃惊]", R.drawable.d_chijing);
        emojiMap.put("[晕]", R.drawable.d_yun);
        emojiMap.put("[泪]", R.drawable.d_lei);
        emojiMap.put("[馋嘴]", R.drawable.d_chanzui);
        emojiMap.put("[抓狂]", R.drawable.d_zhuakuang);
        emojiMap.put("[哼]", R.drawable.d_heng);
        emojiMap.put("[可爱]", R.drawable.d_keai);
        emojiMap.put("[怒]", R.drawable.d_nu);
        emojiMap.put("[汗]", R.drawable.d_han);
        emojiMap.put("[害羞]", R.drawable.d_haixiu);
        emojiMap.put("[睡觉]", R.drawable.d_shuijiao);
        emojiMap.put("[钱]", R.drawable.d_qian);
        emojiMap.put("[偷笑]", R.drawable.d_touxiao);
        emojiMap.put("[笑cry]", R.drawable.d_xiaoku);
        emojiMap.put("[doge]", R.drawable.d_doge);
        emojiMap.put("[喵喵]", R.drawable.d_miao);
        emojiMap.put("[酷]", R.drawable.d_ku);
        emojiMap.put("[衰]", R.drawable.d_shuai);
        emojiMap.put("[闭嘴]", R.drawable.d_bizui);
        emojiMap.put("[鄙视]", R.drawable.d_bishi);
        emojiMap.put("[花心]", R.drawable.d_huaxin);
        emojiMap.put("[鼓掌]", R.drawable.d_guzhang);
        emojiMap.put("[悲伤]", R.drawable.d_beishang);
        emojiMap.put("[思考]", R.drawable.d_sikao);
        emojiMap.put("[生病]", R.drawable.d_shengbing);
        emojiMap.put("[亲亲]", R.drawable.d_qinqin);
        emojiMap.put("[怒骂]", R.drawable.d_numa);
        emojiMap.put("[太开心]", R.drawable.d_taikaixin);
        emojiMap.put("[懒得理你]", R.drawable.d_landelini);
        emojiMap.put("[右哼哼]", R.drawable.d_youhengheng);
        emojiMap.put("[左哼哼]", R.drawable.d_zuohengheng);
        emojiMap.put("[嘘]", R.drawable.d_xu);
        emojiMap.put("[委屈]", R.drawable.d_weiqu);
        emojiMap.put("[吐]", R.drawable.d_tu);
        emojiMap.put("[可怜]", R.drawable.d_kelian);
        emojiMap.put("[打哈气]", R.drawable.d_dahaqi);
        emojiMap.put("[挤眼]", R.drawable.d_jiyan);
        emojiMap.put("[失望]", R.drawable.d_shiwang);
        emojiMap.put("[顶]", R.drawable.d_ding);
        emojiMap.put("[疑问]", R.drawable.d_yiwen);
        emojiMap.put("[困]", R.drawable.d_kun);
        emojiMap.put("[感冒]", R.drawable.d_ganmao);
        emojiMap.put("[拜拜]", R.drawable.d_baibai);
        emojiMap.put("[黑线]", R.drawable.d_heixian);
        emojiMap.put("[阴险]", R.drawable.d_yinxian);
        emojiMap.put("[打脸]", R.drawable.d_dalian);
        emojiMap.put("[傻眼]", R.drawable.d_shayan);
        emojiMap.put("[猪头]", R.drawable.d_zhutou);
        emojiMap.put("[熊猫]", R.drawable.d_xiongmao);
        emojiMap.put("[兔子]", R.drawable.d_tuzi);
    }

    public static int getImageByName(String imgName) {
        Integer integer = emojiMap.get(imgName);
        return integer == null ? -1 : integer;
    }
}
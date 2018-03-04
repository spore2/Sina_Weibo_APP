package scse.sinaweibotest;

import java.util.regex.Pattern;

public interface Constants {

    public static final String PACKAGE_NAME = "scse.sinaweibotest";
    public static final String GLIDE_DRAWABLE_PATH = "android.resource://"+PACKAGE_NAME+"/drawable/";

    public static final String APP_KEY = "92981932";
    public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    public static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

    // #话题#
    public static final String REGEX_TOPIC = "#[\\p{Print}\\p{InCJKUnifiedIdeographs}&&[^#]]+#";
    // [表情]
    public static final String REGEX_EMOTION = "\\[(\\S+?)\\]";
    // url
    public static final String REGEX_URL = "http://[a-zA-Z0-9+&@#/%?=~_\\\\-|!:,\\\\.;]*[a-zA-Z0-9+&@#/%=~_|]";
    // @人
    public static final String REGEX_AT = "@[\\w\\p{InCJKUnifiedIdeographs}-]{1,26}";


    public static final Pattern PATTERN_TOPIC = Pattern.compile(REGEX_TOPIC);
    public static final Pattern PATTERN_EMOTION = Pattern.compile(REGEX_EMOTION);
    public static final Pattern PATTERN_URL = Pattern.compile(REGEX_URL);
    public static final Pattern PATTERN_AT = Pattern.compile(REGEX_AT);

    public static final String SCHEME_TOPIC = "topic:";
    public static final String SCHEME_URL = "url:";
    public static final String SCHEME_AT = "at:";
}

package me.firesun.wechat.enhancement;


import de.robv.android.xposed.XSharedPreferences;

public class PreferencesUtils {

    private static XSharedPreferences instance = null;

    private static XSharedPreferences getInstance() {
        if (instance == null) {
            instance = new XSharedPreferences(PreferencesUtils.class.getPackage().getName());
            instance.makeWorldReadable();
        } else {
            instance.reload();
        }
        return instance;
    }

    public static boolean open() {
        return getInstance().getBoolean("open", false);
    }

    public static boolean notSelf() {
        return getInstance().getBoolean("not_self", false);
    }

    public static boolean notWhisper() {
        return getInstance().getBoolean("not_whisper", false);
    }

    public static String notContains() {
        return getInstance().getString("not_contains", "").replace("，", ",");
    }

    public static boolean delay() {
        return getInstance().getBoolean("delay", false);
    }

    public static int delayMin() {
        return getInstance().getInt("delay_min", 0);
    }

    public static int delayMax() {
        return getInstance().getInt("delay_max", 0);
    }

    public static boolean receiveTransfer() {
        return getInstance().getBoolean("receive_transfer", true);
    }

    public static boolean quickOpen() {
        return getInstance().getBoolean("quick_open", true);
    }

    public static boolean showWechatId() {
        return getInstance().getBoolean("show_wechat_id", false);
    }

    public static String blackList() {
        return getInstance().getString("black_list", "").replace("，", ",");
    }

    public static boolean isAntiRevoke() {
        return getInstance().getBoolean("is_anti_revoke", false);
    }

    public static boolean isAntiSnsDelete() {
        return getInstance().getBoolean("is_anti_sns_delete", false);
    }

    public static boolean isAutoLogin() {
        return getInstance().getBoolean("is_auto_login", false);
    }

    public static boolean isBreakLimit() {
        return getInstance().getBoolean("is_break_limit", false);
    }

}



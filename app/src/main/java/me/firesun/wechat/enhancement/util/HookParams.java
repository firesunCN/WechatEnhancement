package me.firesun.wechat.enhancement.util;

public class HookParams {
    public static final String SAVE_WECHAT_ENHANCEMENT_CONFIG = "wechat.intent.action.SAVE_WECHAT_ENHANCEMENT_CONFIG";
    public static final String WECHAT_ENHANCEMENT_CONFIG_NAME = "wechat_enhancement_config";
    public static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    public static final int VERSION_CODE = 47; //大版本变动时候才需要修改

    public String SQLiteDatabaseClassName = "com.tencent.wcdb.database.SQLiteDatabase";
    public String SQLiteDatabaseUpdateMethod = "updateWithOnConflict";
    public String SQLiteDatabaseInsertMethod = "insert";
    public String SQLiteDatabaseDeleteMethod = "delete";
    public String ContactInfoUIClassName = "com.tencent.mm.plugin.profile.ui.ContactInfoUI";

    public String ContactInfoClassName;
    public String ChatroomInfoUIClassName = "com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI";
    public String WebWXLoginUIClassName = "com.tencent.mm.plugin.webwx.ui.ExtDeviceWXLoginUI";
    public String AlbumPreviewUIClassName = "com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI";
    public String SelectContactUIClassName = "com.tencent.mm.ui.contact.SelectContactUI";
    public String MMActivityClassName = "com.tencent.mm.ui.MMActivity";
    public String SelectConversationUIClassName = "com.tencent.mm.ui.transmit.SelectConversationUI";
    public String SelectConversationUICheckLimitMethod;
    public String LuckyMoneyReceiveUIClassName = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    public String XMLParserClassName;
    public String XMLParserMethod;
    public String MsgInfoClassName;
    public String MsgInfoStorageClassName;
    public String MsgInfoStorageInsertMethod;
    public String ReceiveUIParamNameClassName;
    public String ReceiveUIMethod;
    public String NetworkRequestClassName;
    public String RequestCallerClassName;
    public String RequestCallerMethod;
    public String GetNetworkByModelMethod;
    public String ReceiveLuckyMoneyRequestClassName;
    public String ReceiveLuckyMoneyRequestMethod;
    public String LuckyMoneyRequestClassName;
    public String GetTransferRequestClassName;
    public boolean hasTimingIdentifier = true;
    public String versionName;
    public int versionCode;

    private static HookParams instance = null;

    private HookParams() {
    }

    public static HookParams getInstance() {
        if (instance == null)
            instance = new HookParams();
        return instance;
    }

    public static void setInstance(HookParams i) {
        instance = i;
    }

    public static boolean hasInstance() {
        return instance != null;
    }

}

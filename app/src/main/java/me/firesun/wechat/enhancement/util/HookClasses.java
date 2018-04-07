package me.firesun.wechat.enhancement.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.DexClass;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.firesun.wechat.enhancement.Main;

import static de.robv.android.xposed.XposedBridge.log;


public class HookClasses {
    public static final String SAVE_WECHAT_ENHANCEMENT_CONFIG = "wechat.intent.action.SAVE_WECHAT_ENHANCEMENT_CONFIG";
    public static final String WECHAT_ENHANCEMENT_CONFIG_NAME = "wechat_enhancement_config";

    public static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    public static String SQLiteDatabaseClassName = "com.tencent.wcdb.database.SQLiteDatabase";
    public static String SQLiteDatabaseUpdateMethod = "updateWithOnConflict";
    public static String SQLiteDatabaseInsertMethod = "insert";
    public static String SQLiteDatabaseDeleteMethod = "delete";
    public static String ContactInfoUIClassName = "com.tencent.mm.plugin.profile.ui.ContactInfoUI";
    public static String ChatroomInfoUIClassName = "com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI";
    public static String WebWXLoginUIClassName = "com.tencent.mm.plugin.webwx.ui.ExtDeviceWXLoginUI";
    public static String AlbumPreviewUIClassName = "com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI";
    public static String SelectContactUIClassName = "com.tencent.mm.ui.contact.SelectContactUI";
    public static String MMActivityClassName = "com.tencent.mm.ui.MMActivity";
    public static String SelectConversationUIClassName = "com.tencent.mm.ui.transmit.SelectConversationUI";
    public static String LuckyMoneyReceiveUIClassName = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    public static Class XMLParserClass;
    public static String XMLParserMethod;
    public static Class MsgInfoClass;
    public static Class MsgInfoStorageClass;
    public static String MsgInfoStorageInsertMethod;
    public static Class ReceiveUIParamNameClass;
    public static String ReceiveUIMethod;
    public static Class NetworkRequestClass;
    public static Class RequestCallerClass;
    public static String RequestCallerMethod;
    public static String GetNetworkByModelMethod;
    public static Class ReceiveLuckyMoneyRequestClass;
    public static String ReceiveLuckyMoneyRequestMethod;
    public static Class LuckyMoneyRequestClass;
    public static Class GetTransferRequestClass;
    public static boolean hasTimingIdentifier = true;
    public static String versionName = null;
    private static List<String> wxClasses = new ArrayList();


    private static XSharedPreferences preferencesInstance = null;

    public static void init(Context context, XC_LoadPackage.LoadPackageParam lpparam, String versionName) {
        if (loadConfig(lpparam, versionName))
            return;

        log("failed to load config, start finding...");
        HookClasses.versionName = versionName;
        int versionNum = getVersionNum(versionName);
        if (versionNum >= getVersionNum("6.5.6") && versionNum <= getVersionNum("6.5.23"))
            LuckyMoneyReceiveUIClassName = "com.tencent.mm.plugin.luckymoney.ui.En_fba4b94f";
        if (versionNum < getVersionNum("6.5.8"))
            SQLiteDatabaseClassName = "com.tencent.mmdb.database.SQLiteDatabase";
        if (versionNum < getVersionNum("6.5.4"))
            hasTimingIdentifier = false;

        ApkFile apkFile = null;
        try {
            apkFile = new ApkFile(lpparam.appInfo.sourceDir);
            DexClass[] dexClasses = apkFile.getDexClasses();
            for (int i = 0; i < dexClasses.length; i++) {
                wxClasses.add(ReflectionUtil.getClassName(dexClasses[i]));
            }
        } catch (Error | Exception e) {
        } finally {
            try {
                apkFile.close();
            } catch (Exception e) {
            }
        }

        ReceiveUIParamNameClass = ReflectionUtil.findClassesFromPackage(lpparam.classLoader, wxClasses, "com.tencent.mm", 1)
                .filterByMethod(String.class, "getInfo")
                .filterByMethod(int.class, "getType")
                .filterByMethod(void.class, "reset")
                .firstOrNull();

        XMLParserClass = ReflectionUtil.findClassesFromPackage(lpparam.classLoader, wxClasses, "com.tencent.mm.sdk.platformtools", 0)
                .filterByMethod(Map.class, String.class, String.class)
                .firstOrNull();
        XMLParserMethod = ReflectionUtil.findMethodsByExactParameters(XMLParserClass, Map.class, String.class, String.class)
                .getName();


        ReflectionUtil.Classes storageClasses = ReflectionUtil.findClassesFromPackage(lpparam.classLoader, wxClasses, "com.tencent.mm.storage", 0);
        MsgInfoClass = storageClasses
                .filterByMethod(boolean.class, "isSystem")
                .firstOrNull();

        if (versionNum < getVersionNum("6.5.8")) {
            MsgInfoStorageClass = storageClasses
                    .filterByMethod(long.class, MsgInfoClass)
                    .firstOrNull();
            MsgInfoStorageInsertMethod = ReflectionUtil.findMethodsByExactParameters(MsgInfoStorageClass, long.class, MsgInfoClass)
                    .getName();
        } else {
            MsgInfoStorageClass = storageClasses
                    .filterByMethod(long.class, MsgInfoClass, boolean.class)
                    .firstOrNull();
            MsgInfoStorageInsertMethod = ReflectionUtil.findMethodsByExactParameters(MsgInfoStorageClass, long.class, MsgInfoClass, boolean.class)
                    .getName();
        }


        Class LuckyMoneyReceiveUIClass = XposedHelpers.findClass(LuckyMoneyReceiveUIClassName, lpparam.classLoader);
        ReceiveUIMethod = ReflectionUtil.findMethodsByExactParameters(LuckyMoneyReceiveUIClass,
                boolean.class, int.class, int.class, String.class, ReceiveUIParamNameClass)
                .getName();


        RequestCallerClass = ReflectionUtil.findClassesFromPackage(lpparam.classLoader, wxClasses, "com.tencent.mm", 1)
                .filterByField("foreground", "boolean")
                .filterByMethod(void.class, int.class, String.class, int.class, boolean.class)
                .filterByMethod(void.class, "cancel", int.class)
                .filterByMethod(void.class, "reset")
                .firstOrNull();

        RequestCallerMethod = ReflectionUtil.findMethodsByExactParameters(RequestCallerClass,
                void.class, RequestCallerClass, int.class)
                .getName();


        NetworkRequestClass = ReflectionUtil.findClassesFromPackage(lpparam.classLoader, wxClasses, "com.tencent.mm", 1)
                .filterByMethod(void.class, "unhold")
                .filterByMethod(RequestCallerClass)
                .firstOrNull();


        GetNetworkByModelMethod = ReflectionUtil.findMethodsByExactParameters(NetworkRequestClass,
                RequestCallerClass)
                .getName();

        ReceiveLuckyMoneyRequestClass = ReflectionUtil.findClassesFromPackage(lpparam.classLoader, wxClasses, "com.tencent.mm.plugin.luckymoney", 1)
                .filterByField("msgType", "int")

                .filterByMethod(void.class, int.class, String.class, JSONObject.class)
                .firstOrNull();

        ReceiveLuckyMoneyRequestMethod = ReflectionUtil.findMethodsByExactParameters(ReceiveLuckyMoneyRequestClass,
                void.class, int.class, String.class, JSONObject.class)
                .getName();


        LuckyMoneyRequestClass = ReflectionUtil.findClassesFromPackage(lpparam.classLoader, wxClasses, "com.tencent.mm.plugin.luckymoney", 1)
                .filterByField("talker", "java.lang.String")
                .filterByMethod(void.class, int.class, String.class, JSONObject.class)
                .filterByMethod(int.class, "getType")
                .filterByNoMethod(boolean.class)
                .firstOrNull();

        GetTransferRequestClass = ReflectionUtil.findClassesFromPackage(lpparam.classLoader, wxClasses, "com.tencent.mm.plugin.remittance", 1)
                .filterByField("java.lang.String")
                .filterByNoField("int")

                .filterByMethod(void.class, int.class, String.class, JSONObject.class)
                .filterByMethod(String.class, "getUri")
                .firstOrNull();

        saveConfig(context);
    }

    public static int getVersionNum(String version) {
        String[] v = version.split("\\.");
        if (v.length == 3)
            return Integer.valueOf(v[0]) * 100 * 100 + Integer.valueOf(v[1]) * 100 + Integer.valueOf(v[2]);
        else
            return 0;
    }

    private static boolean loadConfig(XC_LoadPackage.LoadPackageParam lpparam, String curVersionName) {
        try {
            SharedPreferences pref = getPreferencesInstance();

            String versionName = pref.getString("versionName", "");
            if (!versionName.equals(curVersionName)) {
                return false;
            }

            ClassLoader classLoader = lpparam.classLoader;
            SQLiteDatabaseClassName = pref.getString("SQLiteDatabaseClassName", "");
            SQLiteDatabaseUpdateMethod = pref.getString("SQLiteDatabaseUpdateMethod", "");
            SQLiteDatabaseInsertMethod = pref.getString("SQLiteDatabaseInsertMethod", "");
            SQLiteDatabaseDeleteMethod = pref.getString("SQLiteDatabaseDeleteMethod", "");
            ContactInfoUIClassName = pref.getString("ContactInfoUIClassName", "");
            ChatroomInfoUIClassName = pref.getString("ChatroomInfoUIClassName", "");
            WebWXLoginUIClassName = pref.getString("WebWXLoginUIClassName", "");
            AlbumPreviewUIClassName = pref.getString("AlbumPreviewUIClassName", "");
            SelectContactUIClassName = pref.getString("SelectContactUIClassName", "");
            MMActivityClassName = pref.getString("MMActivityClassName", "");
            SelectConversationUIClassName = pref.getString("SelectConversationUIClassName", "");
            LuckyMoneyReceiveUIClassName = pref.getString("LuckyMoneyReceiveUIClassName", "");

            XMLParserClass = ReflectionUtil.findClassIfExists(pref.getString("XMLParserClass", ""), classLoader);
            XMLParserMethod = pref.getString("XMLParserMethod", "");
            MsgInfoClass = ReflectionUtil.findClassIfExists(pref.getString("MsgInfoClass", ""), classLoader);
            MsgInfoStorageClass = ReflectionUtil.findClassIfExists(pref.getString("MsgInfoStorageClass", ""), classLoader);
            MsgInfoStorageInsertMethod = pref.getString("MsgInfoStorageInsertMethod", "");
            ReceiveUIParamNameClass = ReflectionUtil.findClassIfExists(pref.getString("ReceiveUIParamNameClass", ""), classLoader);
            ReceiveUIMethod = pref.getString("ReceiveUIMethod", "");
            NetworkRequestClass = ReflectionUtil.findClassIfExists(pref.getString("NetworkRequestClass", ""), classLoader);
            RequestCallerClass = ReflectionUtil.findClassIfExists(pref.getString("RequestCallerClass", ""), classLoader);
            RequestCallerMethod = pref.getString("RequestCallerMethod", "");
            GetNetworkByModelMethod = pref.getString("GetNetworkByModelMethod", "");
            ReceiveLuckyMoneyRequestClass = ReflectionUtil.findClassIfExists(pref.getString("ReceiveLuckyMoneyRequestClass", ""), classLoader);
            ReceiveLuckyMoneyRequestMethod = pref.getString("ReceiveLuckyMoneyRequestMethod", "");
            LuckyMoneyRequestClass = ReflectionUtil.findClassIfExists(pref.getString("LuckyMoneyRequestClass", ""), classLoader);
            GetTransferRequestClass = ReflectionUtil.findClassIfExists(pref.getString("GetTransferRequestClass", ""), classLoader);
            hasTimingIdentifier = pref.getBoolean("hasTimingIdentifier", true);

            log("load config successful");
            return true;
        } catch (Error | Exception e) {
        }
        return false;
    }

    private static void saveConfig(Context context) {
        try {
            Intent saveConfigIntent = new Intent();
            saveConfigIntent.setAction(SAVE_WECHAT_ENHANCEMENT_CONFIG);
            saveConfigIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

            saveConfigIntent.putExtra("versionName", versionName);
            saveConfigIntent.putExtra("SQLiteDatabaseClassName", SQLiteDatabaseClassName);
            saveConfigIntent.putExtra("SQLiteDatabaseUpdateMethod", SQLiteDatabaseUpdateMethod);
            saveConfigIntent.putExtra("SQLiteDatabaseInsertMethod", SQLiteDatabaseInsertMethod);
            saveConfigIntent.putExtra("SQLiteDatabaseDeleteMethod", SQLiteDatabaseDeleteMethod);
            saveConfigIntent.putExtra("ContactInfoUIClassName", ContactInfoUIClassName);
            saveConfigIntent.putExtra("ChatroomInfoUIClassName", ChatroomInfoUIClassName);
            saveConfigIntent.putExtra("WebWXLoginUIClassName", WebWXLoginUIClassName);
            saveConfigIntent.putExtra("AlbumPreviewUIClassName", AlbumPreviewUIClassName);
            saveConfigIntent.putExtra("SelectContactUIClassName", SelectContactUIClassName);
            saveConfigIntent.putExtra("MMActivityClassName", MMActivityClassName);
            saveConfigIntent.putExtra("SelectConversationUIClassName", SelectConversationUIClassName);
            saveConfigIntent.putExtra("LuckyMoneyReceiveUIClassName", LuckyMoneyReceiveUIClassName);

            saveConfigIntent.putExtra("XMLParserClass", XMLParserClass.getName());
            saveConfigIntent.putExtra("XMLParserMethod", XMLParserMethod);
            saveConfigIntent.putExtra("MsgInfoClass", MsgInfoClass.getName());
            saveConfigIntent.putExtra("MsgInfoStorageClass", MsgInfoStorageClass.getName());
            saveConfigIntent.putExtra("MsgInfoStorageInsertMethod", MsgInfoStorageInsertMethod);
            saveConfigIntent.putExtra("ReceiveUIParamNameClass", ReceiveUIParamNameClass.getName());
            saveConfigIntent.putExtra("ReceiveUIMethod", ReceiveUIMethod);
            saveConfigIntent.putExtra("NetworkRequestClass", NetworkRequestClass.getName());
            saveConfigIntent.putExtra("RequestCallerClass", RequestCallerClass.getName());
            saveConfigIntent.putExtra("RequestCallerMethod", RequestCallerMethod);
            saveConfigIntent.putExtra("GetNetworkByModelMethod", GetNetworkByModelMethod);
            saveConfigIntent.putExtra("ReceiveLuckyMoneyRequestClass", ReceiveLuckyMoneyRequestClass.getName());
            saveConfigIntent.putExtra("ReceiveLuckyMoneyRequestMethod", ReceiveLuckyMoneyRequestMethod);
            saveConfigIntent.putExtra("LuckyMoneyRequestClass", LuckyMoneyRequestClass.getName());
            saveConfigIntent.putExtra("GetTransferRequestClass", GetTransferRequestClass.getName());
            saveConfigIntent.putExtra("hasTimingIdentifier", hasTimingIdentifier);
            context.sendBroadcast(saveConfigIntent);
            log("saving config...");
        } catch (Error | Exception e) {
        }
    }

    private static XSharedPreferences getPreferencesInstance() {
        if (preferencesInstance == null) {
            preferencesInstance = new XSharedPreferences(Main.class.getPackage().getName(), WECHAT_ENHANCEMENT_CONFIG_NAME);
            preferencesInstance.makeWorldReadable();
        } else {
            preferencesInstance.reload();
        }
        return preferencesInstance;
    }
}

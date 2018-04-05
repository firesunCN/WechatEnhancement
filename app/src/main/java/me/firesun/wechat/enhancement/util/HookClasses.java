package me.firesun.wechat.enhancement.util;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.DexClass;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class HookClasses {
    public static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    public static String SQLiteDatabaseClassName = "com.tencent.wcdb.database.SQLiteDatabase";
    public static String SQLiteDatabaseUpdateMethod = "updateWithOnConflict";
    public static String SQLiteDatabaseInsertMethod = "insert";
    public static String SQLiteDatabaseDeleteMethod = "delete";
    public static String ContactInfoUIClassName = "com.tencent.mm.plugin.profile.ui.ContactInfoUI";
    public static String ChatroomInfoUIClassName = "com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI";
    public static String WebWXLoginUIClassName = "com.tencent.mm.plugin.webwx.ui.ExtDeviceWXLoginUI";
    public static Class XMLParserClass;
    public static String XMLParserMethod;
    public static Class MsgInfoClass;
    public static Class MsgInfoStorageClass;
    public static String MsgInfoStorageInsertMethod;
    public static Class ReceiveUIParamNameClass;
    public static String ReceiveUIMethod;
    public static String LuckyMoneyReceiveUIClassName = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    public static Class NetworkRequestClass;
    public static Class RequestCallerClass;
    public static String RequestCallerMethod;
    public static String GetNetworkByModelMethod;
    public static Class ReceiveLuckyMoneyRequestClass;
    public static String ReceiveLuckyMoneyRequestMethod;
    public static Class LuckyMoneyRequestClass;
    public static Class GetTransferRequestClass;
    public static boolean hasTimingIdentifier = true;
    private static String versionName = "";
    private static List<String> wxClasses = new ArrayList();

    public static void init(XC_LoadPackage.LoadPackageParam lpparam, String versionName) {
        HookClasses.versionName = versionName;

        int versionNum = getVersionNum(versionName);
        if (versionNum >= getVersionNum("6.5.6") && versionNum <= getVersionNum("6.5.23"))
            LuckyMoneyReceiveUIClassName = "com.tencent.mm.plugin.luckymoney.ui.En_fba4b94f";
        if (versionNum < getVersionNum("6.5.8"))
            SQLiteDatabaseClassName = "com.tencent.mmdb.database.SQLiteDatabase";
        if (versionNum < getVersionNum("6.5.4"))
            hasTimingIdentifier = false;

        if (!wxClasses.isEmpty())
            return;

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

    }

    public static int getVersionNum(String version) {
        String[] v = version.split("\\.");
        if (v.length == 3)
            return Integer.valueOf(v[0]) * 100 * 100 + Integer.valueOf(v[1]) * 100 + Integer.valueOf(v[2]);
        else
            return 0;
    }

}

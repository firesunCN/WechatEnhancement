package me.firesun.wechat.enhancement.plugin;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.firesun.wechat.enhancement.PreferencesUtils;
import me.firesun.wechat.enhancement.util.HookClasses;
import me.firesun.wechat.enhancement.util.XmlToJson;

import static android.text.TextUtils.isEmpty;
import static android.widget.Toast.LENGTH_LONG;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType;
import static de.robv.android.xposed.XposedHelpers.newInstance;


public class LuckMoney {
    private static LuckMoney instance = null;
    private static Object requestCaller;
    private static List<LuckyMoneyMessage> luckyMoneyMessages = new ArrayList<>();

    private LuckMoney() {
    }

    public static LuckMoney getInstance() {
        if (instance == null)
            instance = new LuckMoney();
        return instance;
    }

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(HookClasses.SQLiteDatabaseClassName, lpparam.classLoader, HookClasses.SQLiteDatabaseInsertMethod, String.class, String.class, ContentValues.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    ContentValues contentValues = (ContentValues) param.args[2];
                    String tableName = (String) param.args[0];
                    if (TextUtils.isEmpty(tableName) || !tableName.equals("message")) {
                        return;
                    }
                    Integer type = contentValues.getAsInteger("type");
                    if (null == type) {
                        return;
                    }
                    if (type == 436207665 || type == 469762097) {
                        handleLuckyMoney(contentValues, lpparam);
                    } else if (type == 419430449) {
                        handleTransfer(contentValues, lpparam);
                    }
                } catch (Error | Exception e) {
                    log("error:" + e);
                }
            }
        });


        XposedHelpers.findAndHookMethod(HookClasses.ReceiveLuckyMoneyRequestClass, HookClasses.ReceiveLuckyMoneyRequestMethod, int.class, String.class, JSONObject.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                try {
                    if (!HookClasses.hasTimingIdentifier) {
                        return;
                    }

                    if (luckyMoneyMessages.size() <= 0) {
                        return;
                    }

                    String timingIdentifier = ((JSONObject) (param.args[2])).getString("timingIdentifier");
                    if (isEmpty(timingIdentifier)) {
                        return;
                    }
                    LuckyMoneyMessage luckyMoneyMessage = luckyMoneyMessages.get(0);
                    Object luckyMoneyRequest = newInstance(HookClasses.LuckyMoneyRequestClass,
                            luckyMoneyMessage.getMsgType(), luckyMoneyMessage.getChannelId(), luckyMoneyMessage.getSendId(), luckyMoneyMessage.getNativeUrlString(), "", "", luckyMoneyMessage.getTalker(), "v1.0", timingIdentifier);
                    callMethod(requestCaller, HookClasses.RequestCallerMethod, luckyMoneyRequest, getDelayTime());
                    luckyMoneyMessages.remove(0);

                } catch (Error | Exception e) {
                    log("error:" + e);
                }
            }
        });

        XposedHelpers.findAndHookMethod(HookClasses.LuckyMoneyReceiveUIClassName, lpparam.classLoader, HookClasses.ReceiveUIMethod, int.class, int.class, String.class, HookClasses.ReceiveUIParamNameClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    if (PreferencesUtils.quickOpen()) {
                        Button button = (Button) findFirstFieldByExactType(param.thisObject.getClass(), Button.class).get(param.thisObject);
                        if (button.isShown() && button.isClickable()) {
                            button.performClick();
                        }
                    }
                } catch (Error | Exception e) {
                    log("error:" + e);
                }
            }
        });

        XposedHelpers.findAndHookMethod(HookClasses.ContactInfoUIClassName, lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    if (PreferencesUtils.showWechatId()) {
                        Activity activity = (Activity) param.thisObject;
                        ClipboardManager cmb = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                        String wechatId = activity.getIntent().getStringExtra("Contact_User");
                        cmb.setText(wechatId);
                        Toast.makeText(activity, "微信ID:" + wechatId + "已复制到剪切板", LENGTH_LONG).show();
                    }
                } catch (Error | Exception e) {
                    log("error:" + e);
                }
            }
        });

        XposedHelpers.findAndHookMethod(HookClasses.ChatroomInfoUIClassName, lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    if (PreferencesUtils.showWechatId()) {
                        Activity activity = (Activity) param.thisObject;
                        String wechatId = activity.getIntent().getStringExtra("RoomInfo_Id");
                        ClipboardManager cmb = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                        cmb.setText(wechatId);
                        Toast.makeText(activity, "微信ID:" + wechatId + "已复制到剪切板", LENGTH_LONG).show();
                    }
                } catch (Error | Exception e) {
                    log("error:" + e);
                }
            }
        });

    }

    private void handleLuckyMoney(ContentValues contentValues, XC_LoadPackage.LoadPackageParam lpparam) throws XmlPullParserException, IOException, JSONException {
        if (!PreferencesUtils.open()) {
            return;
        }

        int status = contentValues.getAsInteger("status");
        if (status == 4) {
            return;
        }

        String talker = contentValues.getAsString("talker");

        String blackList = PreferencesUtils.blackList();
        if (!isEmpty(blackList)) {
            for (String wechatId : blackList.split(",")) {
                if (talker.equals(wechatId.trim())) {
                    return;
                }
            }
        }

        int isSend = contentValues.getAsInteger("isSend");
        if (PreferencesUtils.notSelf() && isSend != 0) {
            return;
        }

        if (PreferencesUtils.notWhisper() && !isGroupTalk(talker)) {
            return;
        }

        if (!isGroupTalk(talker) && isSend != 0) {
            return;
        }

        String content = contentValues.getAsString("content");
        if (!content.startsWith("<msg")) {
            content = content.substring(content.indexOf("<msg"));
        }

        JSONObject wcpayinfo = new XmlToJson.Builder(content).build()
                .getJSONObject("msg").getJSONObject("appmsg").getJSONObject("wcpayinfo");
        String senderTitle = wcpayinfo.getString("sendertitle");
        String notContainsWords = PreferencesUtils.notContains();
        if (!isEmpty(notContainsWords)) {
            for (String word : notContainsWords.split(",")) {
                if (senderTitle.contains(word)) {
                    return;
                }
            }
        }

        String nativeUrlString = wcpayinfo.getString("nativeurl");
        Uri nativeUrl = Uri.parse(nativeUrlString);
        int msgType = Integer.parseInt(nativeUrl.getQueryParameter("msgtype"));
        int channelId = Integer.parseInt(nativeUrl.getQueryParameter("channelid"));
        String sendId = nativeUrl.getQueryParameter("sendid");
        requestCaller = callStaticMethod(HookClasses.NetworkRequestClass, HookClasses.GetNetworkByModelMethod);

        if (HookClasses.hasTimingIdentifier) {
            callMethod(requestCaller, HookClasses.RequestCallerMethod, newInstance(HookClasses.ReceiveLuckyMoneyRequestClass, channelId, sendId, nativeUrlString, 0, "v1.0"), 0);
            luckyMoneyMessages.add(new LuckyMoneyMessage(msgType, channelId, sendId, nativeUrlString, talker));
            return;
        }
        Object luckyMoneyRequest = newInstance(HookClasses.LuckyMoneyRequestClass,
                msgType, channelId, sendId, nativeUrlString, "", "", talker, "v1.0");

        callMethod(requestCaller, HookClasses.RequestCallerMethod, luckyMoneyRequest, getDelayTime());
    }

    private void handleTransfer(ContentValues contentValues, XC_LoadPackage.LoadPackageParam lpparam) throws IOException, XmlPullParserException, PackageManager.NameNotFoundException, InterruptedException, JSONException {
        if (!PreferencesUtils.receiveTransfer()) {
            return;
        }
        JSONObject wcpayinfo = new XmlToJson.Builder(contentValues.getAsString("content")).build()
                .getJSONObject("msg").getJSONObject("appmsg").getJSONObject("wcpayinfo");

        int paysubtype = wcpayinfo.getInt("paysubtype");
        if (paysubtype != 1) {
            return;
        }

        String transactionId = wcpayinfo.getString("transcationid");
        String transferId = wcpayinfo.getString("transferid");
        int invalidtime = wcpayinfo.getInt("invalidtime");

        if (null == requestCaller) {
            requestCaller = callStaticMethod(HookClasses.NetworkRequestClass, HookClasses.GetNetworkByModelMethod);
        }

        String talker = contentValues.getAsString("talker");
        callMethod(requestCaller, HookClasses.RequestCallerMethod, newInstance(HookClasses.GetTransferRequestClass, transactionId, transferId, 0, "confirm", talker, invalidtime), 0);
    }


    private int getDelayTime() {
        int delayTime = 0;
        if (PreferencesUtils.delay()) {
            delayTime = getRandom(PreferencesUtils.delayMin(), PreferencesUtils.delayMax());
        }
        return delayTime;
    }

    private boolean isGroupTalk(String talker) {
        return talker.endsWith("@chatroom");
    }


    private int getRandom(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }


}

class LuckyMoneyMessage {

    private int msgType;

    private int channelId;

    private String sendId;

    private String nativeUrlString;

    private String talker;

    public LuckyMoneyMessage(int msgType, int channelId, String sendId, String nativeUrlString, String talker) {
        this.msgType = msgType;
        this.channelId = channelId;
        this.sendId = sendId;
        this.nativeUrlString = nativeUrlString;
        this.talker = talker;
    }

    public int getMsgType() {
        return msgType;
    }

    public int getChannelId() {
        return channelId;
    }

    public String getSendId() {
        return sendId;
    }

    public String getNativeUrlString() {
        return nativeUrlString;
    }

    public String getTalker() {
        return talker;
    }

}
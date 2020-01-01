package me.firesun.wechat.enhancement.plugin;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.Toast;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.firesun.wechat.enhancement.PreferencesUtils;
import me.firesun.wechat.enhancement.util.HookParams;

import static android.widget.Toast.LENGTH_LONG;
import static me.firesun.wechat.enhancement.util.ReflectionUtil.log;

public class SecretFriend implements IPlugin {

    //暂时只支持隐藏单个id，多id需要建立对照表
    private int secretItemIndex = -1;
    private Class conversationWithCacheAdapterClass;

    @Override
    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("SecretFriend hook " + HookParams.getInstance().ConversationWithCacheAdapterClassName);
            conversationWithCacheAdapterClass = XposedHelpers.findClass(HookParams.getInstance().ConversationWithCacheAdapterClassName, lpparam.classLoader);
        } catch (Error | Exception e) {
        }

        XposedHelpers.findAndHookMethod(HookParams.getInstance().ChattingUIClassName, lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    if (PreferencesUtils.isEnableSecretFriend()) {
                        Activity activity = (Activity) param.thisObject;
                        ClipboardManager cmb = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                        String wechatId = activity.getIntent().getStringExtra("Chat_User");
                        log(wechatId);
                        cmb.setText(wechatId);
                        Toast.makeText(activity, "微信ID:" + wechatId + "已复制到剪切板", LENGTH_LONG).show();
                    }
                } catch (Error | Exception e) {
                }
            }
        });

//        XposedBridge.hookAllConstructors(HookParams.getInstance().ConversationWithCacheAdapterClass, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                if (PreferencesUtils.isEnableSecretFriend()) {
//                    //BaseAdapter adapter = (BaseAdapter) param.thisObject;
//                    secretItemIndex = -1;
//                }
//            }
//        });

        XposedHelpers.findAndHookMethod(conversationWithCacheAdapterClass.getSuperclass(),
                HookParams.getInstance().MMBaseAdapter_getItemInternal, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (PreferencesUtils.isEnableSecretFriend() && secretItemIndex > -1) {
                    BaseAdapter adapter = (BaseAdapter) param.thisObject;
                    if (adapter.getClass().equals(conversationWithCacheAdapterClass)) {
                        int index = (int) param.args[0];
                        if (index >= secretItemIndex) {
                            param.args[0] = index + 1;
                        }
                    }
                }
            }
        });

        XposedHelpers.findAndHookMethod(conversationWithCacheAdapterClass.getSuperclass(),"getCount", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (PreferencesUtils.isEnableSecretFriend()) {
                    log("getCount: " + param.thisObject.getClass().getName());
                    if (param.thisObject.getClass().equals(conversationWithCacheAdapterClass)) {
                        if (secretItemIndex > -1) {
                            int size = (int) param.getResult();
                            log("count: " + size);
                            if (size > 0) {
                                param.setResult(size - 1);
                            }
                        }
                    }
                }
            }
        });

        // Hook notifyDataSetChanged() of base adapters
        XposedHelpers.findAndHookMethod(BaseAdapter.class, "notifyDataSetChanged", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (PreferencesUtils.isEnableSecretFriend()) {
                    BaseAdapter adapter = (BaseAdapter) param.thisObject;
                    if (adapter.getClass().equals(conversationWithCacheAdapterClass)) {
                        secretItemIndex = -1;
                        for (int i = 0; i < adapter.getCount(); i++) {
                            String username = (String) XposedHelpers.getObjectField(adapter.getItem(i), "field_username");
                            if (username.equals(PreferencesUtils.secretFriendIds())) {
                                secretItemIndex = i;
                                break;
                            }
                        }
                    }
                }
            }
        });
    }
}

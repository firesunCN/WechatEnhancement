package me.firesun.wechat.enhancement;

import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import me.firesun.wechat.enhancement.plugin.ADBlock;
import me.firesun.wechat.enhancement.plugin.AntiRevoke;
import me.firesun.wechat.enhancement.plugin.AntiSnsDelete;
import me.firesun.wechat.enhancement.plugin.HideModule;
import me.firesun.wechat.enhancement.plugin.LuckMoney;
import me.firesun.wechat.enhancement.util.HookClasses;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;


public class Main implements IXposedHookLoadPackage {
    private static String wechatVersion = "";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(HookClasses.WECHAT_PACKAGE_NAME)) {
            initParams(lpparam);
            loadPlugins(lpparam);
        }

    }

    private void initParams(LoadPackageParam lpparam) throws Throwable {
        if (wechatVersion.equals("")) {
            Context context = (Context) callMethod(callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread", new Object[0]), "getSystemContext", new Object[0]);
            String versionName = context.getPackageManager().getPackageInfo(lpparam.packageName, 0).versionName;
            log("Found wechat version:" + versionName);
            wechatVersion = versionName;
            HookClasses.init(lpparam, versionName);
        }
    }

    private void loadPlugins(LoadPackageParam lpparam) {
        try {
            ADBlock.getInstance().hook(lpparam);
            AntiRevoke.getInstance().hook(lpparam);
            AntiSnsDelete.getInstance().hook(lpparam);
            LuckMoney.getInstance().hook(lpparam);
            HideModule.getInstance().hook(lpparam);
        } catch (Error | Exception e) {
            log("loadPlugins error" + e);
        }
    }

}

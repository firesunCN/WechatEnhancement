package me.firesun.wechat.enhancement.plugin;


import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.firesun.wechat.enhancement.PreferencesUtils;
import me.firesun.wechat.enhancement.util.HookClasses;


public class ADBlock {
    private static ADBlock instance = null;

    private ADBlock() {
    }

    public static ADBlock getInstance() {
        if (instance == null)
            instance = new ADBlock();
        return instance;
    }

    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(HookClasses.XMLParserClass, HookClasses.XMLParserMethod, String.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                try {
                    if (!PreferencesUtils.isADBlock())
                        return;

                    if (param.args[1].equals("ADInfo"))
                        param.setResult(null);
                } catch (Error | Exception e) {
                }

            }
        });
    }

}

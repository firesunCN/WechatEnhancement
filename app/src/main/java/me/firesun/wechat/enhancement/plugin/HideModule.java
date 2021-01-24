package me.firesun.wechat.enhancement.plugin;

import android.app.ActivityManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.firesun.wechat.enhancement.util.HookParams;


public class HideModule implements IPlugin {
    @Override
    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getInstalledApplications", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    List<ApplicationInfo> applicationList = (List) param.getResult();
                    List<ApplicationInfo> resultApplicationList = new ArrayList<>();
                    for (ApplicationInfo applicationInfo : applicationList) {
                        String packageName = applicationInfo.packageName;
                        if (!(packageName.contains("me.firesun") || packageName.contains("me.weishu") || packageName.contains("xposed"))) {
                            resultApplicationList.add(applicationInfo);
                        }
                    }
                    param.setResult(resultApplicationList);
                } catch (Error | Exception e) {
                }

            }
        });

        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getInstalledPackages", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    List<PackageInfo> packageInfoList = (List) param.getResult();
                    List<PackageInfo> resultpackageInfoList = new ArrayList<>();

                    for (PackageInfo packageInfo : packageInfoList) {
                        String packageName = packageInfo.packageName;
                        if (!(packageName.contains("firesun") || packageName.contains("xposed"))) {
                            resultpackageInfoList.add(packageInfo);
                        }
                    }
                    param.setResult(resultpackageInfoList);
                } catch (Error | Exception e) {
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getPackageInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                try {
                    String packageName = (String) param.args[0];
                    if (packageName.contains("firesun") || packageName.contains("xposed")) {
                        param.args[0] = HookParams.WECHAT_PACKAGE_NAME;
                    }
                } catch (Error | Exception e) {
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getApplicationInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                try {
                    String packageName = (String) param.args[0];
                    if (packageName.contains("firesun") || packageName.contains("xposed")) {
                        param.args[0] = HookParams.WECHAT_PACKAGE_NAME;
                    }
                } catch (Error | Exception e) {
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.app.ActivityManager", lpparam.classLoader, "getRunningServices", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    List<ActivityManager.RunningServiceInfo> serviceInfoList = (List) param.getResult();
                    List<ActivityManager.RunningServiceInfo> resultList = new ArrayList<>();

                    for (ActivityManager.RunningServiceInfo runningServiceInfo : serviceInfoList) {
                        String serviceName = runningServiceInfo.process;
                        if (!(serviceName.contains("firesun") || serviceName.contains("xposed"))) {
                            resultList.add(runningServiceInfo);
                        }
                    }
                    param.setResult(resultList);
                } catch (Error | Exception e) {
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.app.ActivityManager", lpparam.classLoader, "getRunningTasks", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    List<ActivityManager.RunningTaskInfo> serviceInfoList = (List) param.getResult();
                    List<ActivityManager.RunningTaskInfo> resultList = new ArrayList<>();

                    for (ActivityManager.RunningTaskInfo runningTaskInfo : serviceInfoList) {
                        String taskName = runningTaskInfo.baseActivity.flattenToString();
                        if (!(taskName.contains("firesun") || taskName.contains("xposed"))) {
                            resultList.add(runningTaskInfo);
                        }
                    }
                    param.setResult(resultList);
                } catch (Error | Exception e) {
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.app.ActivityManager", lpparam.classLoader, "getRunningAppProcesses", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = (List) param.getResult();
                    List<ActivityManager.RunningAppProcessInfo> resultList = new ArrayList<>();

                    for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfos) {
                        String processName = runningAppProcessInfo.processName;
                        if (!(processName.contains("firesun") || processName.contains("xposed"))) {
                            resultList.add(runningAppProcessInfo);
                        }
                    }
                    param.setResult(resultList);
                } catch (Error | Exception e) {
                }
            }
        });
    }

}

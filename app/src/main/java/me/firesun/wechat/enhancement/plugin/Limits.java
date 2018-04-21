package me.firesun.wechat.enhancement.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.firesun.wechat.enhancement.PreferencesUtils;
import me.firesun.wechat.enhancement.util.HookParams;

import static de.robv.android.xposed.XposedBridge.log;


public class Limits implements IPlugin {
    @Override
    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {

        XposedHelpers.findAndHookMethod(android.app.Activity.class, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                try {
                    if (!PreferencesUtils.isBreakLimit())
                        return;
                    Activity activity = (Activity) param.thisObject;
                    String className = activity.getClass().getName();
                    if (className.equals(HookParams.getInstance().AlbumPreviewUIClassName)) {
                        Intent intent = activity.getIntent();
                        if (intent == null) {
                            return;
                        }
                        int oldLimit = intent.getIntExtra("max_select_count", 9);
                        int newLimit = 1000;
                        if (oldLimit <= 9) {
                            intent.putExtra("max_select_count", oldLimit + newLimit - 9);
                        }
                    }

                    if (className.equals(HookParams.getInstance().SelectContactUIClassName)) {
                        Intent intent = activity.getIntent();
                        if (intent == null) {
                            return;
                        }

                        if (intent.getIntExtra("max_limit_num", -1) == 9) {
                            intent.putExtra("max_limit_num", Integer.MAX_VALUE);
                        }
                    }

                } catch (Error | Exception e) {
                    log("error:" + e);
                }
            }
        });

        XposedHelpers.findAndHookMethod(HookParams.getInstance().MMActivityClassName, lpparam.classLoader, "onCreateOptionsMenu", Menu.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {

                try {
                    if (!PreferencesUtils.isBreakLimit())
                        return;


                    final Activity activity = (Activity) param.thisObject;
                    Menu menu = (Menu) param.args[0];
                    if (!activity.getClass().getName().equals(HookParams.getInstance().SelectContactUIClassName)) {
                        return;
                    }

                    Intent intent = activity.getIntent();
                    if (intent == null)
                        return;
                    boolean checked = intent.getBooleanExtra("select_all_checked", false);

                    MenuItem selectAll = menu.add(0, 2, 0, "");
                    selectAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    TextView btnText = new TextView(activity);
                    btnText.setTextColor(Color.WHITE);
                    btnText.setText("全选");

                    LayoutParams layoutParams = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    Context context = btnText.getContext();
                    float scale = context.getResources().getDisplayMetrics().density;
                    layoutParams.setMarginEnd((int) ((float) 4 * scale + 0.5F));
                    btnText.setLayoutParams(layoutParams);

                    CheckBox btnCheckbox = new CheckBox(activity);
                    btnCheckbox.setChecked(checked);
                    btnCheckbox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            onSelectContactUISelectAll(activity, isChecked);
                        }

                    });

                    layoutParams = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

                    context = btnCheckbox.getContext();
                    scale = context.getResources().getDisplayMetrics().density;
                    layoutParams.setMarginEnd((int) ((float) 6 * scale + 0.5F));
                    btnCheckbox.setLayoutParams(layoutParams);


                    LinearLayout actionView = new LinearLayout(activity);
                    actionView.addView(btnText);
                    actionView.addView(btnCheckbox);
                    actionView.setOrientation(LinearLayout.HORIZONTAL);
                    selectAll.setActionView(actionView);
                } catch (Error | Exception e) {
                }
            }
        });

        XposedHelpers.findAndHookMethod(HookParams.getInstance().SelectContactUIClassName, lpparam.classLoader, "onActivityResult", int.class, int.class, Intent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                try {
                    if (!PreferencesUtils.isBreakLimit())
                        return;
                    int requestCode = (int) param.args[0];
                    int resultCode = (int) param.args[1];
                    Intent data = (Intent) param.args[2];

                    if (requestCode == 5) {
                        Activity activity = (Activity) param.thisObject;
                        activity.setResult(resultCode, data);
                        activity.finish();
                        param.setResult(null);
                    }
                } catch (Error | Exception e) {
                }
            }
        });

        XposedHelpers.findAndHookMethod(HookParams.getInstance().SelectConversationUIClassName, lpparam.classLoader, HookParams.getInstance().SelectConversationUICheckLimitMethod, boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                try {
                    if (!PreferencesUtils.isBreakLimit())
                        return;
                    param.setResult(false);
                } catch (Error | Exception e) {

                }


            }
        });
    }

    private final void onSelectContactUISelectAll(Activity activity, boolean isChecked) {
        try {
            Intent intent = activity.getIntent();
            if (intent == null) {
                return;
            }
            intent.putExtra("select_all_checked", isChecked);
            intent.putExtra("already_select_contact", "");
            if (isChecked) {
                ListView listView = (ListView) XposedHelpers.findFirstFieldByExactType(activity.getClass(), ListView.class).get(activity);
                if (listView == null) {
                    return;
                }

                ListAdapter adapter = listView.getAdapter();
                if (adapter == null) {
                    return;
                }

                adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();

                Field contactField = null;
                Field usernameField = null;
                List<String> userList = new ArrayList<String>();

                for (int i = 0; i < adapter.getCount(); ++i) {
                    Object item = adapter.getItem(i);
                    if (contactField == null) {
                        Field[] fileds = item.getClass().getFields();

                        for (int j = 0; j < fileds.length; j++) {
                            Field field = fileds[j];
                            field.getType().getName();
                            if (field.getType().getName().equals(HookParams.getInstance().ContactInfoClassName)) {
                                contactField = field;
                                break;
                            }
                        }

                        if (contactField == null) {
                            continue;
                        }
                    }

                    Object contact = contactField.get(item);
                    if (contact != null) {
                        if (usernameField == null) {
                            Field[] fields = contact.getClass().getFields();

                            for (int j = 0; j < fields.length; j++) {
                                Field field = fields[j];
                                if (field.getName().equals("field_username")) {
                                    usernameField = field;
                                }
                            }

                            if (usernameField == null) {
                                continue;
                            }
                        }

                        Object username = usernameField.get(contact);
                        if (username != null) {
                            userList.add((String) username);
                        }
                    }
                }
                intent.putExtra("already_select_contact", stringJoin(",", userList));
            }
            activity.startActivityForResult(intent, 5);
        } catch (Error | Exception e) {
        }

    }

    private static String stringJoin(String join, List<String> strAry) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strAry.size(); i++) {
            if (i == (strAry.size() - 1)) {
                sb.append(strAry.get(i));
            } else {
                sb.append(strAry.get(i)).append(join);
            }
        }

        return new String(sb);
    }
}

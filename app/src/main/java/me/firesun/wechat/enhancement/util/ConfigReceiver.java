package me.firesun.wechat.enhancement.util;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


public class ConfigReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            boolean hasExtras = extras != null;
            if (HookParams.SAVE_WECHAT_ENHANCEMENT_CONFIG.equals(action)) {
                if (hasExtras) {
                    SharedPreferences.Editor editor = context.getSharedPreferences(HookParams.WECHAT_ENHANCEMENT_CONFIG_NAME, Context.MODE_WORLD_READABLE).edit();
                    editor.clear();
                    editor.putString("params", extras.getString("params"));
                    editor.commit();
                }
            }
        } catch (Error | Exception e) {
        }
    }

}

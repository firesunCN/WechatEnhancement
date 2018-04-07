package me.firesun.wechat.enhancement.util;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            boolean hasExtras = extras != null;
            if (HookClasses.SAVE_WECHAT_ENHANCEMENT_CONFIG.equals(action)) {
                if (hasExtras) {

                    SharedPreferences.Editor editor = context.getSharedPreferences(HookClasses.WECHAT_ENHANCEMENT_CONFIG_NAME, Context.MODE_WORLD_READABLE).edit();
                    editor.clear();
                    editor.putString("versionName", extras.getString("versionName"));
                    editor.putString("SQLiteDatabaseClassName", extras.getString("SQLiteDatabaseClassName"));
                    editor.putString("SQLiteDatabaseUpdateMethod", extras.getString("SQLiteDatabaseUpdateMethod"));
                    editor.putString("SQLiteDatabaseInsertMethod", extras.getString("SQLiteDatabaseInsertMethod"));
                    editor.putString("SQLiteDatabaseDeleteMethod", extras.getString("SQLiteDatabaseDeleteMethod"));
                    editor.putString("ContactInfoUIClassName", extras.getString("ContactInfoUIClassName"));
                    editor.putString("ChatroomInfoUIClassName", extras.getString("ChatroomInfoUIClassName"));
                    editor.putString("WebWXLoginUIClassName", extras.getString("WebWXLoginUIClassName"));
                    editor.putString("AlbumPreviewUIClassName", extras.getString("AlbumPreviewUIClassName"));
                    editor.putString("SelectContactUIClassName", extras.getString("SelectContactUIClassName"));
                    editor.putString("MMActivityClassName", extras.getString("MMActivityClassName"));
                    editor.putString("SelectConversationUIClassName", extras.getString("SelectConversationUIClassName"));
                    editor.putString("LuckyMoneyReceiveUIClassName", extras.getString("LuckyMoneyReceiveUIClassName"));

                    editor.putString("XMLParserClass", extras.getString("XMLParserClass"));
                    editor.putString("XMLParserMethod", extras.getString("XMLParserMethod"));
                    editor.putString("MsgInfoClass", extras.getString("MsgInfoClass"));
                    editor.putString("MsgInfoStorageClass", extras.getString("MsgInfoStorageClass"));
                    editor.putString("MsgInfoStorageInsertMethod", extras.getString("MsgInfoStorageInsertMethod"));
                    editor.putString("ReceiveUIParamNameClass", extras.getString("ReceiveUIParamNameClass"));
                    editor.putString("ReceiveUIMethod", extras.getString("ReceiveUIMethod"));
                    editor.putString("NetworkRequestClass", extras.getString("NetworkRequestClass"));
                    editor.putString("RequestCallerClass", extras.getString("RequestCallerClass"));
                    editor.putString("RequestCallerMethod", extras.getString("RequestCallerMethod"));
                    editor.putString("GetNetworkByModelMethod", extras.getString("GetNetworkByModelMethod"));
                    editor.putString("ReceiveLuckyMoneyRequestClass", extras.getString("ReceiveLuckyMoneyRequestClass"));
                    editor.putString("ReceiveLuckyMoneyRequestMethod", extras.getString("ReceiveLuckyMoneyRequestMethod"));
                    editor.putString("LuckyMoneyRequestClass", extras.getString("LuckyMoneyRequestClass"));
                    editor.putString("GetTransferRequestClass", extras.getString("GetTransferRequestClass"));
                    editor.putBoolean("hasTimingIdentifier", extras.getBoolean("hasTimingIdentifier"));

                    editor.commit();
                }
            }
        } catch (Error | Exception e) {
        }
    }

}

package me.firesun.wechat.enhancement;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class RangePreference extends DialogPreference {

    private TextView startEditText, endEditText;

    private String startKey, endKey;

    public RangePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_range);


        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String attr = attrs.getAttributeName(i);
            if (attr.equalsIgnoreCase("start")) {
                startKey = attrs.getAttributeValue(i);
            } else if (attr.equalsIgnoreCase("end")) {
                endKey = attrs.getAttributeValue(i);
            }
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        startEditText = (TextView) view.findViewById(R.id.min_value);
        endEditText = (TextView) view.findViewById(R.id.max_value);

        SharedPreferences pref = getSharedPreferences();
        int startValue = pref.getInt(startKey, 0);
        int endValue = pref.getInt(endKey, 0);
        startEditText.setText(String.valueOf(startValue));
        endEditText.setText(String.valueOf(endValue));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            SharedPreferences.Editor editor = getEditor();
            editor.putInt(startKey, Integer.parseInt(startEditText.getText().toString()));
            editor.putInt(endKey, Integer.parseInt(endEditText.getText().toString()));
            editor.commit();
        }
        super.onDialogClosed(positiveResult);
    }
}

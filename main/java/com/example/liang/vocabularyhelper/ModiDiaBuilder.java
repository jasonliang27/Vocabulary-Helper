package com.example.liang.vocabularyhelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

class ModiDiaBuilder extends AlertDialog.Builder {
    private Context mContext;
    private final int COMPLETED = 0;
    private boolean isAutoTranslate;
    private final TranslateUtils translateUtils = new TranslateUtils();

    ModiDiaBuilder(Context context, boolean autoTranslate) {
        super(context);
        mContext = context;
        isAutoTranslate = autoTranslate;
    }

    void setAutoTranslate(boolean b) {
        isAutoTranslate = b;
    }

    public interface UpdateUIInterface {
        void updateUI(String word, String meaning);
    }

    void built(String oriWord, String oriMeaning, final UpdateUIInterface updateUIInterface) {
        final View vModiDia = View.inflate(mContext, R.layout.modify_dialog, null);
        @SuppressLint("HandlerLeak") final Handler translateHandlerModiDia = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == COMPLETED) {
                    ((EditText) vModiDia.findViewById(R.id.etModiDiaMeaning)).setText(msg.getData().getString("result"));
                    ((EditText) vModiDia.findViewById(R.id.etModiDiaMeaning)).selectAll();
                    vModiDia.findViewById(R.id.tvModiDiaStatus).setVisibility(View.INVISIBLE);
                }
            }
        };
        View.OnFocusChangeListener etModiDiaMeaningOnFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && isAutoTranslate) {
                    vModiDia.findViewById(R.id.tvModiDiaStatus).setVisibility(View.VISIBLE);
                    translateUtils.translate(((EditText) vModiDia.findViewById(R.id.etModiDiaWord)).getText().toString(), translateHandlerModiDia);
                }
            }
        };
        ((EditText) vModiDia.findViewById(R.id.etModiDiaWord)).setText(oriWord);
        ((EditText) vModiDia.findViewById(R.id.etModiDiaMeaning)).setText(oriMeaning);
        ((EditText) vModiDia.findViewById(R.id.etModiDiaWord)).selectAll();
        vModiDia.findViewById(R.id.etModiDiaWord).requestFocus();
        vModiDia.findViewById(R.id.etModiDiaMeaning).setOnFocusChangeListener(etModiDiaMeaningOnFocusChangeListener);

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("输入修改内容:")
                .setView(vModiDia)
                .setPositiveButton("修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (((EditText) vModiDia.findViewById(R.id.etModiDiaWord)).getText().toString().equals("") ||
                                ((EditText) vModiDia.findViewById(R.id.etModiDiaMeaning)).getText().toString().equals(""))
                            Toast.makeText(mContext, "单词或翻译不能为空", Toast.LENGTH_LONG).show();
                        else {
                            updateUIInterface.updateUI(((EditText) vModiDia.findViewById(R.id.etModiDiaWord)).getText().toString(),
                                    ((EditText) vModiDia.findViewById(R.id.etModiDiaMeaning)).getText().toString());
                        }
                    }
                })
                .setNegativeButton("取消", null);
        final AlertDialog alertDialog = builder.show();
        ((EditText) vModiDia.findViewById(R.id.etModiDiaMeaning)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (((EditText) vModiDia.findViewById(R.id.etModiDiaWord)).getText().toString().equals("") ||
                            ((EditText) vModiDia.findViewById(R.id.etModiDiaMeaning)).getText().toString().equals(""))
                        Toast.makeText(mContext, "单词或翻译不能为空", Toast.LENGTH_LONG).show();
                    else {
                        updateUIInterface.updateUI(((EditText) vModiDia.findViewById(R.id.etModiDiaWord)).getText().toString(),
                                ((EditText) vModiDia.findViewById(R.id.etModiDiaMeaning)).getText().toString());
                    }
                }
                alertDialog.dismiss();
                return false;
            }
        });
        new Timer().schedule(new TimerTask() {
                                 public void run() {
                                     ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(vModiDia.findViewById(R.id.etModiDiaWord), 0);
                                 }
                             },
                500);
    }

}

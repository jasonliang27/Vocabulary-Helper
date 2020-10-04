package com.example.liang.vocabularyhelper;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFrag extends PreferenceFragment {

    WordlistDB db;
    DBModifiedHandler dbModifiedHandler;

    public SettingsFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);
    }

    void newInstance(WordlistDB db, DBModifiedHandler dbModifiedHandler) {
        this.db = db;
        this.dbModifiedHandler = dbModifiedHandler;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference("prImport").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                        .setTitle("确定导入数据库？")
                        .setMessage("当前所有数据将被覆盖。" + getString(R.string.cannot_undo))
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.importDB();
                                dbModifiedHandler.onModified();
                            }
                        })
                        .show();
                return false;
            }
        });
        findPreference("prExport").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                db.exportDB();
                return false;
            }
        });
        findPreference("prResetDB").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                        .setTitle("确定重置数据库？")
                        .setMessage(getString(R.string.cannot_undo))
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.resetDB();
                                dbModifiedHandler.onModified();
                            }
                        })
                        .show();
                return false;
            }
        });
    }

    interface DBModifiedHandler {
        void onModified();
    }

}

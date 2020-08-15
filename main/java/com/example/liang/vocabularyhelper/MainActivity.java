package com.example.liang.vocabularyhelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<Map<String, Object>> lists;
    private SimpleAdapter adapter;
    private ListView listView;
    EditText etWord,etMeaning;
    boolean isAutoTranslate;
    View.OnFocusChangeListener etMeaningOnFocusChangeListener;
    Handler translateHandler;
    ModiDiaBuilder modiDiaBuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        etWord= findViewById(R.id.etWord);
        etMeaning= findViewById(R.id.etMeaning);
        setTitle("快速添加");
        //ListView
        lists = new ArrayList<>();
        adapter = new SimpleAdapter(MainActivity.this, lists, R.layout.item_template, new String[]{"words","meanings"}, new int[]{R.id.lis_word,R.id.lis_meaning});
        listView = findViewById(R.id.listview);
        listView.setAdapter(adapter);
        ((NavigationView)findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_quickadd);

        initEvent();
    }

    @SuppressLint("HandlerLeak")
    public void initEvent(){
        final TranslateUtils translateUtils=new TranslateUtils();
        isAutoTranslate = getSharedPreferences("data", MODE_PRIVATE).getBoolean("isAutoTranslate", true);
        //按钮监听
        findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addHistoryItem(view.getContext());
            }
        });
        etMeaning.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId==EditorInfo.IME_ACTION_DONE)
                    addHistoryItem(getBaseContext());
                return false;
            }
        });
        final int COMPLETED = 0;

        translateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == COMPLETED) {
                    ((EditText) findViewById(R.id.etMeaning)).setText(msg.getData().getString("result"));
                    ((EditText) findViewById(R.id.etMeaning)).selectAll();
                    findViewById(R.id.tvStatus).setVisibility(View.INVISIBLE);
                }
            }
        };
        etMeaningOnFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && isAutoTranslate) {
                    findViewById(R.id.tvStatus).setVisibility(View.VISIBLE);
                    translateUtils.translate(((EditText) findViewById(R.id.etWord)).getText().toString(), translateHandler);
                }
            }
        };
        findViewById(R.id.etMeaning).setOnFocusChangeListener(etMeaningOnFocusChangeListener);

        modiDiaBuilder = new ModiDiaBuilder(this, isAutoTranslate);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                modiDiaBuilder.built(((TextView) ((LinearLayout) view).getChildAt(1)).getText().toString(),
                        ((TextView) ((LinearLayout) view).getChildAt(2)).getText().toString(), new ModiDiaBuilder.UpdateUIInterface() {
                            @Override
                            public void updateUI(String word, String meaning) {
                                ((TextView) ((LinearLayout) view).getChildAt(1)).setText(word);
                                ((TextView) ((LinearLayout) view).getChildAt(2)).setText(meaning);
                            }
                        });
            }
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.getItem(0).setChecked(isAutoTranslate);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.mAutoTranslate:
                item.setChecked(isAutoTranslate = !item.isChecked());
                modiDiaBuilder.setAutoTranslate(isAutoTranslate);
                getSharedPreferences("data", MODE_PRIVATE).edit().putBoolean("isAutoTranslate", isAutoTranslate).apply();
                break;
            case R.id.action_settings:
                //TODO 设置
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_quickadd) {
            setTitle("快速添加");
        } else if (id == R.id.nav_wordsbook) {
            setTitle("单词本");
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void addHistoryItem(Context context){

        if (etWord.getText().toString().equals("") || etMeaning.getText().toString().equals("")) {
            Toast.makeText(context,"单词或翻译不能为空",Toast.LENGTH_LONG).show();
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("words", etWord.getText().toString());
        map.put("meanings",etMeaning.getText().toString().equals("")?etMeaning.getHint().toString():etMeaning.getText().toString());
        lists.add(0,map);
        adapter.notifyDataSetChanged();
        etMeaning.setText("");
        etWord.setText("");
        etWord.requestFocus();
    }

}


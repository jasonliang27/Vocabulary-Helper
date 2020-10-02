package com.example.liang.vocabularyhelper;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        QuickAddFrag.OnFragmentInteractionListener,
        WordsBookFrag.OnFragmentInteractionListener {

    boolean isAutoTranslate;
    QuickAddFrag quickAddFrag = new QuickAddFrag();
    WordsBookFrag wordsBookFrag;
    int currentPage;
    android.support.v4.app.Fragment currentFragment;
    WordlistDB db;
    Menu optionMenu;

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
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                View v = getWindow().peekDecorView();
                if (null != v) {
                    assert imm != null;
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setTitle("快速添加");
        ((NavigationView)findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_quickadd);
        setAutoTranslate(getSharedPreferences("data", MODE_PRIVATE).getBoolean("isAutoTranslate", true));
        currentPage = R.id.nav_quickadd;

        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.fragment_main, quickAddFrag);
        fragmentTransaction.show(currentFragment = quickAddFrag);
        fragmentTransaction.commit();

        db = new WordlistDB(this);
        //Log.d("dbdbdb",String.valueOf(db.removeAll()));
        quickAddFrag.setDataBase(db);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (currentPage == R.id.nav_wordsbook) {
            if (wordsBookFrag.onPressBack())
                super.onBackPressed();
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        initMenu(menu);
        optionMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar itemHandler clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            //主菜单
            case R.id.mAutoTranslate:
                item.setChecked(setAutoTranslate(!item.isChecked()));
                getSharedPreferences("data", MODE_PRIVATE).edit().putBoolean("isAutoTranslate", isAutoTranslate).apply();
                break;
            case R.id.mRefresh:
                item.setEnabled(false);
                wordsBookFrag.loadList(item);
                break;
            case R.id.action_settings:
                //TODO 设置
                break;

            //编辑菜单
            case R.id.mWBMSelectAll:
                item.setTitle(!wordsBookFrag.adapter.isSelectAll() ? "取消全选" : "全选");
                wordsBookFrag.adapter.setSelectAll();
                break;
            case R.id.mWBMDelete:
                wordsBookFrag.deleteSelectedItem();
                break;
            case R.id.mWBClearData:
                wordsBookFrag.clearSelectedItemData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view itemHandler clicks here.
        int id = item.getItemId();

        if (id != currentPage) {
            if (wordsBookFrag != null)
                if (wordsBookFrag.getAdapter().isEditMode())
                    wordsBookFrag.getAdapter().exitEditMode();
            optionMenu.getItem(1).setVisible(false);
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if (currentPage == R.id.nav_quickadd)
                fragmentTransaction.remove(currentFragment);
            else
                fragmentTransaction.hide(currentFragment);
            if (id == R.id.nav_quickadd) {
                setTitle("快速添加");
                fragmentTransaction.add(R.id.fragment_main, quickAddFrag);
                fragmentTransaction.show(currentFragment = quickAddFrag);
            } else if (id == R.id.nav_wordsbook) {
                setTitle("单词本");
                if (wordsBookFrag == null) {
                    initWordBookFrag();
                    fragmentTransaction.add(R.id.fragment_main, wordsBookFrag);
                }
                fragmentTransaction.show(currentFragment = wordsBookFrag);
//DEBUG                optionMenu.getItem(1).setVisible(true);
            } else if (id == R.id.nav_slideshow) {

            } else if (id == R.id.nav_manage) {

            } else if (id == R.id.nav_share) {

            } else if (id == R.id.nav_send) {

            }
            fragmentTransaction.commit();
            currentPage = id;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public boolean setAutoTranslate(boolean b) {
        quickAddFrag.setAutoTranslate(b);
        if (wordsBookFrag != null)
            wordsBookFrag.setAutoTranslate(b);
        isAutoTranslate = b;
        return b;
    }

    void initMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.getItem(0).setChecked(isAutoTranslate);
    }

    void initWordBookFrag() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final Drawable navigationIconDrawable = toolbar.getNavigationIcon();
        wordsBookFrag = new WordsBookFrag();
        wordsBookFrag.newInstance(db, isAutoTranslate, new WordsBookFrag.SetWBEditBarInterface() {
            @Override
            public void setEditBar(boolean isEditMode) {//修改单词本编辑栏状态
                if (isEditMode) {
                    optionMenu.clear();
                    getMenuInflater().inflate(R.menu.wb_edit_bar, optionMenu);
                    setTitle("");
                    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_outline);
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            wordsBookFrag.adapter.exitEditMode();
                        }
                    });
                    //findViewById(R.id.toolbar).setBackgroundColor(getColor(R.color.colorDark));//设置toolbar颜色
                } else {
                    optionMenu.clear();
                    initMenu(optionMenu);
                    setTitle("单词本");
                    toolbar.setNavigationIcon(navigationIconDrawable);
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
                        }
                    });
                }
            }

            @Override
            public void updateTitle(int n) {
                setTitle(String.format("已选中 %d 项", n));
            }
        });
        quickAddFrag.setWordsBookFrag(wordsBookFrag);
    }
}


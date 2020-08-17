package com.example.liang.vocabularyhelper;

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
                item.setChecked(setAutoTranslate(!item.isChecked()));
                getSharedPreferences("data", MODE_PRIVATE).edit().putBoolean("isAutoTranslate", isAutoTranslate).apply();
                break;//TODO isAutoTranslate
            case R.id.action_settings:
                //TODO 设置
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id != currentPage) {
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.hide(currentFragment);
            if (id == R.id.nav_quickadd) {
                setTitle("快速添加");
                fragmentTransaction.show(currentFragment = quickAddFrag);
            } else if (id == R.id.nav_wordsbook) {
                setTitle("单词本");
                if (wordsBookFrag == null) {
                    wordsBookFrag = new WordsBookFrag();
                    fragmentTransaction.add(R.id.fragment_main, wordsBookFrag);
                }
                fragmentTransaction.show(currentFragment = wordsBookFrag);
                //fragmentTransaction.replace(R.id.fragment_main,wordsBookFrag);
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
        isAutoTranslate = b;
        return b;
    }
}


package com.example.liang.vocabularyhelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class QuickAddFrag extends Fragment {
    EditText etWord, etMeaning;
    boolean isAutoTranslate;
    View.OnFocusChangeListener etMeaningOnFocusChangeListener;
    Handler translateHandler;
    ModiDiaBuilder modiDiaBuilder;
    Context mContext;
    private OnFragmentInteractionListener mListener;
    private List<Map<String, Object>> lists;
    private SimpleAdapter adapter;
    private ListView listView;
    private WordlistDB db;

    public QuickAddFrag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for mContext fragment
        View view = inflater.inflate(R.layout.fragment_quick_add, container, false);

        etWord = view.findViewById(R.id.etWord);
        etMeaning = view.findViewById(R.id.etMeaning);
        lists = new ArrayList<>();
        adapter = new SimpleAdapter(mContext, lists, R.layout.item_template, new String[]{"words", "meanings"}, new int[]{R.id.lis_word, R.id.lis_meaning});
        listView = view.findViewById(R.id.listview);
        listView.setAdapter(adapter);

        initEvent(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @SuppressLint("HandlerLeak")
    public void initEvent(final View view) {
        final TranslateUtils translateUtils = new TranslateUtils();
        //按钮监听
        view.findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addHistoryItem(view.getContext());
            }
        });
        etMeaning.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    addHistoryItem(mContext);
                return false;
            }
        });
        final int COMPLETED = 0;

        translateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == COMPLETED) {
                    ((EditText) view.findViewById(R.id.etMeaning)).setText(msg.getData().getString("result"));
                    ((EditText) view.findViewById(R.id.etMeaning)).selectAll();
                    view.findViewById(R.id.tvStatus).setVisibility(View.INVISIBLE);
                }
            }
        };
        etMeaningOnFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View tvView, boolean b) {
                if (b && isAutoTranslate) {
                    view.findViewById(R.id.tvStatus).setVisibility(View.VISIBLE);
                    translateUtils.translate(((EditText) view.findViewById(R.id.etWord)).getText().toString(), translateHandler);
                }
            }
        };
        view.findViewById(R.id.etMeaning).setOnFocusChangeListener(etMeaningOnFocusChangeListener);

        modiDiaBuilder = new ModiDiaBuilder(mContext, isAutoTranslate);
        final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
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
        };
        listView.setOnItemClickListener(onItemClickListener);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                final Map<String, Object> old = lists.get(i);
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(INPUT_METHOD_SERVICE);
                View v = getActivity().getWindow().peekDecorView();
                if (null != v) {
                    assert imm != null;
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                lists.remove(i);
                adapter.notifyDataSetChanged();
                //TODO 删除项
                Snackbar.make(listView, "已删除单词 " + ((TextView) ((LinearLayout) view).getChildAt(1)).getText().toString(), Snackbar.LENGTH_LONG)
                        .setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                lists.add(i, old);
                                adapter.notifyDataSetChanged();
                                //TODO 恢复项
                            }
                        })
                        .show();
                listView.setOnItemClickListener(null);
                new Timer().schedule(new TimerTask() {
                    public void run() {
                        listView.setOnItemClickListener(onItemClickListener);
                    }
                }, 750);
                return false;
            }
        });
    }

    public void addHistoryItem(Context context) {
        if (etWord.getText().toString().equals("") || etMeaning.getText().toString().equals("")) {
            Toast.makeText(context, "单词或翻译不能为空", Toast.LENGTH_LONG).show();
            return;
        }
        String word = etWord.getText().toString();
        String meaning = etMeaning.getText().toString().equals("") ? etMeaning.getHint().toString() : etMeaning.getText().toString();
        Map<String, Object> map = new HashMap<>();
        map.put("words", word);
        map.put("meanings", meaning);
        lists.add(0, map);
        adapter.notifyDataSetChanged();
        db.addItem(word, meaning);
        etMeaning.setText("");
        etWord.setText("");
        etWord.requestFocus();
        db.getAllItems(new WordlistDB.ItemHandlerInterface() {
            @Override
            public void itemHandler(Map<String, String> dataRow) {
                db.logDataRow(dataRow);
            }
        });
        db.getItemById(1, new WordlistDB.ItemHandlerInterface() {
            @Override
            public void itemHandler(Map<String, String> dataRow) {
                db.logDataRow(dataRow);
            }
        });
    }

    public void setAutoTranslate(boolean b) {
        isAutoTranslate = b;
        if (modiDiaBuilder != null)
            modiDiaBuilder.setAutoTranslate(isAutoTranslate);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void setDataBase(WordlistDB dataBase) {
        db = dataBase;
    }
}

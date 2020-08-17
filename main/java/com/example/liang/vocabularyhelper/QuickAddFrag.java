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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain mContext fragment must implement the
 * {@link QuickAddFrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link QuickAddFrag#newInstance} factory method to
 * create an instance of mContext fragment.
 */
public class QuickAddFrag extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    EditText etWord, etMeaning;
    boolean isAutoTranslate;
    View.OnFocusChangeListener etMeaningOnFocusChangeListener;
    Handler translateHandler;
    ModiDiaBuilder modiDiaBuilder;
    Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private List<Map<String, Object>> lists;
    private SimpleAdapter adapter;
    private ListView listView;

    public QuickAddFrag() {
        // Required empty public constructor
    }

    /**
     * Use mContext factory method to create a new instance of
     * mContext fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QuickAddFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static QuickAddFrag newInstance(String param1, String param2) {
        QuickAddFrag fragment = new QuickAddFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for mContext fragment
        View view = inflater.inflate(R.layout.fragment_quick_add, container, false);

        //mContext=getActivity().getBaseContext();
        etWord = view.findViewById(R.id.etWord);
        etMeaning = view.findViewById(R.id.etMeaning);
        //ListView
        lists = new ArrayList<>();
        adapter = new SimpleAdapter(mContext, lists, R.layout.item_template, new String[]{"words", "meanings"}, new int[]{R.id.lis_word, R.id.lis_meaning});
        listView = view.findViewById(R.id.listview);
        listView.setAdapter(adapter);

        initEvent(view);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        Map<String, Object> map = new HashMap<>();
        map.put("words", etWord.getText().toString());
        map.put("meanings", etMeaning.getText().toString().equals("") ? etMeaning.getHint().toString() : etMeaning.getText().toString());
        lists.add(0, map);
        adapter.notifyDataSetChanged();
        //TODO 添加项
        etMeaning.setText("");
        etWord.setText("");
        etWord.requestFocus();
    }

    public void setAutoTranslate(boolean b) {
        isAutoTranslate = b;
        if (modiDiaBuilder != null)
            modiDiaBuilder.setAutoTranslate(isAutoTranslate);
    }

    /**
     * mContext interface must be implemented by activities that contain mContext
     * fragment to allow an interaction in mContext fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

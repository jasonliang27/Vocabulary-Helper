package com.example.liang.vocabularyhelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WordsBookFrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WordsBookFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WordsBookFrag extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private View mView;
    WordlistDB db;
    List<Map<String, Object>> lists;
    ListView listView;
    WordBookListAdapter adapter;
    private Context mContext;
    Map<String, Object> lastRemovedItem;
    Boolean isAutoTranslate;
    int wordsCount;

    public WordsBookFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WordsBookFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static WordsBookFrag newInstance(String param1, String param2) {
        WordsBookFrag fragment = new WordsBookFrag();
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
        // Inflate the layout for this fragment
        return mView = inflater.inflate(R.layout.fragment_words_book, container, false);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
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

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView = view.findViewById(R.id.lisWords);
        loadList(null);
    }

    boolean isNull(Object o) {
        return o == null;
    }

    public void setDataBase(WordlistDB db) {
        this.db = db;
    }

    public void setAutoTranslate(Boolean autoTranslate) {
        isAutoTranslate = autoTranslate;
        if (adapter != null)
            adapter.setAutoTranslate(isAutoTranslate);
    }

    String timestampToDate(long t, long current, boolean isShowDate) {
        int daysago = (int) ((current - t) / 86400000);
        if (daysago <= 1 && !isShowDate)
            return "1天内";
        else if (isShowDate)
            return new SimpleDateFormat("yyyy/MM/dd").format(new Date(Long.parseLong(String.valueOf(t))));
        else
            return String.valueOf(daysago) + "天前";
    }

    public void addItem(int id) {
        db.getItemById(id, new WordlistDB.ItemHandlerInterface() {
            @Override
            public void itemHandler(Map<String, String> dataRow) {
                long timestamp = System.currentTimeMillis();
                Map<String, Object> map = new HashMap<>();
                map.put("words", dataRow.get(WordlistDB.ColNames.word));
                map.put("meanings", dataRow.get(WordlistDB.ColNames.meaning));
                map.put("rate", "");
                map.put("days_ago", "从未测试");
                map.put("add_date", timestampToDate(Long.valueOf(dataRow.get(WordlistDB.ColNames.add_date)), timestamp, true));
                map.put("id", String.valueOf(Integer.valueOf(dataRow.get(WordlistDB.ColNames.id))));//非测试代码，不可删除
                lists.add(map);
                adapter.notifyDataSetChanged();
            }
        });
        ((TextView) mView.findViewById(R.id.tvWordsConut)).setText("共" + String.valueOf(++wordsCount) + "个单词");
    }

    public void loadList(final MenuItem item) {
        final View view = getView();
        lists = new ArrayList<>();
        adapter = new WordBookListAdapter(mContext, lists, R.layout.wb_item_template, new String[]{"words", "meanings", "rate", "days_ago", "add_date", "id"},
                new int[]{R.id.tvWBItemWord, R.id.tvWBItemMeaning, R.id.tvWBItemRate, R.id.tvWBItemDaysAgo, R.id.tvWBItemAddDate, R.id.tvWBItemId});
        adapter.newInstance(db, isAutoTranslate);
        listView.setAdapter(adapter);
        class LoadWordsTask extends AsyncTask<String, Integer, String> {
            private int len;
            private List<Map<String, Object>> tmpList = new ArrayList<>();

            @Override
            protected String doInBackground(String... params) {
                final Integer[] i = {0};
                try {
                    Thread.currentThread().sleep(150);//等待抽屉收回
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                db.getAllItems(new WordlistDB.ItemHandlerInterface() {
                    @Override
                    public void itemHandler(Map<String, String> dataRow) {
                        long timestamp = System.currentTimeMillis();
                        Map<String, Object> map = new HashMap<>();
                        map.put("words", dataRow.get(WordlistDB.ColNames.word));
                        map.put("meanings", dataRow.get(WordlistDB.ColNames.meaning));
                        map.put("rate", isNull(dataRow.get(WordlistDB.ColNames.correct_rate)) ? "" : String.valueOf(Math.round(Float.valueOf(dataRow.get(WordlistDB.ColNames.correct_rate)) * 1000) / 1000.0) + "%");
                        map.put("days_ago", isNull(dataRow.get(WordlistDB.ColNames.test_date)) ? "从未测试" : timestampToDate(Long.valueOf(dataRow.get(WordlistDB.ColNames.test_date)), timestamp, false));
                        map.put("add_date", timestampToDate(Long.valueOf(dataRow.get(WordlistDB.ColNames.add_date)), timestamp, true));
                        map.put("id", String.valueOf(Integer.valueOf(dataRow.get(WordlistDB.ColNames.id))));//DEBUG
                        tmpList.add(map);
                        publishProgress(++i[0]);
                    }
                });
                return null;
            }

            @Override
            protected void onPreExecute() {
                view.findViewById(R.id.lisWords).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.pbWBLoading).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.tvWBStatus)).setText("正在加载...");
                view.findViewById(R.id.tvWBStatus).setVisibility(View.VISIBLE);
                len = db.getRowsCount();
            }

            @SuppressLint("DefaultLocale")
            @Override
            protected void onProgressUpdate(Integer... progresses) {
                ((TextView) view.findViewById(R.id.tvWBStatus)).setText(String.format("%d/%d (%.1f%%)", progresses[0], len, progresses[0] / (float) len * 100));
            }

            @Override
            protected void onPostExecute(String result) {
                view.findViewById(R.id.pbWBLoading).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.tvWBStatus).setVisibility(View.INVISIBLE);
                lists.addAll(tmpList);
                adapter.notifyDataSetChanged();
                ((TextView) view.findViewById(R.id.tvWordsConut)).setText("共" + String.valueOf(wordsCount = adapter.getCount()) + "个单词");
                view.findViewById(R.id.lisWords).setVisibility(View.VISIBLE);
                if (item != null)
                    item.setEnabled(true);
            }
        }
        new LoadWordsTask().execute();
    }

    public void removeItem(int indexFromLast) {
        lastRemovedItem = lists.get(lists.size() - indexFromLast - 1);
        lists.remove(lists.size() - indexFromLast - 1);
        adapter.notifyDataSetChanged();
        ((TextView) mView.findViewById(R.id.tvWordsConut)).setText("共" + String.valueOf(--wordsCount) + "个单词");
    }

    public void undoRemove(int id) {
        lastRemovedItem.put("id", id);
        lists.add(lastRemovedItem);
        adapter.notifyDataSetChanged();
        ((TextView) mView.findViewById(R.id.tvWordsConut)).setText("共" + String.valueOf(++wordsCount) + "个单词");
    }

    public void modifyItem(int indexFromLast, String word, String meaning) {
        Map<String, Object> item = lists.get(lists.size() - indexFromLast - 1);
        item.put("words", word);
        item.put("meanings", meaning);
        adapter.notifyDataSetChanged();
    }

    boolean onPressBack() {
        if (adapter.isEditMode()) {
            adapter.exitEditMode();
            return false;
        } else
            return true;
    }

}

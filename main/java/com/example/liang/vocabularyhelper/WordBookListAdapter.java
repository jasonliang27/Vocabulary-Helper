package com.example.liang.vocabularyhelper;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class WordBookListAdapter extends SimpleAdapter {

    private Context mContext;
    private SparseBooleanArray isChecked;
    private boolean isEditMode;
    private WordlistDB db;
    private boolean isAutoTranslate;
    private View.OnClickListener onClickModifyItem;
    WordsBookFrag.SetWBEditBarInterface setWBEditBarInterface;
    int selectedItemCount = 0;
    private boolean isSelectAll = false;

    WordBookListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        mContext = context;
        isChecked = new SparseBooleanArray();
        isEditMode = false;
        onClickModifyItem = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new ModiDiaBuilder(mContext, isAutoTranslate).built(((TextView) ((ConstraintLayout) view).getChildAt(0)).getText().toString(),
                        ((TextView) ((ConstraintLayout) view).getChildAt(1)).getText().toString(), new ModiDiaBuilder.UpdateUIInterface() {
                            @Override
                            public void updateUI(String word, String meaning) {
                                db.modifyData(Integer.valueOf(((TextView) ((ConstraintLayout) view).getChildAt(2)).getText().toString()), word, meaning);
                                ((TextView) ((ConstraintLayout) view).getChildAt(0)).setText(word);
                                ((TextView) ((ConstraintLayout) view).getChildAt(1)).setText(meaning);
                            }
                        });
            }
        };
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        final ViewHolder mViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.wb_item_template, viewGroup, false);
            mViewHolder = new ViewHolder();
            mViewHolder.cb = convertView.findViewById(R.id.cbWBItemCheck);
            convertView.setTag(mViewHolder);
            isChecked.put(i, false);
            convertView.setOnClickListener(onClickModifyItem);
        } else
            mViewHolder = (ViewHolder) convertView.getTag();

        if (isEditMode)//设置项目单击事件
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setCheckByIndex(i, !isChecked.get(i));
                    mViewHolder.cb.setChecked(isChecked.get(i));
                }
            });
        else
            convertView.setOnClickListener(onClickModifyItem);

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //进入编辑模式
                setEditMode(true);
                setCheckByIndex(i, true);
                return true;
            }
        });
        mViewHolder.cb.setVisibility(isEditMode ? View.VISIBLE : View.INVISIBLE);
        mViewHolder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (isEditMode) {
                    if (b)//更新标题栏数字
                        selectedItemCount++;
                    else
                        selectedItemCount--;
                    setWBEditBarInterface.updateTitle(selectedItemCount);
                }
                isChecked.put(i, b);
            }
        });
        mViewHolder.cb.setChecked(isChecked.get(i));
        return super.getView(i, convertView, viewGroup);
    }

    private void setCheckByIndex(int index, boolean b) {
        isChecked.put(index, b);
    }

    void newInstance(WordlistDB db, boolean isAutoTranslate, WordsBookFrag.SetWBEditBarInterface setWBEditBarInterface) {
        this.db = db;
        this.isAutoTranslate = isAutoTranslate;
        this.setWBEditBarInterface = setWBEditBarInterface;
    }

    void setAutoTranslate(boolean autoTranslate) {
        isAutoTranslate = autoTranslate;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    private void setEditMode(boolean editMode) {
        setActionBar(editMode);
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    void exitEditMode() {
        //退出编辑模式
        isChecked.clear();
        selectedItemCount = 0;
        isSelectAll = false;
        setEditMode(false);
    }

    class ViewHolder {
        CheckBox cb;
    }

    void setActionBar(boolean isEditMode) {
        setWBEditBarInterface.setEditBar(isEditMode);
    }

    void setSelectAll() {
        for (int i = 0; i < getCount(); i++)
            setCheckByIndex(i, !isSelectAll);
        notifyDataSetChanged();
        isSelectAll = !isSelectAll;
    }

    public boolean isSelectAll() {
        return isSelectAll;
    }

}

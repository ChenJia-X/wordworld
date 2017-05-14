package com.example.man.word_world.search;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.man.word_world.R;
import com.example.man.word_world.database.DBManager;
import com.example.man.word_world.search.model.Bean;
import com.example.man.word_world.search.util.CommonAdapter;
import com.example.man.word_world.search.util.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by man on 2016/11/27.
 */
public class Fragment_search extends Fragment implements View.OnClickListener {

    /**
     * 输入框
     */
    private EditText etInput;

    /**
     * 删除键
     */
    private ImageView ivDelete;

    /**
     * 返回按钮
     */
    private Button btnSearch;

    /**
     * 搜索结果列表view
     */
    private ListView lvResults;

    private List<Bean> dbData;

    /**
     * 搜索历史数据
     */
    private List<Bean> historyData;

    /**
     * 搜索历史数据adapter
     */
    private CommonAdapter<Bean> historyAdapter;

    /**
     * 搜索过程中自动补全数据
     */
    private List<Bean> autoCompleteData;

    /**
     * 自动补全列表adapter
     */
    private CommonAdapter<Bean> autoCompleteAdapter;

    /**
     * 默认提示框显示项的个数
     */
    private static int DEFAULT_AUTOCOMPLETE_SIZE = 10;

    /**
     * 提示框显示项的个数
     */
    private static int historySize;

    private DBManager dbManager;

    public Fragment_search(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_layout,container,false);
        initData();
        return view;
    }


    @Override
    public void onStart(){
        super.onStart();
        initViews();
    }


    /**
     * 初始化数据
     */
    private void initData() {
        dbManager =new DBManager(getActivity());
        //从数据库获取数据
        getWordsData();
        //初始化搜索历史数据
        getHistoryData();
        //初始化自动补全数据
        getAutoCompleteData(null);
        //初始化搜索结果数据
        //getResultData(null);
    }


    /**
     * 获取单词 数据
     */
    private void getWordsData() {
        Cursor cursor=dbManager.getWordsData();
        dbData=new ArrayList<>();
        while (cursor.moveToNext()){
            Bean bean=new Bean();
            bean.setWord(cursor.getString(cursor.getColumnIndex("english")));
            dbData.add(bean);
        }
        cursor.close();
    }


    /**
     * 获取搜索历史data 和adapter
     */
    private void getHistoryData() {
        Cursor cursor=dbManager.getHistoryData();
        historySize=cursor.getCount();
        if (historyData == null){
            historyData = new ArrayList<>(historySize);
        }else{
            historyData.clear();
        }
        while (cursor.moveToNext()){
            Bean bean=new Bean();
            bean.setWord(cursor.getString(cursor.getColumnIndex("spelling")));
            historyData.add(bean);
        }
        if (historyAdapter == null){
            historyAdapter= new CommonAdapter<Bean>(getActivity(), android.R.layout.simple_list_item_1, historyData) {
                @Override
                public void convert(ViewHolder holder, int position) {
                    holder.setText(android.R.id.text1 , mData.get(position).getWord());
                }
            };
        }else{
            historyAdapter.notifyDataSetChanged();
        }
        cursor.close();
    }


    /**
     * 获取自动补全data 和adapter
     */
    private void getAutoCompleteData(String text) {
        if (autoCompleteData == null) {
            //初始化
            autoCompleteData = new ArrayList<>(DEFAULT_AUTOCOMPLETE_SIZE);
        } else {
            // 根据text 获取auto data
            autoCompleteData.clear();
            for (int i = 0, count = 0; i < dbData.size()
                    && count < DEFAULT_AUTOCOMPLETE_SIZE; i++) {
                Bean bean=dbData.get(i);
                if (bean.getWord().startsWith(text.trim())) {
                    autoCompleteData.add(bean);
                    count++;
                }
            }
        }
        if (autoCompleteAdapter == null) {
            autoCompleteAdapter = new CommonAdapter<Bean>(getActivity(), android.R.layout.simple_list_item_1, autoCompleteData) {
                @Override
                public void convert(ViewHolder holder, int position) {
                    holder.setText(android.R.id.text1 , mData.get(position).getWord());
                }
            };
        } else {
            autoCompleteAdapter.notifyDataSetChanged();
        }
    }


    /**
     * 初始化视图
     */
    private void initViews() {
        //注册控件
        etInput=(EditText)getActivity().findViewById(R.id.search_et_input);
        ivDelete=(ImageView)getActivity().findViewById(R.id.search_iv_delete);
        btnSearch =(Button)getActivity().findViewById(R.id.search_btn);
        lvResults = (ListView) getActivity().findViewById(R.id.main_lv_search_results);
        lvResults.setAdapter(historyAdapter);

        //设置点击事件
        ivDelete.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

        //设置监听
        etInput.addTextChangedListener(new EditChangedListener());
        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH && !getWord().equals("")) {
                    autoCompleteSearching(getWord());
                }
                return true;
            }
        });

        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (lvResults.getAdapter() == historyAdapter){
                    historySearching(historyData.get(position).getWord().trim());
                }else if (lvResults.getAdapter() == autoCompleteAdapter){
                    autoCompleteSearching(autoCompleteData.get(position).getWord().trim());
                }
            }
        });
    }


    /**
     * 通知监听者 进行搜索操作
     * @param text
     */
    private void autoCompleteSearching(String text){
        //隐藏软键盘
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        //搜索
        Search(text);
    }


    private void historySearching(String text){
        //搜索
        Search(text);
    }


    private void Search(String text) {
        //将搜索的单词添加到搜索历史表中
        dbManager.refreshHistoryData(text);
        getHistoryData();
        DictActivity.actionStart(getActivity(),text);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_iv_delete:
                etInput.setText("");
                lvResults.setAdapter(historyAdapter);
                ivDelete.setVisibility(View.GONE);
                break;
            case R.id.search_btn:
                if (!getWord().equals("")){
                    Search(getWord());
                }
                break;
        }
    }


    private class EditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (!"".equals(charSequence.toString())) {
                ivDelete.setVisibility(View.VISIBLE);
                //更新autoComplete数据
                onRefreshAutoComplete(charSequence.toString());
            } else {
                ivDelete.setVisibility(View.GONE);
                lvResults.setAdapter(historyAdapter);
            }
        }
        private void onRefreshAutoComplete(String text) {
            //更新数据
            lvResults.setAdapter(autoCompleteAdapter);
            getAutoCompleteData(text);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

    }


    private String getWord(){
        return etInput.getText().toString().trim();
    }
}

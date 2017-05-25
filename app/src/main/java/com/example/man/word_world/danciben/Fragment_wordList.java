package com.example.man.word_world.danciben;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.man.word_world.R;
import com.example.man.word_world.database.DBManager;
import com.example.man.word_world.search.DictActivity;
import com.example.man.word_world.search.util.CommonAdapter;
import com.example.man.word_world.search.util.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by man on 2016/12/29.
 */
public class Fragment_wordList extends Fragment{
    private ListView listView;
    private List<String> wordListData;
    private DBManager dbManager;
    private CommonAdapter<String> wordListAdapter;

    public Fragment_wordList(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_list,container,false);
        getData();
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        initView();
    }


    public void getData(){
        if (dbManager==null)
            dbManager=new DBManager(getActivity());
        Cursor cursor=dbManager.getWordListData();
        if (wordListData ==null)
            wordListData =new ArrayList<>();
        //先清空wordListData,再从数据库添加可以避免只加新单词的麻烦
        if (wordListData.size()!=cursor.getCount())
            wordListData.clear();
        while (cursor.moveToNext() && wordListData.size()!=cursor.getCount()){
            wordListData.add(cursor.getString(cursor.getColumnIndex("word")));
        }
        //更新数据
        if (wordListAdapter !=null){
            wordListAdapter.notifyDataSetChanged();
        }
        cursor.close();
    }

    public void initView(){
        listView=(ListView)getActivity().findViewById(R.id.lv_wordsbook);
        wordListAdapter =new CommonAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, wordListData) {
            @Override
            public void convert(ViewHolder holder, int position) {
                holder.setText(android.R.id.text1,mData.get(position));
            }
        };
        listView.setAdapter(wordListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DictActivity.actionStart(getActivity(),wordListData.get(position));
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDialog(wordListData.get(position));
                return false;
            }
        });
    }

    private void showDialog(final String word){
        AlertDialog.Builder dialog=new AlertDialog.Builder(getActivity());
        dialog.setMessage("要将该单词从单词本中删除吗？");
        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbManager.deleteFromWordList(word);
                getData();
            }
        });
        dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}

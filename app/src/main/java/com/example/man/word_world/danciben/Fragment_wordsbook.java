package com.example.man.word_world.danciben;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.man.word_world.R;
import com.example.man.word_world.database.DBManager;
import com.example.man.word_world.search.util.CommonAdapter;
import com.example.man.word_world.search.util.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by man on 2016/12/29.
 */
public class Fragment_wordsbook extends Fragment{
    private ListView listView;
    private TextView textView;
    private List<String> wordsbookData;
    private DBManager dbManager;
    private CommonAdapter<String> wordsbookAdapter;
    public Fragment_wordsbook(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wordsbook,container,false);
        getData();
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        initView();
    }


    public void getData(){
        dbManager=new DBManager(getActivity());
        Cursor cursor=dbManager.getWordsBookData();
        wordsbookData =new ArrayList<>();
        while (cursor.moveToNext()){
            wordsbookData.add(cursor.getString(cursor.getColumnIndex("word")));
        }
        cursor.close();
    }

    public void initView(){
        /*textView=(TextView)getActivity().findViewById(R.id.text_danciben);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });*/
        listView=(ListView)getActivity().findViewById(R.id.lv_wordsbook);
        wordsbookAdapter=new CommonAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,wordsbookData) {
            @Override
            public void convert(ViewHolder holder, int position) {
                holder.setText(android.R.id.text1,mData.get(position));
            }
        };
        listView.setAdapter(wordsbookAdapter);
    }

}

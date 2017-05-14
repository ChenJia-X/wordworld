package com.example.man.word_world.search;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.man.word_world.R;
import com.example.man.word_world.database.DBManager;
import com.example.man.word_world.search.util.CommonAdapter;
import com.example.man.word_world.search.util.ViewHolder;
import com.example.man.word_world.search.wordcontainer.Dict;
import com.example.man.word_world.search.wordcontainer.Mp3Player;
import com.example.man.word_world.search.wordcontainer.WordValue;

import java.util.List;

public class DictActivity extends Activity{

    public TextView textDictWord=null;
    public TextView textDictPhonSymbolEng=null;
    public TextView textDictPhonSymbolUSA=null;
    public TextView textDictInterpret=null;
    public ListView listViewDictSentence=null;
    public ImageButton imageBtnDictAddToWordList=null;
    public ImageButton imageBtnDictHornEng=null;
    public ImageButton imageBtnDictHornUSA=null;

    public DBManager dbManager=null;

    public Dict dict=null;
    public WordValue w=null;
    public Mp3Player mp3Box=null;

    public static String searchedWord=null;

    public Handler dictHandler=null;

    public static void actionStart(Context context,String word){
        Intent intent=new Intent(context,DictActivity.class);
        intent.putExtra("word",word);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dict);
        //对searchedWord进行初始化
        Intent intent=this.getIntent();
        searchedWord=intent.getStringExtra("word");
        dict=new Dict(DictActivity.this,"dict");
        initial();
        setOnClickLis();

    }

    /**
     * 该方法可能需要访问网络，因此放在线程里进行
     */
    public void initial(){
        textDictWord=(TextView) findViewById(R.id.text_dict_word);
        textDictInterpret=(TextView)findViewById(R.id.text_dict_interpret);
        textDictPhonSymbolEng=(TextView)findViewById(R.id.text_dict_phosymbol_eng);
        textDictPhonSymbolUSA=(TextView)findViewById(R.id.text_dict_phosymbol_usa);
        listViewDictSentence=(ListView)findViewById(R.id.listview_dict_sentence);
        imageBtnDictAddToWordList=(ImageButton)findViewById(R.id.image_btn_dict_add_to_wordlist);
        imageBtnDictHornEng=(ImageButton)findViewById(R.id.image_btn_dict_horn_accent_eng);
        imageBtnDictHornUSA=(ImageButton)findViewById(R.id.image_btn_dict_horn_accent_usa);

        mp3Box=new Mp3Player(DictActivity.this, "dict");
        dbManager=new DBManager();
        dictHandler=new Handler(Looper.getMainLooper());

        new ThreadDictSearchWordAndSetInterface().start();

        //设置查找的文本
        textDictWord.setText(searchedWord);
    }


    public void setOnClickLis(){
        imageBtnDictHornEng.setOnClickListener(new IBDictPlayMusicByAccentClickLis(Mp3Player.ENGLISH_ACCENT));
        imageBtnDictHornUSA.setOnClickListener(new IBDictPlayMusicByAccentClickLis(Mp3Player.USA_ACCENT));
        imageBtnDictAddToWordList.setOnClickListener(new IBDictAddWordToGlossaryClickLis());
    }


    private class ThreadDictSearchWordAndSetInterface extends Thread{
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //调用该方法后首先初始化界面
            dictHandler.post(new RunnableDictSetTextInterface(searchedWord, "", "", "", null));

            w=null;//对wordvalue初始化
            if(!dict.isWordExist(searchedWord)){//数据库中没有单词记录，从网络上进行同步
                if((w=dict.getWordFromInternet(searchedWord))==null || w.getInterpret().equals("")){
                    return;//错词、没有释义的词语不添加进词典
                }
                dict.insertWordToDict(w,true);//默认添加到词典中
            }//能走到这一步说明从网上同步成功，数据库中一定存在单词记录

            w=dict.getWordFromDict(searchedWord);
            String searchStr=w.getWord();
            String phoSymEng=w.getPsE();
            String phoSymUSA=w.getPsA();
            String interpret=w.getInterpret();
            List<String> sentList=w.getSentList();//一定不会是null
            dictHandler.post(new RunnableDictSetTextInterface(searchStr, phoSymEng, phoSymUSA, interpret, sentList));
            if(phoSymEng.equals("")==false && phoSymUSA.equals("")==false){    //只有有音标时才去下载音乐
                mp3Box.playMusicByWord(searchedWord, Mp3Player.ENGLISH_ACCENT, true, false);
                mp3Box.playMusicByWord(searchedWord, Mp3Player.USA_ACCENT, true, false);
            }
        }
    }

    /**
     * 切换到主线程中设置UI控件的属性
     */
    private class RunnableDictSetTextInterface implements Runnable {
        String searchStr=null;
        String phoSymEng=null;
        String phoSymUSA=null;
        String interpret=null;
        List<String> sentList=null;

        public RunnableDictSetTextInterface(String searchStr, String phoSymEng, String phoSymUSA, String interpret,
                                            List<String> sentList) {
            super();
            this.searchStr = searchStr;
            this.phoSymEng = "英["+phoSymEng+"]";
            this.phoSymUSA = "美["+phoSymUSA+"]";
            this.interpret = interpret;
            this.sentList = sentList;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            textDictWord.setText(searchStr);
            textDictPhonSymbolEng.setText(phoSymEng);
            textDictPhonSymbolUSA.setText(phoSymUSA);
            textDictInterpret.setText(interpret);
            if(sentList==null){     //对链表为空进行防护
                return;
            }
            listViewDictSentence.setAdapter(new CommonAdapter<String>(DictActivity.this,
                    R.layout.dict_sentence_list_item,sentList) {
                @Override
                public void convert(ViewHolder holder, int position) {
                    holder.setText(R.id.text_dict_sentence_list_item , mData.get(position));
                }
            });
        }
    }


    class IBDictPlayMusicByAccentClickLis implements OnClickListener{

        public int accentTemp=0;

        public IBDictPlayMusicByAccentClickLis(int accentTemp) {
            super();
            this.accentTemp = accentTemp;
        }

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub

            mp3Box.playMusicByWord(searchedWord, accentTemp, false, true);

        }

    }


    class IBDictAddWordToGlossaryClickLis implements OnClickListener{

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            showAddDialog();
        }

        public void showAddDialog(){
            if(searchedWord==null)
                return;
            AlertDialog.Builder dialog=new AlertDialog.Builder(DictActivity.this);
            dialog.setMessage("把"+searchedWord+"添加到单词本?");
            dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    insertWordToGlossary();
                }
                public void insertWordToGlossary(){
                    if(w==null || w.getInterpret().equals("")){
                        Toast.makeText(DictActivity.this, "单词格式错误", Toast.LENGTH_SHORT).show();
                        return;                   //若是不是有效单词，那么将不能添加到单词本
                    }
                    boolean isSuccess=dbManager.insertWordInfoToWordList(searchedWord, w.getInterpret(), false);
                    if(isSuccess){
                        Toast.makeText(DictActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(DictActivity.this, "单词已存在", Toast.LENGTH_SHORT).show();
                    }
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


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        mp3Box.isMusicPermitted=true;
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        mp3Box.isMusicPermitted=false;
        super.onPause();
    }
}
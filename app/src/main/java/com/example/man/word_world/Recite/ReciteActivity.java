package com.example.man.word_world.Recite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.example.man.word_world.R;
import com.example.man.word_world.Recite.wordcontainer.WordBox;
import com.example.man.word_world.Recite.wordcontainer.WordInfo;
import com.example.man.word_world.search.wordcontainer.Dict;
import com.example.man.word_world.search.wordcontainer.Mp3Player;
import com.example.man.word_world.search.wordcontainer.WordValue;

import java.util.Random;
import java.util.logging.Handler;

public class ReciteActivity extends Activity {
    private static final String TAG = "ReciteActivity";
    private WordBox wordBox;
    private WordInfo wordInfo;
    private WordValue wordValue;
    private Mp3Player mp3Player;
    private Dict dict;
    private Random random;

    private String word;
    private String interpret;
    private int wrong;
    private int right;
    private int grasp;

    private ViewFlipper viewFlipper;

    private TextView textReciteQuestion;
    private TextView textReciteGrasp;
    private ImageView imageBtnPlayMusicOfWord;
    private ImageView imageBtnDeleteWordFromDB;
    private Button buttonA;
    private Button buttonB;
    private Button buttonC;
    private Button buttonD;
    private Button[] buttons=new Button[4];
    private WordInfo[] wordInfos=new WordInfo[3];

    private TextView wrongTextWord;
    private ImageButton wrongImageBtnBackCursor;
    private ImageButton wrongImageBtnReciteHornAccentEng;
    private ImageButton wrongImageBtnReciteHornAccentUsa;
    private TextView wrongTextPhosymbolEng;
    private TextView wrongTextPhosymbolUsa;
    private TextView wrongTextInterpret;
    private TextView wrongTextSentence;
    private TextView wrongTextSentenceInChinese;
    private TextView wrongTextOtherWord1;
    private TextView wrongTextOtherInterpret1;
    private TextView wrongTextOtherWord2;
    private TextView wrongTextOtherInterpret2;
    private TextView wrongTextOtherWord3;
    private TextView wrongTextOtherInterpret3;


    public static void actionStart(Context context){
        Intent intent=new Intent(context,ReciteActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recite);
        wordBox=new WordBox(this,"glossary");
        mp3Player=new Mp3Player(this,"dict");
        dict=new Dict(this,"dict");
        random=new Random();
        getData();
        initView();
        setView();
    }


    private void getData() {
        wordInfo=wordBox.popWord();
        word=wordInfo.getWord();
        interpret=wordInfo.getInterpret();
        wrong=wordInfo.getWrong();
        right=wordInfo.getRight();
        grasp=wordInfo.getGrasp();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mp3Player.playMusicByWord(word,Mp3Player.USA_ACCENT,true,false);
            }
        }).start();
        wordInfos[0]=wordBox.getWordByRandom();
        wordInfos[1]=wordBox.getWordByRandom();
        wordInfos[2]=wordBox.getWordByRandom();
    }


    private void setView() {
        textReciteQuestion.setText(word);
        textReciteGrasp.setText("掌握程度"+right+"/10     答错"+wrong+"次");
        imageBtnPlayMusicOfWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp3Player.playMusicByWord(word,Mp3Player.USA_ACCENT,false,true);
            }
        });
        imageBtnDeleteWordFromDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(ReciteActivity.this);
                dialog.setMessage("是否删除该单词？");
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wordBox.removeWordFromDatabase(word);
                        updateWord();
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
        });
        //设置选项
        int i=random.nextInt(4);
        int k=0;
        for (int j=0;j<4;j++){
            if (j==i)
                buttons[j].setText(interpret);
            else{
                buttons[j].setText(wordInfos[k].getInterpret());
                k++;
            }
        }
    }


    private void updateWord(){
        getData();
        setView();
    }


    private void initView() {
        viewFlipper=(ViewFlipper)findViewById(R.id.flipper_recite);

        //初始化R.id.sub_layout_recite_main中的控件
        textReciteQuestion=(TextView)findViewById(R.id.text_recite_question);
        textReciteGrasp=(TextView)findViewById(R.id.text_recite_grasp);
        imageBtnPlayMusicOfWord=(ImageView)findViewById(R.id.image_btn_play_music_of_word);
        imageBtnDeleteWordFromDB=(ImageView)findViewById(R.id.image_btn_delete_word_from_db);
        buttonA=(Button)findViewById(R.id.button_a);
        buttonB=(Button)findViewById(R.id.button_b);
        buttonC=(Button)findViewById(R.id.button_c);
        buttonD=(Button)findViewById(R.id.button_d);
        buttonA.setOnClickListener(new SelectChoiceListener(buttonA));
        buttonB.setOnClickListener(new SelectChoiceListener(buttonB));
        buttonC.setOnClickListener(new SelectChoiceListener(buttonC));
        buttonD.setOnClickListener(new SelectChoiceListener(buttonD));
        buttons[0]=buttonA;
        buttons[1]=buttonB;
        buttons[2]=buttonC;
        buttons[3]=buttonD;

        //初始化R.id.sub_layout_recite_wrong中的控件
        wrongTextWord=(TextView)findViewById(R.id.wrong_text_word);
        wrongImageBtnBackCursor=(ImageButton)findViewById(R.id.wrong_image_btn_back_cusor);
        wrongImageBtnReciteHornAccentEng=(ImageButton)findViewById(R.id.wrong_recite_horn_accent_eng);
        wrongImageBtnReciteHornAccentUsa=(ImageButton)findViewById(R.id.wrong_recite_horn_accent_usa);
        wrongTextPhosymbolEng=(TextView)findViewById(R.id.wrong_text_phosymbol_eng);
        wrongTextPhosymbolUsa=(TextView)findViewById(R.id.wrong_text_phosymbol_usa);
        wrongTextInterpret=(TextView)findViewById(R.id.wrong_text_interpret);
        wrongTextSentence=(TextView)findViewById(R.id.wrong_text_sentence);
        wrongTextSentenceInChinese=(TextView)findViewById(R.id.wrong_text_sentence_in_chinese);
        wrongTextOtherWord1=(TextView)findViewById(R.id.wrong_text_other_word_1);
        wrongTextOtherInterpret1=(TextView)findViewById(R.id.wrong_text_other_interpret_1);
        wrongTextOtherWord2=(TextView)findViewById(R.id.wrong_text_other_word_2);
        wrongTextOtherInterpret2=(TextView)findViewById(R.id.wrong_text_other_interpret_2);
        wrongTextOtherWord3=(TextView)findViewById(R.id.wrong_text_other_word_3); ;
        wrongTextOtherInterpret3=(TextView)findViewById(R.id.wrong_text_other_interpret_3);
        wrongImageBtnBackCursor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.showPrevious();
                updateWord();
            }
        });
    }


    private class SelectChoiceListener implements View.OnClickListener {
        private Button mbutton;

        public SelectChoiceListener(Button button) {
            mbutton=button;
        }

        @Override
        public void onClick(View v) {
            if (mbutton.getText().equals(interpret)){
                wordBox.feedBack(wordInfo,true);
                updateWord();
            }else {
                wordBox.feedBack(wordInfo,false);
                Log.d(TAG, "onClick: "+word);
                new DownloadTask().execute();
            }
        }

        class DownloadTask extends AsyncTask<Void,Integer,Boolean>{

            @Override
            protected Boolean doInBackground(Void... params) {
                if (!dict.isWordExist(word)){
                    wordValue=dict.getWordFromInternet(word);
                    dict.insertWordToDict(wordValue,false);
                }else
                    wordValue=dict.getWordFromDict(word);
                //下载音频文件
                mp3Player.playMusicByWord(word,Mp3Player.ENGLISH_ACCENT,true,false);
                mp3Player.playMusicByWord(word,Mp3Player.USA_ACCENT,true,false);
                publishProgress();
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                wrongTextWord.setText(word);

                wrongImageBtnReciteHornAccentEng.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mp3Player.playMusicByWord(word,Mp3Player.ENGLISH_ACCENT,false,true);
                    }
                });
                wrongTextPhosymbolEng.setText(wordValue.getPsE());
                wrongImageBtnReciteHornAccentUsa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mp3Player.playMusicByWord(word,Mp3Player.USA_ACCENT,false,true);
                    }
                });
                wrongTextPhosymbolUsa.setText(wordValue.getPsA());

                wrongTextInterpret.setText(wordValue.getInterpret());
                wrongTextSentence.setText(wordValue.getOrigList().get(0));
                wrongTextSentenceInChinese.setText(wordValue.getTransList().get(0));

                wrongTextOtherWord1.setText(wordInfos[0].getWord());
                wrongTextOtherInterpret1.setText(wordInfos[0].getInterpret());
                wrongTextOtherWord2.setText(wordInfos[1].getWord());
                wrongTextOtherInterpret2.setText(wordInfos[1].getInterpret());
                wrongTextOtherWord3.setText(wordInfos[2].getWord());
                wrongTextOtherInterpret3.setText(wordInfos[2].getInterpret());
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                viewFlipper.showNext();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}

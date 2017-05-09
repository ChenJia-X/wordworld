package com.example.man.word_world.Recite.text_parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;
import com.example.man.word_world.database.DBManager;


/**
 * Created by man on 2016/12/29.
 */
public class WordListParser {
    public DBManager dbManager=null;
    public Context context=null;
    public String tableName=null;

    public WordListParser(){

    }


    public WordListParser(Context context, String tableName) {
        this.context=context;
        this.tableName=tableName;
        dbManager=new DBManager();
    }


    public void parse(String lineStr){
        int countWord=0;
        int countInterpret=0;
        int count=0;
        String strInterpret="";
        String str="";
        char[] charArray=null;
        Pattern patternWord=Pattern.compile("[a-zA-Z]+[ ]+");
        //"%>[^<%%>]*<%"
        Pattern patternInterpret=Pattern.compile("%E>[^<S%%E>]+<S%");
        Matcher matcherWord=patternWord.matcher(lineStr);
        Matcher matcherInterpret=null;
        ArrayList<String> wordList=new ArrayList<>();
        ArrayList<String> interpretList=new ArrayList<>();

        //处理word
        while(matcherWord.find()){
            str=matcherWord.group();
            charArray=str.toCharArray();
            if(charArray.length>0 && (charArray[0]>='A'&& charArray[0]<='Z' )){
                charArray[0]+=('a'-'A');
                str=new String(charArray,0,charArray.length);     //首字母去掉大写
            }
            wordList.add(str.trim());
        }
        if(wordList.size()<=0)
            return;

        //替换、添加标记
        matcherWord.reset(lineStr);
        if(matcherWord.find()){
            strInterpret=matcherWord.replaceAll("<S%%E>");
        }
        strInterpret+="<S%%E>";

        //处理interpret
        matcherInterpret=patternInterpret.matcher(strInterpret);
        while(matcherInterpret.find()){
            str=matcherInterpret.group();
            interpretList.add(new String(str.toCharArray(),3,str.length()-6));
        }

        countWord=wordList.size();
        countInterpret=interpretList.size();
        count=countWord>countInterpret?countInterpret:countWord;
        dbManager.getDatabase().delete("wordsList",null,null);//删除表中所有数据
        for(int i=0;i<count;i++){
            dbManager.insertWordInfoToGlossary(wordList.get(i), interpretList.get(i), true);
        }
    }

//    public boolean isOfAnWord(int index,char[] str){
//        int i=index;
//        for( ;i<str.length;i++  ){
//            if(isAlpha(str[i])==false)
//                break;
//        }
//        if(i==index)
//            return false;
//        if(i>=str.length)
//            return true;
//        if(str[i]=='.')
//            return false;
//        return true;
//
//    }
//
//
//    public boolean isAlpha(char ch){
//        if((ch>='A'&&ch<='Z') ||(ch>='a'&&ch<='z')){
//            return true;
//        }
//        else
//            return false;
//    }
//
//
//    public boolean isChinese(char ch){
//        if(ch>129)
//            return true;
//        else
//            return false;
//    }

}

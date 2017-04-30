package com.example.man.word_world.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.man.word_world.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by man on 2016/11/18.
 */
public class DBManager {
    private SQLiteDatabase database;
    private int HISTORY_SIZE=20;

    // 定义数据库的存放路径
    private final String DATABASE_PATH = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/dictionary";
    private final String DATABASE_FILENAME = "dictionary.db";

    //历史搜索表
    private static final String CREATE_HISTORY_SEARCH ="create table if not exists HistorySearch("
            +" spelling string primary key,"
            +" time integer)";

    //从网络上下载下来存储在本地的单词的表
    //pse、psa：英、美式音标
    //prone、prona：英、美式发音地址
    //interpret：释义
    //sentorig、senttrans：例句的英文、中文
    private static final String CREATE_DICT ="create table if not exists dict("
            +"word text,"
            +"pse text,"
            +"prone text,"
            +"psa text,"
            +"prona text,"
            +"interpret text,"
            +"sentorig text,"
            +"senttrans text)";

    //单词本
    private static final String CREATE_GLOSSARY ="create table if not exists glossary("
            +"word text,"
            +"interpret text,"
            +"right text,"
            +"wrong text,"
            +"grasp int,"
            +"learned int)";

    //词汇学习表
    private static final String CREATE_WORDS_LIST ="create table if not exists wordsList("
            +"word text,"
            +"interpret text,"
            +"right int,"
            +"wrong int,"
            +"grasp int,"
            +"learned int)";

    //用户词汇表
    private static final String CREATE_RECITE_DATA ="create table if not exists reciteData("
            +"course_name text primary key,"
            +"total_words int,"
            +"start_time text,"
            +"interdays int,"
            +"end_time int)";


    public DBManager(){
    }

    public DBManager(Context context){
        database=openDB(context);
        //创建HistorySearch表
        database.execSQL(CREATE_HISTORY_SEARCH);
        database.execSQL(CREATE_DICT);
        database.execSQL(CREATE_GLOSSARY);
        database.execSQL(CREATE_WORDS_LIST);
        database.execSQL(CREATE_RECITE_DATA);
    }

    public Cursor getWordsBookData(){
        String sql="select word from glossary";
        Cursor cursor=database.rawQuery(sql,null);
        return cursor;
    }

    public Cursor getWordsData(){
        String sql="select english from t_words";
        Cursor cursor=database.rawQuery(sql,null);
        return cursor;
    }

    public Cursor getHistoryData(){
        //按照查询时间降序排列
        String sql="select spelling from HistorySearch order by time DESC";
        Cursor cursor=database.rawQuery(sql,null);
        return cursor;
    }

    public void refreshHistoryData(String text){
        Cursor cursor=database.rawQuery("select * from HistorySearch order by time DESC",null);
        int count=cursor.getCount();
        //判断是否有重复的查询单词，有则删除
        while(cursor.moveToNext()){
            int i=cursor.getColumnIndex("spelling");
            if (cursor.getString(i).equals(text)){
                database.execSQL("delete from HistorySearch where spelling=?",new String[]{text});
                //database.delete("HistorySearch","spelling=?",new String[]{text});
                count-=1;
                break;
            }
        }
        //如果超出history的大小，则删除最早的一个单词
        if (count>=HISTORY_SIZE){
            cursor.moveToLast();
            database.execSQL("delete from HistorySearch where spelling=?",new String[]{cursor.getString(cursor.getColumnIndex("spelling"))});
            //database.delete("HistorySearch","spelling=?",new String[] {cursor.getString(cursor.getColumnIndex("spelling"))});
        }
        //获取当前系统时间，time为1970.1.1以来的毫秒数
        Date date=new Date(System.currentTimeMillis());
        long time=date.getTime();
        //将单词插入表中
        database.execSQL("insert into HistorySearch(spelling,time) values(?,?)",new Object[]{text,time});
        /*ContentValues contentValues=new ContentValues();
        contentValues.put("spelling",text);
        contentValues.put("time",time);
        database.insert("HistorySearch",null,contentValues);
        contentValues.clear();*/
        cursor.close();
    }

    /**
     * 将单词添加到单词本的表里去
     * @param word
     * @param interpret
     * @param OverWrite
     * @return
     */
    public Boolean insertWordInfoToGlossary(String word, String interpret, Boolean OverWrite){
        Cursor cursor = null;
        if(database ==null){
            database=getDatabase();
        }
        cursor =database.rawQuery("select word from glossary where word=?",new String[]{word});
        if (cursor.moveToNext()){
            return true;
        }else {
            ContentValues values = new ContentValues();
            values.put("word", word);
            values.put("interpret", interpret);
            values.put("right", 0);
            values.put("wrong", 0);
            values.put("grasp", 0);
            values.put("learned", 0);
            database.insert("glossary", null, values);
            cursor.close();
            return true;
        }
    }

    /**
     * 将单词添加到Recite表中
     * @param word
     * @param interpret
     * @param overWrite
     * @return
     */
    public boolean insertWordInfoToDataBase(String word, String interpret, boolean overWrite) {
        Cursor cursor = null;
        if(database ==null){
            database=getDatabase();
        }
        cursor =database.rawQuery("select word from wordsList where word=?",new String[]{word});
        //cursor = database.query("glossary", new String[] {"word"}, "word=?", new String[] {word}, null, null, null);
        //若改单词已经存在
        if(cursor.moveToNext()) {
            if(overWrite) {//是否覆写原有数据
                ContentValues values = new ContentValues();
                values.put("interpret", interpret);
                values.put("right", 0);//答对次数
                values.put("wrong", 0);
                values.put("grasp", 0);//掌握程度
                values.put("learned", 0);//用于标志该单词是否已经背过
                database.update("wordsList", values, "word=?", new String[] {word});
                cursor.close();
                return true;
            }else{
                cursor.close();
                return false;
            }
        }else{
            ContentValues values = new ContentValues();
            values.put("word", word);
            values.put("interpret", interpret);
            values.put("right", 0);
            values.put("wrong", 0);
            values.put("grasp", 0);
            values.put("learned", 0);
            database.insert("wordsList", null, values);
            cursor.close();
            return true;
        }
    }

    public Boolean insertReciteData(String course_name,int total_words,String start_time,int interdays,String end_times){
        database.execSQL("insert into reciteDta(course_name,total_words,start_time,interdays,end_time)values(?,?,?,?,?)",
                new Object[]{course_name, total_words, start_time, interdays, end_times});
        return true;
    }

    public Cursor getReciteData(){
        String sql="select * from reciteData";
        return database.rawQuery(sql,null);
    }

    public Cursor query(String[] word ){
        String sql="select chinese from t-words where english=?";
        Cursor cursor=database.rawQuery(sql,word);
        return cursor;
    }

    public void closeDB(){
        database.close();
    }

    public SQLiteDatabase openDB(Context context ) {
        try {
            // 获得dictionary.db文件的绝对路径
            String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
            File dir = new File(DATABASE_PATH);

            // 如果/sdcard/dictionary目录不存在，创建这个目录
            if (!dir.exists())
                dir.mkdir();

            // 如果在/sdcard/dictionary目录中不存在
            // dictionary.db文件，则从res\raw目录中复制这个文件到
            // SD卡的目录（/sdcard/dictionary）
            if (!(new File(databaseFilename)).exists()) {

                // 获得封装dictionary.db文件的InputStream对象
                InputStream is = context.getResources().openRawResource(
                        R.raw.dictionary);
                FileOutputStream fos = new FileOutputStream(databaseFilename);
                byte[] buffer = new byte[8192];
                int count = 0;
                // 开始复制dictionary.db文件
                while ((count = is.read(buffer)) > 0) {

                    fos.write(buffer, 0, count);
                }
                // 关闭文件流
                fos.close();
                is.close();
            }

            // 打开/sdcard/dictionary目录中的dictionary.db文件
            return getDatabase();

        } catch (Exception e) {

        }
        // 如果打开出错，则返回null
        return null;
    }

    // 打开/sdcard/dictionary目录中的dictionary.db文件
    public SQLiteDatabase getDatabase(){
        String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(
                databaseFilename, null);
        return database;
    }

}

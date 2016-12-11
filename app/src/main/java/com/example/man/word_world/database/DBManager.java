package com.example.man.word_world.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.man.word_world.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

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

    //历史搜索表,phe、pha为英式音标和美食音标
    /*+"phe string,"
            +"phe_address sting,"
            +"pha string,"
            +"pha_address sting,"
            +"meaning string,"
            +"picture_address integer)"*/
    private static final String CREATE_HISTORY_SEARCH ="create table if not exists HistorySearch("
            +" spelling string primary key,"
            +" time integer)";


    public DBManager(Context context){
        database=openDB(context);
        //创建HistorySearch表
        database.execSQL(CREATE_HISTORY_SEARCH);
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
            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(
                    databaseFilename, null);
            return database;

        } catch (Exception e) {

        }
        // 如果打开出错，则返回null
        return null;
    }

}

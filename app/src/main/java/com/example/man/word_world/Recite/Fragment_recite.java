package com.example.man.word_world.Recite;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.man.word_world.R;
import com.example.man.word_world.Recite.text_parser.WordListParser;
import com.example.man.word_world.Recite.wordcontainer.WordBox;
import com.example.man.word_world.database.DBManager;
import com.example.man.word_world.search.util.FileUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by man on 2016/12/29.
 */
public class Fragment_recite extends Fragment {
    private static final String TAG = "Fragment_recite";
    public static final int MODE1_INSERT_FROM_SDCARD = 0;
    public static final int MODE2_INSERT_FROM_RES_GRE = 1;
    public static Boolean isSetted=false;
    public static int accent;
    public static String courseName;
    public static int totalWords;
    public static String start_time;
    public static int interdays;
    public static String end_time;
    public static int todayWordCount;
    public static int deadlineDay;
    public static int deadlineMonth;
    public static int deadlineYear;
    public static int glossaryRawResourceID;

    private DBManager dbManager;
    private Cursor cursor;
    private WordBox wordBox;

    private Button buttonSetInsertLocalCet4;
    private Button buttonSetInsertLocalCet6;
    private Button buttonSetInsertLocalGRE;
    private Button buttonStartRecite;
    private Button buttonSetDeadline;
    private EditText editTime;
    private TextView textGeneralTodayWords;
    private TextView textGeneralLearnWords;
    private TextView textGeneralGraspWords;
    private TextView textGeneralNeedLearnWords;
    private TextView textGeneralCourse;
    private RoundProgressBar reciteProgressBar;

    static
    {
        courseName = "";
        accent = 1;
        glossaryRawResourceID = 0;
        deadlineYear = 0;
        deadlineMonth = 0;
    }

    public Fragment_recite(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recite,container,false);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        //检测是否创建过学习计划
        dbManager=new DBManager(getActivity());
        getData();
        initViews();
        setViews();
        cursor.close();
    }

    private void getData() {
        cursor=dbManager.getReciteData();
        if (cursor.moveToNext() && cursor!=null){//对cursor的任何操作，一定不要忘记添加这句，否则会抛出异常闪退
            courseName=cursor.getString(cursor.getColumnIndex("course_name"));
            start_time=cursor.getString(cursor.getColumnIndex("start_time"));
            end_time=cursor.getString(cursor.getColumnIndex("end_time"));
            if (end_time!=null) isSetted=true;
            totalWords=cursor.getInt(cursor.getColumnIndex("total_words"));
            interdays=cursor.getInt(cursor.getColumnIndex("interdays"));
            todayWordCount=totalWords/interdays;
        }
        wordBox=new WordBox(getActivity(),"glossary");
    }


    private void initViews() {
        buttonSetInsertLocalCet4 = (Button)getActivity().findViewById(R.id.button_set_insert_local_res_cet4);
        buttonSetInsertLocalCet6 = (Button)getActivity().findViewById(R.id.button_set_insert_local_res_cet6);
        buttonSetInsertLocalGRE = (Button)getActivity().findViewById(R.id.button_set_insert_local_res_gre);
        buttonStartRecite = (Button)getActivity().findViewById(R.id.btn_recite_start);
        buttonSetDeadline =(Button)getActivity().findViewById(R.id.btn_set_deadline);
        editTime=(EditText)getActivity().findViewById(R.id.edit_time);
        textGeneralTodayWords=(TextView)getActivity().findViewById(R.id.text_general_today_should_grasp);
        textGeneralNeedLearnWords=(TextView)getActivity().findViewById(R.id.text_dict_general_need_learn);
        textGeneralLearnWords=(TextView)getActivity().findViewById(R.id.text_dict_general_learn);
        textGeneralGraspWords=(TextView)getActivity().findViewById(R.id.text_dict_general_grasp);
        textGeneralCourse=(TextView)getActivity().findViewById(R.id.text_general_course);
        reciteProgressBar=(RoundProgressBar)getActivity().findViewById(R.id.recite_progressbar);

        buttonSetInsertLocalCet4.setOnClickListener(new BsetInsertLocalGlossaryClickListener(this,R.raw.cet4));
        buttonSetInsertLocalCet6.setOnClickListener(new BsetInsertLocalGlossaryClickListener(this,R.raw.cet6));
        buttonSetInsertLocalGRE.setOnClickListener(new BsetInsertLocalGlossaryClickListener(this,R.raw.gre));
        buttonSetDeadline.setOnClickListener(new BsetDeadlineClickListener());
        buttonStartRecite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbManager.isReciteDataExist()){
                    if (isSetted)
                        ReciteActivity.actionStart(getActivity());
                    else showAlertDialog("请设置完成时间！");
                }
                else checkCourse();
            }
        });

        if (isSetted) editTime.setText(cursor.getString(cursor.getColumnIndex("end_time")));
        else editTime.setText("请设置完成时间！");
        editTime.setInputType(InputType.TYPE_NULL);//禁止修改
    }


    private void setViews() {
        textGeneralTodayWords.setText("今日需要学习"+todayWordCount+"个单词");
        textGeneralLearnWords.setText("已学习   "+(totalWords-wordBox.getWordCountOfUnlearned())+"个");
        textGeneralGraspWords.setText("已掌握   "+wordBox.getWordCountByGrasp(10,1)+"个");
        textGeneralNeedLearnWords.setText("未学习   "+wordBox.getWordCountOfUnlearned()+"个");
        textGeneralCourse.setText(courseName);
        reciteProgressBar.setProgress(wordBox.getTotalLearnProgress());
    }


    class BsetInsertLocalGlossaryClickListener implements View.OnClickListener {
        private int mglossaryResId;
        public BsetInsertLocalGlossaryClickListener(Fragment_recite fragment_recite, int glossaryResId) {
            mglossaryResId=glossaryResId;
        }

        @Override
        public void onClick(View v) {
            glossaryRawResourceID=mglossaryResId;
            AlertDialog.Builder dialog=new AlertDialog.Builder(getActivity());
            dialog.setMessage("覆盖词库原有记录？");
            dialog.setCancelable(true);
            dialog.setPositiveButton("是",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (glossaryRawResourceID){
                        case R.raw.cet4:
                            courseName="CET4";
                            totalWords=3662;
                            break;
                        case R.raw.cet6:
                            courseName="CET6";
                            totalWords=2083;
                            break;
                        case R.raw.gre:
                            courseName="GRE";
                            totalWords=2985;
                            break;
                    }
                    new UpdateGlossaryTask().execute();
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

    class UpdateGlossaryTask extends AsyncTask<Void,Integer,Boolean>{
        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setTitle("词库导入中");
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            updateGlossary();

            if (start_time==null){
                Calendar calendar=Calendar.getInstance();
                start_time=calendar.get(Calendar.YEAR)+"."+(calendar.get(Calendar.MONTH)+1)+"."+calendar.get(Calendar.DAY_OF_MONTH);
            }
            dbManager.updateReciteData_words(courseName,totalWords,start_time);

            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.dismiss();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }

        /**
         * 将从Raw中读取对应的txt文件并添加到数据库的wordsList表中
         */
        public void updateGlossary() {
            //从Raw中读取对应的txt文件
            /*InputStream inputStream = getResources().openRawResource(glossaryRawResourceID);
            FileUtils fileUtils=new FileUtils();
            WordListParser wordListParser=new WordListParser(getActivity(),"glossary");
            wordListParser.parse(fileUtils.getStringFromInputStream(inputStream));*/
            dbManager.insertWordfromDBToGlossary(courseName.toLowerCase());
        }
    }

    class BsetDeadlineClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!checkCourse())
                return;//防止空指针

            //http://blog.csdn.net/u010142437/article/details/9103087
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    getActivity(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            monthOfYear+=1;//monthOfYear初始值为0-11,所以需要加1
                            deadlineYear=year;
                            deadlineMonth=monthOfYear;
                            deadlineDay=dayOfMonth;
                            calculateTime();
                            //更新数据
                            getData();
                            setViews();
                        }
                    }
                    , calendar.get(Calendar.YEAR)
                    , calendar.get(Calendar.MONTH)
                    , calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        }

        //计算学习总天数
        private void calculateTime() {
            ArrayList<Integer> arrayList=new ArrayList<>();
            Pattern pattern=Pattern.compile("[0-9]+");
            Matcher matcher=pattern.matcher(start_time);
            while (matcher.find()){
                arrayList.add(Integer.parseInt(matcher.group()));
            }
            Calendar calendar=Calendar.getInstance();
            calendar.set(arrayList.get(0),arrayList.get(1),arrayList.get(2));
            long time1=calendar.getTimeInMillis();//start_time
            calendar.set(deadlineYear,deadlineMonth,deadlineDay);
            long time2=calendar.getTimeInMillis();//end_time
            interdays=(int) ((time2-time1)/(1000*60*60*24));//注意强制转换的运算优先级
            end_time=deadlineYear+"."+deadlineMonth+"."+deadlineDay;
            dbManager.updateReciteData_time(courseName,interdays,end_time);
            Toast.makeText(getActivity(),"总共学习"+interdays+"天",Toast.LENGTH_LONG).show();
            editTime.setText(deadlineYear + "." + deadlineMonth + "." + deadlineDay);
            isSetted=true;
            setViews();//同步
        }
    }

    private Boolean checkCourse(){
        if(courseName.equals("")){
            showAlertDialog("请先使用本地词库创建学习课程！");
            return false;
        }
        else return true;
    }

    private void showAlertDialog(String message){
        AlertDialog.Builder dialog=new AlertDialog.Builder(getActivity());
        dialog.setMessage(message);
        dialog.setPositiveButton("是",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}

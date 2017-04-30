package com.example.man.word_world.Recite;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.man.word_world.R;
import com.example.man.word_world.Recite.diyview.MyProgressBar;
import com.example.man.word_world.Recite.text_parser.WordListParser;
import com.example.man.word_world.database.DBManager;
import com.example.man.word_world.search.util.FileUtils;

import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by man on 2016/12/29.
 */
public class Fragment_recite extends Fragment {
    public static final int MODE1_INSERT_FROM_SDCARD = 0;
    public static final int MODE2_INSERT_FROM_RES_GRE = 1;
    public static int accent;
    public static String courseName;
    public static int totalWords = 0;
    public static int deadlineDay = 0;
    public static int deadlineMonth;
    public static int deadlineYear;
    public static int delay = 0;
    public static int glossaryRawResourceID;

    protected static final int STOP = 0x10000;
    protected static final int NEXT = 0x10001;
    private int iCount=0;
    private DBManager dbManager;

    private Button buttonSetInsertLocalCet4;
    private Button buttonSetInsertLocalCet6;
    private Button buttonSetInsertLocalGRE;
    private Button buttonStartRecite;
    private Button buttonSetDeadline;
    private EditText editTime;
    private Button buttonDialogYes;
    private Button buttonDialogNo;
    private MyProgressBar myProgressBar;

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
        //initData();
        dbManager=new DBManager(getActivity());
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        initViews();
    }

    private void initViews() {
        buttonSetInsertLocalCet4 = (Button)getActivity().findViewById(R.id.button_set_insert_local_res_cet4);
        buttonSetInsertLocalCet6 = (Button)getActivity().findViewById(R.id.button_set_insert_local_res_cet6);
        buttonSetInsertLocalGRE = (Button)getActivity().findViewById(R.id.button_set_insert_local_res_gre);
        buttonStartRecite = (Button)getActivity().findViewById(R.id.btn_recite_start);
        buttonSetDeadline =(Button)getActivity().findViewById(R.id.btn_set_deadline);
        editTime=(EditText)getActivity().findViewById(R.id.edit_time);

        buttonSetInsertLocalCet4.setOnClickListener(new BsetInsertLocalGlossaryClickListener(this,R.raw.cet4));
        buttonSetInsertLocalCet6.setOnClickListener(new BsetInsertLocalGlossaryClickListener(this,R.raw.cet6));
        buttonSetInsertLocalGRE.setOnClickListener(new BsetInsertLocalGlossaryClickListener(this,R.raw.gre));
        buttonSetDeadline.setOnClickListener(new BsetDeadlineClickListener());
    }

    private class BsetInsertLocalGlossaryClickListener implements View.OnClickListener {
        private int mglossaryResId;
        public BsetInsertLocalGlossaryClickListener(Fragment_recite fragment_recite, int glossaryResId) {
            mglossaryResId=glossaryResId;
        }

        @Override
        public void onClick(View v) {
            glossaryRawResourceID=mglossaryResId;
            switch (glossaryRawResourceID){
                case R.raw.cet4:
                    courseName="CET4";
                    totalWords=4000;//数字只是举例
                    break;
                case R.raw.cet6:
                    courseName="CET6";
                    totalWords=4000;
                    break;
                case R.raw.gre:
                    courseName="GRE";
                    totalWords=4000;
                    break;
            }
            showInsertAlertDialog(MODE2_INSERT_FROM_RES_GRE);//参数是猜测的不一定对
        }
    }

    public void showInsertAlertDialog(int mode) {
        AlertDialog dialog=new AlertDialog.Builder(getActivity(),R.style.Translucent_NoTitle).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_if_layout);
        buttonDialogYes = (Button)window.findViewById(R.id.dialog_confirm);
        buttonDialogNo = (Button)window.findViewById(R.id.dialog_cancel);
        buttonDialogYes.setOnClickListener(new BDialogSetOverWriteOrNotClickLis(dialog, true, mode));
        buttonDialogNo.setOnClickListener(new BDialogSetOverWriteOrNotClickLis(dialog, false, mode));
        buttonDialogYes.setText("是");
        buttonDialogNo.setText("否");
        TextView dialogText = (TextView)window.findViewById(R.id.dialog_text);
        dialogText.setText("覆盖词库原有记录？");
    }

    private class BDialogSetOverWriteOrNotClickLis implements View.OnClickListener {
        AlertDialog malertDialog;
        int mode;
        boolean isDeleteOrigin = false;
        public BDialogSetOverWriteOrNotClickLis(AlertDialog alertDialog, boolean isDeleteOrigin, int mode) {
            malertDialog=alertDialog;
            isDeleteOrigin = isDeleteOrigin;
            mode = mode;
        }
        public void onClick(View arg0) {
            /*myProgressBar=(MyProgressBar)getActivity().findViewById(R.id.progressBar_update_glossary);
            RelativeLayout relativeLayout=(RelativeLayout) getActivity().findViewById(R.id.rel_set_progress_back);
            relativeLayout.setVisibility(View.VISIBLE);
            malertDialog.cancel();*/
            ProgressDialog progressDialog=new ProgressDialog(getActivity());
            progressDialog.setTitle("词库导入中");
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(true);
            progressDialog.show();
            new ThreadUpdateGlossary(getActivity(), isDeleteOrigin, mode).start();
        }
    }

    private class ThreadUpdateGlossary extends Thread{
        boolean isDeleteOrigin = false;
        int mode = 0;
        public ThreadUpdateGlossary(Context context, boolean isDeleteOrigin, int mode) {
            isDeleteOrigin = isDeleteOrigin;
            mode = mode;
        }
        public void run() {
            super.run();
            // 每秒步长为10增加,到100%时停止
            for(int i=0 ; i < 20; i++){
                try{
                    iCount = (i + 1) * 10;
                    Thread.sleep(1000);
                    if(i >= 19){
                        Message msg = new Message();
                        msg.what = STOP;
                        mHandler.sendMessage(msg);
                        break;
                    }else{
                        Message msg = new Message();
                        msg.what = NEXT;
                        mHandler.sendMessage(msg);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //
            updateGlossary(isDeleteOrigin, mode);
        }
    }

    //定义一个Handler
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what) {
                case STOP:
                    myProgressBar.setVisibility(View.GONE);
                    Thread.currentThread().interrupt();
                    break;
                case NEXT:
                    if(!Thread.currentThread().isInterrupted()){
                        myProgressBar.setProgress(iCount);
                    }
                    break;
            }
        }
    };

    /**
     * 将从Raw中读取对应的txt文件并添加到数据库的wordsstudy表中
     * @param isDelteOriginData
     * @param mode
     */
    public void updateGlossary(boolean isDelteOriginData, int mode) {
        //从Raw中读取对应的txt文件
        InputStream inputStream = getResources().openRawResource(glossaryRawResourceID);
        FileUtils fileUtils=new FileUtils();
        WordListParser wordListParser=new WordListParser(getActivity(),"wordsList");
        wordListParser.parse(fileUtils.getStringFromInputStream(inputStream));
    }

    private class BsetDeadlineClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //先检测是否创建过学习计划
            if(courseName.equals(""))
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
                        }
                    }
                    , calendar.get(Calendar.YEAR)
                    , calendar.get(Calendar.MONTH)
                    , calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
            //计算学习总天数
            String start_time=calendar.get(Calendar.YEAR)+"."+(calendar.get(Calendar.MONTH)+1)+"."+calendar.get(Calendar.DAY_OF_MONTH);
            String end_time=deadlineYear+"."+deadlineMonth+"."+deadlineDay;
            calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
            long time1=calendar.getTimeInMillis();
            calendar.set(deadlineYear,deadlineMonth-1,deadlineDay);
            long time2=calendar.getTimeInMillis();
            int interdays=(int) (time2-time1)/(1000*60*60*24);
            Toast.makeText(getActivity(),interdays+"",Toast.LENGTH_SHORT).show();
            dbManager.insertReciteData(courseName,totalWords,start_time,interdays,end_time);
            editTime.setText(deadlineYear + "." + deadlineMonth + "." + deadlineDay);
        }
    }
}

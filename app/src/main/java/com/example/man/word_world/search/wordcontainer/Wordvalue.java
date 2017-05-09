package com.example.man.word_world.search.wordcontainer;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by man on 2016/12/24.
 */
public class WordValue {
    public String word=null,psE=null,pronE=null,psA=null,pronA=null,
            interpret=null,sentOrig=null,sentTrans=null;



    public WordValue(String word, String psE, String pronE, String psA,
                     String pronA, String interpret, String sentOrig, String sentTrans) {
        super();
        this.word = ""+word;
        this.psE = ""+psE;
        this.pronE = ""+pronE;
        this.psA = ""+psA;
        this.pronA = ""+pronA;
        this.interpret = ""+interpret;
        this.sentOrig = ""+sentOrig;
        this.sentTrans = ""+sentTrans;
    }

    public WordValue() {
        super();
        this.word = "";      //防止空指针异常
        this.psE = "";
        this.pronE = "";
        this.psA = "";
        this.pronA = "";
        this.interpret = "";
        this.sentOrig = "";
        this.sentTrans = "";
    }

    public ArrayList<String> getOrigList(){
        ArrayList<String> list=new ArrayList<>();
        BufferedReader br=new BufferedReader(new StringReader(this.sentOrig));
        String str=null;
        try{
            while((str=br.readLine())!=null){
                list.add(str);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<String> getTransList(){
        ArrayList<String> list=new ArrayList<String>();
        BufferedReader br=new BufferedReader(new StringReader(this.sentTrans));
        String str=null;
        try{
            while((str=br.readLine())!=null){
                list.add(str);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<String> getSentList(){
        ArrayList<String> list=new ArrayList<>();
        ArrayList<String> listorig=getOrigList();
        ArrayList<String> listtrans=getTransList();
        try {
            for(int i=0;i<listorig.size();i++){
                list.add(listorig.get(i)+"\n"+listtrans.get(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPsE() {
        return psE;
    }

    public void setPsE(String psE) {
        this.psE = psE;
    }

    public String getPronE() {
        return pronE;
    }

    public void setPronE(String pronE) {
        this.pronE = pronE;
    }

    public String getPsA() {
        return psA;
    }

    public void setPsA(String psA) {
        this.psA = psA;
    }

    public String getPronA() {
        return pronA;
    }

    public void setPronA(String pronA) {
        this.pronA = pronA;
    }

    public String getInterpret() {
        return interpret;
    }

    public void setInterpret(String interpret) {
        this.interpret = interpret;
    }

    public String getSentOrig() {
        return sentOrig;
    }

    public void setSentOrig(String sentOrig) {
        this.sentOrig = sentOrig;
    }

    public String getSentTrans() {
        return sentTrans;
    }

    public void setSentTrans(String sentTrans) {
        this.sentTrans = sentTrans;
    }

    public void printInfo(){
        System.out.println(this.word);
        System.out.println(this.psE);
        System.out.println(this.pronE);
        System.out.println(this.psA);
        System.out.println(this.pronA);
        System.out.println(this.interpret);
        System.out.println(this.sentOrig);
        System.out.println(this.sentTrans);
    }
}

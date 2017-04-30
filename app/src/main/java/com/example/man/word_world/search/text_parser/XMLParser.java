package com.example.man.word_world.search.text_parser;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;

/**
 * Created by man on 2016/12/24.
 */
public class XMLParser {
    public SAXParserFactory factory=null;
    public XMLReader reader=null;

    public XMLParser(){
        try {
            factory=SAXParserFactory.newInstance();
            reader=factory.newSAXParser().getXMLReader();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void parseJinShanXml(DefaultHandler content, InputSource inSource){
        if(inSource==null)
            return;
        try {
            reader.setContentHandler(content);
            reader.parse(inSource);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /*public void parseDailySentenceXml(DailySentContentHandler contentHandler, InputSource inSource){
        if(inSource==null)
            return;
        try {
            reader.setContentHandler(contentHandler);
            reader.parse(inSource);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/
}

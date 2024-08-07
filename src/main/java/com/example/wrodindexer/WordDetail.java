package com.example.wrodindexer;


import java.util.ArrayList;
import java.util.List;

public class WordDetail {
    private int count = 0;
    private String word = "";
    private List<String> filesFullPath = new ArrayList<String>();

    public int getCount() {
        return count;
    }

    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
    }
    public List<String> getFileFullPath() {
        return filesFullPath;
    }
    public void addFile(String fileFullPath) {
        filesFullPath.add(fileFullPath);
    }
    public void incrementCount() {
        count++;
    }

}

package com.example.wrodindexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Worker implements Runnable {
    ReverseIndexer ri = null;

    String threadName;

    public Worker(ReverseIndexer ri, String threadName) {
        this.ri = ri;
        this.threadName = threadName;
        System.out.println(threadName);
    }

    @Override
    public void run() {
        while(true) {
            if (ri.isWorkAvailable()) {
                createIndex(ri.getWork());

            }
            else {
                try {
                    synchronized (ri.lock) {
                        ri.lock.wait(2*100*60);
                    }

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }


        //ri.workers.shutdown();
    }

    public void createIndex(File f) {
        System.out.println(this.threadName + " Processing starts " + f.getName());
        List<String> content = readFileContent(f);
        constructWordDetailsDataStructure(content, f);
        System.out.println(this.threadName + " Processing completed " + f.getName());

        moveFile(f);
    }

    public static final String destDir = "C:\\personel\\ProcessedFiles";

    public void moveFile(File f) {
        String name=null;
        try {
            File tempf = new File(destDir+"\\"+f.getName());
            if(tempf.exists()) {
                System.out.println(this.threadName + " if  name  " + f.getName());
                int i=1;
                while(tempf.exists()) {
                    name = tempf.getName();
                    String[] arrOfStr = name.split("\\.",-1);
                    arrOfStr[0]=arrOfStr[0]+i;
                    name = arrOfStr[0]+"."+arrOfStr[1];
                    i++;
                    tempf = new File(destDir+"\\"+name);
                }
            }
            else {
                name = tempf.getName();
                System.out.println(this.threadName + " else name  " + f.getName());
            }
            Files.move(Paths.get(f.getAbsolutePath()), Paths.get(destDir+"\\"+name));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<String> readFileContent(File f) {
        List<String> content = new ArrayList<String>();
        Scanner sc = null;
        try {
            sc = new Scanner(f);
            String input;
            StringBuffer sb = new StringBuffer();
            while (sc.hasNextLine()) {
                input = sc.nextLine();
                sb.append(input + " ");
            }
            content.add(sb.toString());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (sc != null)
                sc.close();
        }
        return content;

    }

    private void constructWordDetailsDataStructure(List<String> content, File f) {
        String[] tempSplit = null;
        for (String words : content) {
            tempSplit = words.split(" ");
            for (String word : tempSplit) {
                if (ri.containsKey(word)) {
                    WordDetail wordDetail = ri.getWordDetails(word);
                    wordDetail.incrementCount();
                    wordDetail.addFile(f.getAbsolutePath());
                    wordDetail.setWord(word);
                    ri.wordDetails.put(word, wordDetail);
                } else {
                    WordDetail wordDetail = new WordDetail();
                    wordDetail.setWord(word);
                    wordDetail.incrementCount();
                    wordDetail.addFile(f.getAbsolutePath());
                    ri.wordDetails.put(word, wordDetail);
                }
            }
        }
    }

    public void search() {
        boolean flag = true;
        Scanner sc = new Scanner(System.in);
        String wordToBeSearched = sc.next();
        for(String key : ri.wordDetails.keySet()) {
            if(key.equals(wordToBeSearched)) {
                flag = true;
            }
            else {
                flag = false;
            }
        }
        if(flag == true) {
            System.out.println("Word found");
        }
        else {
            System.out.println("Word not found");
        }
        sc.close();
    }

}

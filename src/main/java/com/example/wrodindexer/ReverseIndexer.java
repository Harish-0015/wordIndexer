package com.example.wrodindexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

//import com.textRead.WordDetail;

public class ReverseIndexer {
    ExecutorService workers = null;
    final static String dir = "C:\\personel\\IndexFiles";
    final static int numWorker = 5;
    String[] wordSplit = null;
    public Map<String, WordDetail> wordDetails = new HashMap<String, WordDetail>();

    File[] fileList = null;
    LinkedList<File> linkFileList = null;
    File folder = null;
    void readNumberOfFilesFromDir(boolean noCounterInit) {
        folder = new File(dir);

        fileList = folder.listFiles();
        List<File> list = Arrays.asList(fileList);
        linkFileList = new LinkedList<File>(list);
        if(noCounterInit) {
            if(linkFileList.size() > 0)
                workDoneCounter = new AtomicInteger(linkFileList.size());
            else
                workDoneCounter = new AtomicInteger(0);
        }

        System.out.println("copying file");
    }
    Object lock = new Object();

    public ReverseIndexer() {
        readNumberOfFilesFromDir(true);
        workers = Executors.newFixedThreadPool(numWorker);

        //System.out.println(linkFileList.size());
        Worker w1 = null;
        for (int i = 0; i < numWorker; i++) {
            w1 = new Worker(this, "thread" + i);
            workers.execute(w1);
        }
        checkAndReadForMoreFile();
        toStartSeach();
    }

    public WordDetail getWordDetails(String word) {

        return wordDetails.get(word);
    }

    public File getWork() {
        synchronized (lock) {
            workDoneCounter.getAndDecrement();
            return linkFileList.remove();

        }

    }

    public AtomicInteger workDoneCounter = null;

    public synchronized File getWorkV1() {
        return linkFileList.remove();
    }

    public  boolean isWorkAvailable() {
        synchronized (lock) {
            return (linkFileList.size() > 0 ? true : false);

        }

    }

    public boolean containsKey(String word) {
        return wordDetails.containsKey(word);
    }

    public void put(String key, WordDetail value) {
        wordDetails.put(key, value);
    }

    public static void main(String[] args) throws Exception {

        ReverseIndexer ri = new ReverseIndexer();
        while(ri.isWorkAvailable()) {
            Thread.currentThread().sleep(1*100*60);
        }


    }

    public void checkAndReadForMoreFile() {
        lookupfForFile.setDaemon(true);
        lookupfForFile.start();
    }


    Thread lookupfForFile = new Thread(new Runnable() {

        @Override
        public void run() {
            while (true) {
                if(workDoneCounter.get()==0 && folder.listFiles().length > 0) {
                    readNumberOfFilesFromDir(true);
                    synchronized (lock) {
                        lock.notifyAll();
                    }

                }
                else {
                    try {
                        Thread.currentThread().sleep(2*100*60);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    });

    void toStartSeach() {
        Thread t = new Thread(search);
        t.setDaemon(true);
        t.start();
    }
/*
	Runnable search = new Runnable() {
		public void run() {
			Scanner sc = new Scanner(System.in);
			while(true) {
				if(sc.hasNext()) {

					String word = sc.next();
					WordDetail wd = wordDetails.get(word.trim());
					if(wd != null) {
						System.out.println(word+" Result found in the following files: ");
						Iterator i = wd.getFileFullPath().iterator();
						while(i.hasNext()) {
							System.out.println(i.next());
						}
					}
					else {
						System.out.println(word + " Word not found ");
					}

				}
				else {
					try {
						Thread.currentThread().sleep(2*100*60);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	};
	*/

    Callable<WordDetail> cal = new Callable<WordDetail>() {

        @Override
        public WordDetail call() throws Exception {
            Scanner sc = new Scanner(System.in);
            WordDetail wd;
            while(true) {
                if(sc.hasNext()) {

                    String word = sc.next();
                    wd = wordDetails.get(word.trim());
                    if(wd != null) {
                        System.out.println(word+" Result found in the following files: ");
                        Iterator i = wd.getFileFullPath().iterator();
                        while(i.hasNext()) {
                            System.out.println(i.next());

                        }
                        return wd;
                    }
                    else {
                        System.out.println(word + " Word not found ");
                        return wd;
                    }

                }
                else {
                    try {
                        Thread.currentThread().sleep(2*100*60);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        }
    };

    FutureTask<WordDetail> f = new FutureTask<WordDetail>(cal);
    Thread search = new Thread(f);

    public WordDetail search(String word) {
        return wordDetails.get(word);

    }

}



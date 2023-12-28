package org.swdc.toybox.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.toybox.core.FSPathAggregate;

import java.io.File;
import java.io.IOException;

public class Test {

    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws IOException {
        File file = new File("F:\\BaiduNetdiskDownload\\software\\products\\COSY");
        File file1 = new File("E:\\BaiduNetdiskDownload\\");
        FSPathAggregate FSPathAggregate = new FSPathAggregate(file1.getAbsolutePath());
        FSPathAggregate FSPathAggregate1 = new FSPathAggregate(file.getAbsolutePath());

        FSPathAggregate agg = FSPathAggregate1.getMatchedPart(FSPathAggregate);
        System.err.println(agg.getByLevel(agg.size()));
    }

}

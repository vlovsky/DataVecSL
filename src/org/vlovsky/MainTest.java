/*-
 * Copyright 2017 Vlad Sadilovki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.vlovsky;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.datavec.api.util.ClassPathResource;
import org.datavec.api.writable.Writable;
import org.datavec.spark.transform.misc.WritablesToStringFunction;
import org.vlovsky.etl.DataLoadConductor;
import org.vlovsky.etl.ParserException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by vlad on 4/1/2017.
 */
public class MainTest {
    public static void main (String[] args) throws IOException, ParserException {
        SparkConf spConf = new SparkConf();
        spConf.setMaster("local[*]");
        spConf.setAppName("DataVec Example");

        JavaSparkContext spCtx = new JavaSparkContext(spConf);
        testStringScript(spCtx);
        testFileScript(spCtx);
    }

    private static void testStringScript(JavaSparkContext spCtx) throws IOException, ParserException {
        String loadScr =
                "load data\n" +
                        "from :1\n" +
                        "fields terminated by ','\n" +
                        "into var\n" +
                        "when TransactionAmountUSD >= 0 and MerchantCountryCode in enum(\"USA\",\"CAN\")\n" +
                        "as(\n" +
                        "         DateTimeString string filler,\n" +
                        "         rem HourOfDay integer extract(hour, toDate(DateTimeString, \"YYYY-MM-DD HH:mm:ss.SSS\", \"UTC\")),\n" +
                        "         CustomerID string filler,\n" +
                        "         MerchantID string filler,\n" +
                        "         NumItemsInTransaction integer,\n" +
                        "         MerchantCountryCode enum(\"USA\",\"CAN\",\"FR\",\"MX\"),\n" +
                        "         TransactionAmountUSD double (if 50.0 > TransactionAmountUSD then 0.0 else TransactionAmountUSD),\n" +
                        "         FraudLabel enum(\"Fraud\",\"Legit\"));";


        String dataSrc = new ClassPathResource("example/exampledata.csv").getFile().getAbsolutePath();
        DataLoadConductor dataLoader = new DataLoadConductor(loadScr, dataSrc);

        dataLoader.execute(spCtx);
        JavaRDD<List<Writable>> processedData = dataLoader.getLoadedData();

        //For the sake of this example, let's collect the data locally and print it:
        JavaRDD<String> processedAsString = processedData.map(new WritablesToStringFunction(","));

        List<String> processedCollected = processedAsString.collect();
        JavaRDD<String> sourceData = dataLoader.getSourceData();
        List<String> inputDataCollected = sourceData.collect();

        System.out.println("\n\n---- Original Data ----");
        for(String s : inputDataCollected) System.out.println(s);

        System.out.println("\n\n---- Processed Data ----");
        for(String s : processedCollected) System.out.println(s);
    }

    private static void testFileScript(JavaSparkContext spCtx) throws IOException, ParserException {
        InputStream sc = new BufferedInputStream(new FileInputStream(new ClassPathResource("load.ctl").getFile()));
        String dataSrc = new ClassPathResource("example/exampledata.csv").getFile().getAbsolutePath();
        DataLoadConductor dataLoader = new DataLoadConductor(sc, dataSrc);

        dataLoader.execute(spCtx);
        JavaRDD<List<Writable>> processedData = dataLoader.getLoadedData();

        //For the sake of this example, let's collect the data locally and print it:
        JavaRDD<String> processedAsString = processedData.map(new WritablesToStringFunction(","));

        List<String> processedCollected = processedAsString.collect();
        JavaRDD<String> sourceData = dataLoader.getSourceData();
        List<String> inputDataCollected = sourceData.collect();

        System.out.println("\n\n---- Original Data ----");
        for(String s : inputDataCollected) System.out.println(s);

        System.out.println("\n\n---- Processed Data ----");
        for(String s : processedCollected) System.out.println(s);
    }
}

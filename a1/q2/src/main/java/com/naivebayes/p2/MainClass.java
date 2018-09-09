package com.naivebayes.p2;

import java.io.*;
import java.net.URI;
import java.util.HashMap;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
//import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.hdfs.DistributedFileSystem;

public class MainClass extends Configured implements Tool 
{
    double m,qy,cyany;
    HashMap<String, Double> mapprior = new HashMap<String, Double>();
    HashMap<String, Long> map = new HashMap<String, Long>();
     public int run(String[] args) throws Exception 
     {
        int numreducers = Integer.parseInt(args[0]);
        String trainfile = args[2];
        String testfile = args[3];
        String output = args[1];
        
        JobControl jobControl = new JobControl("NBjobChain"); 
        Configuration conf1 = getConf();

        Job job1 = Job.getInstance(conf1);  
        job1.setJarByClass(MainClass.class);
        job1.setJobName("NBPreprocessing&Counting");
        job1.setNumReduceTasks(numreducers);

        FileInputFormat.setInputPaths(job1, new Path(trainfile));
        FileOutputFormat.setOutputPath(job1, new Path(output + "/temp"));

        job1.setMapperClass(LabelwordMapper.class);
        job1.setReducerClass(CountReducer.class);
        //job1.setCombinerClass(SumReducer.class);

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(LongWritable.class);

        ControlledJob controlledJob1 = new ControlledJob(conf1);
        controlledJob1.setJob(job1);

        jobControl.addJob(controlledJob1);
        Configuration conf2 = getConf();

        Job job2 = Job.getInstance(conf2);
        job2.setJarByClass(MainClass.class);
        job2.setNumReduceTasks(numreducers);
        job2.setJobName("Counting Vocabs");

        FileInputFormat.setInputPaths(job2, new Path(output + "/temp"));
        FileOutputFormat.setOutputPath(job2, new Path(output + "/vocabcount"));

        job2.setMapperClass(CountvocabMapper.class);
        job2.setReducerClass(CountReducer.class);
        //job2.setCombinerClass(SumReducer2.class);

        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(LongWritable.class);
        //job2.setInputFormatClass(TextInputFormat.class);

        //job2.setSortComparatorClass(IntComparator.class);
        ControlledJob controlledJob2 = new ControlledJob(conf2);
        controlledJob2.setJob(job2);

    // make job2 dependent on job1
        controlledJob2.addDependingJob(controlledJob1); 
    // add the job to the job control
        jobControl.addJob(controlledJob2);
        Thread jobControlThread = new Thread(jobControl);
        jobControlThread.start();

        while (!jobControl.allFinished()) {
        try {
          Thread.sleep(500);
          } 
        catch (Exception e) {}
        }
          
        System.exit(0);  
        return (job1.waitForCompletion(true) ? 0 : 1);   
      } 
      public static void main(String[] args) throws Exception 
      { 
        MainClass obj = new MainClass();
        int exitCode = ToolRunner.run(obj, args); 
        DistributedFileSystem fileSystem = new DistributedFileSystem();
        Configuration conf = new Configuration();
        fileSystem.initialize(new URI("hdfs://turing.cds.iisc.ac.in:8088"), conf);
        FSDataInputStream input = fileSystem.open(new Path(args[1]+"/vocabcount/p*"));
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        obj.m = Double.parseDouble((br.readLine().split("\t"))[1]);
        obj.qy = 1.0/50.0;
        obj.cyany=1.0;
        System.out.println(obj.m);
        input = fileSystem.open(new Path(args[1]+"/temp/p*"));
        br = new BufferedReader(new InputStreamReader(input));        
        String element;
        while(true)
        {
          element = br.readLine();
          if(element.charAt(0)!='#' || element.charAt(0)!='$')
            break;
          if(element.charAt(1)=='#')
          {
            obj.cyany = Double.parseDouble(element.split("\t")[1]);
            continue;
          }
          String key[] = element.split("\t");
          switch(element.charAt(0))
          {
            case '#': obj.mapprior.put(key[0].substring(1), new Double(Double.parseDouble(key[1])/obj.cyany));
                      break;
            case '$': obj.map.put(key[0].substring(1), Long.parseLong(key[1]));
                      break;
          }
        }
        
        System.exit(exitCode);
      }
}
package com.naivebayes.p2;


import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapred.*;

public class CountvocabMapper extends Mapper <LongWritable, Text, Text, LongWritable> 
{
	private final static LongWritable one = new LongWritable(1);
  private Text wre = new Text("w");
	public void map(LongWritable value, Text key, Context context) throws IOException, InterruptedException
  {
		String element = key.toString();    
    if(element.compareTo("%")>0 && element.compareTo("%~")<0)
    {
      context.write(wre, one);
    }
	}
}
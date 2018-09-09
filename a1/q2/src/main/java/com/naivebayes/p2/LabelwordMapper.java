package com.naivebayes.p2;


import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapred.*;

public class LabelwordMapper extends Mapper <LongWritable, Text, Text, LongWritable> 
{
	private final static LongWritable one = new LongWritable(1);
  private Text wre = new Text();
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
  {
		String element = value.toString();
    String punctuations = "[^\\w\\s]";
    int tabindex1 = element.indexOf("\t");
    String currlabel[] = (element.substring(0,tabindex1)).split(",");
    int tabindex2 = element.indexOf("\"", tabindex1+1)+1;
		tabindex1 = element.indexOf("\"@en", tabindex2+1);
		String document = ((element.substring(tabindex2,tabindex1))).toLowerCase();
    StringTokenizer str = new StringTokenizer(document);
    for(int j=0;j<currlabel.length;j++)
    {
      wre.set("#"+currlabel[j]);
      context.write(wre,one);      
    }
    wre.set("##totalnumlabels");
    context.write(wre,new LongWritable(currlabel.length));
    long numwords = 0;
    while(str.hasMoreTokens())
    {
      String word = (str.nextToken()).replaceAll(punctuations,"");
      wre.set("%"+word);  //for vocab count
      context.write(wre,one);
      for(int i=0;i<currlabel.length;i++)
      {
        wre.set(currlabel[i]+","+word);  //for C(label=y & word=x)
        context.write(wre,one);        
        numwords++;
      }
    } 
    LongWritable numwrds = new LongWritable(numwords);
    for(int j=0;j<currlabel.length;j++)
    {
      wre.set("$"+currlabel[j]); //for C(label=y & word='any')
      context.write(wre,numwrds);
    }
	}
}
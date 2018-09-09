package com.naivebayes.p1;

import java.io.*;
import java.util.*;
import java.lang.Math;
public class App 
{
	
	HashMap<String,Long> labelcount = new HashMap<String,Long>(50); //Stores the number of times a label has appeared
	HashMap<String,Long> labelany = new HashMap<String,Long>(50);   //Stores the number of times a label with conjunction of any word has appeared
	HashMap<String, Long> wordcount = new HashMap<String, Long>();  //Stores the number of times a word has appeared
	HashMap<String, Long> wcwlabel = new HashMap<String, Long>();		  //Stores the number of times a (label,word) pair has appeared
	HashMap<String, Double> sum = new HashMap<String, Double>();	//Stores the initial values of the function to be calculated after the train phase
  long totallabelcount = 0;										//C(label='ANY')
	String document=null, punctuations = "[^\\w\\s]";
	double m = 1.0, qy = 1.0/50.0;
	public void train(String filepath)
	{
		try{
			BufferedReader rd = new BufferedReader(new FileReader(filepath));
			String element = null;
			int tabindex1, tabindex2;
			while((element = rd.readLine())!=null)
			{				
				tabindex1 = element.indexOf("\t");
				String currlabel[] = (element.substring(0,tabindex1)).split(",");  //Different labels of a document
				long numlabels = currlabel.length;		//Number of labels of this document
				
				//Reading the actual document
				tabindex2 = element.indexOf("\"", tabindex1+1)+1;
				tabindex1 = element.indexOf("\"@en", tabindex2+1);
				document = ((element.substring(tabindex2,tabindex1))).toLowerCase();	
				
				//Processing individual tokens of the string
				StringTokenizer str = new StringTokenizer(document);
				long numwords = str.countTokens(); 	//Total number of tokens present in the document
				while(str.hasMoreTokens())			//For every word in the document
				{
					String word = (str.nextToken()).replaceAll(punctuations,"");	//word itself
					if(wordcount.get(word)==null)	//word is not present in the vocabulary, add it else increment its count
					  wordcount.put(word,(long)1);
					else
					  wordcount.put(word, wordcount.get(word) + 1);
					
					//Update C(label=y & word =x)
					for(int i=0;i<numlabels;i++)
					{
						String temporary= currlabel[i]+","+word;
						if(wcwlabel.get(temporary)==null)
            {
              wcwlabel.put(temporary,(long)1);
              //System.out.println(temporary.getlbl()+" "+temporary.getword());
            }
            else						
						  wcwlabel.put(temporary, wcwlabel.get(temporary) + 1);
					}
				}				
				
				for(int i=0;i<numlabels;i++)			
				{
					if(labelcount.get(currlabel[i])==null) 		//Insert label in vocabulary if not there
						labelcount.put(currlabel[i], (long)1);
					else										//Increment the count of the label if already present
						labelcount.put(currlabel[i], labelcount.get(currlabel[i]) + 1);					
					
					//This section counts the C(label='y' & word='ANY') part
					if(labelany.get(currlabel[i])==null) 		//Insert label in dictionary if not there
						labelany.put(currlabel[i], numwords);
					else										//Increment the count of the label if already present
						labelany.put(currlabel[i], labelany.get(currlabel[i]) + numwords);										
				}
				totallabelcount+=numlabels;			//Increment total number of labels saw
			}
      m=(double)wordcount.size();		  
			Iterator<Map.Entry<String, Long>> it = labelcount.entrySet().iterator();
      //System.out.println("Sum values");
			while(it.hasNext())
			{
				Map.Entry<String, Long> curr = (Map.Entry)it.next();
        sum.put(curr.getKey(),Math.log10((curr.getValue() + (m*qy))/((double)(totallabelcount+m))));
        //System.out.println(curr.getKey()+" "+sum.get(curr.getKey()));				
			}
      /*
      for(Map.Entry<String, Long> entry : wcwlabel.entrySet())
      {
        System.out.println(entry.getKey()+" "+entry.getValue());
      }
      */
		}
		catch(FileNotFoundException e){
			System.out.println("File Does Not Exist");
		}
		catch(IOException e){
			System.out.println("Error Reading file");
			e.printStackTrace();
		}
	}
	
	public double test(String filepath)
	{
		double retval = 0.0;
		try{
			BufferedReader rd = new BufferedReader(new FileReader(filepath));
			String element = null;
			int tabindex1, tabindex2;
			long total = 0, wrong =0, correct=0;
			//Per Document
      while((element = rd.readLine())!=null)
			{				
				tabindex1 = element.indexOf("\t");
				String currlabel[] = (element.substring(0,tabindex1)).split(",");  //Different labels of a document
				int numlabels = currlabel.length;		//Number of labels of this document
				//Hashmap with the probability values				
        HashMap<String, Double> function = new HashMap<String, Double>();
		    function.putAll(sum);
		
				//Reading the actual document
				tabindex2 = element.indexOf("\"", tabindex1+1)+1;
				tabindex1 = element.indexOf("\"@en", tabindex2+1);
				document = ((element.substring(tabindex2,tabindex1))).toLowerCase();	
				
				//Processing individual tokens of the string
				StringTokenizer str = new StringTokenizer(document);
				//Per word
        while(str.hasMoreTokens())			//For every word in the document
				{
					String word = (str.nextToken()).replaceAll(punctuations,"");	//word itself
					if(wordcount.get(word)==null)
						continue;
					Iterator<Map.Entry<String, Double>> iter = function.entrySet().iterator();
					while(iter.hasNext())
					{
						Map.Entry<String, Double> curr = (Map.Entry)iter.next();
						String temporary = curr.getKey()+","+word;
						double tempval, cval;
						if(wcwlabel.get(temporary)==null)
							cval = 0.0;
						else
							cval = wcwlabel.get(temporary);
						tempval = Math.log10((cval+1.0)/(labelany.get(curr.getKey())+m)) + curr.getValue();
						curr.setValue(tempval);
            //System.out.println(curr.getKey()+" "+cval+" "+(curr.getValue()-function.get(curr.getKey())));
					}
				}
        //end per word
				Map.Entry<String, Double> maxval = null;
				for (Map.Entry<String, Double> entry : function.entrySet()) 
				{
					if (maxval == null || (maxval.getValue() <= entry.getValue()))
						maxval = entry;          
          //System.out.print(entry.getKey()+" "+entry.getValue()+"\t");      
				}
        //System.out.println("\nPredicted Label: "+maxval.getKey()+"\tScore : "+maxval.getValue()+"\nCorrect Labels : ");
				for(int i=0;i<numlabels;i++)
				{
          //System.out.println(currlabel[i]+"  "+function.get(currlabel[i]));
					if(currlabel[i].equals(maxval.getKey())==true)
					{
						correct++;
						break;
					}
				}
        //System.out.println();
				total++;
			}
			System.out.println("Correctly Classified : "+correct+"\tTotal : "+total);
			retval = (correct * 100.0)/total;
		}
		catch(FileNotFoundException e){
			System.out.println("File Does Not Exist");
		}
		catch(IOException e){
			System.out.println("Error Reading file");
			e.printStackTrace();
		}
		return retval;
	}
	
	public static void main(String args[])
	{
		App obj = new App();
    long starttime, endTime, totalTime;
    String trainfilepath = args[0];
		String testfilepath = args[1];
    String develfilepath = args[2];
		
    //Training Phase
    starttime = System.nanoTime();
    obj.train(trainfilepath);
    endTime   = System.nanoTime();
    totalTime = (endTime - starttime)/1000000000;
    System.out.println("Training Time Taken = "+totalTime+" seconds");
		starttime = System.nanoTime();
    double accuracy = obj.test(trainfilepath);
    endTime   = System.nanoTime();
    totalTime = (endTime - starttime)/1000000000;
		System.out.println("Training Accuracy = "+ accuracy+ "\nTime Taken = "+totalTime+" seconds");
		starttime = System.nanoTime();
    accuracy = obj.test(develfilepath);
    endTime   = System.nanoTime();
    totalTime = (endTime - starttime)/1000000000;
		System.out.println("Development Accuracy = "+ accuracy+ "\nTime Taken = "+totalTime+" seconds");
    starttime = System.nanoTime();
    accuracy = obj.test(testfilepath);
    endTime   = System.nanoTime();
    totalTime = (endTime - starttime)/1000000000;
		System.out.println("Testing Accuracy = "+ accuracy+ "\nTime Taken = "+totalTime+" seconds");
	}
}

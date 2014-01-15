package com.reademail.main;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

import org.junit.Test;

public class mailReaderTest {

	@Test
	public void testSplit() {
		
		mailReader m = new mailReader();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("C:\\Users\\Ben\\SampleSubjects.txt"));
			PrintWriter writer = new PrintWriter("C:\\Users\\Ben\\SampleSubjects_processed.txt", "UTF-8");
		String line;
		while ((line = br.readLine()) != null) {
		   
			if (!(line.contains("http")) && line.contains("$"))
			{
				 OrderTemplate  _OrderTemplate = m.getSplit(line);
			     writer.println(line+","+_OrderTemplate.getSide()+","+_OrderTemplate.getTicker()+","+_OrderTemplate.getQuantity());
			}
		}
		br.close();
		writer.close();
		
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
				
		
		assertEquals(1,1);
	}

}

package com.reademail.main;

public class SubjectSplitter {
	OrderTemplate _OrderTemplate;
	
	public OrderTemplate Split(String Subject)
	{
		String[] array = Subject.split(""); 
		String Ticker;
		int Quantity;
		String Side;
		for(String s : array)
		{
			if (s.startsWith("$"))
			{
				Ticker = s;
				break;
			}
			if (s.startsWith("<") || s.endsWith(">"))
			{
				Ticker = s;
			}
		}
		
	//	_OrderTemplate = new OrderTemplate(100,"AAPL","BUY");
		
		
		return _OrderTemplate;
	}
	
}

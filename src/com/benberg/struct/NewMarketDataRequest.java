package com.benberg.struct;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class NewMarketDataRequest implements Serializable{
	private String Ticker;
	private String CorrelationId;
	private String MarketDataJson;
	private String TimeFrame;
	private static final long serialVersionUID = 1L;
	public NewMarketDataRequest(String Ticker,String CorrId, String TimeFrame)
	{
		this.Ticker = Ticker;
		this.CorrelationId = CorrId;
		this.TimeFrame = TimeFrame;
	}
	public NewMarketDataRequest(String Ticker,String CorrId, String MarketData,boolean hasData)
	{
		this.Ticker = Ticker;
		this.CorrelationId = CorrId;
		this.MarketDataJson=MarketData;
	}
	public String GetTicker()
	{
		return Ticker;
	}
	public String GetCorrelationId()
	{
		return CorrelationId;
	}
	public String GetMarketDataJson()
	{
		return MarketDataJson;
	}
	public String GetTimeFrame()
	{
		return TimeFrame;
	}
	 public byte[] toBytes() {
	      byte[]bytes; 
	      ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	      try{ 
	        ObjectOutputStream oos = new ObjectOutputStream(baos); 
	        oos.writeObject(this); 
	        oos.flush();
	        oos.reset();
	        bytes = baos.toByteArray();
	        oos.close();
	        baos.close();
	      } catch(IOException e){ 
	        bytes = new byte[] {};
	     System.out.println(e.toString());
	      }         
	      return bytes; 
	    }
}

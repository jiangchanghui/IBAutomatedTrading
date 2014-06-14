package com.benberg.struct;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class NewMarketDataRequest implements Serializable{
	private String Ticker;
	private String CorrelationId;
	private String MarketDataJson;
	private static final long serialVersionUID = 1L;
	public NewMarketDataRequest(String Ticker,String CorrId)
	{
		this.Ticker = Ticker;
		this.CorrelationId = CorrId;
	}
	public NewMarketDataRequest(String Ticker,String CorrId,String MarketDataJson)
	{
		this.Ticker = Ticker;
		this.CorrelationId = CorrId;
		this.MarketDataJson = MarketDataJson;
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

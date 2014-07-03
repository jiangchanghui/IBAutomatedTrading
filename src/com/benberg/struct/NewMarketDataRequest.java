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
	private boolean RealTime;
	private static final long serialVersionUID = 1L;
	public static int MARKETDATA = 1;
	public  static int RSIDATA = 2;
	private static RequestType Type;		
	//public static final int HISTORICAL = 1;
//	public static final int LIVE = 2;
//	private int type;
	public NewMarketDataRequest(boolean Realtime,String Ticker,String CorrId, String TimeFrame,RequestType Type)
	{
		this.RealTime = Realtime;
		this.Ticker = Ticker;
		this.CorrelationId = CorrId;
		this.TimeFrame = TimeFrame;
		this.Type = Type;
	}
	public RequestType getType()
	{
		return Type;
	}
	public boolean IsRealTime()
	{
		return RealTime;
	}
//	public int GetType()
//	{
//		return type;		
//	}
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

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
	private RequestType RequestType;		
	//public static final int HISTORICAL = 1;
//	public static final int LIVE = 2;
//	private int type;
	public NewMarketDataRequest(boolean Realtime,String Ticker,String CorrId, String TimeFrame,RequestType requestType)
	{
		this.RealTime = Realtime;
		this.Ticker = Ticker;
		this.CorrelationId = CorrId;
		this.TimeFrame = TimeFrame;
		this.RequestType = requestType;
	}
	public RequestType getType()
	{
		return RequestType;
	}
	public boolean IsRealTime()
	{
		return RealTime;
	}
//	public int GetType()
//	{
//		return type;		
//	}
	public NewMarketDataRequest(String Ticker,String correlationId, String MarketData,boolean hasData)
	{
		this.Ticker = Ticker;
		this.CorrelationId = correlationId;
		this.MarketDataJson=MarketData;
	}
	public NewMarketDataRequest(String ticker, RequestType requestType) {
		this.RequestType = requestType;
		this.Ticker=ticker;
	}
	public NewMarketDataRequest(String ticker, String correlationId,RequestType requestType,boolean realTime) {
		this.Ticker = ticker;
		this.CorrelationId = correlationId;
		this.RequestType = requestType;
		this.RealTime = realTime;
		
	
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
	public RequestType GetRequestType()
	{
		return RequestType;
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

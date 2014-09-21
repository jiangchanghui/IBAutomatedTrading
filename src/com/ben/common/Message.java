package com.ben.common;

import java.io.Serializable;

import com.ib.controller.Bar;



public class Message implements Serializable{
	protected static final long serialVersionUID = 1112122200L;
	public static final int NEW_REAL_TIME_MARKET_DATA_REQUEST=0;
	public static final int REAL_TIME_BAR=1;
	public static final int UNSUBSCRIBE_MARKET_DATA=2;
	private int type;
	private String ticker;
	private int reqId=0;
	private Bar bar;
	public Message(int type,String ticker,int reqId)
	{
		this.type = type;
		this.ticker = ticker;
		this.reqId=reqId;
	}
	public Message(int type, String ticker, Bar bar,int reqId) 
	{
		this.type = type;
		this.ticker = ticker;
		this.bar=bar;
		this.reqId = reqId;
		
	}
	
	public int getType() {
		
		return type;
	}
	
	public Bar getBar()
	{
		return bar;
	}
	public int getReqId() {
		// TODO Auto-generated method stub
		return reqId;
	}
	
	
	public String toString()
	{
		return "{type:"+type+",ticker:"+ticker+",reqId:"+reqId+"}";
	}
	
	public String getTicker() {
		return ticker;
	}
}

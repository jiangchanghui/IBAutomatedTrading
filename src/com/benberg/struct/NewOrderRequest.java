package com.benberg.struct;

import java.io.Serializable;

import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;

public class NewOrderRequest implements Serializable{

	String Ticker;
	String SenderId;
	int Quantity;
	OrderType OrdType;
	double LimitPx;
	Action Side;
	public NewOrderRequest(String Ticker,String SenderId) {
		this.Ticker = Ticker;
		this.OrdType=OrderType.MKT;
		this.Quantity = 100;
		this.Side=Action.BUY;
		this.SenderId = SenderId;
	}
	
	public NewOrderRequest(String Ticker,int Quantity,OrderType OrdType,double LimitPx,Action side,String SenderId) {
		this.Ticker = Ticker;
		this.Quantity = Quantity;
		this.OrdType = OrdType;
		this.LimitPx = LimitPx;
		this.Side = side;
		this.SenderId = SenderId;
	}
	@Override
	public String toString()
	{
		return "Side:"+Side+",Quantity:"+Quantity+ ",Ticker:"+Ticker+",OrderType:"+OrdType.toString()+",LimitPx:"+LimitPx+",SenderId:"+SenderId ;
	}
	
	public String Ticker()
	{
		return Ticker;
	}
	public int Quantity()
	{
		return Quantity;
	}
	public OrderType OrderType()
	{
		return OrdType;
	}
	public Action Side()
	{
		return Side;
	}
	public double LimitPx()
	{
		return LimitPx;
	}
}

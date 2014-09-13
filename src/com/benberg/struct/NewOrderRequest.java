package com.benberg.struct;

import java.io.Serializable;

import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;

public class NewOrderRequest implements Serializable{

	String Ticker;
	int Quantity;
	OrderType OrdType;
	double LimitPx;
	Action Side;
	public NewOrderRequest(String Ticker) {
		this.Ticker = Ticker;
		this.OrdType=OrderType.MKT;
		this.Quantity = 100;
		this.Side=Action.BUY;
	}
	
	public NewOrderRequest(String Ticker,int Quantity,OrderType OrdType,double LimitPx,Action side) {
		this.Ticker = Ticker;
		this.Quantity = Quantity;
		this.OrdType = OrdType;
		this.LimitPx = LimitPx;
		this.Side = side;

	}
	@Override
	public String toString()
	{
		return Side+" "+Quantity+ " "+Ticker+" "+OrdType.toString()+" "+LimitPx;
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

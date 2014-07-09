package com.benberg.struct;

import java.io.Serializable;

import com.ib.controller.Bar;

public class MarketDataTick implements Serializable{

	Bar bar;
	String Ticker;
		
	public MarketDataTick(String Ticker, Bar bar) {
		this.bar = bar;
		this.Ticker = Ticker;
		

	}
	public String getTicker(){
		return Ticker;
	}
	public Bar getBar() {
		return bar;
	}
}

package com.reademail.main;
import com.ib.controller.Types.Action;
public class OrderTemplate {
    
    private int Quantity;
    private String Ticker;
    private Action Side;
    OrderTemplate(int Quantity, String Ticker, Action Side) {
            this.Quantity = Quantity;
            this.Ticker = Ticker;
            this.Side = Side;
            }
   
    
   
    public OrderTemplate() {
		// TODO Auto-generated constructor stub
	}



	public int getQuantity() {
            return Quantity;
    }
    public String getTicker() {
            return Ticker;
    }
    public Action getSide() {
        return Side;
}
}
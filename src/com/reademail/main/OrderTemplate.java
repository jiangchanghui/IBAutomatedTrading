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
   
    
   
    int getQuantity() {
            return Quantity;
    }
    String getTicker() {
            return Ticker;
    }
    Action getSide() {
        return Side;
}
}
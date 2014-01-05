package com.web.server;

import javax.jws.WebMethod;
import javax.jws.WebService;

import apidemo.CreateOrderFromEmail;
import apidemo.OrdersPanel.OrdersModel;


import com.ib.sample.main;



@WebService 
public class Index {

	
	
	@WebMethod
	public String getPositions()
	{
		CreateOrderFromEmail C = new CreateOrderFromEmail();
		
		String m_model = C.GetPositions();
		
		
		
		return m_model;
	}
}

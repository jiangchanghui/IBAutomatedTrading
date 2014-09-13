package com.posttrade.main;

import apidemo.util.NewTabbedPanel.INewTab;

import com.ib.controller.ApiController.IAccountHandler;
import com.ib.controller.Position;

public class AccountValueHander implements  IAccountHandler {

	@Override
	public void accountValue(String account, String key, String value,
			String currency) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accountTime(String timeStamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accountDownloadEnd(String account) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatePortfolio(Position position) {
		// TODO Auto-generated method stub
		
	}

}

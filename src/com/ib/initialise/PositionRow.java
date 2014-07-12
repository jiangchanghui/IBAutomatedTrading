package com.ib.initialise;

import com.ib.controller.NewContract;

class PositionRow {
	String m_account;
	NewContract m_contract;
	int m_position;
	double m_avgCost;

	void update(String account, NewContract contract, int position, double avgCost) {
		m_account = account;
		m_contract = contract;
		m_position = position;
		m_avgCost = avgCost;
	}
}
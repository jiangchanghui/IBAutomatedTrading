package com.ib.sample;



class PositionKey {
	String m_account;
	int m_conid;

	PositionKey( String account, int conid) {
		m_account = account;
		m_conid = conid;
	}
	
	@Override public int hashCode() {
		return m_account.hashCode() + m_conid;
	}
	
	@Override public boolean equals(Object obj) {
		PositionKey other = (PositionKey)obj;
		return m_account.equals( other.m_account) && m_conid == other.m_conid;
	}
}
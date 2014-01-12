package com.web.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

public class WebServer {
	
	
	public WebServer() throws UnknownHostException
	{
		 
   
      new Index().start();
      
		
	}
}

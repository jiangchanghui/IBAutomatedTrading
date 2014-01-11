package com.web.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

public class WebServer {
	
	
	public WebServer() throws UnknownHostException
	{
		  
        /* 
           if (!_env.equals("DEV") && !_env.equals("PROD"))
           {
                    S.WriteLog("ERROR : Unknown parameters = "+args);
                    System.exit(-1);
           }
          */ 
         //     S.WriteLog("Starting with params = "+args[0]);
           String IP = InetAddress.getLocalHost().getHostAddress();
           
           if (IP.equals("127.0.1.1"))        IP="192.168.0.6";
           
       //     S.WriteLog("IP Address : "+IP);
            
    //   Endpoint.publish("http://"+IP+":5123/web", new Index());

      new Index().start();
      
		
	}
}

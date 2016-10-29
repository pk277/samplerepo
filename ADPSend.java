package com.cisco.opsdata.misc;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.transport.http.HttpTransportProperties.Authenticator;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.cisco.opsdata.model.changeset.ASFWSStub;

public class ADPSend implements GlobalVariables {

	public static HttpRequestRetryHandler getRetryHandler() {
		//httpclient.setHttpRequestRetryHandler
		HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				return false;
			}
		};
		return myRetryHandler;
	}
	
	public static void sendPayload(String payload, String messageId, String service) throws IOException {
		ASFWSStub asfwstub = null;
		ASFWSStub.Agreement agreement = null;
		
		String ASFWS_URL = DES_LT_URL;  //http://wwwin-tools-lt.cisco.com/bre/services/ASFWS
		
		List<String> list = new ArrayList<String>();
		try {
			agreement = new ASFWSStub.Agreement();
			agreement.setFromPartyID("desx.gen"); // this is party id which help authorization at DES 
			agreement.setToPartyID("desx.gen"); // this is party id which help authorization at DES
			agreement.setService(service); // service Id (74 for ADPEvent, 75 for OPSEvent
			
			agreement.setMessageID(messageId); // any random unique messageId
			
			ASFWSStub.SendMessage sendMessage = new ASFWSStub.SendMessage();
			sendMessage.setMessage(payload);
					
			try {
				System.out.println("Sending message...");
			
				// creating Authenticator object and setting it to SOAPWS Stub
				HttpTransportProperties.Authenticator  auth = new HttpTransportProperties.Authenticator();
				List<String> authSchemes = new ArrayList<String>();
				authSchemes.add(Authenticator.BASIC);
				auth.setAuthSchemes(authSchemes);
				
				// this is AD Gen Account and Password used to invoke ASFWS URL shown above (dev, stage, lt and prod will share same userid
				// however it is recommended to keep separate entries for each life cycle.
				auth.setUsername("imdrol.gen");
				auth.setPassword("intelligentmatch");
				
				auth.setPreemptiveAuthentication(true);
				
				System.out.println("Contract setup URL : "+ASFWS_URL);
				
				asfwstub = new ASFWSStub(ASFWS_URL);
				asfwstub._getServiceClient().getOptions().setAction("soapaction");
				asfwstub._getServiceClient().getOptions().setSoapVersionURI(org.apache.axiom.soap.SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
				asfwstub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, auth);
				asfwstub._getServiceClient().getOptions().setProperty(HTTPConstants.CHUNKED, false);
				
				// invoking the WS Method sendMessage()
				ASFWSStub.SendMessageResponse res = asfwstub.sendMessage(sendMessage, agreement);
	
				System.out.println("Received response for WS client"+res.get_return());
				
				list.add("SUCCESS");
				list.add("Request created successfully");
				
				System.out.println("successfully invoked.." );
			} catch (Exception e) {
				System.out.println("Exception while sending message to webservice : "+e.getMessage());
				e.printStackTrace();
				list.add("UNSUCCESS");
				list.add(e.getMessage());
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
}

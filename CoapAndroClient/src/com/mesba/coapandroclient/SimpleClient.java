package com.mesba.coapandroclient;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.ws4d.coap.Constants;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapClient;
import org.ws4d.coap.interfaces.CoapClientChannel;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapRequestCode;

import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

public class SimpleClient implements CoapClient {

	// This is in the workspace
	private static final String SERVER_ADDRESS = "192.168.0.100";
    private static final int PORT = Constants.COAP_DEFAULT_PORT;

    CoapChannelManager channelManager = null;
    CoapClientChannel clientChannel = null;
    
    private String responseRes = "Waiting for responses";
    
    TextView myTxt;
    
    public void runTestClient(String data, String ipAddress, TextView txt){
    	try {
    		myTxt = txt;
    		
    		//If server IP changes
    		if(ipAddress.matches(""))
    		{
    			ipAddress = SERVER_ADDRESS;
    		}
    		
			clientChannel = channelManager.connect(this, InetAddress.getByName(ipAddress), PORT);
			
			CoapRequest coapRequest = clientChannel.createRequest(true, CoapRequestCode.POST);
			coapRequest.setContentType(CoapMediaType.text_plain);
			coapRequest.setPayload(data);
			
//			coapRequest = clientChannel.createRequest(true, CoapRequestCode.POST);
//			coapRequest.setContentType(CoapMediaType.octet_stream);
//			coapRequest.setContentType(CoapMediaType.text_plain);
//			coapRequest.setToken("CDBA".getBytes());
//			coapRequest.setUriHost("192.168.10.103");
//			coapRequest.setUriPort(5683);
			coapRequest.setUriPath("/devices/");
//			coapRequest.setObserveOption(2);
			//coapRequest.setUriQuery("a=1&b=2&c=3");
//			coapRequest.setProxyUri("http://proxy.org:5683/proxytest");
			
			clientChannel.sendMessage(coapRequest);
			Log.i("Client REQ STAT", "Sent Request...");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }
    
	@Override
	public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
		Log.e("Connection Failed", "For IP: " + channel.getRemoteAddress() + " : " + channel.getRemotePort());
		Log.e("Not Reachable:", "" + notReachable);
		Log.e("Reset By Server:", "" + resetByServer);
	}

	@Override
	public void onResponse(CoapClientChannel channel, CoapResponse response) {
		responseRes = new String(response.getPayload());
//		responseRes = "Received response: " + response.toString() + "; " + responseRes;
    	Log.i("Printing out the Response", responseRes);
    	
    	//Accessing View which is running under UI thread from another Thread
    	myTxt.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				myTxt.setText(responseRes);
				myTxt.setTextColor(Color.BLUE);
			}
		});
	}
	
	

}

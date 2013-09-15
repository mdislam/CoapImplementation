/* Copyright [2011] [University of Rostock]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

package org.ws4d.coap.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.interfaces.CoapServer;
import org.ws4d.coap.interfaces.CoapServerChannel;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapResponseCode;

public class BasicCoapServer implements CoapServer {
	private static final int PORT = 5683;
	static int counter = 0;

	private HashMap<String, DeviceInfo> devices = new HashMap<String, DeviceInfo>();
	private ArrayList<HashMap<String, LocationCollector>> locationsList = new ArrayList<HashMap<String, LocationCollector>>();

	// HashMap<String, DeviceLocation> locationDB = new HashMap();

	public static void main(String[] args) {
		System.out.println("Start CoAP Server on port " + PORT);
		BasicCoapServer server = new BasicCoapServer();

		CoapChannelManager channelManager = BasicCoapChannelManager
				.getInstance();
		channelManager.createServerListener(server, PORT);
	}

	@Override
	public CoapServer onAccept(CoapRequest request) {
		System.out.println("CoAP Server: Accept connection...");
		return this;
	}

	@Override
	public void onRequest(CoapServerChannel channel, CoapRequest request) {
		System.out.println("CoAP Server - CoAP Request: " + request.toString()
				+ " URI: " + request.getUriPath());

		String requestMessage = new String(request.getPayload());
		System.out.println("Received message: " + requestMessage);

		CoapResponse response = null;

		// registration
//		if (request.getUriPath().equals("/devices")) {
//			response = channel.createResponse(request,
//					CoapResponseCode.Content_205);
//			response.setContentType(CoapMediaType.text_plain);
//			// response.setMessageID(1);
//			
//			if(this.RegisterDevices(channel, requestMessage))
//				response.setPayload("REGISTERED :-)".getBytes());
//			else
//				response.setPayload("REGISTRATION FAILURE!".getBytes());
//		}
//		// Store location information of specific device
//		else if (request.getUriPath().startsWith("/location")) {
//			// TODO set devices location in run time memory
//			response = channel.createResponse(request,
//					CoapResponseCode.Content_205);
//			response.setContentType(CoapMediaType.text_plain);
//			// response.setMessageID(2);
//			response.setPayload(StoreLocationData(requestMessage, request.getUriPath()).getBytes());
//			// StoreLocationData(requestMessage, request.getUriPath());
//		}
//		// respond to the GET request of clients
//		else if (request.getUriPath().equals("/getLocations")) {
//			// TODO send all devices locations to the requested client
//			response = channel.createResponse(request,
//					CoapResponseCode.Content_205);
//			response.setContentType(CoapMediaType.json);
//			response.setPayload(GetLocationInfo().getBytes());
//		}
//		else {
//			response = channel.createResponse(request,
//					CoapResponseCode.Content_205);
//			response.setContentType(CoapMediaType.text_plain);
//			// response.setMessageID(2);
//
//			response.setPayload("SUCCESS".getBytes());
//		}
		
		response = channel.createResponse(request,
				CoapResponseCode.Content_205);
		response.setContentType(CoapMediaType.text_plain);
		// response.setMessageID(2);

		response.setPayload("SUCCESS".getBytes());

		channel.sendMessage(response);
	}

	@Override
	public void onSeparateResponseFailed(CoapServerChannel channel) {
		System.out.println("Separate response transmission failed.");

	}
	
	/**
	 * This function will register the device in server
	 * @param channel
	 * @param requestString
	 * @return
	 */
	private boolean RegisterDevices(CoapServerChannel channel, String requestString) {
		String deviceId, sensorName = null;

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(requestString);

			DeviceInfo device = new DeviceInfo();

			deviceId = jsonObject.getString("deviceId");
			sensorName = jsonObject.getString("sensors");

			// check if the device is already registered or not
			if (devices.get(deviceId) != null) {
				System.out.println("Device " + deviceId
						+ " is already registered in the server");
			} else {
				device.setDeviceId(deviceId);
				device.setIpAddress(channel.getRemoteAddress());
				device.setSensorName(sensorName);

				// storing the device information
				this.devices.put(deviceId, device);

				System.out.println("Device " + deviceId
						+ " is registered in the server");
			}
			
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	/**
	 * This function will store the location information of devices
	 * @param locationDataString
	 * @param url
	 * @return
	 */
	private String StoreLocationData(String locationDataString, String url) {
		System.out.println("Message: " + locationDataString);
		HashMap<String, LocationCollector> map = new HashMap<String, LocationCollector>();
		String deviceId = url.split("/")[2];
		// If device isn't registered in the server then ask for registration
		if(IsDeviceDataExists(deviceId) == null)
			return "NOT REGISTERED";
		
		try {
			
			System.out.println("Device ID: " + deviceId);

			JSONObject mainJson = new JSONObject(locationDataString);
			JSONObject sensorReadings = mainJson
					.getJSONObject("sensorReadings");

			String sensorName = sensorReadings.getString("sensor");

			JSONArray readings = sensorReadings.getJSONArray("readings");
			JSONObject singleReading = readings.getJSONObject(0);

			String timeValue = singleReading.getString("time");
			JSONArray values = singleReading.getJSONArray("values");

			double lat = values.getDouble(0);
			double lng = values.getDouble(1);

			System.out.println(sensorName);
			System.out.println(timeValue);
			System.out.println("" + lat);
			System.out.println("" + lng);

			LocationCollector collector = new LocationCollector();

			collector.setDeviceId(deviceId);
			collector.setTimeValue(timeValue);
			collector.setLatitude(lat);
			collector.setLongitude(lng);

			map.put(deviceId, collector);

			locationsList.add(map);

			return "SUCCESS";
		} catch (JSONException e) {
			e.printStackTrace();
			return "FAILED in STORING";
		}
	}

	/**
	 * If client requests for location then server will respond with this function
	 * @return
	 */
	private String GetLocationInfo() {
		try {
			String allLocations = "";

			if (locationsList.size() < 1) {
				JSONObject responseJsnObj = new JSONObject();
				responseJsnObj.put("message", "no data exists!!!");
				allLocations = responseJsnObj.toString();
			} else {
				JSONArray responseJsnArr = new JSONArray();

				for (int index = 0; index < locationsList.size(); index++) {
					Set<?> set = locationsList.get(index).entrySet();
					Iterator<?> i = set.iterator();
					// Display elements
					while (i.hasNext()) {
						Map.Entry me = (Map.Entry) i.next();
						JSONObject locationData = new JSONObject();

						LocationCollector deviceLocations = new LocationCollector();
						deviceLocations = (LocationCollector) me.getValue();

						locationData.put("deviceId", deviceLocations.getDeviceId());
						locationData.put("time", deviceLocations.getTimeValue());
						locationData.put("lattitude", deviceLocations.getLatitude());
						locationData.put("longitude", deviceLocations.getLongitude());

						responseJsnArr.put(locationData);
					}
				}

				allLocations = responseJsnArr.toString();
			}

			return allLocations;
		} catch (Exception exp) {
			exp.printStackTrace();
			return null;
		}
	}

	private DeviceInfo IsDeviceDataExists(String deviceId) {
		
		return devices.get(deviceId);
	}

	/**
	 * This function is responsible for sending subscription to the client
	 * device
	 * 
	 * @param device
	 */
	// private void SendSubscription(DeviceInfo device) {
	// try {
	// Subscriptions s1 = new Subscriptions();
	// s1.setSamplingInterval(5000);
	// s1.setSendingInterval(10000);
	// s1.setSensorName(device.getSensorName(3));
	// s1.setDuration(120000);
	//
	// Subscriptions s2 = new Subscriptions();
	// s2.setSamplingInterval(-1);
	// s2.setSendingInterval(-1);
	// s2.setSensorName(device.getSensorName(2));
	// s2.setDuration(5000);
	//
	// JSONObject subscriptionMessage = new JSONObject();
	// JSONArray subscriptionArray = new JSONArray();
	//
	// subscriptionArray.put(s1.toJSON());
	// subscriptionArray.put(s2.toJSON());
	// subscriptionMessage.put("subscriptions", subscriptionArray);
	//
	// CoapClientChannel clientChannel = this.channelManager.connect(this,
	// device.getIpAddress(), device.getPort());
	//
	// CoapRequest coapRequest = clientChannel.createRequest(true,
	// CoapRequestCode.PUT);
	// coapRequest.setUriPath("/subscriptions");
	// coapRequest.setPayload(subscriptionMessage.toString());
	// clientChannel.sendMessage(coapRequest);
	// }
	//
	// catch (JSONException e) {
	// e.printStackTrace();
	// }
	// }
}

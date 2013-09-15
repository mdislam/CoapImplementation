/**
 * This class is not in work right now
 */

package org.ws4d.coap.server;

import org.json.JSONException;
import org.json.JSONObject;

public class Subscriptions {
	private String sensorName;
	private int samplingInterval;
	private int sendingInterval;
	private int duration;

	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public int getSamplingInterval() {
		return samplingInterval;
	}

	public void setSamplingInterval(int samplingInterval) {
		this.samplingInterval = samplingInterval;
	}

	public int getSendingInterval() {
		return sendingInterval;
	}

	public void setSendingInterval(int sendingInterval) {
		this.sendingInterval = sendingInterval;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public void fromJSON(JSONObject jsonObject) {
		try {
			this.sensorName = jsonObject.getString("sensorName");
			this.samplingInterval = jsonObject.getInt("samplingInterval");
			this.sendingInterval = jsonObject.getInt("sendingInterval");
			this.duration = jsonObject.getInt("duration");
		} catch (JSONException e) {
			e.printStackTrace();
			// Log.e(getClass().getSimpleName(), e.toString());
		}
	}

	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		try {
			ret.put("sensorName", this.sensorName);
			ret.put("samplingInterval", this.samplingInterval);
			ret.put("sendingInterval", this.sendingInterval);
			ret.put("duration", this.duration);
		} catch (JSONException e) {
			e.printStackTrace();
			// Log.e(getClass().getSimpleName(), e.toString());
		}
		return ret;
	}
}

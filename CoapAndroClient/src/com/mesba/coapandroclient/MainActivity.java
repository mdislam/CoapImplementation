package com.mesba.coapandroclient;

import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {
	TextView responseTv;
	CheckBox chk;
	EditText edt;
	String names = "";

	private SensorManager mSensorManager;
	private List<Sensor> msensorList;
	private static String result = "";

	private HashMap<Sensor, TextView> sensorWidgets;
	private LinearLayout layout;

	private TextView mSensorsTot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		responseTv = (TextView) findViewById(R.id.response);
		chk = (CheckBox) findViewById(R.id.checkBox1);
		edt = (EditText) findViewById(R.id.editText1);
		layout = (LinearLayout) findViewById(R.id.main_layout);
		sensorWidgets = new java.util.HashMap<Sensor, TextView>();

		mSensorsTot = (TextView) findViewById(R.id.sensoritot);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// List of Sensors Available
		msensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

		mSensorsTot.setText(msensorList.size() + " Sensors!");
		mSensorsTot.setTextColor(Color.RED);

//		new CountDownTimer(10000, 1000) {
//
//			public void onTick(long millisUntilFinished) {
//				// mSensorsTot.setText("seconds remaining: " +
//				// millisUntilFinished / 1000);
//			}
//
//			public void onFinish() {
//				mSensorsTot.setText(result);
//			}
//		}.start();

		String sSensList = new String("");

		// get the sensor list and set the details information in TextView
		for (int i = 0; i < msensorList.size(); i++) {
			// int sensorType = msensorList.get(i).getType();
			sSensList = "" + msensorList.get(i).getName() + " | MAX: "
					+ msensorList.get(i).getMaximumRange() + " | POWER: "
					+ msensorList.get(i).getPower() + " | RES: "
					+ msensorList.get(i).getResolution() + "\n";
			String[] words = msensorList.get(i).getName().split(" ");

			// this is the name of sensor list which will be sent to server
			names = names + words[words.length - 2].toLowerCase() + ";";

			TextView labelView = new TextView(this);
			labelView.setText(sSensList);
			labelView.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			TextView valueView = new TextView(this);
			valueView.setText("0");
			valueView.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			sensorWidgets.put(msensorList.get(i), valueView);

			layout.addView(labelView);
			layout.addView(valueView);
		}

		final Button sendServer = (Button) findViewById(R.id.sendServer);

		chk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (chk.isChecked()) {
					edt.setEnabled(true);
				} else {
					edt.setEnabled(false);
					edt.setText("");
				}
			}
		});

		sendServer.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.i("Client APP STAT", "Starting Coap Android Client...");
				// ++++++++++++++++++++++++++++++++++++++
				// Using a different Class
				// ++++++++++++++++++++++++++++++++++++++
				SimpleClient client = new SimpleClient();
				client.channelManager = BasicCoapChannelManager.getInstance();

				String data = CreateJsonStrin(names);

				String ipAddress = edt.getText().toString();
				client.runTestClient(data, ipAddress, responseTv);
			}
		});
	}

	/**
	 * This function will return the JSON string
	 * 
	 * @param sensorsName
	 * @return
	 */
	private String CreateJsonStrin(String sensorsName) {
		String data = "";

		String deviceId = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);

		JSONObject json = new JSONObject();
		JSONArray sensorAray = new JSONArray();

		String[] words = sensorsName.split(";");

		for (int i = 0; i < words.length; i++) {
			sensorAray.put(words[i]);
		}

		try {
			json.put("sensors", sensorAray);
			json.put("deviceId", deviceId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		data = json.toString();

		return data;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		String temp = "";

		for (int i = 0; i < event.values.length; i++) {
			temp += event.values[i] + "	";
		}

		// update the sensor reading to the GUI
		result = result + "[-]" + temp;
		sensorWidgets.get(event.sensor).setText(temp);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(getClass().getSimpleName(), "onResume called");

		for (int i = 0; i < msensorList.size(); i++) {
			mSensorManager.registerListener(this, msensorList.get(i), 2000000);
		}
	}
}

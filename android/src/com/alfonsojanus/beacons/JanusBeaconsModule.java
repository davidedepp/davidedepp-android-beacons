/**
 * Copyright 2014 Janus, Inc. All rights reserved.
 * http://www.janus.it
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Alfonso Davide Pilato
 */

package com.alfonsojanus.beacons;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.radiusnetworks.ibeacon.*;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiConvert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

/**
 * Events: enteredRegion, exitedRegion, determinedRegionState, beaconProximity, beaconRanges
 */
@Kroll.module(name="JanusBeacons", id="com.alfonsojanus.beacons")
public class JanusBeaconsModule extends KrollModule implements IBeaconConsumer
{

	private static IBeaconManager iBeaconManager;
	private static BeaconTransmitter beaconTransmitter;
	private static Timer timer;
	boolean oldStatusBluethoot;
	boolean newStatusBluethoot;
	boolean firstTimeChangeStatusBluethoot = true;
	// Standard Debugging variables
	private static final String TAG = "JanusBeaconsModule";

	private boolean autoRange = true;

	public JanusBeaconsModule() {
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(TAG, "onAppCreate: Janus Android Beacons 1.0.0");

		iBeaconManager = IBeaconManager.getInstanceForApplication(app);

		// set some less battery-intensive settings compared to the defaults
		//iBeaconManager.setForegroundScanPeriod(1200);
		//iBeaconManager.setForegroundBetweenScanPeriod(2300);
		//iBeaconManager.setBackgroundScanPeriod(10000);
		//iBeaconManager.setBackgroundBetweenScanPeriod(60 * 1000);
		try {
		   iBeaconManager.setForegroundScanPeriod(1100l); // 1100 mS
		   iBeaconManager.setForegroundBetweenScanPeriod(0l); // 0ms
		   iBeaconManager.setBackgroundScanPeriod(10000); // 10s
		   iBeaconManager.setBackgroundBetweenScanPeriod(60000); // 60s
		   iBeaconManager.updateScanPeriods();
		}
		catch (RemoteException e) {
		   Log.d(TAG, "Cannot talk to service");
		}

		// to see debugging from the Radius networks lib, set this to true
		IBeaconManager.LOG_DEBUG = false;
	}

	/**
	 * See if Bluetooth 4.0 & LE is available on device
	 *
	 * @return true if iBeacons can be used, false otherwise
	 */
	@Kroll.method
	public boolean checkAvailability() {
		try {
			return iBeaconManager.checkAvailability();
		} catch (Exception ex) {
			return false;
		}
	}
	
	/**
	 * bluetoothStatus: fire event if status changed
	 *
	 * @return status on if Bluethoot is on, off otherwise
	 */
	@Kroll.method
	public void requestBluetoothStatus(){		
		// your code goes here
		if(timer != null){
			timer.cancel();
       		timer.purge();	
		}
		timer = new Timer();
    	TimerTask task = new TimerTask() {
        	public void run()
        	{
        		newStatusBluethoot = checkAvailability();
        		if(firstTimeChangeStatusBluethoot){// solo la prima volta
        			oldStatusBluethoot = !newStatusBluethoot;
        			firstTimeChangeStatusBluethoot = !firstTimeChangeStatusBluethoot;
        		}
        		if(newStatusBluethoot != oldStatusBluethoot){
	        		KrollDict e = new KrollDict();
	        		if(checkAvailability())
	        			e.put("status","on");
	        		else 
	        			e.put("status","off");
	        		fireEvent("bluetoothStatus", e);
	        		oldStatusBluethoot = newStatusBluethoot;
        		}
        	}
    	};	
		timer.scheduleAtFixedRate(task, new Date(), 3000l);		
	}
	

	/**
	 * Throttles down iBeacon library when app placed in background (but you have to
	 * detect this yourself, this module does not know when apps are put in background).
	 *
	 * @param flag Whether to enable background mode or not.
	 */
	@Kroll.method
	public void setBackgroundMode(boolean flag)
	{
		Log.d(TAG, "setBackgroundMode: " + flag);

		if (!checkAvailability()) {
			Log.d(TAG, "Bluetooth LE not available or no permissions on this device");
			return;
		}
		iBeaconManager.setBackgroundMode(this, flag);


	}

	/**
	 * Turns on auto ranging. When auto ranging is on, upon entering a region, this
	 * module will automatically begin ranging for beacons within that region, and
	 * stop ranging for beacons when the region is exited. Note ranging requires more
	 * battery power so care should be taken with this setting.
	 */
	@Kroll.method
	public void enableAutoRanging()
	{
		setAutoRange(true);
	}

	/**
	 * Turns off auto ranging. See description of enableAutoRanging for more details.
	 *
	 * @see #enableAutoRanging()
	 */
	@Kroll.method
	public void disableAutoRanging()
	{
		setAutoRange(false);
	}

	/**
	 * Turns auto ranging on or off. See description of enableAutoRanging for more details.
	 *
	 * @param autoRange if true, turns on auto ranging. Otherwise, turns it off.
	 *
	 * @see #enableAutoRanging()
	 *
	 */
	@Kroll.method
	public void setAutoRange(boolean autoRange)
	{
		Log.d(TAG, "setAutoRange: " + autoRange);
		this.autoRange = autoRange;

	}

	/**
	 * Set the scan periods for the bluetooth scanner.
	 *
	 * @param scanPeriods the scan periods.
	 */
	@Kroll.method
	public void setScanPeriods(Object scanPeriods)
	{

		Log.d(TAG, "setScanPeriods: " + scanPeriods);

		HashMap<String, Object> dict = (HashMap<String, Object>)scanPeriods;

		int foregroundScanPeriod = TiConvert.toInt(dict, "foregroundScanPeriod");
		int foregroundBetweenScanPeriod = TiConvert.toInt(dict, "foregroundBetweenScanPeriod");
		int backgroundScanPeriod = TiConvert.toInt(dict, "backgroundScanPeriod");
		int backgroundBetweenScanPeriod = TiConvert.toInt(dict, "backgroundBetweenScanPeriod");

		iBeaconManager.setForegroundScanPeriod(foregroundScanPeriod);
		iBeaconManager.setForegroundBetweenScanPeriod(foregroundBetweenScanPeriod);
		iBeaconManager.setBackgroundScanPeriod(backgroundScanPeriod);
		iBeaconManager.setBackgroundBetweenScanPeriod(backgroundBetweenScanPeriod);
	}

	/**
	 * Start monitoring a region.
	 * @param region the region to monitor, expected to be a property dictionary from javascript code.
	 */
	@Kroll.method
	public void startMonitoringForRegion(Object region)
	{
		Log.d(TAG, "startMonitoringForRegion: " + region);

		if (!checkAvailability()) {
			Log.d(TAG, "Bluetooth LE not available or no permissions on this device");
			return;
		}
		try {
			HashMap<String, Object> dict = (HashMap<String, Object>)region;

			String identifier = TiConvert.toString(dict, "identifier");
			String uuid = TiConvert.toString(dict, "uuid").toLowerCase();
			Integer major = (dict.get("major") != null) ? TiConvert.toInt(dict, "major") : null;
			Integer minor = (dict.get("minor") != null) ? TiConvert.toInt(dict, "minor") : null;

			Region r = new Region(identifier, uuid, major, minor);

			Log.d(TAG, "Beginning to monitor region " + r);
			iBeaconManager.startMonitoringBeaconsInRegion(r);
		} catch (RemoteException ex) {
			Log.e(TAG, "Cannot start monitoring region " + TiConvert.toString(region, "identifier"), ex);
		}
	}


	/**
	 * Compatibility method for popular iOS FOSS iBeacon library.
	 *
	 * @see #startRangingForRegion(Object)
	 *
	 * @param region the region to range, expected to be a property dictionary from javascript code.
	 */
	@Kroll.method
	public void startRangingForBeacons(Object region) {
		startRangingForRegion(region);
	}

	/**
	 * Start ranging a region. You can only range regions into which you have entered.
	 *
	 * @param region the region to range, expected to be a property dictionary from javascript code.
	 */
	@Kroll.method
	public void startRangingForRegion(Object region)
	{
		Log.d(TAG, "startRangingForRegion: " + region);

		if (!checkAvailability()) {
			Log.d(TAG, "Bluetooth LE not available or no permissions on this device");
			return;
		}
		try {
			HashMap<String, Object> dict = (HashMap<String, Object>)region;

			String identifier = TiConvert.toString(dict, "identifier");
			String uuid = TiConvert.toString(dict, "uuid").toLowerCase();
			Integer major = (dict.get("major") != null) ? TiConvert.toInt(dict, "major") : null;
			Integer minor = (dict.get("minor") != null) ? TiConvert.toInt(dict, "minor") : null;

			Region r = new Region(identifier, uuid, major, minor);

			Log.d(TAG, "Beginning to monitor region " + r);
			iBeaconManager.startRangingBeaconsInRegion(r);
		} catch (RemoteException ex) {
			Log.e(TAG, "Cannot start ranging region " + TiConvert.toString(region, "identifier"), ex);
		}
	}


	/**
	 * Stop monitoring everything.
	 */
	@Kroll.method
	public void stopMonitoringAllRegions()
	{

		Log.d(TAG, "stopMonitoringAllRegions");

		for (Region r : iBeaconManager.getMonitoredRegions()) {
			try {
				iBeaconManager.stopMonitoringBeaconsInRegion(r);
				Log.d(TAG, "Stopped monitoring region " + r);
			} catch (RemoteException ex) {
				Log.e(TAG, "Cannot stop monitoring region " + r.getUniqueId(), ex);
			}
		}

	}

	/**
	 * Stop ranging for everything.
	 */
	@Kroll.method
	public void stopRangingForAllBeacons()
	{

		Log.d(TAG, "stopRangingForAllBeacons");

		for (Region r : iBeaconManager.getRangedRegions()) {
			try {
				iBeaconManager.stopRangingBeaconsInRegion(r);
				Log.d(TAG, "Stopped ranging region " + r);
			} catch (RemoteException ex) {
				Log.e(TAG, "Cannot stop ranging region " + r.getUniqueId(), ex);
			}
		}
	}


	@Override
	public void onStart(Activity activity)
	{
		// This method is called when the module is loaded and the root context is started

		Log.d(TAG, "[MODULE LIFECYCLE EVENT] start");
		iBeaconManager.bind(this);

		super.onStart(activity);
	}

	@Override
	public void onStop(Activity activity)
	{
		// This method is called when the root context is stopped

		Log.d(TAG, "[MODULE LIFECYCLE EVENT] stop");

		if (!iBeaconManager.isBound(this)) {
			iBeaconManager.bind(this);
		}
		super.onStop(activity);
	}

	@Override
	public void onPause(Activity activity)
	{
		// This method is called when the root context is being suspended

		Log.d(TAG, "[MODULE LIFECYCLE EVENT] pause");
		if (!iBeaconManager.isBound(this)) {
			iBeaconManager.bind(this);
		}

		super.onPause(activity);
	}

	@Override
	public void onResume(Activity activity)
	{
		// This method is called when the root context is being resumed

		Log.d(TAG, "[MODULE LIFECYCLE EVENT] resume");
		if (!iBeaconManager.isBound(this)) {
			iBeaconManager.bind(this);
		}

		super.onResume(activity);
	}

	@Override
	public void onDestroy(Activity activity)
	{
		// This method is called when the root context is being destroyed

		Log.d(TAG, "[MODULE LIFECYCLE EVENT] destroy");
		iBeaconManager.unBind(this);

		super.onDestroy(activity);
	}

	public void onIBeaconServiceConnect() {

		Log.d(TAG, "onIBeaconServiceConnect");
		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {

			public void didEnterRegion(Region region) {

				Log.d(TAG, "Entered region: " + region);

				try {
					if (autoRange) {
						Log.d(TAG, "Beginning to autoRange region " + region);
						iBeaconManager.startRangingBeaconsInRegion(region);
					}
					KrollDict e = new KrollDict();
					e.put("identifier", region.getUniqueId());
					fireEvent("enteredRegion", e);
				} catch (RemoteException ex) {
					Log.e(TAG, "Cannot turn on ranging for region " + region.getUniqueId(), ex);
				}
			}

			public void didExitRegion(Region region) {

				Log.d(TAG, "Exited region: " + region);

				try {
					iBeaconManager.stopRangingBeaconsInRegion(region);
					KrollDict e = new KrollDict();
					e.put("identifier", region.getUniqueId());
					fireEvent("exitedRegion", e);
				} catch (RemoteException ex) {
					Log.e(TAG, "Cannot turn off ranging for region " + region.getUniqueId(), ex);
				}
			}

			public void didDetermineStateForRegion(int state, Region region) {
				if (state == INSIDE) {
					try {
						if (autoRange) {
							Log.d(TAG, "Beginning to autoRange region " + region);
							iBeaconManager.startRangingBeaconsInRegion(region);
						}
						KrollDict e = new KrollDict();
						e.put("identifier", region.getUniqueId());
						e.put("regionState", "inside");
						fireEvent("determinedRegionState", e);
					} catch (RemoteException e) {
						Log.e(TAG, "Cannot turn on ranging for region during didDetermineState" + region);
					}
				} else if (state == OUTSIDE) {
					try {
						iBeaconManager.stopRangingBeaconsInRegion(region);
						KrollDict e = new KrollDict();
						e.put("identifier", region.getUniqueId());
						e.put("regionState", "outside");
						fireEvent("determinedRegionState", e);
					} catch (RemoteException e) {
						Log.e(TAG, "Cannot turn off ranging for region during didDetermineState" + region);
					}
				} else {
					Log.d(TAG, "Unknown region state: " + state + " for region: " + region);
				}

			}
		});

		iBeaconManager.setRangeNotifier(new RangeNotifier() {
			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
				KrollDict evt = new KrollDict();
				Gson gson = new Gson();
		        String jsoniBeacons = gson.toJson(iBeacons);
		        evt.put("jsoniBeacons", jsoniBeacons);
				fireEvent("beaconRanges", evt);

				for (IBeacon beacon : iBeacons) { //handler beaconProximity
					// identifier, uuid,major,minor,proximity,fromProximity,accuracy,rssi
					KrollDict e = new KrollDict();
					e.put("identifier", region.getUniqueId());
					e.put("uuid", beacon.getProximityUuid());
					e.put("major", beacon.getMajor());
					e.put("minor", beacon.getMinor());
					e.put("proximity", getProximityName(beacon.getProximity()));
					e.put("accuracy", beacon.getAccuracy());
					e.put("rssi", beacon.getRssi());
					e.put("power", beacon.getTxPower());
					fireEvent("beaconProximity", e);
				}
			}

		});
	}
	/**
	 * Create New beacon startAdvertising.
	 */
	@Kroll.method
	public boolean startAdvertisingBeacons(Object region)
	{
		Log.d(TAG, "startAdvertising: " + region);
		int checkSupports  = BeaconTransmitter.checkTransmissionSupported(TiApplication.getInstance().getApplicationContext());
		if(checkSupports == BeaconTransmitter.SUPPORTED){
			HashMap<String, Object> dict = (HashMap<String, Object>)region;
			
			String identifier = TiConvert.toString(dict, "identifier");
			Log.d(TAG, "identifier: " + identifier);
			String uuid = TiConvert.toString(dict, "uuid").toLowerCase();
			String major = (dict.get("major") != null) ? TiConvert.toString(dict, "major") : null;
			String minor = (dict.get("minor") != null) ? TiConvert.toString(dict, "minor") : null;
			
			Beacon beacon = new Beacon.Builder()
	        .setId1(uuid) //"2f234454-cf6d-4a0f-adf2-f4911ba9ffa6"
	        .setId2(major) //1
	        .setId3(minor) //2
	        .setManufacturer(0x0118) //Radius Networks, Inc. https://www.bluetooth.org/en-us/specification/assigned-numbers/company-identifiers
	        .setTxPower(-59)
	        .setDataFields(Arrays.asList(new Long[] {0l}))
	        .build();
			BeaconParser beaconParser = new BeaconParser()
	        	.setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
			beaconTransmitter = new BeaconTransmitter(TiApplication.getInstance().getApplicationContext(), beaconParser); 
			beaconTransmitter.startAdvertising(beacon);
			return true;
		}else{
			Log.d(TAG, "Beacon Transmitter required Android 5.0 and Bluethoot 4.1" + checkSupports);
			return false;
		}
	}
	
	/**
	 * Stop New beacon stopAdvertising.
	 */
	@Kroll.method
	public void stopAdvertisingBeacons()
	{	
		beaconTransmitter.stopAdvertising();
	}
	
    // methods compare different version 
	public static int compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        int cmp = s1.compareTo(s2);
        return cmp;
    }
    
	public static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }
    
    public static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }
	
    // methods getProximityName
	public static String getProximityName(int p) {
		switch (p) {
			case IBeacon.PROXIMITY_FAR:
				return "far";
			case IBeacon.PROXIMITY_IMMEDIATE:
				return "immediate";
			case IBeacon.PROXIMITY_NEAR:
				return "near";
			default:
				return "unknown";
		}
	}


	// methods to bind and unbind
	public Context getApplicationContext() {
		return super.getActivity().getApplicationContext();
	}

	public void unbindService(ServiceConnection serviceConnection) {
		Log.d(TAG, "unbindService");
		super.getActivity().unbindService(serviceConnection);
	}

	public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
		Log.d(TAG, "bindService");
		return super.getActivity().bindService(intent, serviceConnection, i);
	}
}


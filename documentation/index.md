# janusbeacons Module

## Usage

Place the ZIP file into your project's root directory, and declare the module and required android permissions in your `tiapp.xml` file (or in your custom `platform/android/AndroidManifest.xml` file if you are using that):

	<ti:app>
		...
		<android xmlns:android="http://schemas.android.com/apk/res/android">
			<manifest package="[YOUR_APP_PACKAGE_NAME]">
				<uses-sdk	android:minSdkVersion="10"
							android:targetSdkVersion="18"/>
				<uses-permission
					android:name="android.permission.BLUETOOTH"/>
				<uses-permission
					android:name="android.permission.BLUETOOTH_ADMIN"/>
				<application>
					<service	android:enabled="true"
								android:exported="true"
								android:isolatedProcess="false"
								android:label="iBeacon"
								android:name="com.radiusnetworks.ibeacon.service.IBeaconService">
					</service>
					<service	android:enabled="true"
								android:name="com.radiusnetworks.ibeacon.IBeaconIntentProcessor">
								<meta-data android:name="background" android:value="true" />
						<intent-filter
							android:priority="1" >
							<action android:name="[YOUR_APP_PACKAGE_NAME].DID_RANGING"/>
							<action android:name="[YOUR_APP_PACKAGE_NAME].DID_MONITORING"/>
						</intent-filter>
					</service>
				</application>
			</manifest>
		</android>
		...
		<modules>
			<module platform="android">com.alfonsojanus.beacons</module>
		</modules>
		...
	</ti:app>

Don't forget to replace the `[YOUR_APP_PACKAGE_NAME]` with your app's package name, e.g. *com.companyname.app*, and you can read [Radius Networks' docs](http://altbeacon.github.io/android-beacon-library/configure.html) on this topic as well.

Become an iBeacon: (only Nexus 9, Nexus 6, Samsung SM-G900F e SM-G900V)

	var janusbeacons = require("com.alfonsojanus.beacons");

	TiBeacons.addEventListener("advertisingStatus", function(event) {
    	Ti.API.info(event.status);
	});

	TiBeacons.startAdvertisingBeacon({
   		uuid : "00000000-0000-0000-0000-000000000000",
   		identifier : "TiBeacon Test",
   		major: 1,
   		minor: 2
	});

Start monitoring for iBeacons in one or more regions. 

	TiBeacons.startMonitoringForRegion({
    	uuid : "00000000-0000-0000-0000-000000000000",
   	 	identifier : "Test Region 1",
	});

	TiBeacons.startMonitoringForRegion({
    	uuid : "00000000-0000-0000-0000-000000000001",
    	identifier : "Test Region 2 (group-specific)",
    	major: 1
	});

	TiBeacons.startMonitoringForRegion({
    	uuid : "00000000-0000-0000-0000-000000000002",
   	 	identifier : "Test Region 3 (device-specific)",
    	major: 1,
   		minor: 2
	});
	
Listen for region events:

	TiBeacons.addEventListener("enteredRegion", alert);
	TiBeacons.addEventListener("exitedRegion", alert);
	TiBeacons.addEventListener("determinedRegionState", alert);
	
Decide whether you want auto-ranging, and turn it on via `TiBeacons.setAutoRange(true)` if you want it, or `TiBeacons.setAutoRange(false)` if not.	

Start ranging beacons in a region. This takes takes more energy and will report the approximate distance of the device to the beacon.

	TiBeacons.startRangingForBeacons({
    	uuid : "00000000-0000-0000-0000-000000000002",
    	identifier : "Test Region",
 	   	major: 1, //optional
   	 	minor: 2 //optional
	});

Listen for the range events:

	TiBeacons.addEventListener("beaconRanges", function(event) {
  	 	alert(event.beacons);
	});

Or just listen for beacon proximity changes:

	TiBeacons.addEventListener("beaconProximity", function(e){
   		alert("beacon "+e.major+"/"+e.minor+" is now "+e.proximity);
	});
	
Stopping monitoring/ranging

To turn everything off:

    TiBeacons.stopRangingForAllBeacons();
    TiBeacons.stopMonitoringAllRegions();


## Hardware Status

Find out if bluetooth is on or off (or unauthorized or unsupported or resetting):

	TiBeacons.addEventListener("bluetoothStatus", function(e){
  	 	if (e.status != "on") {
      		Ti.API.error("bluetooth is not on");
   		}
	});

	TiBeacons.requestBluetoothStatus();

## Author

* Alfonso Davide Pilato (Janus Company Software)
* `alfonso.pilato@gmail.com`
* [`@davidedepp`](http://twitter.com/davidedepp)

## License

Copyright (c) 2014, Alfonso Davide Pilato Inc. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Notice

This product includes software developed at
[The Radius Networks](http://www.radiusnetworks.com/) (http://www.radiusnetworks.com/).

Android IBeacon Service

Copyright 2013 Radius Networks

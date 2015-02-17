# janusbeacons Module

## Usage

See this example app for usage: TiBeacons Example App

Become an iBeacon:

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


## Permission and Hardware Status

Find out if bluetooth is on or off (or unauthorized or unsupported or resetting):

	TiBeacons.addEventListener("bluetoothStatus", function(e){
  	 	if (e.status != "on") {
      		Ti.API.error("bluetooth is not on");
   		}
	});

	TiBeacons.requestBluetoothStatus();

## Author

TODO: Enter your author name, email and other contact
details you want to share here.

## License

TODO: Enter your license/legal information here.

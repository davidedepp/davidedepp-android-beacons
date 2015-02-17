var win = Ti.UI.createWindow({backgroundColor:'white'});
var label = Ti.UI.createLabel({color:'#000000',text:"",top: 100,width: Ti.UI.SIZE, height: Ti.UI.SIZE});
win.add(label);
var buttonStop = Titanium.UI.createButton({title: 'Stop Beacon',top: 150,width: Ti.UI.SIZE, height: Ti.UI.SIZE});
win.add(buttonStop);
var enter = true;
var minBeacon = null;
var MAXPROXIMITY = 1.5;
var TiBeacons = require('com.alfonsojanus.beacons');

Titanium.API.info("requestBluetoothStatus");
TiBeacons.addEventListener('bluetoothStatus', bluetoothStatus);
TiBeacons.requestBluetoothStatus();

buttonStop.addEventListener('click',function(e){
	Titanium.API.info("Stop Beacon");
	TiBeacons.stopMonitoringAllRegions();
	removeListeners();
});
win.open();

/*Beacon*/
function beaconStart(){
	TiBeacons.setAutoRange(true); // è come se si attivase un startRangingForBeacons
	TiBeacons.startMonitoringForRegion({
	    uuid : "B9407F30-F5F8-466E-AFF9-25556B57FE6D",
	    identifier : "Estimote"
	});
	addListeners();
}
function bluetoothStatus(e){
	if(e.status == "off"){
		alert("spento");
		Ti.API.info("Devi attivare il Bluethoot");
	}else if(e.status == "on" && enter){
		setTimeout(function() {// prima di attivare la ricerca dei beacon in Androdid l'ActivityView deve essere già istanziata altrimenti va in crash 
			beaconStart();
		}, 500);
		enter =  !enter;
	}
}
function handleProximity(e){
	//Ti.API.info("handleProximity"+ JSON.stringify(e));
}

function updateRanges(e) {
	var jsonObj = JSON.parse(e.jsoniBeacons);
	//var minValueProximity = Math.min.apply(Math,jsonObj.map(function(o){return o.proximity;}));
	Ti.API.info("jsoniBeacons: "+ JSON.stringify(e.jsoniBeacons));
	for(var i=0;i<jsonObj.length;i++){ //Titanium.API.info(i +": "+ jsonObj[i].minor);
		if(jsonObj[0].accuracy > -1 && jsonObj[0].accuracy < MAXPROXIMITY){
			minBeacon = jsonObj[0].minor;
		}else if(jsonObj[0].accuracy > -1 && jsonObj[0].accuracy > MAXPROXIMITY){
			minBeacon = null;
		}
	}
	if (minBeacon != null) {
		label.text = minBeacon + " " + randomString(5);
	}
	//Ti.API.info("updateRanges"+ JSON.stringify(e.jsoniBeacons));
	//var myNewObject = JSON.parse(e.jsoniBeacons);
	//Titanium.API.info("minor: "+myNewObject[0].minor);
	//Titanium.API.info("accuracy: "+myNewObject[0].accuracy);
	//Titanium.API.info("updateRangesmyNewObject:"+myNewObject);
	/*Titanium.API.info("updateRangesmyNewObjectlength:"+myNewObject.length);
	Titanium.API.info("updateRangesmyNewObject:"+myNewObject[0]);
	Titanium.API.info("updateRangesminor:"+myNewObject[0].minor);*/
}
function enterRegion(e) {
	Ti.API.info("exitRegion"+ JSON.stringify(e));//JSON.stringify(e)
}
function exitRegion(e) {
	Ti.API.info("exitRegion"+ JSON.stringify(e));//JSON.stringify(e)
}
function determinedRegionState(e){
	Ti.API.info('determinedRegionState' + JSON.stringify(e));
}
function addListeners() {
	TiBeacons.addEventListener("determinedRegionState", determinedRegionState);
	
	TiBeacons.addEventListener("enteredRegion", enterRegion);
	TiBeacons.addEventListener("exitedRegion", exitRegion);
	
	TiBeacons.addEventListener("beaconRanges", updateRanges);
	TiBeacons.addEventListener("beaconProximity", handleProximity);	
}
function removeListeners() {
	TiBeacons.removeEventListener("determinedRegionState", determinedRegionState);
	
	TiBeacons.removeEventListener("enteredRegion", enterRegion);
	TiBeacons.removeEventListener("exitedRegion", exitRegion);
	
	TiBeacons.removeEventListener("beaconRanges", updateRanges);
	TiBeacons.removeEventListener("beaconProximity", handleProximity);
}
function randomString(length) {
    var result = '';
    var chars = 'abcdefghijklmnopqrstuvwxyz';
    for (var i = length; i > 0; --i) result += chars[Math.round(Math.random() * (chars.length - 1))];
    return result;
}

/*
 * Device how beacon only Nexus 9, Nexus 6, Samsung SM-G900F e SM-G900V
 	var isStarted = TiBeacons.startAdvertisingBeacons({
	    uuid : "B9407F30-F5F8-466E-AFF9-25556B57FE6D",
	    identifier : "Estimote",
	    major:1,
	    minor:2
	});
	Titanium.API.info("Start How Beacon: "+isStarted);
*/

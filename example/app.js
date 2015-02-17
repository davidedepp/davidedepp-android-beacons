// This is a test harness for your module
// You should do something interesting in this harness
// to test out the module and to provide instructions
// to users on how to use it by example.

var win = Ti.UI.createWindow({
	backgroundColor:'white'
});
var label = Ti.UI.createLabel();
win.add(label);
win.open();

if (Ti.Platform.name == "android") {
    var mod = require('com.alfonsojanus.beacons');
    label.text = "module is => " + mod + "and checkAvailability says: " + mod.checkAvailability();
} else {
    label.text = "alfonsojanus.beacons not supported on " + Ti.Platform.name;
}


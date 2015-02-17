/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

/** This is generated, do not edit by hand. **/

#include <jni.h>

#include "Proxy.h"

		namespace com {
		namespace alfonsojanus {
		namespace beacons {


class JanusBeaconsModule : public titanium::Proxy
{
public:
	explicit JanusBeaconsModule(jobject javaObject);

	static void bindProxy(v8::Handle<v8::Object> exports);
	static v8::Handle<v8::FunctionTemplate> getProxyTemplate();
	static void dispose();

	static v8::Persistent<v8::FunctionTemplate> proxyTemplate;
	static jclass javaClass;

private:
	// Methods -----------------------------------------------------------
	static v8::Handle<v8::Value> stopRangingForAllBeacons(const v8::Arguments&);
	static v8::Handle<v8::Value> setAutoRange(const v8::Arguments&);
	static v8::Handle<v8::Value> startRangingForBeacons(const v8::Arguments&);
	static v8::Handle<v8::Value> disableAutoRanging(const v8::Arguments&);
	static v8::Handle<v8::Value> enableAutoRanging(const v8::Arguments&);
	static v8::Handle<v8::Value> startMonitoringForRegion(const v8::Arguments&);
	static v8::Handle<v8::Value> requestBluetoothStatus(const v8::Arguments&);
	static v8::Handle<v8::Value> stopMonitoringAllRegions(const v8::Arguments&);
	static v8::Handle<v8::Value> startAdvertisingBeacons(const v8::Arguments&);
	static v8::Handle<v8::Value> startRangingForRegion(const v8::Arguments&);
	static v8::Handle<v8::Value> setScanPeriods(const v8::Arguments&);
	static v8::Handle<v8::Value> stopAdvertisingBeacons(const v8::Arguments&);
	static v8::Handle<v8::Value> setBackgroundMode(const v8::Arguments&);
	static v8::Handle<v8::Value> checkAvailability(const v8::Arguments&);

	// Dynamic property accessors ----------------------------------------

};

		} // beacons
		} // alfonsojanus
		} // com

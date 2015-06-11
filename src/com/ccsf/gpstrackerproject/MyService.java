package com.ccsf.gpstrackerproject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

public class MyService extends Service implements LocationListener
{
	private LocationManager locationManager;
	// flag for GPS status
	boolean isGPSEnabled = false;
	// flag for GPS status
	boolean canGetLocation = false;
	// location
	Location location;
	// latitude
	double latitude; 
	// longitude
	double longitude; 
	// altitude
	double altitude;
	String locationResults = "";
	// The minimum distance to change Updates in meters, 1 meter
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
	// The minimum time between updates in milliseconds, 1 seconds
	private static final long MIN_TIME_BW_UPDATES = 1 * 30 * 1000;
	Toast myToast;
	
	SharedPreferences pref;
	Editor edit;
	
//	int numOfLocRequest = 0;
	
	// last appended latitude, use for compare distance
//	double laLatitude;
	double lastLatitude = 0.0;
	// last appended longitude, use for compare distance
//	double laLongitude;
	double lastLongitude = 0.0;
//	String[] tmpArray;
	private static final double MIN_DISTANCE_TO_APPEND = 0;	//100 meter
	double subDistance = 0.0;
	double lastSubDistance = 0.0;
	String timeStampStr = "";
	String lastTimeStampStr = "";
	long subTimeSpent = 0;
	double subSpeed = 0.0;
	
	double totalDistance = 0.0;
	long totalTimeSpent = 0;
	double averageSpeed = 0.0;
	double totalSubSpeed = 0.0;
	int subSpeedCounter = 0;
	
	int numOfPlaceMark = 0;
	
	@Override
    public IBinder onBind(Intent intent)
	{
        return null;
    }
 
	// next time init stuffs in here
    @Override
    public void onCreate()
    {
    	Toast.makeText(this, "MyService Created ", Toast.LENGTH_SHORT).show();
    	pref = getSharedPreferences("prefName", Context.MODE_PRIVATE);
    	edit = pref.edit();
    }
 
//    @Override
//    public void onStart(Intent intent, int startId)
//    {
//        Toast.makeText(this, "MyService Started", Toast.LENGTH_LONG).show();
//    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
    	getLocation();
    	Toast.makeText(this, "MyService Started", Toast.LENGTH_SHORT).show();
    	return START_STICKY;
    }
 
    @Override
    public void onDestroy()
    {
        Toast.makeText(this, "Servics Stopped", Toast.LENGTH_SHORT).show();
        locationResults = "";
        edit.putString("longLat", locationResults);
//		edit.apply();
        canGetLocation = false;
//        numOfLocRequest = 0;
        lastLatitude = 0.0;
        lastLongitude = 0.0;
        subDistance = 0.0;
    	lastSubDistance = 0.0;
    	timeStampStr = "";
    	lastTimeStampStr = "";
    	subTimeSpent = 0;
    	subSpeed = 0.0;
    	totalDistance = 0.0;
    	edit.putString("totalDistance", ""+totalDistance);
//		edit.apply();
    	totalTimeSpent = 0;
    	edit.putString("totalTimeSpent", ""+totalTimeSpent);
//		edit.apply();
    	averageSpeed = 0.0;
    	edit.putString("averageSpeed", ""+averageSpeed);
		edit.apply();
    	totalSubSpeed = 0.0;
    	subSpeedCounter = 0;
        super.onDestroy();
    }
    
    public Location getLocation()
	{
		try
		{
	    	System.out.println("in getLocation() in try");
	    	
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			// getting GPS status, don't need to check network, this app don't use data
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			
	    	System.out.println("in getLocation() still in try");
	    	
			if (!isGPSEnabled)
			{
				// GPS provider is disabled
				System.out.println("GPS provider is disabled");
			}
			else
			{
				System.out.println("GPS provider is enabled");
		    	
				canGetLocation = true;

				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
						MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				if (locationManager != null)
				{
					System.out.println("locationManager != null");
					
					location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if (location != null)
					{
						System.out.println("location != null");
						
						latitude = location.getLatitude();
						longitude = location.getLongitude();
						altitude = location.getAltitude();
						
						appendToResult();
					}
					else
					{
						System.out.println("location == null");
					}
				}
				else
				{
					System.out.println("locationManager == null");
				}
			}
		}
		catch (Exception e)
		{
			Toast.makeText(this, "in getLocation() in Exception", Toast.LENGTH_SHORT).show();
	    	System.out.println("in getLocation() in Exception");
			e.printStackTrace();
		}
		return location;
	}
	
	@Override
	public void onLocationChanged(Location location)
	{
		// onLocationChanged only start working while the service start and GPS enabled
		if(canGetLocation)
		{
			System.out.println("in onLocationChanged() can Get Location");
			
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			altitude = location.getAltitude();
			
			appendToResult();
		}
		else
		{
			System.out.println("in onLocationChanged() can not Get Location");
		}
	}
	
	/**
	 * count number of line from string base on ";", start from 0
	 * @param string
	 * @return int
	 */
	private static int countLines(String str)
	{
		String[] lines = str.split(";");
		return lines.length;
	}
	
	/**
	 * this method is to limit the amount of location append to the result string by check distance,
	 * append location to result string the first time without any checking,
	 * after result string has one or more location,
	 * get last appended location and compare to new location, to get distance difference,
	 * if distance difference more than specify minimum distance, append to result
	 */
	public void appendToResult()
	{
		// !!! just testing, later need to change >= 0 to == 0 !!!
		// get the first location without compare distance
		if(locationResults.length() == 0)
		{
			System.out.println("mytab: get the first location");
			
			// I count line before the first time append to a empty string, so 0 + 1.
//			numOfLocRequest = countLines(locationResults) + 1;
		
			timeStampStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
			locationResults = locationResults + "m 0(mps) 0(m) " 
					+ timeStampStr + "/desc./" 
					+ longitude + "," + latitude + "," + altitude + ";";
//			locationResults = locationResults + "m 0(km) " 
//					+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) +"/desc./" 
//					+ longitude + "," + latitude + "," + altitude + ";";
			
			numOfPlaceMark = countLines(locationResults);
			
			lastLongitude = longitude;
			lastLatitude = latitude;
			lastTimeStampStr = timeStampStr;
			
			myToast = Toast.makeText(getBaseContext(), "New Lo/La: " + latitude + ", " + longitude, Toast.LENGTH_SHORT);
			myToast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
			myToast.show();
			System.out.println(locationResults);
			
			edit.putString("longLat", locationResults);
			edit.putString("totalDistance", ""+totalDistance);
			edit.putString("totalTimeSpent", ""+totalTimeSpent);
			edit.putString("averageSpeed", ""+averageSpeed);
			
			edit.putString("numOfPcMk", ""+numOfPlaceMark);
			
			edit.apply();
		}
		/* can use simple flags to keep old variable, 
		 * but split current result string is plan a head if need to load in a file and append to it.
		 * 
		 * if have result use the last long lat to compare new long lat
		 * only append to result if more than min distance difference
		 * 
		 * example of result string contain 2 location:
		 * m 0(km) 2015-05-28 18:59:12/desc./-122.42309767,37.79784815,200.0;
		 * m 0.14(km) 2015-05-28 18:59:16/desc./-122.42193934,37.79698642,33.0;
		 * m 0.1(km) 2015-05-28 19:00:25/desc./-122.42180558,37.79618319,28.0;
		 * 
		 * tips to myself, split with
		 * ';' for each location
		 * ',' to get the end of the whole string and long/lat/alt
		 * '/' 3 fields of kml placemark/description/coordinates
		 * ' ' to get the beginning of the whole string and placemark filed which I put speed/distance/date/time
		 */
		else
		{
//			tmpArray = locationResults.split(",");
//			//-2 is second to last of ,
//			laLatitude = Double.parseDouble(tmpArray[tmpArray.length - 2]);
//			//-3 is third to last of ,
//			tmpArray = tmpArray[tmpArray.length - 3].split("/");
//			//-1 is last of /
//			laLongitude = Double.parseDouble(tmpArray[tmpArray.length - 1]);
			
			LongLatToDistance lltd = new LongLatToDistance(lastLongitude, lastLatitude, longitude, latitude);
			System.out.println("mytab: last long lat : " + lastLongitude + ", " + lastLatitude);
			
			if(lltd.getDistance() >= MIN_DISTANCE_TO_APPEND)
			{
				subDistance = lltd.getDistance();
				System.out.println("mytab: can append: " + lltd.getDistance() + " meter");
				
				// I count line before the first time append to a empty string, so 0 + 1.
//				numOfLocRequest = countLines(locationResults) + 1;

				timeStampStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
				subTimeSpent = new FindTimeDifference(lastTimeStampStr, timeStampStr).getDifference();
				// speed unit of measurement is meter/second
				subSpeed = subDistance / subTimeSpent;
				
				locationResults = locationResults + "m " + subSpeed + "(mps) " + subDistance + "(m) " 
						+ timeStampStr + "/"+subTimeSpent+":"+lastTimeStampStr+":"+timeStampStr+"/" 
						+ longitude + "," + latitude + "," + altitude + ";";
//				locationResults = locationResults + "m " + subDistance + "(km) " 
//						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) +"/desc./" 
//						+ longitude + "," + latitude + "," + altitude + ";";

				numOfPlaceMark = countLines(locationResults);
				
				lastLongitude = longitude;
				lastLatitude = latitude;
				lastSubDistance = subDistance;
				lastTimeStampStr = timeStampStr;
				
				totalDistance = totalDistance + subDistance;
				totalTimeSpent = totalTimeSpent + subTimeSpent;
				totalSubSpeed = totalSubSpeed + subSpeed;
				subSpeedCounter++;
				averageSpeed = totalSubSpeed / subSpeedCounter;
				
				myToast = Toast.makeText(getBaseContext(), "New Lo/La: " + latitude + ", " + longitude, Toast.LENGTH_SHORT);
				myToast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
				myToast.show();
				System.out.println(locationResults);
				
				edit.putString("longLat", locationResults);
				edit.putString("totalDistance", ""+totalDistance);
				edit.putString("totalTimeSpent", ""+totalTimeSpent);
				edit.putString("averageSpeed", ""+averageSpeed);
				
				edit.putString("numOfPcMk", ""+numOfPlaceMark);
				
				edit.apply();
			}
			else
			{
				System.out.println("mytab: can not append: " + lltd.getDistance());
			}
		}
	}
 
	@Override
	public void onProviderDisabled(String provider)
	{
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
		Toast.makeText(getBaseContext(), "Gps is turned off!! ",Toast.LENGTH_SHORT).show();
	}
 
	@Override
	public void onProviderEnabled(String provider)
	{
		Toast.makeText(getBaseContext(), "Gps is turned on!! ",Toast.LENGTH_SHORT).show();
	}
 
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	}
}

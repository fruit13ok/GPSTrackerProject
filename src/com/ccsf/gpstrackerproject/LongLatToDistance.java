package com.ccsf.gpstrackerproject;

import java.math.BigDecimal;
/**
 * I found formula in these sites:
 * http://www.ig.utexas.edu/outreach/googleearth/latlong.html
 */
public class LongLatToDistance
{
	double dlon;
	double lon1;	//-122.419146
	double lon2;	//-122.416313
	double dlat;
	double lat1;	//37.807806
	double lat2;	//37.793903
	double r;
	double d;
	double resultDistance = 0.0;
	
	public LongLatToDistance(double givenLon1, double givenLat1, double givenLon2, double givenLat2)
	{
		lon1 = degreeToRadian(givenLon1);
		lon2 = degreeToRadian(givenLon2);
		lat1 = degreeToRadian(givenLat1);
		lat2 = degreeToRadian(givenLat2);
		
		dlon = lon2 - lon1;
		dlat = lat2 - lat1;

		r = 6371;	//radius of the earth (km) at 39 degrees from the equator
		// the formula to find distance, in km, no idea how accurate the formula is 
		d = Math.acos( Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(dlon) ) * r;
		resultDistance = setSignificantDigit(d, 2);
//		System.out.println(resultDistance + " km");
		System.out.println((resultDistance * 1000) + " m");
	}
	
	public static double degreeToRadian(double degree)
	{
		double radian = degree * Math.PI/180; // radians = degrees * pi/180
		return radian;
	}
	
	public static double setSignificantDigit(double num, int digit)
	{
		BigDecimal bd = new BigDecimal(num);
		bd = bd.setScale(digit, BigDecimal.ROUND_CEILING);
		double newNum = bd.doubleValue();
		return newNum;
	}

	// return distance in meter
	public double getDistance()
	{
		return (resultDistance * 1000);
	}
}

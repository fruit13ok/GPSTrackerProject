package com.ccsf.gpstrackerproject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FindTimeDifference
{
	String strDate1;
	String strDate2;
	SimpleDateFormat format;
	Date date1;
	Date date2;

	/**
	 *  default constructor just for testing
	 */
	public FindTimeDifference()
	{
		// call the next constructor
		this("0000-00-00 00:00:00", "0000-00-00 00:00:00");
	}
	
	/**
	 * use this constructor, give 2 datetime in string
	 * @param givenStrDate1
	 * @param givenStrDate2
	 */
	public FindTimeDifference(String givenStrDate1, String givenStrDate2)
	{
		strDate1 = givenStrDate1;
		strDate2 = givenStrDate2;
		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		date1 = null;
		date2 = null;
	}
	
	/**
	 * return difference in second
	 * @return long
	 */
	public long getDifference()
	{
		try
		{
			date1 = format.parse(strDate1);
			date2 = format.parse(strDate2);

			// millisecond to second
			long diffSecond = (date2.getTime() - date1.getTime()) / 1000;
			System.out.println("mytab: each time spent " + diffSecond + " second");
			return diffSecond;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}
}

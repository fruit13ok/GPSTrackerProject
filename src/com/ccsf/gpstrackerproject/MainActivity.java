package com.ccsf.gpstrackerproject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity implements OnClickListener
{
	AlarmManager alarm;
	Calendar cal;
	Intent intent;
	PendingIntent pintent;
	
	SharedPreferences pref;
	String locationResults;
	
	StringToXML stx;
	
	File checkFile;
	TextView tvPersLoca;
	EditText etServerURL;
	EditText etServerVariable;
	
	String totalDistanceResults;
	String totalTimeSpentResult;
	String averageSpeedResult;
	
	TextView tvDistanceTimeSpeed;
	
	Timer timer;
	Timer timer2;
	
	TimerTask dsasTimerTask;
	TimerTask deTimerTask;
	TimerTask dsTimerTask;
	TimerTask deTimerTaskMonth;
	TimerTask npmTimerTask;
	TimerTask dSendTimerTask;
	final Handler handler = new Handler();
	
	long minuteInitDelayInMs;
	long hourInitDelayInMs;
	long dayInitDelayInMs;
	long minuteIntervalInMs;
	long hourIntervalInMs;
	long dayIntervalInMs;
	
	SeekBar seekBar;	//size of seekbar define in xml, 5, start from 0, so 6 selection
	OnSeekBarChangeListener osbcl;
	
	String numOfPlaceMark;
	TextView tvNumOfPcMk;
	String lastUpload;
	TextView tvLastUpload;
	
	boolean isByPlaceMark;
	int numOfPcMkBeforeSend;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		pref = getSharedPreferences("prefName", Context.MODE_PRIVATE);
//		locationResults = "";
		
		tvPersLoca = (TextView) findViewById(R.id.tvPersLoca);
		tvDistanceTimeSpeed = (TextView) findViewById(R.id.tvDistanceTimeSpeed);
		
		Button btnStart = (Button) findViewById(R.id.btnStart);
        Button btnEnd = (Button) findViewById(R.id.btnEnd);
        Button btnShowLoca = (Button) findViewById(R.id.btnShowLoca);
        Button btnSend = (Button) findViewById(R.id.btnSend);
        
        btnStart.setOnClickListener(this);
        btnEnd.setOnClickListener(this);
        btnShowLoca.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        
        etServerURL = (EditText) findViewById(R.id.etServerURL);
        etServerVariable = (EditText) findViewById(R.id.etServerVariable);
        
        minuteInitDelayInMs = 60 * 1000;
    	hourInitDelayInMs = 60 * 60 * 1000;
    	dayInitDelayInMs = 24 * 60 * 60 * 1000;
        minuteIntervalInMs = 60 * 1000;
        hourIntervalInMs = 60 * 60 * 1000;
        dayIntervalInMs = 24 * 60 * 60 * 1000;
        
        tvNumOfPcMk = (TextView) findViewById(R.id.tvNumOfPcMk);
        tvLastUpload = (TextView) findViewById(R.id.tvLastUpload);
        
        isByPlaceMark = false;
        numOfPcMkBeforeSend = 3;
        
        seekBar = (SeekBar) findViewById(R.id.sbUploadControl);
        osbcl = new OnSeekBarChangeListener()
		{
			int progress = 0;

			@Override
			public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser)
			{
				progress = progresValue;
//				Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
//				Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
			}

			// here I should initiate and re-initiate the whole task, so old task will be deleted
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				Toast.makeText(getApplicationContext(), "progress: " + progress, Toast.LENGTH_SHORT).show();
//				Toast.makeText(getApplicationContext(), " Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
			}
		};
		seekBar.setOnSeekBarChangeListener(osbcl);
	}
	
	@Override
    public void onClick(View v)
	{
        switch (v.getId())
        {
        case R.id.btnStart:
//        	doStart();
        	startTimer();
            break;
        case R.id.btnEnd:            
            doEnd();
            stoptimertask();
            stoptimertask2();
            break;
        case R.id.btnShowLoca:
        	doShowAndSave();
        	break;
        case R.id.btnSend:
        	doSend();
        	break;
        default:
            break;
        }
    }
	
	@Override
	protected void onStart()
	{
		super.onStart();
		updateNumOfPlaceMark();
		updateLastUpload();
	}
	
	@Override
	protected void onRestart()
	{
		super.onRestart();
		updateNumOfPlaceMark();
		updateLastUpload();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		updateNumOfPlaceMark();
		updateLastUpload();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		updateNumOfPlaceMark();
		updateLastUpload();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		doEnd();
	}
	
	// I was trying to make the back key as exit key.
	// did not see any change with this callback method or without it.
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		this.finish();
	}
	
	public void startTimer()
	{
		doEnd();
		doStart();
		stoptimertask();
		//set a new Timer
		timer = new Timer();
		
		//initialize the TimerTask's job
		doShowAndSaveTimerTask();
		doEndTimerTask();
		doStartTimerTask();
		doEndTimerTaskMonth();
		doSendTimerTask();
		
		stoptimertask2();
		timer2  = new Timer();
		numOfPlaceMarkTimerTask();
		timer2.schedule(npmTimerTask, 0, minuteIntervalInMs);
		
		// TimerTask do and finish one task at a time, not multi-task, so should not have differ tasks time coalition
		//schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
		
		switch(seekBar.getProgress())
		{
		case 0:	//once
			// not start any task
			isByPlaceMark = false;
			break;
		case 1:	//minutely
			isByPlaceMark = false;
			timer.schedule(dsasTimerTask, minuteInitDelayInMs, minuteIntervalInMs);
			timer.schedule(deTimerTask, minuteInitDelayInMs + 5000, minuteIntervalInMs);
			timer.schedule(dsTimerTask, minuteInitDelayInMs + 10000, minuteIntervalInMs);
			timer.schedule(dSendTimerTask, minuteInitDelayInMs + 15000, minuteIntervalInMs);
			break;
		case 2:	//hourly
			isByPlaceMark = false;
			timer.schedule(dsasTimerTask, hourInitDelayInMs, hourIntervalInMs);
			timer.schedule(deTimerTask, hourInitDelayInMs + 5000, hourIntervalInMs);
			timer.schedule(dsTimerTask, hourInitDelayInMs + 10000, hourIntervalInMs);
			timer.schedule(dSendTimerTask, hourInitDelayInMs + 15000, hourIntervalInMs);
			break;
		case 3:	//daily
			isByPlaceMark = false;
			timer.schedule(dsasTimerTask, dayInitDelayInMs, dayIntervalInMs);
			timer.schedule(deTimerTask, dayInitDelayInMs + 5000, dayIntervalInMs);
			timer.schedule(dsTimerTask, dayInitDelayInMs + 10000, dayIntervalInMs);
			timer.schedule(dSendTimerTask, dayInitDelayInMs + 15000, dayIntervalInMs);
			break;
		case 4:	//monthly
			isByPlaceMark = false;
			// deTimerTaskMonth with doEndTimerTaskMonth() will call startTimer() recursively
			timer.schedule(dsasTimerTask, nextMonth());
			timer.schedule(dSendTimerTask, nextMonth());
			timer.schedule(deTimerTaskMonth, nextMonth());
			break;
		case 5:	//waypoint
			// forgot what this is ask Aaron
			isByPlaceMark = true;
			break;
		}
	}

	public void stoptimertask()
	{
		//stop the timer, if it's not already null
		if (timer != null || dsasTimerTask != null || deTimerTask != null || dsTimerTask != null || 
				deTimerTaskMonth != null || dSendTimerTask != null)
		{
			dsasTimerTask.cancel();
			dsasTimerTask = null;
			deTimerTask.cancel();
			deTimerTask = null;
			dsTimerTask.cancel();
			dsTimerTask = null;
			deTimerTaskMonth.cancel();
			deTimerTaskMonth = null;
			dSendTimerTask.cancel();
			dSendTimerTask = null;
			timer.cancel();
			timer = null;
		}
	}
	
	// call by send button
	public void stoptimertask2()
	{
		if (timer2 != null || npmTimerTask != null)
		{
			npmTimerTask.cancel();
			npmTimerTask = null;
			timer2.cancel();
			timer2 = null;
		}
	}
	
	public void doShowAndSaveTimerTask()
	{
		dsasTimerTask = new TimerTask()
		{
			public void run()
			{
				handler.post(new Runnable()
				{
					public void run()
					{
						doShowAndSave();
					}
				});
			}
		};
	}
	
	public void doEndTimerTask()
	{
		deTimerTask = new TimerTask()
		{
			public void run()
			{
				handler.post(new Runnable()
				{
					public void run()
					{
						doEnd();
					}
				});
			}
		};
	}
	
	public void doSendTimerTask()
	{
		dSendTimerTask = new TimerTask()
		{
			public void run()
			{
				handler.post(new Runnable()
				{
					public void run()
					{
						doSend();
					}
				});
			}
		};
	}
	
	public void doEndTimerTaskMonth()
	{
		deTimerTaskMonth = new TimerTask()
		{
			public void run()
			{
				handler.post(new Runnable()
				{
					public void run()
					{
						doEnd();
						// this work, recursive call looks danger, use other way it I can.
						startTimer();
					}
				});
			}
		};
	}
	
	public void doStartTimerTask()
	{
		dsTimerTask = new TimerTask()
		{
			public void run()
			{
				handler.post(new Runnable()
				{
					public void run()
					{
						doEnd();
						doStart();
					}
				});
			}
		};
	}
	
	public void numOfPlaceMarkTimerTask()
	{
		npmTimerTask = new TimerTask()
		{
			public void run()
			{
				handler.post(new Runnable()
				{
					public void run()
					{
						updateNumOfPlaceMark();
						if(Integer.parseInt(numOfPlaceMark) >= numOfPcMkBeforeSend && isByPlaceMark == true)
						{
							doShowAndSave();
							doEnd();
							doSend();
							// this work, recursive call looks danger, use other way it I can.
							startTimer();
						}
					}
				});
			}
		};
	}
	
	public void doStart()
	{
		startService(new Intent(this, MyService.class));
        cal = Calendar.getInstance();
        intent = new Intent(this, MyService.class);
        pintent = PendingIntent.getService(this, 0, intent, 0);

        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // Start service every 60 seconds,
        // for real test or real app, use at least 15 minutes, longer the better if still got enough data.
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                1 * 60 * 1000, pintent);
	}
	
	// this part only work if user only put app to background not close it,
	// I think is because the Intent and PendingIntent will be lost track by the system after close the app,
	// in service onStartCommand() START_STICKY will restart the service after close with new Intent.
	// 
	// When the last service was still running, I press start service, and end service, it DID END,
	// I think when start the same service again will ends the old service.
	// 
	// try this site, persist the intent:
	// http://stackoverflow.com/questions/2812650/any-workaround-to-save-an-intent-in-settings
	public void doEnd()
	{
		stopService(new Intent(getBaseContext(), MyService.class));
        if(alarm != null)
        {
        	alarm.cancel(pintent);
        }
        locationResults = "";
        tvPersLoca.setText(locationResults);
        tvDistanceTimeSpeed.setText("");
	}
	
	public void doShowAndSave()
	{
		locationResults = pref.getString("longLat", "");
    	totalDistanceResults = pref.getString("totalDistance", "");
    	totalTimeSpentResult = pref.getString("totalTimeSpent", "");
    	averageSpeedResult = pref.getString("averageSpeed", "");
    	if(locationResults.isEmpty())
    	{
    		tvPersLoca.setText("no data");
    		tvDistanceTimeSpeed.setText("no data");
    	}
    	else
    	{
    		stx = new StringToXML(locationResults);
        	stx.makeXmlFile();
        	tvPersLoca.setText(stx.getXmlString());
        	tvDistanceTimeSpeed.setText("distance: " + totalDistanceResults 
        			+ "(m), time: " + totalTimeSpentResult + "(s), speed: " + averageSpeedResult + " (mps)");
        	
        	// make kmz file to sd card
        	new XMLToKMZ(stx.getXmlString());
        	
        	// this just for my stationary work testing, need connection
        	// send / post the result string, less work
//        	new HttpStrFileToServer(stx.getXmlString());
        	
        	// after the above test work, make another button for send, then use this instead
        	// send / post the file content after read from the file we just saved
//        	new HttpStrFileToServer();
    	}
    	tvPersLoca.setMovementMethod(new ScrollingMovementMethod());
    	tvPersLoca.setTextColor(Color.BLUE);
	}
	
	public void doSend()
	{
		checkFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() 
    			+ "/MyFiles/trackedPlaces.kml");
    	if(checkFile.exists())
    	{
    		System.out.println("mytab: last Modify date" + checkFile.getPath() + ", " 
    				+ new Date(checkFile.lastModified()).toString());
    		// I try not to create HttpStrFileToServer object, because anonymous inner class will close itself when done. 
    		
			new HttpStrFileToServer(etServerURL.getText().toString(), etServerVariable.getText().toString());
			
//			stoptimertask2();
			tvNumOfPcMk.setText("0 waypoints in memory");
			lastUpload = "last upload" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
			tvLastUpload.setText(lastUpload);
			pref.edit().putString("numOfPcMk", "0");
			pref.edit().putString("lastUpload", lastUpload);
			pref.edit().apply();
    	}
    	else
    	{
    		System.out.println("file not exist");
    	}
	}
	
	private Date nextMonth()
	{ 
		Calendar runDate = Calendar.getInstance();
//		runDate.add(Calendar.MONTH, 1);//set to next month
		runDate.add(Calendar.MINUTE, 1);//set to next minute
		System.out.println("mytab: "+runDate.getTime());
		return runDate.getTime();
	}
	
	public void updateNumOfPlaceMark()
	{
		numOfPlaceMark = pref.getString("numOfPcMk", "0 waypoints in memory");
		tvNumOfPcMk.setText(numOfPlaceMark + " waypoints in memory");
	}
	
	public void updateLastUpload()
	{
		lastUpload = pref.getString("lastUpload", "last upload n/a");
		tvLastUpload.setText(lastUpload);
	}
}

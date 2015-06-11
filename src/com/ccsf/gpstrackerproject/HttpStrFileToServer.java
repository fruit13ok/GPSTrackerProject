package com.ccsf.gpstrackerproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.Header;

import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Base64;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

public class HttpStrFileToServer
{
	RequestParams params = new RequestParams();
	String myFolder = "/MyFiles";
	String myFile = "trackedPlaces.kml";
//	String myFile = "textfile1.txt";		//inside have 2 characters "f1"
//	String myFile = "trackedPlaces.kmz";
	
	File mFile = getSDcardFile(myFolder, myFile);
	String fileContent = readFromFile(mFile);
	
	// In this I send / post as a String, to send as Base64 file uncomment those parts.
//	public HttpStrFileToServer()
//	{
//		/*
//		 * !!! SEND STRING WAY !!! put / upload a String to the "route" variable,
//		 * if the server want a .kml file this string is the way to go.
//		 */
//		params.put("route", fileContent);
//		
//		/*
//		 * !!! SEND FILE WAY !!! on hills IT UPDATED THE LAST MODIFY TIME, and UPDATE THE FILE CONTENT,
//		 * if server want a .kmz file (encoded with Base64 and byte) use this way, need decode from server side.
//		 * I believe http post pass both sting and byte, but for byte is need to encode with Base64,
//		 * on the server side need to have the variable with the same name, (might be more, need to see server code )
//		 */
////		byte[] myBFile = fileToByteArray(mFile);
////		String myStrBase64 = ByteArrayToBase64(myBFile);
////		params.put("route", myStrBase64);
//		
//		testAsync(URL, params);
//	}

	// this constructor should be use with interface with use input server url and server variable
	// server url "http://hills.ccsf.edu/~user/cgi-bin/receive.py"
	// server variable "route"
	public HttpStrFileToServer(String givenURL, String givenServerVar)
	{
		params.put(givenServerVar, fileContent);
		testAsync(givenURL, params);
	}
		
	// this constructor does least thing, just send the given result string
//	public HttpStrFileToServer(String giveStrResult)
//	{
//		params.put("route", giveStrResult);
//		testAsync(URL, params);
//	}
	
	private void testAsync(String url, RequestParams myParams)
    {
//		MyHttpClient.get(url, myParams, new TextHttpResponseHandler()
		MyHttpClient.get(url, myParams, new AsyncHttpResponseHandler()
	    {
			@Override
//			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e)
			public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e)
			{
				// called when response HTTP status is "4XX" (eg. 401, 403, 404)
				System.out.println("mytab: fail");
				System.out.println("mytab: statusCode: " + statusCode + ", headers: " + headers + ", response: " + errorResponse);
			}

			@Override
//			public void onSuccess(int statusCode, Header[] headers, String response)
			public void onSuccess(int statusCode, Header[] headers, byte[] response)
			{
//				myParams.getEntity(progressHandler);	//still researching getEntity
				// called when response HTTP status is "200 OK"
				System.out.println("mytab: statusCode: " + statusCode + ", headers: " + headers + ", response: " + response);
				System.out.println("mytab:" + headers.length + " header:");
				for(int i = 0; i < headers.length; i++)
				{
					System.out.println("----mytab:" + headers[i]);
				}
//				System.out.println("mytab:" + response.length + " response:");
//				for(int j = 0; j < response.length; j++)
//				{
//					System.out.println("----mytab:" + response[j]);
//				}
			}
			
			@Override
			public void onStart()
			{
				// called before request is started
			}
			
			@Override
			public void onRetry(int retryNo)
			{
				// called when request is retried
			}
	    });
    }
	
	public File getSDcardFile(String folderName, String fileName)
	{
		//---SD Storage---
		// get dir of sd card location
		// the get return this symlink which point to the next real path:
		// /storage/emulated/0
		// this real path has the file /MyFiles/textfile.txt
		// /mnt/shell/emulated/0
        File sdCard = Environment.getExternalStorageDirectory();
//        sdcardDirectoryForSamsungPhone();
        System.out.println("mytab: " + sdCard.getAbsolutePath() + folderName);
        File directory = new File (sdCard.getAbsolutePath() + folderName);
        File file = new File(directory, fileName);
//        File file = new File("/mnt/shell/emulated/0/MyFiles/trackedPlaces.kml");
        System.out.println("mytab: " + file.getAbsolutePath());
        if(file.exists())
        {
        	System.out.println("mytab: exists");
        	return file;
        }
        else
        {
        	System.out.println("mytab: not exists");
        	return null;
        }
	}
	
	/*
	// this part show how to convert file into array of byte.
	public byte[] fileToByteArray(File file)
	{
		try
		{
			FileInputStream fIn2 = new FileInputStream(file);
			byte[] bFile = new byte[(int) file.length()];
			fIn2.read(bFile);
			fIn2.close();
			return bFile;
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			return null;
		}
	}
	
	// byte array can encode into Base64 string by use class Base64
	public String ByteArrayToBase64(byte[] bFile)
	{
		String strBase64 = Base64.encodeToString(bFile, Base64.DEFAULT);
		return strBase64;
	}
	*/

	public String readFromFile(File file)
	{
		try
		{
            // read byte from file
            FileInputStream fIn = new FileInputStream(file);
            // convert byte to character
            InputStreamReader isr = new InputStreamReader(fIn);
            
            // this character array refer to a block of characters to convert to string at one time
            // it is set to 100 characters per block
            // a string have 2 billion character size, so I assume biggest block size can be 2 billion
			char[] inputBuffer = new char[100];
			String s = "";

			int charRead;
			// the loop end when read EOF from InputStreamReader
			// since block size is 100, so if InputStreamReader has <=100 characters it loop once, else loop more
			// read() read from InputStreamReader and store them to character array
			// return number of character read from InputStreamReader or -1 if EOF from InputStreamReader
			while ((charRead = isr.read(inputBuffer))>0)
			{
				// see saved character array in logcat, DO NOT CAST character array to string
				//System.out.println(inputBuffer);
				
				//---convert the chars to a String---
				String readString = String.copyValueOf(inputBuffer, 0, charRead);
				s += readString;

				inputBuffer = new char[100];
			}
//			System.out.println("mytab: " + s);
//			System.out.println("mytab: File loaded successfully!");
			isr.close();
			return s;
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			return null;
		}
	}
}

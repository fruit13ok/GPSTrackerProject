package com.ccsf.gpstrackerproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.os.Environment;
import android.widget.Toast;

public class XMLToKMZ
{
	String XmlString;
	static final int READ_BLOCK_SIZE = 100;
	String myFolder;
	String myFile;
	String myZipFile;
	boolean fileCreated;
	
	public XMLToKMZ(String givenXMLString)
	{
		XmlString = givenXMLString;
		myFolder = "/MyFiles";
		myFile = "trackedPlaces.kml";
		myZipFile = "trackedPlaces.kmz";
		fileCreated = false;
		
		writeToFile();
		zipTheFile();
	}
	
	/**
	 * folder name just a user made folder use to contain the file, not the whole directory absolute path
	 * @param folderName
	 * @param fileName
	 */
	public File createFileToSDcard(String folderName, String fileName)
	{
		try
		{
            //---SD Card Storage---
			// get dir of sd card location
			// the get return this symlink which point to the next real path:
	        // /storage/emulated/0
	        // this real path has the file /MyFiles/textfile.txt
	        // /mnt/shell/emulated/0
            File sdCard = Environment.getExternalStorageDirectory();
//          sdcardDirectoryForSamsungPhone();
            // make dir
            File directory = new File (sdCard.getAbsolutePath() + folderName);
            directory.mkdirs();
            // create file if not already exist
            File file = new File(directory, fileName);
            fileCreated = file.createNewFile();
            if (fileCreated)
            {
                System.out.println("Created empty file: " + file.getPath());
            }
            else
            {
                System.out.println("Failed to create empty file: " + file.getPath());
            }
            return file;
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			return null;
		}
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
        File directory = new File (sdCard.getAbsolutePath() + folderName);
        File file = new File(directory, fileName);
        System.out.println(file.getAbsolutePath());
        if(file.exists())
        {
        	System.out.println("exists");
        	return file;
        }
        else
        {
        	return null;
        }
	}
	
	public void writeToFile()
	{
		try
		{
            // write byte to file
            FileOutputStream fOut = new FileOutputStream(createFileToSDcard(myFolder, myFile));
            
            // convert character to byte 
			OutputStreamWriter osw = new OutputStreamWriter(fOut);

			//---write the string to the file---
			osw.write(XmlString);
			osw.flush(); 
			osw.close();

			//---display file saved message---
			System.out.println("File saved successfully!");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	/*
	public void readFromFile()
	{
		try
		{
            // read byte from file
            FileInputStream fIn = new FileInputStream(getSDcardFile(myFolder, myFile));
            // convert byte to character
            InputStreamReader isr = new InputStreamReader(fIn);
            
            // this character array refer to a block of characters to convert to string at one time
            // it is set to 100 characters per block
            // a string have 2 billion character size, so I assume biggest block size can be 2 billion
			char[] inputBuffer = new char[READ_BLOCK_SIZE];
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

				inputBuffer = new char[READ_BLOCK_SIZE];
			}
			System.out.println(s);
			System.out.println("File loaded successfully!");
			isr.close();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	*/
	
	// !!! FOR MY PHONE (Samsung G5) IT IS NOT IN SD CARD !!!
	public void sdcardDirectoryForSamsungPhone()
	{
      if(android.os.Build.DEVICE.equalsIgnoreCase("Samsung") || android.os.Build.MANUFACTURER.equalsIgnoreCase("Samsung"))	
      {
    	  myFolder = "/mnt/extSdCard/MyFiles";
      }
	}
	
	public void zipTheFile()
	{
		byte[] buffer = new byte[1024];
		 
    	try
    	{
    		//createFileToSDcard(myFolder, myFile)
    		FileOutputStream fos = new FileOutputStream(createFileToSDcard(myFolder, myZipFile));
    		ZipOutputStream zos = new ZipOutputStream(fos);
    		ZipEntry ze= new ZipEntry(myFile);
    		zos.putNextEntry(ze);
    		FileInputStream in = new FileInputStream(getSDcardFile(myFolder, myFile));
 
    		int len;
    		while ((len = in.read(buffer)) > 0)
    		{
    			zos.write(buffer, 0, len);
    		}
 
    		in.close();
    		zos.closeEntry();
    		zos.close();
 
    		System.out.println("Done");
    	}
    	catch(IOException ex)
    	{
    	   ex.printStackTrace();
    	}
	}
}

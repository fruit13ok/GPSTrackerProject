package com.ccsf.gpstrackerproject;

import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class StringToXML
{
	String placeNameText;
	String placeDescriptionText;
	String placeCoordinatesText;
	
	// currently use this variable for both document and folder name
	String placeFolder = "places Folder";
	String isOpen = "1";
	String folderDescription = "keep placemarks";
	
	// split given string of placemarks into array of each placemark
	String[] placemarks;
	String[] placemarkDatas;
	String defaultPlacemarks;
	
	String xmlString;
	
	String pathNameText = "myPath";
	String pathDescriptionText = "a path";
	String pathCoordinatesText = "";
	
	public StringToXML()
	{
		defaultPlacemarks = new String("Simple placemark/Attached to the ground." +
				" Intelligently places itself at the height of the underlying" +
				" terrain./-122.0822035425683,37.42228990140251,0;");
		placemarks = defaultPlacemarks.split(";");
		placeNameText = "";
		placeDescriptionText = "";
		placeCoordinatesText = "";
	}
	
	public StringToXML(String givenPlacemarks)
	{
		placemarks = givenPlacemarks.split(";");
		placeNameText = "";
		placeDescriptionText = "";
		placeCoordinatesText = "";
	}
	
	public void makeXmlFile()
	{
		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root element and its attribute
			Document doc = docBuilder.newDocument();
			
			// add a new line between document heading and root element
			// java don't need this line, it insert newline already, only android need it
			doc.appendChild(doc.createTextNode("\n"));
			
			Element rootElementKml = doc.createElement("kml");
			doc.appendChild(rootElementKml);
			rootElementKml.setAttribute("xmlns", "http://www.opengis.net/kml/2.2");
			rootElementKml.setAttribute("xmlns:gx", "http://www.google.com/kml/ext/2.2");
			rootElementKml.setAttribute("xmlns:kml", "http://www.opengis.net/kml/2.2");
			rootElementKml.setAttribute("xmlns:atom", "http://www.w3.org/2005/Atom");
			
			Element elementDocument = doc.createElement("Document");
			rootElementKml.appendChild(elementDocument);
			
			Element elementDocumentName = doc.createElement("name");
			elementDocumentName.appendChild(doc.createTextNode(placeFolder));
			elementDocument.appendChild(elementDocumentName);
			
			Element elementFolder = doc.createElement("Folder");
			elementDocument.appendChild(elementFolder);
			
			Element elementFolderName = doc.createElement("name");
			elementFolderName.appendChild(doc.createTextNode(placeFolder));
			elementFolder.appendChild(elementFolderName);
			
			Element elementIsOpen = doc.createElement("open");
			elementIsOpen.appendChild(doc.createTextNode(isOpen));
			elementFolder.appendChild(elementIsOpen);
			
			Element elementFolderDescription = doc.createElement("description");
			elementFolderDescription.appendChild(doc.createTextNode(folderDescription));
			elementFolder.appendChild(elementFolderDescription);
			
			// append each set of element inside root element
			// iterate each placemarks array string and split them inside the loop to extra data
			for(String placemark : placemarks)
			{
				// split 1 string of placemark into 3 data
				placemarkDatas = placemark.split("/");
				placeNameText = placemarkDatas[0];
				placeDescriptionText = placemarkDatas[1];
				placeCoordinatesText = placemarkDatas[2];
				
				pathCoordinatesText = pathCoordinatesText + placeCoordinatesText + " ";
				
				// root element -> elements
				Element elementPlacemark = doc.createElement("Placemark");
				elementFolder.appendChild(elementPlacemark);

				// root element -> elements -> child elements
				Element elementName = doc.createElement("name");
				elementName.appendChild(doc.createTextNode(placeNameText));
				elementPlacemark.appendChild(elementName);

				// root element -> elements -> child elements
				Element elementDescription = doc.createElement("description");
				elementDescription.appendChild(doc.createTextNode(placeDescriptionText));
				elementPlacemark.appendChild(elementDescription);
				
				// root element -> elements -> child elements
				Element elementPoint = doc.createElement("Point");
				elementPlacemark.appendChild(elementPoint);
				
				// root element -> elements -> child elements -> grand child elements
				Element elementCoordinates = doc.createElement("coordinates");
				elementCoordinates.appendChild(doc.createTextNode(placeCoordinatesText));
				elementPoint.appendChild(elementCoordinates);
			}

			
			// path line connect each placemark
			Element elementPath = doc.createElement("Placemark");
			elementFolder.appendChild(elementPath);
			
			Element elementPathName = doc.createElement("name");
			elementPathName.appendChild(doc.createTextNode(pathNameText));
			elementPath.appendChild(elementPathName);
			
			Element elementPathDescription = doc.createElement("description");
			elementPathDescription.appendChild(doc.createTextNode(pathDescriptionText));
			elementPath.appendChild(elementPathDescription);
			
			Element elementLineString = doc.createElement("LineString");
			elementPath.appendChild(elementLineString);
			
			Element elementPathCoordinates = doc.createElement("coordinates");
			elementPathCoordinates.appendChild(doc.createTextNode(pathCoordinatesText));
			elementLineString.appendChild(elementPathCoordinates);
			
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			//StreamResult result = new StreamResult(new File("C:\\Users\\Liu\\Desktop\\gsoc\\other project idea research\\yifile.xml"));

			// Output to console for testing
			// short hand output without create string object
			//StreamResult result = new StreamResult(System.out);
			StreamResult result = new StreamResult(new StringWriter());
			
			// Indentation
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
			// apply convert from data to xml with above rules
			transformer.transform(source, result);

			xmlString = result.getWriter().toString();
			System.out.println(xmlString);
			
			System.out.println("File saved!");
		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
		catch (TransformerException tfe)
		{
			tfe.printStackTrace();
		}
	}
	
	public String getXmlString()
	{
		return xmlString;
	}
}

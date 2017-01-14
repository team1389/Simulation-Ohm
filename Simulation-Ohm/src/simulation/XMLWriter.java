package simulation;
import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.newdawn.slick.geom.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class XMLWriter {

	public void saveToXML(ArrayList<Point> points){
		 
		 try{
		File fileName = new File("file.xml");
		 DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
		 DocumentBuilder docBuilder = docFac.newDocumentBuilder();
		  Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("Boundaries");
			for(Point p: points){
			       Element X = doc.createElement("X Value");
		            X.appendChild(doc.createTextNode(Float.toString(p.getX())));
		            rootElement.appendChild(X);

		            Element Y = doc.createElement("Y Value");
		            Y.appendChild(doc.createTextNode(Float.toString(p.getY())));
		            rootElement.appendChild(Y);
			
		            TransformerFactory transformerFactory = TransformerFactory.newInstance();
		    		Transformer transformer = transformerFactory.newTransformer();
		    		DOMSource source = new DOMSource(doc);
		    		StreamResult result = new StreamResult(fileName);
		    		transformer.transform(source, result);
			}
			
				
				
				
				
			
		 }
		 catch(ParserConfigurationException pce){
			 pce.printStackTrace();
		 }catch (TransformerException tfe) {
				tfe.printStackTrace();
		  }
	}
}
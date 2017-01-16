package simulation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Shape;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLWriter {

	public void writeShapes(List<Shape> boundaries, List<Shape> feeders,List<Shape> dropoffs) {

		try {
			File fileName = new File("file.xml");
			DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element jank = doc.createElement("data");
			Element bEle = doc.createElement("boundaries");
			Element  fEle= doc.createElement("feeders");
			Element dEle = doc.createElement("dropoffs");
			doc.appendChild(jank);
			jank.appendChild(bEle);
			jank.appendChild(fEle);
			jank.appendChild(dEle);
			for (Shape s : boundaries) {
				System.out.println("saving point");
				Element shape = doc.createElement("shape");
				for(int i = 0; i<s.getPointCount()*2; i+=2){
					Element point = doc.createElement("point");
					point.setAttribute("x", Float.toString(s.getPoints()[i]));
					point.setAttribute("y", Float.toString(s.getPoints()[i+1]));
				shape.appendChild(point);
				}
				bEle.appendChild(shape);
			}
			for (Shape s : feeders) {
				System.out.println("saving point");
				Element shape = doc.createElement("shape");
				for(int i = 0; i<s.getPointCount()*2; i+=2){
					Element point = doc.createElement("point");
					point.setAttribute("x", Float.toString(s.getPoints()[i]));
					point.setAttribute("y", Float.toString(s.getPoints()[i+1]));
				shape.appendChild(point);
				}
				fEle.appendChild(shape);
			}
			for (Shape s : dropoffs) {
				System.out.println("saving point");
				Element shape = doc.createElement("shape");
				for(int i = 0; i<s.getPointCount()*2; i+=2){
				Element point = doc.createElement("point");
				point.setAttribute("x", Float.toString(s.getPoints()[i]));
				point.setAttribute("y", Float.toString(s.getPoints()[i+1]));
				shape.appendChild(point);
				}
				dEle.appendChild(shape);
			}
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(fileName);
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	public List<Shape> getBoundaries() {
		try {
			File fileName = new File("file.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document document = db.parse(fileName);
			NodeList points = document.getElementsByTagName("Boundaries");
			IntStream s = IntStream.range(0, points.getLength());
			return s.mapToObj(i -> {
				Node x = points.item(i).getAttributes().getNamedItem("x");
				Node y = points.item(i).getAttributes().getNamedItem("y");
				return new Point(Float.parseFloat(x.getTextContent()), Float.parseFloat(y.getTextContent()));
			}).collect(Collectors.toList());
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		return new ArrayList<Point>();

	}
}
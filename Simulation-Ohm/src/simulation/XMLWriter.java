package simulation;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLWriter {

	public void writeShapes(List<Shape> boundaries, List<Shape> dropoffs, List<Shape> pickups) {

		try {
			File fileName = new File("file.xml");
			DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element data = doc.createElement("data");
			Element bEle = doc.createElement("boundaries");
			Element fEle = doc.createElement("feeders");
			Element dEle = doc.createElement("dropoffs");
			doc.appendChild(data);
			data.appendChild(bEle);
			for (Shape s : boundaries) {
				System.out.println("saving point");
				Element shape = doc.createElement("shape");
				for (int i = 0; i < s.getPointCount() * 2; i += 2) {
					Element point = doc.createElement("point");
					point.setAttribute("x" , Float.toString(s.getPoints()[i]));
					point.setAttribute("y" , Float.toString(s.getPoints()[i + 1]));
					shape.appendChild(point);
				}
				bEle.appendChild(shape);
			}
			data.appendChild(fEle);
			for (Shape s : pickups) {
				System.out.println("saving pickup shape");
				Element shape = doc.createElement("shape");
				for (int i = 0; i < s.getPointCount() * 2; i += 2) {
					Element point = doc.createElement("point");
					point.setAttribute("x" , Float.toString(s.getPoints()[i]));
					point.setAttribute("y" , Float.toString(s.getPoints()[i + 1]));
					shape.appendChild(point);
				}
				fEle.appendChild(shape);
			}
			data.appendChild(dEle);
			for (Shape s : dropoffs) {
				System.out.println("saving dropoff shape");
				Element shape = doc.createElement("shape");
				for (int i = 0; i < s.getPointCount() * 2; i += 2) {
					Element point = doc.createElement("point");
					point.setAttribute("x" , Float.toString(s.getPoints()[i]));
					point.setAttribute("y" , Float.toString(s.getPoints()[i + 1]));
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
			NodeList shapes = document.getElementsByTagName("boundaries").item(0).getChildNodes();
			IntStream s = IntStream.range(0, shapes.getLength());
			return s.mapToObj(i -> {
				ArrayList<Node> list = new ArrayList<Node>();
				
					for(int counter = 0; counter< shapes.item(i).getChildNodes().getLength(); counter++){
					Node x = shapes.item(i).getChildNodes().item(counter).getAttributes().getNamedItem("x");
					Node y = shapes.item(i).getChildNodes().item(counter).getAttributes().getNamedItem("y");
					list.add(x);
					list.add(y);
					System.out.println(x);
					System.out.println(y);
					}
				
				return new Polygon(new float[] { Float.parseFloat(list.get(0).getTextContent()),
						Float.parseFloat(list.get(1).getTextContent()), Float.parseFloat(list.get(2).getTextContent()),
						Float.parseFloat(list.get(3).getTextContent()), Float.parseFloat(list.get(4).getTextContent()),
						Float.parseFloat(list.get(5).getTextContent()) });
			}).collect(Collectors.toList());
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		return new ArrayList<>();
	}
	public List<Shape> getFeeders() {
		try {
			File fileName = new File("file.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document document = db.parse(fileName);
			NodeList shapes = document.getElementsByTagName("feeders").item(0).getChildNodes();
			IntStream s = IntStream.range(0, shapes.getLength());
			return s.mapToObj(i -> {
				ArrayList<Node> list = new ArrayList<Node>();
				
					for(int counter = 0; counter< shapes.item(i).getChildNodes().getLength(); counter++){
					Node x = shapes.item(i).getChildNodes().item(counter).getAttributes().getNamedItem("x");
					Node y = shapes.item(i).getChildNodes().item(counter).getAttributes().getNamedItem("y");
					list.add(x);
					list.add(y);
					System.out.println(x);
					System.out.println(y);
					}
				
				return new Polygon(new float[] { Float.parseFloat(list.get(0).getTextContent()),
						Float.parseFloat(list.get(1).getTextContent()), Float.parseFloat(list.get(2).getTextContent()),
						Float.parseFloat(list.get(3).getTextContent()), Float.parseFloat(list.get(4).getTextContent()),
						Float.parseFloat(list.get(5).getTextContent()) });
			}).collect(Collectors.toList());
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		return new ArrayList<>();
	}
	public List<Shape> getDropoffs() {
		try {
			File fileName = new File("file.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document document = db.parse(fileName);
			NodeList shapes = document.getElementsByTagName("dropoffs").item(0).getChildNodes();
			IntStream s = IntStream.range(0, shapes.getLength());
			return s.mapToObj(i -> {
				ArrayList<Node> list = new ArrayList<Node>();
				
					for(int counter = 0; counter< shapes.item(i).getChildNodes().getLength(); counter++){
					Node x = shapes.item(i).getChildNodes().item(counter).getAttributes().getNamedItem("x");
					Node y = shapes.item(i).getChildNodes().item(counter).getAttributes().getNamedItem("y");
					list.add(x);
					list.add(y);
					System.out.println(x);
					System.out.println(y);
					}
				
				return new Polygon(new float[] { Float.parseFloat(list.get(0).getTextContent()),
						Float.parseFloat(list.get(1).getTextContent()), Float.parseFloat(list.get(2).getTextContent()),
						Float.parseFloat(list.get(3).getTextContent()), Float.parseFloat(list.get(4).getTextContent()),
						Float.parseFloat(list.get(5).getTextContent()) });
			}).collect(Collectors.toList());
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		return new ArrayList<>();
	}
}

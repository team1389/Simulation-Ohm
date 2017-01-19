package simulation.drive_sim.xml;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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

import simulation.drive_sim.DriveSimulator;
import simulation.drive_sim.field.AlliedBoundary;

public class XMLShapeWriter {
	Document document;
	File writeFile;

	public XMLShapeWriter(String fileName) {
		writeFile = new File(fileName);
		try {
			initDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void writeShapes(List<Shape> boundaries, List<AlliedBoundary> pickups, List<AlliedBoundary> dropoffs) {
		Element data = document.createElement("data");
		document.appendChild(data);

		writeShapesList("boundaries", boundaries, data);
		writeAlliedBoundariesList("pickups", pickups, data);
		writeAlliedBoundariesList("dropoffs", dropoffs, data);

		try {
			writeToFile();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	private void initDocument() throws ParserConfigurationException {
		DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFac.newDocumentBuilder();
		document = docBuilder.newDocument();
	}

	private void writeToFile() throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(writeFile);
		transformer.transform(source, result);

	}

	private void writeShapesList(String listTag, List<Shape> shapes, Element father) {
		Element shapeListElement = document.createElement(listTag);
		writeShapeList(shapeListElement, shapes);
		father.appendChild(shapeListElement);
	}

	private void writeShapeList(Element shapeListElement, List<Shape> shapes) {
		for (Shape shape : shapes) {
			Element shapeElement = document.createElement("shape");
			shapeListElement.appendChild(shapeElement);
			List<Point> p = XMLShapeWriter.getPoints(shape);
			p.stream().map(this::makePointNode).forEach(shapeElement::appendChild);
		}
	}

	private void writeAlliedBoundariesList(String listTag, List<AlliedBoundary> shapes, Element father) {
		Element shapeListElement = document.createElement(listTag);
		writeAlliedBoundariesList(shapeListElement, shapes);
		father.appendChild(shapeListElement);
	}

	private void writeAlliedBoundariesList(Element listElement, List<AlliedBoundary> shapes) {
		for (AlliedBoundary shape : shapes) {
			Element shapeElement = document.createElement("shape");
			shapeElement.setAttribute("alliance", shape.getAlliance().name());
			listElement.appendChild(shapeElement);
			List<Point> p = XMLShapeWriter.getPoints(shape.getBoundary());
			p.stream().map(this::makePointNode).forEach(shapeElement::appendChild);
		}

	}

	public static List<Point> getPoints(Shape shape) {
		Point[] points = new Point[shape.getPointCount()];
		for (int p = 0; p < points.length; p++) {
			float[] rawPoint = shape.getPoint(p);
			points[p] = new Point(rawPoint[0] / DriveSimulator.scale, rawPoint[1] / DriveSimulator.scale);
		}
		return Arrays.asList(points);
	}

	public Node makePointNode(Point point) {
		Element pointElement = document.createElement("point");
		pointElement.setAttribute("x", Float.toString(point.getX()));
		pointElement.setAttribute("y", Float.toString(point.getY()));
		return pointElement;
	}

}

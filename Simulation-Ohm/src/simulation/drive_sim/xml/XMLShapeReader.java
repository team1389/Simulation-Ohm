package simulation.drive_sim.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.team1389.util.AddList;

import simulation.drive_sim.DriveSimulator;

public class XMLShapeReader {
	Document document;
	File readFile;
	boolean filePresent;

	public XMLShapeReader(String fileName) {
		readFile = new File(fileName);
		try {
			initDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Shape> getBoundaries() {
		return filePresent ? getShapes("boundaries") : new ArrayList<>();
	}

	public List<Shape> getDropoffs() {
		return filePresent ? getShapes("dropoffs") : new ArrayList<>();
	}

	public List<Shape> getPickups() {
		return filePresent ? getShapes("pickups") : new ArrayList<>();
	}

	private List<Shape> getShapes(String shapeListTag) {
		Node shapeListElement = document.getElementsByTagName(shapeListTag).item(0);
		List<Node> shapeNodes = getShapeNodes(shapeListElement);
		return shapeNodes.stream().map(this::getShapeFromNode).collect(Collectors.toList());
	}

	private Shape getShapeFromNode(Node shapeNode) {
		List<Node> pointNodes = convertToList(shapeNode.getChildNodes());
		List<Point> pointList = pointNodes.stream().map(this::pointFromNode).collect(Collectors.toList());
		AddList<Float> pointsRaw = new AddList<>();
		pointList.forEach(p -> pointsRaw.put(p.getX(), p.getY()));
		return new Polygon(convertList(pointsRaw));
	}

	private float[] convertList(List<Float> floatList) {
		float[] floatArray = new float[floatList.size()];
		int i = 0;

		for (Float f : floatList) {
			floatArray[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
		}
		return floatArray;

	}

	private Point pointFromNode(Node pointNode) {
		float x = Float.parseFloat(pointNode.getAttributes().getNamedItem("x").getNodeValue())*DriveSimulator.scale;
		float y = Float.parseFloat(pointNode.getAttributes().getNamedItem("y").getNodeValue())*DriveSimulator.scale;
		return new Point(x, y);
	}

	private List<Node> getShapeNodes(Node shapeListElement) {
		return convertToList(shapeListElement.getChildNodes()).stream().filter(n -> n.getNodeName().equals("shape"))
				.collect(Collectors.toList());
	}

	private List<Node> convertToList(NodeList nodes) {
		return IntStream.range(0, nodes.getLength()).mapToObj(i -> nodes.item(i)).collect(Collectors.toList());
	}

	private void initDocument() throws ParserConfigurationException, SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		db = dbf.newDocumentBuilder();
		try {
			document = db.parse(readFile);
			filePresent = true;
		} catch (IOException e) {
			filePresent = false;
			// e.printStackTrace();
		}
	}
}

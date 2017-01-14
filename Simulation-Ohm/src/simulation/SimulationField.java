package simulation;

import java.util.ArrayList;

import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Point;

public class SimulationField {

	ArrayList<Line> lines;
	ArrayList<Point> points = new ArrayList<Point>();
	public SimulationField(ArrayList<Line> startingBoundries){
		lines = startingBoundries;
	}
	
	public SimulationField(){
		this(new ArrayList<Line>());
	}
	
	
	
	public ArrayList<Line> getLines(){
		return lines;
	}
	
	public static Point dne = new Point(-1, -1);
	private Point startPoint = dne;
	public void addPoint(Point p){
		points.add(p);
		if (points.size() > 1) {
			Point point1 = points.get(points.size() - 1);
			Point point2 = points.get(points.size() - 2);
			if (point1 != dne && point2 != dne) {
				lines.add(new Line(point1.getX(), point1.getY(), point2.getX(), point2.getY()));
			}
			if(point1 == dne && point2 != dne){
				lines.add(new Line(point2.getX(), point2.getY(), points.get(0).getX(), points.get(0).getY()));
			}
			if(point1 != dne && point2 == dne){
				startPoint = p;
			}
		}

	}
}

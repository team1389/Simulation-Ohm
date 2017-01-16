package simulation;

import java.util.ArrayList;
import java.util.Arrays;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Polygon;

public class SimulationField {
	private static String mapPath = "pretty field.png";
	private Image fieldMap;

	private ArrayList<Polygon> boundries = new ArrayList<Polygon>();
	private ArrayList<Polygon> gearPickups = new ArrayList<Polygon>();
	private ArrayList<Polygon> gearDropoffs = new ArrayList<Polygon>();
	private ArrayList<Line> temporary = new ArrayList<Line>();

 	public SimulationField(int width, int height) {
		boundries.add(generateStartingBoundaries(width, height));
		try {
			fieldMap = new Image(mapPath).getScaledCopy(width, height);
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	private static Polygon generateStartingBoundaries(float width, float height) {
		return new Polygon(new float[]{0, 0, 0, height, width, height, width, 0});
	}

	public void render(Graphics g) {
		fieldMap.draw();
		
		g.setLineWidth(2);
		if(boundries.size() != 0){
			boundries.forEach(g::draw);
		}
		g.setColor(Color.blue);
		if(gearPickups.size() != 0){
			gearPickups.forEach(g::draw);
		}
		g.setColor(Color.green);
		if(gearDropoffs.size() != 0){
			gearDropoffs.forEach(g::draw);
		}
		g.setColor(Color.black);
		if(temporary.size() != 0){
			temporary.forEach(g::draw);
		}
		

		
	}


	private ArrayList<Point> points = new ArrayList<Point>();
	public void addPoint(Point p) {
		System.out.println(p);
		points.add(p);
		int size = points.size();
		if(size > 1){
			temporary.add(new Line(p.getX(), p.getY(), points.get(size - 2).getX(), points.get(size - 2).getY()));
		}
	}

	private Polygon finish() {
		Polygon poly = new Polygon();
		for(Point p : points){
			poly.addPoint(p.getX(), p.getY());
		}
		return poly;
	}




	public void finishBoundry() {
		if(points.size() == 0) return;
		boundries.add(finish());
		clearTemp();
	}



	public void finishGearPickup() {
		if(points.size() == 0) return;
		gearPickups.add(finish());
		clearTemp();
	}


	public void finishGearDropoff() {
		if(points.size() == 0) return;
		gearDropoffs.add(finish());
		clearTemp();
	}
	
	private void clearTemp() {
		temporary = new ArrayList<Line>();
		points = new ArrayList<Point>();
	}


	public ArrayList<Polygon> getBoundries() {
		return boundries;
	}

	public ArrayList<Polygon> getGearPickups() {
		return gearPickups;
	}

	public ArrayList<Polygon> getGearDropoffs() {
		return gearDropoffs;
	}

}

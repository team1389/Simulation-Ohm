package simulation.drive_sim.field;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;

import simulation.drive_sim.Alliance;
import simulation.drive_sim.Resources;

public class SimulationField {
	private static String mapPath = "res/pretty field.png";
	private Image fieldMap;
	private static final boolean showModels = true;
	private static Alliance myAlliance = Alliance.BLUE;

	private ArrayList<Shape> boundries = new ArrayList<>();
	private ArrayList<AlliedBoundary> gearPickups = new ArrayList<>();
	private ArrayList<AlliedBoundary> gearDropoffs = new ArrayList<>();
	private ArrayList<Line> temporary = new ArrayList<Line>();
	private DriverStation driverStation;
	private Image visibility;

	public SimulationField(int width, int height) {
		// boundries.add(generateStartingBoundaries(width, height));
		driverStation = DriverStation.Center;
		visibility = DriverStation.getVisibility(driverStation, myAlliance).getScaledCopy(width, height);
		try {
			fieldMap = new Image(mapPath).getScaledCopy(width, height);
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	public void render(Graphics g) {
		fieldMap.draw();
		if (showModels) {
			g.setLineWidth(2);
			if (boundries.size() != 0) {
				g.setColor(Color.red);
				boundries.forEach(g::draw);
			}
			gearPickups.forEach(gp -> gp.render(g, Color.orange));
			gearDropoffs.forEach(gd -> gd.render(g, Color.green));
			g.setColor(Color.black);
			temporary.forEach(g::draw);
		}
	}

	public void renderVisibility() {
		visibility.draw();
	}

	private ArrayList<Point> points = new ArrayList<Point>();

	public void addPoint(Point p) {
		points.add(p);
		int size = points.size();
		if (size > 1) {
			temporary.add(new Line(p.getX(), p.getY(), points.get(size - 2).getX(), points.get(size - 2).getY()));
		}
	}

	private Shape finish() {
		Polygon poly = new Polygon();
		for (Point p : points) {
			poly.addPoint(p.getX(), p.getY());
		}
		return poly;
	}

	public void finishBoundry() {
		if (points.size() == 0)
			return;
		addBoundary(finish());
		clearTemp();
	}

	public void finishGearPickup() {
		if (points.size() == 0)
			return;
		gearPickups.add(new AlliedBoundary(finish(), myAlliance));
		clearTemp();
	}

	static final float roomErr = 2;

	public void finishGearDropoff() {
		if (points.size() == 0)
			return;
		gearDropoffs.add(new AlliedBoundary(finish(), myAlliance));
		clearTemp();
	}

	public void addBoundary(Shape shape) {
		boundries.add(shape);
	}

	public void addPickup(AlliedBoundary shape) {
		gearPickups.add(shape);
	}

	public void addDropoff(AlliedBoundary shape) {
		gearDropoffs.add(shape);
	}

	private void clearTemp() {
		temporary = new ArrayList<Line>();
		points = new ArrayList<Point>();
	}

	public List<Shape> getBoundries() {
		return boundries;
	}

	public List<AlliedBoundary> getGearPickups() {
		return gearPickups;
	}

	public List<AlliedBoundary> getGearDropoffs() {
		return gearDropoffs;
	}

	public enum DriverStation {
		Boiler, Center, Feeder();
		public static Image getVisibility(DriverStation ds, Alliance alliance) {
			Image image = null;
			try {
				image = new Image(getVisibilityString(ds, alliance)).getFlippedCopy(alliance.isBlue(), false);
			} catch (SlickException e) {
				e.printStackTrace();
			}
			return image;
		}

		private static String getVisibilityString(DriverStation ds, Alliance alliance) {
			switch (ds) {
			case Boiler:
				return alliance.isBlue() ? Resources.ds1vis : Resources.ds3vis;
			case Center:
				return Resources.ds2vis;
			case Feeder:
				return alliance.isBlue() ? Resources.ds3vis : Resources.ds1vis;
			default:
				return Resources.ds2vis;

			}

		}

	}

}

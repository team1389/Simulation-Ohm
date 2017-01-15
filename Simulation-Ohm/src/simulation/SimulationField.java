package simulation;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Point;

public class SimulationField {
	static String mapPath = "pretty field.png";
	Image fieldMap;
	ArrayList<Line> lines;
	ArrayList<Point> points = new ArrayList<Point>();

	public SimulationField(int width, int height) {
		lines = generateStartingBoundaries(width, height);
		try {
			fieldMap = new Image(mapPath).getScaledCopy(width, height);
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	private static ArrayList<Line> generateStartingBoundaries(float width, float height) {
		ArrayList<Line> lines = new ArrayList<Line>();
		int buffer = 0;
		lines.add(new Line(buffer, buffer, buffer, height - buffer));
		lines.add(new Line(buffer, height - buffer, width - buffer, height - buffer));
		lines.add(new Line(width - buffer, height - buffer, width - buffer, buffer));
		lines.add(new Line(width - buffer, buffer, buffer, buffer));
		return lines;
	}

	public void render(Graphics g) {
		fieldMap.draw();
		getLines().forEach(g::draw);
	}

	public ArrayList<Line> getLines() {
		return lines;
	}

	public static Point DoesNotExist = new Point(-1, -1);
	private ArrayList<Point> startPoints = new ArrayList<Point>();

	public void addPoint(Point p) {
		points.add(p);
		if (points.size() > 1) {
			Point point1 = points.get(points.size() - 1);
			Point point2 = points.get(points.size() - 2);
			if (point1 != DoesNotExist) {
				if (point2 != DoesNotExist) {
					lines.add(makeLine(point1, point2));
				} else {
					startPoints.add(p);
				}
			}
			if (point1 == DoesNotExist && point2 != DoesNotExist) {
				Point temp = startPoints != null ? startPoints.get(startPoints.size() - 1) : DoesNotExist;
				lines.add(makeLine(temp, point2));
				points.set(startPoints.size() - 1, temp);
			}
		} else {
			startPoints.add(p);
		}
	}

	private Line makeLine(Point point1, Point point2) {
		return new Line(point1.getX(), point1.getY(), point2.getX(), point2.getY());

	}

	public void removeLast() {
		if (!points.isEmpty()) {
			Point temp = points.remove(points.size() - 1);
			if (temp == startPoints.get(startPoints.size() - 1)) {
				startPoints.remove(startPoints.size() - 1);
			}
			Line maybeRemove = lines.get(lines.size() - 1);
			if (maybeRemove.getX1() == temp.getX() && maybeRemove.getY1() == temp.getY()) {
				lines.remove(maybeRemove);
			} else if (maybeRemove.getX2() == temp.getX() && maybeRemove.getY2() == temp.getY()) {
				lines.remove(maybeRemove);
			}
		}
		System.out.println(points.size());
	}

}

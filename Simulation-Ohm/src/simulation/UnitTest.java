package simulation;

import java.util.List;

import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;

import com.team1389.util.AddList;

public class UnitTest {
	public static void main(String[] args) {
		new XMLShapeWriter("test.xml").writeShapes(getShapeList(), getShapeList(), getShapeList());
		System.out.println(new XMLShapeReader("test.xml").getBoundaries());
	}

	public static List<Shape> getShapeList() {
		AddList<Shape> s = new AddList<>();
		return s.put(new Polygon(new float[] { 0, 0, 0, 1, 1, 1, 1, 0 }),
				new Polygon(new float[] { 0, 0, 0, 1, 1, 1, 1, 0 }),
				new Polygon(new float[] { 0, 0, 0, 1, 1, 1, 1, 0 }));
	}
}

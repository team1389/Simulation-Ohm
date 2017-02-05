package simulation.drive_sim.robot;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.geom.Vector2f;

import com.team1389.trajectory.RigidTransform2d;
import com.team1389.trajectory.Rotation2d;
import com.team1389.trajectory.Translation2d;

import simulation.drive_sim.Alliance;
import simulation.drive_sim.DriveSimulator;
import simulation.drive_sim.DriveTrain;
import simulation.drive_sim.Resources;
import simulation.drive_sim.field.AlliedBoundary;
import simulation.drive_sim.field.SimulationField;

public class RenderableRobot extends SimulationRobot {
	private static final int gearSize = (int) (30 * DriveSimulator.scale);
	private Translation2d collisionOffset;

	public RenderableRobot(SimulationField field, DriveTrain train) {
		this(field, train, Alliance.RED);
	}

	public RenderableRobot(SimulationField field, DriveTrain train, Alliance alliance) {
		super(field, train, alliance);
		this.collisionOffset = new Translation2d();
	}

	public RigidTransform2d getStartPos() {
		return alliance == Alliance.BLUE ? startPosBlue : startPosRed;
	}

	public boolean checkCollision(Line l) {
		return getBoundingBox().intersects(l);
	}

	@Override
	public void update(double dt) {
		super.update(dt);
	//	updateCollision();
	}

	public void render(GameContainer container, Graphics g) throws SlickException {
		// Drawing robot
		robot.setRotation((float) getHeadingDegrees() + 90);
		robot.setCenterOfRotation(robotWidth / 2, robotHeight / 2);
		robot.drawCentered(getRenderX(), getRenderY());
		// Drawing gear
		if (carryingGear) {
			Image Gear = new Image(Resources.gearImage).getScaledCopy(gearSize, gearSize);
			Gear.setCenterOfRotation(gearSize / 2, gearSize / 2);
			Gear.setRotation((float) getHeadingDegrees());
			Gear.draw(getX() - gearSize / 2, getY() - gearSize / 2);
		}

	}

	private void updateCollision() {
		updateBoundaryCollision();
		updateGearCollision();
	}

	private void updateGearCollision() {
		for (AlliedBoundary gearPickup : field.getGearPickups()) {
			if (gearPickup.isRobotEligible(this) && !carryingGear) {
				System.out.println("picked up gear");
				carryingGear = true;
			}
		}

		for (AlliedBoundary gearDropoff : field.getGearDropoffs()) {
			if (gearDropoff.isRobotEligible(this) && carryingGear) {
				carryingGear = false;
				System.out.println("dropped off gear");
				gearsDelivered++;
			}
		}
	}

	private void updateBoundaryCollision() {
		Vector2f extraTranslate = new Vector2f((float) collisionOffset.getX(), (float) collisionOffset.getY());
		for (Shape p : field.getBoundries()) {
			for (int i = 0; i < p.getPointCount(); i++) {
				float[] point1 = p.getPoint(i);
				float[] point2 = p.getPoint((i + 1) % (p.getPointCount()));
				Line l = new Line(point1[0], point1[1], point2[0], point2[1]);
				while (checkCollision(l)) {
					Vector2f translateDirection = new Vector2f((float) getHeadingDegrees());
					Vector2f unitVector = translateDirection.normalise();
					Vector2f antiUnitVector = unitVector.copy().negate();
					double secondDistance = l.distance(new Vector2f(getX(), getY()).add(unitVector));
					double thirdDistance = l.distance(new Vector2f(getX(), getY()).sub(unitVector));
					if (secondDistance > thirdDistance) {
						extraTranslate = unitVector.scale(collisionReboundDistancePerTick).add(extraTranslate);
					} else {
						extraTranslate = antiUnitVector.scale(collisionReboundDistancePerTick).add(extraTranslate);
					}
				}
			}
		}
		collisionOffset = new Translation2d(extraTranslate.x, extraTranslate.y);
	}

	private float getRenderX() {
		return (float) getRenderPosition().getTranslation().getX();
	}

	private float getRenderY() {
		return (float) getRenderPosition().getTranslation().getY();
	}

	private float getHeadingDegrees() {
		return (float) getRenderPosition().getRotation().getDegrees();
	}

	private RigidTransform2d getRenderPosition() {
		RigidTransform2d shifted = new RigidTransform2d(getPose()).transformBySimple(getStartPos()).transformBySimple(
				new RigidTransform2d(collisionOffset, new Rotation2d()));
		return new RigidTransform2d(new Translation2d(shifted.getTranslation().getX() * DriveSimulator.scale,
				shifted.getTranslation().getY() * DriveSimulator.scale), shifted.getRotation());
	}

	public Polygon getBoundingBox() {
		float renderX = getRenderX();
		float renderY = getRenderY();
		Polygon r = new Polygon();
		r.addPoint(renderX - robotWidth / 2, renderY - robotWidth / 2);
		r.addPoint(renderX + robotWidth / 2, renderY - robotWidth / 2);
		r.addPoint(renderX + robotWidth / 2, renderY + robotWidth / 2);
		r.addPoint(renderX - robotWidth / 2, renderY + robotWidth / 2);
		r = (Polygon) r.transform(Transform.createRotateTransform((float) getRelativeHeadingRads(), renderX, renderY));
		return r;
	}
}

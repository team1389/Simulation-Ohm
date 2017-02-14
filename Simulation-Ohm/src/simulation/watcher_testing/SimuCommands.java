package simulation.watcher_testing;

import java.io.File;

import com.team1389.command_framework.CommandScheduler;
import com.team1389.command_framework.CommandUtil;
import com.team1389.watch.Watcher;

import simulation.Simulator;

public class SimuCommands {

	public static void main(String[] args) throws InterruptedException{
		System.setProperty("org.lwjgl.librarypath", new File("native/" + getOsName()).getAbsolutePath());
		Simulator.initWPILib();
		
		CommandScheduler scheduler = new CommandScheduler();
		scheduler.schedule(CommandUtil.createCommand(() -> false).setName("one"));
		scheduler.schedule(CommandUtil.createCommand(() -> false).setName("two")); 
		scheduler.schedule(CommandUtil.createCommand(() -> false).setName("three")); 
		scheduler.schedule(CommandUtil.createCommand(() -> false).setName("three")); 
		scheduler.schedule(CommandUtil.createCommand(() -> false));
		Watcher watcher = new Watcher();
		watcher.watch(scheduler);
		watcher.outputToDashboard();
		while(true){
			Watcher.update();
			Thread.sleep(50);
		}
		//Sorry this way of displaying watchers is ugly
	}
	
	private static String getOsName() {
		String property = System.getProperty("os.name");
		return property.toLowerCase().substring(0, property.indexOf(' '));
	}
}

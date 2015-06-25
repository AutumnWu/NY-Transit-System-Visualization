package oopfinalv2;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Painter;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.DefaultWaypoint;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import org.json.JSONObject;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultWaypoint;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.painter.CompoundPainter;

import com.vividsolutions.jts.geom.Coordinate;

import javax.swing.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
//import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
/**
 *
 * @author Autumn
 */
public class MainFrame extends JFrame{//in this frame we tried the actual map. Yet we don't have 
	//time to implement all our things to this map
	//Now we are just running Bay Max on this map as subways.
	
	static long DisplayTime=17000;
	
	static int timeSpeed=1;
	static HashMap<String,Shape> shapeMap = new HashMap<String,Shape>();//returnShapeMap()
	static HashMap<String,Coordinate> stopMap = new HashMap<String,Coordinate>(); //to store stops
	static HashMap<String, Trajectory> transit;//<trip ID:Trajectory Object>
	static Color currentColor= Color.RED;
	int nOfSubwayH = 0;
	int averageSpeedH = 0;
	int red = 0;
	int green = 0;
	final static int W = 700;
	final static int H = 700;
	final static int DELAY = 100;
	static int zoom;
	static int size;
	static CustomPainter painter;
	static HashSet<Waypoint> Waypoints=new HashSet<Waypoint>();
    public MainFrame(){
    } 
	
	public static void main(String[] args) throws InterruptedException{
		/////////////////////////////////////////
		JXMapViewer mapViewer=new JXMapViewer();
		TileFactoryInfo info=new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory=new DefaultTileFactory(info);
		mapViewer.setTileFactory(tileFactory);
		tileFactory.setThreadPoolSize(8);
		GeoPosition center=new GeoPosition(40.713065,-74.004131);
		mapViewer.setZoom(10);
		mapViewer.setAddressLocation(center);

		////////////////////////////////////////
		MainFrame frame=new MainFrame();
		frame.setSize(W, H);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		////////////////////////////////

		initialize();
		painter = new CustomPainter();
		painter.setWaypoints(Waypoints);
		
		WaypointRenderer wr = new WaypointRenderer(){
			  @Override//底下这一片会决定paint出来的是什么东西
			  public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
			        Image markerImg=new ImageIcon("C:/Users/Administrator/Desktop/2.png").getImage();
			        g.drawImage(markerImg,-5,-5,null);
			        //g.setColor(Color.RED);
			        //g.drawRect(-size, -size, 2*size, 2*size);
			        return true;
			  		}
				};
		painter.setRenderer(wr);
		//end of set renderer
				
		mapViewer.setOverlayPainter(painter);
		
		//mapViewer.addOverlayPainter(painter);
		
		frame.getContentPane().add(mapViewer);
		///////////////////////////////////////////////////////////////////////
			  
		while (true) {
			Waypoints=new HashSet<Waypoint>();
			
			for (Map.Entry<String, Trajectory> entry : transit.entrySet()) //this should paint all the stops onto the map. (despite time)
			{
				Trajectory currentTr = entry.getValue();
				Waypoint www = currentTr. returnWaypoint(DisplayTime);
				if (www!=null)
				Waypoints.add(www);
				DisplayTime=(DisplayTime+timeSpeed)%86400;
				//currentTr.returnWaypoint(DisplayTime);
			}
			
			painter.setWaypoints(Waypoints);
			
			zoom = mapViewer.getZoom();
			size = (10-zoom); 
			if (size<2){size = 1;}
			Thread.sleep(DELAY); // delay time for each cycle.
			//System.out.println(zoom);
		}
		///////////////////////////////////////////////////
	}
	
	public static void initialize(){
		transit = GTFSParser.parseTrips();
		shapeMap = GTFSParser.returnShapeMap();
		stopMap = GTFSParser.returnStopMap();
		stopInitialize();
		
		System.out.println("MainFrame Initialize Completed!");
	}
	
	public static void stopInitialize(){
		for (Map.Entry<String, Coordinate> entry : stopMap.entrySet()) //this should paint all the stops onto the map. (despite time)
		{
			Coordinate c = entry.getValue();
			Waypoints.add(new Waypoint(c.x,c.y));
		}
	}
}
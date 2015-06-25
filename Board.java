package oopVisualize;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.vividsolutions.jts.geom.Coordinate;

import testFiles.ReadCSV;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

@SuppressWarnings("serial")
public class Board extends JPanel {
	//A JPanel class that tackle visualization methods, 
	//the board updates colors and locations to display and paints on the canvas

	static double xBuffer = 74.31089000000005;
	static double yBuffer = 40.99619999999998;
	static double bufferChangeAmount = 0.02;
	static int xScale = 1075;
	static int yScale = 1075;
	static double size = 3;
	static long DisplayTime=21600;
	static int WEEKDAY = 0;//
	static String[] weektext = {"MON","TUE","WED","THU","FRI","SAT","SUN"};
	static long checkTime = 0;//help to collect data on time-nOfSubway graph
	static int timeSpeed=1;
	static HashMap<String,Shape> shapeMap = new HashMap<String,Shape>();//returnShapeMap()
	static int showMap = 0;
	static int showShape = 1;
	static int showTrip = 1;
	static int showStop = 1;
	
	static Trajectory HTr; //HTr means highlight trajectory
	
	int nOfSubwayH = 0;
	int averageSpeedH = 0;
	int red = 0;
	int green = 0;
	
	static Image img= null;
	
	static int mouseX;
	static int mouseY;
	
	JLabel timeText = new JLabel(); //used for showing time and speed
	JLabel showText = new JLabel();
	
	HashMap<String,Trajectory> transit;//<trip ID:Trajectory Object>
	HashMap<String,Trajectory> transit2;//<trip ID:Trajectory Object>
	Shape currentShape;

	public Board() { //board initialize.
		addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseX=e.getX();
				mouseY=e.getY();
				System.out.println(mouseX + " " +mouseY);
				for (Map.Entry<String, Trajectory> entry : transit.entrySet()) //this should paint all the trips onto the map. (despite time)
				{
					entry.getValue().clicked(DisplayTime, mouseX, mouseY);;
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
		
		
		addKeyListener(new KeyListener() {//keyboard 
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {//check for key press event
				Trajectory.keyPressed(e);
				Shape.keyPressed(e);
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) //
				{
					xBuffer-=bufferChangeAmount;
				}
				if (e.getKeyCode() == KeyEvent.VK_LEFT) //
				{
					xBuffer+=bufferChangeAmount;
				}		
				if (e.getKeyCode() == KeyEvent.VK_DOWN) //
				{
					yBuffer-=bufferChangeAmount;
				}
				if (e.getKeyCode() == KeyEvent.VK_UP) //
				{
					yBuffer+=bufferChangeAmount;
				}
				
				if (e.getKeyCode() == KeyEvent.VK_EQUALS) //zoom in
				{
					xScale=(int)(xScale*1.25);
					yScale=(int)(yScale*1.25);
					bufferChangeAmount *= 0.91;
					yBuffer-=bufferChangeAmount;
					xBuffer-=bufferChangeAmount;
					size*=1.1;
				}
				if (e.getKeyCode() == KeyEvent.VK_MINUS) //zoom out
				{
					xScale=(int)(xScale*0.8);
					yScale=(int)(yScale*0.8);
					bufferChangeAmount *= 1.1;
					yBuffer+=bufferChangeAmount;
					xBuffer+=bufferChangeAmount;
					size*=0.90909;
				}
				
				if (e.getKeyCode() == KeyEvent.VK_9) //speed down
				{
					if (timeSpeed > 0)
					timeSpeed = timeSpeed-1;
				}
				
				if (e.getKeyCode() == KeyEvent.VK_0) //speed up
				{
					timeSpeed = timeSpeed+1;
				}
				

				if (e.getKeyCode() == KeyEvent.VK_M) //show map
				{
					showMap = (showMap+1)%2;
				}
				
				if (e.getKeyCode() == KeyEvent.VK_T) //show trips
				{
					showTrip = (showTrip+1)%2;
				}
				
				if (e.getKeyCode() == KeyEvent.VK_S) //show shape
				{
					showShape = (showShape+1)%2;
				}
				
				if (e.getKeyCode() == KeyEvent.VK_N) //to next hour
				{
					DisplayTime=(DisplayTime+3600)%86400;
				}
				
				if (e.getKeyCode() == KeyEvent.VK_W) //to next weekday
				{
					WEEKDAY = (WEEKDAY + 1)%7;
				}
				
				
				if (e.getKeyCode() == KeyEvent.VK_P) //show stops
				{
					showStop = (showStop+1)%2;
				}
				
				if (e.getKeyCode() == KeyEvent.VK_R)//for testing
				{
					for (Map.Entry<String, Route> entry : GTFSParser.routeMap.entrySet()) //this should paint all the stops onto the map. (despite time)
					{
						String currentRId = entry.getKey();
						Route currentR = entry.getValue();
						double routeSpeed = currentR.getSpeed();
						System.out.println(currentRId+" SPEED: "+routeSpeed);
					}
				}
			}//end of key pressed
		});
		setFocusable(true);
		transit = GTFSParser.parseTrips();//get transit.
		//transit2 = GTFSParser.parseTrips2();
		shapeMap = GTFSParser.returnShapeMap();
		this.add(timeText);
		timeText.setText("Current Time: "+" Current Time Elapsing Speed:");
		timeText.setLocation(150, 50);
		timeText.setFont(timeText.getFont().deriveFont((float) 16.0));
		this.add(showText);
		showText.setText("Highlight Route:     ");//for showing the highlighted route
		img=getMap();
	}//end of board initialize
	
	@Override
	public void paint(Graphics g) { //just paint everything
		//System.out.println("Board now paint.");
		Graphics2D g2d = (Graphics2D) g;
		g.setColor(Color.DARK_GRAY);
		super.paint(g);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		if (showMap == 1){paintMap(g2d,img);}
		////////////////////////////////MAPMAPMAPMAP////////////
		
		Trajectory currentTr;
		int drawX,drawY;
		
		if (showShape == 1){
		for (Map.Entry<String, Shape> entry : shapeMap.entrySet()) //this should paint all the shapes onto the map. (despite time)
		{
			g2d.setColor(Color.LIGHT_GRAY);
			currentShape = entry.getValue();
			currentShape.paint(g2d);//
		}}
		
		if (showStop == 1){
			for (Map.Entry<String, Trajectory> entry : transit.entrySet()) //this should paint all the stops onto the map. (despite time)
			{
				currentTr = entry.getValue();
				for (Map.Entry<Long, Coordinate> e2 : currentTr.trajectory.entrySet()){//for every entry in trajectory*Long time:Coordinate*
					drawX=(int)((xBuffer+e2.getValue().x)*xScale);
					drawY=(int)((yBuffer-e2.getValue().y)*yScale);
	
					g2d.setColor(Color.LIGHT_GRAY);
					g2d.fillRect(drawX-(int)size,drawY-(int)size,2*(int)size,2*(int)size);
				}
			    //entry.getValue().paint(g2d);
			}
		}
		
		Trajectory.resetTotalSpeed(); //for speed monitoring
		
		if (Trajectory.DISPLAYMODE==2){
			for (Map.Entry<String, Trajectory> entry : transit.entrySet()) //will display under Mass mode.
			{
				currentTr = entry.getValue();
				currentTr.resetNear();
				for (Map.Entry<String, Trajectory> entry2 : transit.entrySet())
				{
					Trajectory Tr2 = entry2.getValue();
					currentTr.checkNear(Tr2,DisplayTime);
				}
			}
		}
		
		for (Map.Entry<String, Trajectory> entry : transit.entrySet()) //this should paint all the trips onto the map. (despite time)
		{
			currentTr = entry.getValue();
			currentTr.paintOnTime(g2d, (DisplayTime));
		}
		
		//System.out.println("Number of subways: "+ Trajectory.nOfSubway+" Average Speed: "+Trajectory.getAverageSpeed());
		
		nOfSubwayH = Trajectory.nOfSubway;//these 2 are for showing the speed and number of subways.
		averageSpeedH = (int)((Trajectory.getAverageSpeed()-4)*20);

		///paint 
		red = nOfSubwayH * 3;
		if (red>254){red = 255;}
		g2d.setColor(new Color(red,0,0));
		g2d.fillRect(50,500-nOfSubwayH,20,nOfSubwayH );
		
		green = (int)(averageSpeedH*3);
		if (green>254){green = 255;}
		if (green<140){green = 140;}
		if (averageSpeedH < 30){
			red = (int)((30-averageSpeedH)*8);
		}
		
		try{
		g2d.setColor(new Color(red,green,0));}
		catch(Exception e){g2d.setColor(new Color(averageSpeedH*2,0,0));}
		
		g2d.fillRect(80,500-averageSpeedH*2,20,averageSpeedH*2);
		
		int h = (int)(DisplayTime/3600); ////the following will show the correct current time
		int m = (int)((DisplayTime-h*3600)/60);
		int s = (int)((DisplayTime-h*3600)-m*60);
		String hh = h<10? "0"+h:""+h;
		String mm = m<10? "0"+m:""+m;
		String ss = s<10? "0"+s:""+s;
		String timeString = hh+":"+mm+":"+ss;
		timeText.setText(weektext[WEEKDAY]+" "+timeString+" Current Time Elapsing Speed: "+timeSpeed+" Number Of Subways: "+Trajectory.nOfSubway);
		
		DisplayTime=(DisplayTime+timeSpeed)%86400; //change time
		
		//checkTime += timeSpeed;
		//if (DisplayTime<21590&&DisplayTime>21500){timeSpeed = 0;}///////only for testing. Will help get info about the speed of routes.
		
		//System.out.println("time: " +(DisplayTime));
		//This part is for collecting data. Only for testing
		/* This part can be used for collecting data on subway number of speed.
		if (checkTime>600){
			System.out.println(timeString+" "+Trajectory.nOfSubway+" "+Trajectory.averageSpeed);
			checkTime = checkTime % 600;
		}
		*/
		if (HTr!=null)
		{
		HTr.paintHighlight(g2d,DisplayTime); //paint the highlighted trip.
		showText.setText("Highlight Route: "+GTFSParser.routeMap.get(HTr.routeId).name);
		}
		
		try{
		Thread.sleep(Main.DELAY); // delay time for each cycle.
		repaint();
		}catch(Exception e){}
	}//end of paint
	
	public int getDisplayX(double x){
		return (int)((xBuffer+x)*xScale);		
	}
	
	public int getDisplayY(double y){
		return (int)((yBuffer-y)*yScale);
	}
	
	public Image getMap(){ //Will display a map downloaded online. But cannot support zoom in/out
		Image img = null;
	     try{
	         URL imageURL = new URL("http://maps.google.com/maps/api/staticmap?center=(40.712582,-74.009781)&zoom=10&size=640x640&maptype=roadmap");
	         // Case 1
	         img = ImageIO.read(imageURL);
	         }catch(Exception e)
	     		{
	        	 System.out.println("Wrong!");}
	     return img;
	}
	
	public void paintMap(Graphics2D g2d,Image img){ // paint the map. not supporting zoom in/out
	     g2d.drawImage(img,0,0,null);
	     }
}
	
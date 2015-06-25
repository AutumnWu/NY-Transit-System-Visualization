package oopVisualize;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import com.vividsolutions.jts.geom.Coordinate;

public class Trajectory {///a trajectory is about a trip.
	//Each Trajectory object corresponds to a trip, 
	//along with its waypoints and timestamps. The major storage place 
	//for processed data. Board will refer to the information in all
	//the Trajectory object to paint vehicles to their proper locations.
	
	static double xBuffer = 74.31089000000005;//buffers to paint map to correct position
	static double yBuffer = 40.99619999999998;
	static double bufferChangeAmount = 0.02;
	static int xScale = 1075; //decide how big everything is.
	static int yScale = 1075;
	static double baseSize = 6;//will add another size to help with this basesize.
	static int DISPLAYMODE = 0;
	static int nOfSubway = 0; //number of running subways
	static double totalSpeed = 0;
	static double averageSpeed = 0;
	static HashMap<String,Route> routeMap = new HashMap<String,Route>();
	
	TreeMap<Long,Integer> speeds;//use for speed visualization
	int speed;
	
	long startTime=0;
	long endTime=86399;//start and end time of a trip!
	
	String routeId;
	int directionId;
	String shapeId;
	
	String tripId;
	String serviceId;
	
	////used for getting current speed.//
	long lastTime=0;
	long timeInterval=0;
	double lastX;
	double lastY;
	double distance=0.0;
	Coordinate lastCoordinate = new Coordinate(0,0);
	///// speed = distance / time;
	
	TreeMap<Long, Coordinate> trajectory; //long is for time//
	TreeMap<Long, Coordinate> waypoints; //long is for time//in waypoints *time-Coordinate*
	
	TreeMap<Long, int[]>nearestTwoPoints;//the nearest two points to a stop
	
	Coordinate currentDisplayC;
	double currentDisplayX;//
	double currentDisplayY;
	int drawX=0;
	int drawY=0;
	
	int clicked = 0;
	
	int near=0;
	
	///////color in normal mode!
	Random r;
	Color randomColor;
	///////
	
	
	////////////////The third algorithm///////////////
	Coordinate A;//
	Coordinate B;
	long tA,tB,t;
	double XA,XB,YA,YB;
	
	////////////////The third algorithm ends///////////////
	
	//########################################################################//
	
	public Trajectory(){
		///////
		r = new Random();
		randomColor = new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255));
		///////a random color is formed when a trip is initialized

		trajectory = new TreeMap<Long, Coordinate>();
		waypoints = new TreeMap<Long, Coordinate>();
		nearestTwoPoints = new TreeMap<Long, int[]>();
		speeds = new TreeMap<Long,Integer>();
	}
	
	public Coordinate getPosition(Long time)
	{//get the position of the vehicle at that timestamp		
		return null;
	}
	
	public void setTrajectory(String routeId,String serviceId,String tripId,int directionId,String shapeId){
		this.routeId=routeId;
		this.serviceId=serviceId;
		this.tripId=tripId;
		this.directionId=directionId;
		this.shapeId=shapeId;
	}
	
	public void addStop(Long time,Coordinate C){//add a new <*key=time*,*value = coordinate*>
		trajectory.put(time,C);
	}
	
	public void addWaypoint(Long time,Coordinate C){//add a new <*key=time*,*value = coordinate*>
		waypoints.put(time,C);
	}
	
	public void addNearestIndex(long time, int i1, int i2){
		nearestTwoPoints.put(time,new int[]{i1,i2});
		//System.out.println("NeareatTwoPointsIndexadded, time: "+time+" i1: "+i1+" i2: "+i2);
	}
	
	public int[] getNearestIndex(long time){//will return two nearest points to a stop
		return nearestTwoPoints.get(time);
	}
	
	public void paintHighlight(Graphics2D g2d,long time){
		if (drawX!=0&&Board.showTrip == 1&&time<endTime&&time>startTime&&GTFSParser.serviceMap.get(serviceId).checkActive(Board.WEEKDAY))//只有当现在时间这一趟trip在跑的情况下才会paint出来
		{
			g2d.setColor(Color.BLUE);
			g2d.fillOval(drawX-(int)baseSize-1,drawY-(int)baseSize-1,2*(int)baseSize+2,2*(int)baseSize+2);
			GTFSParser.shapeMap.get(shapeId).paintHighlight(g2d);
		}
	}
	
	public boolean checkActive(long time){
		if (time<endTime&&time>startTime&&GTFSParser.serviceMap.get(serviceId).checkActive(Board.WEEKDAY)){
			return true;
		}
		return false;
	}
	
	public void paintOnTime(Graphics2D g2d,long time)
	{
		if (time<endTime&&time>startTime&&GTFSParser.serviceMap.get(serviceId).checkActive(Board.WEEKDAY))//只有当现在时间这一趟trip在跑的情况下才会paint出来
		{
			tA = waypoints.floorKey(time);
			tB = waypoints.ceilingKey(time);//tB>tA
			//speed = speeds.floorEntry(time).getValue();//so speed = viechle's speed between A->B 
			//change this to new speed method now.
			if (tA==tB)
			{
				currentDisplayC = waypoints.get(tA);
				currentDisplayX = currentDisplayC.x;
				currentDisplayY = currentDisplayC.y;
			}else
			{
				A = waypoints.get(tA);
				B = waypoints.get(tB);
				XA = A.x;
				YA = A.y;
				XB = B.x;
				YB = B.y;
				currentDisplayX =XA+((double)(time-tA)/(double)(tB-tA))*(XB-XA); //algorithm!
				currentDisplayY =YA+((double)(time-tA)/(double)(tB-tA))*(YB-YA);
				currentDisplayC = new Coordinate(currentDisplayX,currentDisplayY);
			}

			timeInterval = time-lastTime;
			distance = currentDisplayC.distance(lastCoordinate);
			if (Board.timeSpeed !=0){
			double speedToUpdate = (distance/timeInterval*100000);
			
			/*//In this part we let the program run for 24 hours to get the data for the speed of each route.
			if (speedToUpdate>20){speedToUpdate = 20;}
			if (speedToUpdate<3){speedToUpdate =3;}
			if (Board.timeSpeed != 0){
			routeMap.get(routeId).updateSpeed(speedToUpdate);}
			*////only for testing
			
			speed = (int) speedToUpdate; // calculate speed in real time
			}
			if (speed>20){speed = 20;}
			if (speed<3){speed =3;}
			
			drawX=(int)((xBuffer+currentDisplayX)*xScale);//drawx and drawy are the x,y for drawing on the screen
			drawY=(int)((yBuffer-currentDisplayY)*yScale);
			
			if (DISPLAYMODE==0)
			{g2d.setColor(randomColor);}else if (DISPLAYMODE==1){
			g2d.setColor(SpeedColor.getColor(speed));//testing!!!!
			}else if (DISPLAYMODE==2){
				g2d.setColor(MassColor.getColor(near));
			}
			
			if (drawX!=0&&Board.showTrip == 1){
				if (DISPLAYMODE!=2){
					g2d.fillOval(drawX-(int)baseSize,drawY-(int)baseSize,2*(int)baseSize,2*(int)baseSize);
				}else{
					g2d.fillOval(drawX-(int)baseSize-2*near,drawY-(int)baseSize-2*near,2*(int)baseSize+4*near,2*(int)baseSize+4*near);
				}
			}

			lastTime = time;
			lastCoordinate = currentDisplayC; //update the time and location for last time so that we can calculate speed.
			
			nOfSubway += 1;
			totalSpeed += speed;//
			//System.out.println(near);
		}//if in time
	}//end of paint on time
	
	public void setStartEnd(){//这个方法是在trajectory，也就是stops已经输入了之后再执行的，它会通过treemap的结构找到这个trip的起止时间
		//set the start and end time of this trip.
		startTime = trajectory.firstKey();//由于treemap结构可以直接找到最大最小的key，而key又是直接的时间值，所以可以这样执行
		//advantage of treemap.
		endTime = trajectory.lastKey();
		//System.out.println("Start at: "+startTime+" end at: "+endTime);
	}
	
	public static void keyPressed(KeyEvent e){
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
		
		if (e.getKeyCode() == KeyEvent.VK_EQUALS) //
		{
			xScale=(int)(xScale*1.25);
			yScale=(int)(yScale*1.25);
			bufferChangeAmount *= 0.91;
			yBuffer-=bufferChangeAmount;
			xBuffer-=bufferChangeAmount;
			baseSize*=1.1;
		}
		if (e.getKeyCode() == KeyEvent.VK_MINUS) //
		{
			xScale=(int)(xScale*0.8);
			yScale=(int)(yScale*0.8);
			bufferChangeAmount *= 1.1;
			yBuffer+=bufferChangeAmount;
			xBuffer+=bufferChangeAmount;
			baseSize*=0.90909;
		}
		if (e.getKeyCode() == KeyEvent.VK_C) //
		{
			DISPLAYMODE = (DISPLAYMODE+1)%3;
		}
	}//end of keypressed
	
	public void clicked(long time,int mouseX,int mouseY){///////////////////////////MOUSE EVENT!
		if (time<endTime&&time>startTime){
			if (mouseX>drawX-baseSize&&mouseX<drawX+baseSize&&mouseY>drawY-baseSize&&mouseY<drawY+baseSize){//if mouse in area
				Board.HTr=this;
			}
		}
	}
	
	public static void resetTotalSpeed(){
		totalSpeed = 0;
		nOfSubway = 0;
	}
	
	public static double getAverageSpeed(){
		if ( nOfSubway != 0){
		averageSpeed = totalSpeed / nOfSubway;}else{averageSpeed=0;}
		if (averageSpeed <4){averageSpeed=4;}
		return averageSpeed;
	}
	public static void getRouteMap(HashMap<String,Route> rMap){//this enables all tr to refer to routeMap
		routeMap = rMap;
	}
	
	public void resetNear(){//reset the nubmer of subways near (in Mass mode.)
		near = 0;
	}
	
	public void checkNear(Trajectory Tr2,long time){//supporting Mass mode.
		if (Tr2.checkActive(time)){
			if(getBounds().intersects(Tr2.getBounds())){ //if two subways within communicate area
				near += 1;
			}	
		}
	}
	
	public Rectangle getBounds() {//used for mouse event!
		return new Rectangle((int)(drawX-1.5*baseSize), (int)(drawY-1.5*baseSize),(int) (3*baseSize), (int)(3*baseSize));
	}
	
}

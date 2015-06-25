package oopfinalv2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdesktop.swingx.mapviewer.Waypoint;

import com.vividsolutions.jts.geom.Coordinate;

public class Trajectory {///a trajectory is about a trip.
	
	static double xBuffer = 74.14;
	static double yBuffer = 40.91;
	static double bufferChangeAmount = 0.01;
	static int xScale = 2100;
	static int yScale = 2100;
	static double baseSize = 6;//will add another size to help with this basesize.
	static int DISPLAYMODE = 0;
	static int nOfSubway = 0; //number of running subways
	static double totalSpeed = 0;
	static double averageSpeed = 0;
	
	TreeMap<Long,Integer> speeds;//use for speed visualization
	int speed;
	
	long startTime=0;
	long endTime=86399;//������ֵ����һ��trip����ֹʱ��
	
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
	TreeMap<Long, Coordinate> waypoints; //long is for time//waypoints����ͻ���Ŵ������shape��*time-Coordinate*
	
	TreeMap<Long, int[]>nearestTwoPoints;//���ﴢ�������ĳһ��stop���������·�����index
	
	Coordinate currentDisplayC;
	double currentDisplayX;//���������һ��Ҫ�õ��ģ�������trajectory�ǿ���display�Ļ�����ô�������ֵ��
	double currentDisplayY;
	int drawX=0;
	int drawY=0;
	
	///////��������ɫ����
	Random r;
	Color randomColor;
	///////
	
	
	////////////////���������㷨��ʼ///////////////
	Coordinate A;//���������ڵ��������㷨��Ҳ���ǵ�ĳһ�ض�ʱ�䲢û�о�ȷ��waypoint��֮��Ӧʱ
	Coordinate B;
	long tA,tB,t;
	double XA,XB,YA,YB;
	
	////////////////���������㷨����///////////////
	
	//########################################################################//
	
	public Trajectory(){
		///////��������ɫ����
		r = new Random();
		randomColor = new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255));
		///////Ҳ����˵ÿ��Tr����ʱ�Զ�����һ�������ɫ

		trajectory = new TreeMap<Long, Coordinate>();
		waypoints = new TreeMap<Long, Coordinate>();
		nearestTwoPoints = new TreeMap<Long, int[]>();
		speeds = new TreeMap<Long,Integer>();
	}
	
	public Coordinate getPosition(Long time)
	{//get the position of the vehicle at that timestamp		
		return null;
	}
	
	public boolean isActive(Long time)
	{
		return true;
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
	
	public int[] getNearestIndex(long time){//��������ĳstop���������point��index����
		return nearestTwoPoints.get(time);
	}

	public Color returnColor(long time){
		if (time<endTime&&time>startTime)
			return randomColor;
		return null;
	}
	
	public Waypoint returnWaypoint(long time){
		if (time<endTime&&time>startTime)//ֻ�е�����ʱ����һ��trip���ܵ�����²Ż�paint����
		{
			tA = waypoints.floorKey(time);
			tB = waypoints.ceilingKey(time);//�Ѿ�������tB>tA
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
				currentDisplayX =XA+((double)(time-tA)/(double)(tB-tA))*(XB-XA);
				currentDisplayY =YA+((double)(time-tA)/(double)(tB-tA))*(YB-YA);
				currentDisplayC = new Coordinate(currentDisplayX,currentDisplayY);
			}
			
			timeInterval = time-lastTime;
			distance = currentDisplayC.distance(lastCoordinate);
			if (MainFrame.timeSpeed !=0){
			speed = (int) (distance/timeInterval*100000); // calculate speed in real time
			}
			if (speed>20){speed = 20;}
			drawX=(int)((xBuffer+currentDisplayX)*xScale);
			drawY=(int)((yBuffer-currentDisplayY)*yScale);
						
			/*
			if (waypoints.get(time)!=null)
			{//�����ĳһʱ����һ�г��պ���ĳ��waypoint�Ļ����͸��������꣨���ڻ�û�м���������㷨��
				currentDisplayC = waypoints.get(time);
				currentDisplayX = currentDisplayC.x;
				currentDisplayY = currentDisplayC.y;
				drawX=(int)((currentDisplayX+xBuffer)*xScale);
				drawY=(int)((currentDisplayY+yBuffer)*yScale);
			}else{//�����ǵ��������㷨����Ҫ���ݣ�����������
				*/
			
			lastTime = time;
			lastCoordinate = currentDisplayC; //update the time and location for last time so that we can calculate speed.
		
			nOfSubway += 1;
			totalSpeed += speed;//ÿһ��������ʾ�ĵ���������뵽�µ����ٶ�����
			return new Waypoint(currentDisplayX,currentDisplayY);
	}
		return null;
	}
	
	public void paintOnTime(Graphics2D g2d,long time)
	{
		if (time<endTime&&time>startTime)//ֻ�е�����ʱ����һ��trip���ܵ�����²Ż�paint����
		{
			tA = waypoints.floorKey(time);
			tB = waypoints.ceilingKey(time);//�Ѿ�������tB>tA
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
				currentDisplayX =XA+((double)(time-tA)/(double)(tB-tA))*(XB-XA);
				currentDisplayY =YA+((double)(time-tA)/(double)(tB-tA))*(YB-YA);
				currentDisplayC = new Coordinate(currentDisplayX,currentDisplayY);
			}
			
			timeInterval = time-lastTime;
			distance = currentDisplayC.distance(lastCoordinate);
			if (MainFrame.timeSpeed !=0){
			speed = (int) (distance/timeInterval*100000); // calculate speed in real time
			}
			if (speed>20){speed = 20;}
			drawX=(int)((xBuffer+currentDisplayX)*xScale);
			drawY=(int)((yBuffer-currentDisplayY)*yScale);
			if (DISPLAYMODE==0)
			{g2d.setColor(randomColor);}else if (DISPLAYMODE==1){
			g2d.setColor(SpeedColor.getColor(speed));//testing!!!!
			}
			if (drawX!=0){g2d.fillOval(drawX-(int)baseSize,drawY-(int)baseSize,2*(int)baseSize,2*(int)baseSize);}
			
			/*
			if (waypoints.get(time)!=null)
			{//�����ĳһʱ����һ�г��պ���ĳ��waypoint�Ļ����͸��������꣨���ڻ�û�м���������㷨��
				currentDisplayC = waypoints.get(time);
				currentDisplayX = currentDisplayC.x;
				currentDisplayY = currentDisplayC.y;
				drawX=(int)((currentDisplayX+xBuffer)*xScale);
				drawY=(int)((currentDisplayY+yBuffer)*yScale);
			}else{//�����ǵ��������㷨����Ҫ���ݣ�����������
				*/
			
			lastTime = time;
			lastCoordinate = currentDisplayC; //update the time and location for last time so that we can calculate speed.
		
			nOfSubway += 1;
			totalSpeed += speed;//ÿһ��������ʾ�ĵ���������뵽�µ����ٶ�����
		}//if in time
	}//end of method
	
	public void setStartEnd(){//�����������trajectory��Ҳ����stops�Ѿ�������֮����ִ�еģ�����ͨ��treemap�Ľṹ�ҵ����trip����ֹʱ��
		startTime = trajectory.firstKey();//����treemap�ṹ����ֱ���ҵ������С��key����key����ֱ�ӵ�ʱ��ֵ�����Կ�������ִ��
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
			DISPLAYMODE = (DISPLAYMODE+1)%2;
		}
	}//end of keypressed
	
	public void clicked(){///////////////////////////
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
	
}

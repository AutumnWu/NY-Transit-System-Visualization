package oopfinalv2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import testFiles.ReadCSV;

public class GTFSParser { //ArrayList<Trajectory> parseTrips(String trip File)

	static HashMap<String,Trajectory> transit = new HashMap<String,Trajectory>();//give back a list of trajectories		
	static HashMap<String,Coordinate> stopMap = new HashMap<String,Coordinate>(); //to store stops
	static HashMap<String,Shape> shapeMap = new HashMap<String,Shape>();
	
	static double minX=99999, minY=99999,maxX=-9999,maxY=-9999,middleX=0,middleY=0;
	
	public static HashMap<String,Trajectory> parseTrips(){ // 每次有一个新的tripid就加一个新的trajectory
		ArrayList<String[]> stops = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/stops.txt");
		ArrayList<String[]> trips = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/trips.txt");
		ArrayList<String[]> stopTimes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/stop_times.txt");
		ArrayList<String[]> shapes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/shapes.txt");
		ArrayList<String[]> calendar = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/calendar.txt");
		ArrayList<String[]> calendar_dates = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/calendar_dates.txt");
		ArrayList<String[]> transfers = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/transfers.txt");
		ArrayList<String[]> routes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/routes.txt");
		
		tripInitialize(transit,trips);//处理完trips.txt
		stopInitialize(stopMap,stops);
		shapeInitialize(shapeMap,shapes);
		System.out.println("MIN: "+ minX+"   "+minY);
		System.out.println("MAX: "+ maxX+"   "+maxY);
		System.out.println("MIDDLE: "+ ((maxX+minX)/2 )+"   "+((maxY+minY)/2 ));
		
		stopTimeInitialize(transit,stopMap,stopTimes);
		projectStop(shapeMap,transit);
		setTimestamp(shapeMap,transit);
		return transit;//transit<tripId,Trajectory>//
	}
	
	/*
	public static HashMap<String,Trajectory> parseTrips2(){ // 每次有一个新的tripid就加一个新的trajectory
		ArrayList<String[]> stops = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/stops.txt");
		ArrayList<String[]> trips = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/trips2.txt");
		ArrayList<String[]> stopTimes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/stop_times.txt");
		ArrayList<String[]> shapes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/shapes.txt");
		ArrayList<String[]> calendar = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/calendar.txt");
		ArrayList<String[]> calendar_dates = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/calendar_dates.txt");
		ArrayList<String[]> transfers = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/transfers.txt");
		ArrayList<String[]> routes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/routes.txt");
		
		tripInitialize(transit,trips);//处理完trips.txt
		stopInitialize(stopMap,stops);
		shapeInitialize(shapeMap,shapes);
		System.out.println("MIN: "+ minX+"   "+minY);
		System.out.println("MAX: "+ maxX+"   "+maxY);
		System.out.println("MIDDLE: "+ ((maxX+minX)/2 )+"   "+((maxY+minY)/2 ));
		
		stopTimeInitialize(transit,stopMap,stopTimes);
		projectStop(shapeMap,transit);
		setTimestamp(shapeMap,transit);
		return transit;//transit<tripId,Trajectory>//
	}
*/
	public static void projectStop(HashMap<String,Shape> shapeMap,HashMap<String,Trajectory> transit)
	{//会把每个stop的位置都投影到shape的路径上面
		Trajectory tr;
		Coordinate C0=new Coordinate(0,0);
		Coordinate C1=new Coordinate(0,0);
		Coordinate C2=new Coordinate(0,0);
		double a,b,c;
		Coordinate Cmin1=new Coordinate();//the current two points that form a line that is closest to this stop
		Coordinate Cmin2=new Coordinate();
		Coordinate Cnew;
		double minDistance=99999; // current minimal distance
		double distance;
		ArrayList<Coordinate> coordinates;
		int pointCount=0;
		int index1=0,index2=0;//这两个数字指代离某一个stop最近的两个路径点在对应的shape对象里面的index
		
		for (Map.Entry<String, Trajectory> entry : transit.entrySet()) //############EEERROORORRRR
		{
		    tr = entry.getValue(); //get a trip
		    coordinates = shapeMap.get(tr.shapeId).coordinates;//得到该trip对应的shape里的那一串点
		    pointCount = shapeMap.get(tr.shapeId).pointCount;
		    long time=0;//use this to refer to a stop in the trajectory
		    for (Map.Entry<Long, Coordinate> e2 : tr.trajectory.entrySet()) //
			{
		    	time = e2.getKey();
				C0 = e2.getValue(); //get a the coordinate of a stop (for mapping it to a line of shape)
				//System.out.println("Inaccurate Stop Location: x: "+C0.x+" y: "+C0.y);
				minDistance = 99999;//refresh minDistance
				
				for (int i=0;i<pointCount-1;i++)//i 是指代某一个shape对象里面的i个路径点
				{
					C1 = coordinates.get(i);
					C2 = coordinates.get(i+1);
					a=C1.distance(C2)*C1.distance(C2);
					b= C0.distance(C1);
					c =C0.distance(C2);
					distance = (b+c)*(b+c)-a*a;
					if (distance < minDistance)
					{
						minDistance = distance;
						Cmin1 = C1;
						Cmin2 = C2;
						index1 = i;
						index2 = i+1;
					}
				}//这个for运行完了之后距离某stop最短的两个点就找到了
				
			    Cnew = (new LineSegment(Cmin1,Cmin2)).project(C0); //完成project
			    //System.out.println("NEW Stop moved: +x: "+(Cnew.x-C0.x)+" +y: "+(Cnew.y-C0.y));
			    C0=Cnew;//这个可以代替下面一行么..?
			    //tr.trajectory.put(time,Cnew);//更新这一个Trajectory里面的这一个stop的位置
			    tr.addNearestIndex(time,index1,index2);//把最近的两个路径点的index加到这个Trajectory里面，以后会用到（第二个数学方程）
			}//for stop
		}//for trip
	}

	public static void shapeInitialize(HashMap<String,Shape> shapeMap,ArrayList<String[]> shapes){
		String currentShapeId="zzz";
		double currentX;
		double currentY;
		//int newX;
		//int newY;
		int i = 1;
		Shape currentShape = new Shape("zzz"); // the first Shape will not be used.
		String[] currentRawShape=shapes.get(i);
	
		for (i=1;i<shapes.size();i++){
			if (! shapes.get(i)[0].equals(currentShapeId)){//if a new shape starts
				currentShapeId = shapes.get(i)[0];//update currentShapeId 
				currentShape = new Shape(currentShapeId); //if id changed then create a new Shape
				shapeMap.put(currentShapeId,currentShape); //add this shape to array
			}
			currentRawShape=shapes.get(i); //an array of string
			currentX=Double.parseDouble(currentRawShape[1]);
			currentY=Double.parseDouble(currentRawShape[2]);
			//newX=(int)((currentX+xBuffer)*xScale); //get
			//newY=(int)((currentY+yBuffer)*yScale);// get
			currentShape.addPoint(currentX,currentY); // add a new point to current shape
		
			
		}
	}
	
	public static void stopTimeInitialize(HashMap<String,Trajectory> transit,
			HashMap<String,Coordinate> stopMap,ArrayList<String[]> stopTimes){ //has to be called after trips initialized
		int i =1;
		String tripId;
		long time=0;
		String rawTime;
		String stopId;
		String[] rawStopTime;
		Trajectory tr=new Trajectory();
		String currentTripId="zzz";
		for (i=1;i<stopTimes.size();i++){
			//try{
				//so that we don't have an error
				rawStopTime = stopTimes.get(i);
				tripId=rawStopTime[0];
				if (!currentTripId.equals(tripId)){//reading stop time for a new trip
					tr = transit.get(tripId); //get new trip
					currentTripId = tripId;//update tripId
				}
				rawTime=rawStopTime[1]; //
				time = convertTime(rawTime); // get new time
				stopId=rawStopTime[3];
				if (tr!=null&&stopMap.get(stopId)!=null)
				{
					tr.addStop(time, stopMap.get(stopId));//add this stop to the trip it relates to.
				}
			//}catch(Exception e){
			//	System.out.println("Invalid Input in stop times!");
			//}
		}
	}
	
	public static void stopInitialize(HashMap<String,Coordinate> stopMap,ArrayList<String[]> stops){//把stopMap填好
		String currentStopId="zzz";
		double currentX;
		double currentY;
		int newX;
		int newY;
		int i = 1;
		Stop currentStop;
		String[] currentRawStop;
	
		for (i=1;i<stops.size();i++){
			currentStop = new Stop();
			currentRawStop=stops.get(i);
			currentStopId = currentRawStop[0];//update currentShapeId
			currentX=Double.parseDouble(currentRawStop[4]);
			currentY=Double.parseDouble(currentRawStop[5]);

			//newX=(int)((currentX+xBuffer)*xScale);
			//newY=(int)((currentY+yBuffer)*yScale);
			//currentStop.addStop(newX,newY,currentStopId);
			stopMap.put(currentStopId,new Coordinate(currentX,currentY)); //put <*key=stopid*,*value=Coordinate*> into map
			//currentX&Y are values on planet Earth
		}
	}
	
	public static void tripInitialize(HashMap<String,Trajectory> transit ,ArrayList<String[]> trips){//把transit list里面填好！
		String routeId;
		String serviceId;
		String tripId;
		int directionId;
		String shapeId;
		int i = 1;
		Trajectory currentTrajectory;
		String[] currentRawTrip;
	
		for (i=1;i<trips.size();i++){
			try{
			currentTrajectory = new Trajectory();//new trip
			currentRawTrip=trips.get(i);//get raw data in string
			
			routeId=currentRawTrip[0];
			serviceId=currentRawTrip[1];
			tripId=currentRawTrip[2];
			directionId=Integer.parseInt(currentRawTrip[4]);
			shapeId=currentRawTrip[6];
  
			currentTrajectory.setTrajectory(routeId,serviceId,tripId,directionId,shapeId);
			transit.put(tripId,currentTrajectory); //put stop into map
			}catch(Exception e){
				//System.out.println("Invalid Input in trip.");
			}
		}
	}
	
	public static int convertTime(String string){ // will take a time string and return (int) time in seconds
		int timeInSecond;
		String[] s2;
		int h;
		int m;
		int s;
		s2=string.split(":");
		h = Integer.parseInt(s2[0]);
		m = Integer.parseInt(s2[1]);
		s = Integer.parseInt(s2[2]);
		timeInSecond = h*3600+m*60+s;
		return timeInSecond;
	}
	
	public static void setTimestamp(HashMap<String,Shape> shapeMap,HashMap<String,Trajectory> transit)
	{//这个里面现在有第二部分的算法，第三部分还没有加入进来。
		Trajectory tr;
		int nOfTrip=0;//这个是测试时用来记录程序进度的
		int wait=0;
		for (Map.Entry<String, Trajectory> entry : transit.entrySet()) //####
		{//这个for结构里面是每一个tr
		    tr = entry.getValue(); //get a new tr Object
		    Coordinate A = null;
		    Coordinate B = null;
		    long tA=0,tB=0;//time of A, time of B
		    long tAB; //两站之间间隔的时间
		    int iA1=0,iA2=0,iB1=0,iB2=0;
		    int startIndex,endIndex; //这两个是AB两个stop之间那些shape点的开始和结束点的index
		    int[] indexArray; //这个里面放4个index然后通过sort找到第2，3个
		    int direction; //这个是用来看到底A先还是B先 这个会决定是A然后那一串shape点还是说先B然后那一串shape点
		    for (Map.Entry<Long, Coordinate> e2 : tr.trajectory.entrySet()) //
			{//这个for结构里面是每一个B(也就是A,B点组合)这个部分是对的
		    	if (A==null)
		    	{//这个是只在这个forloop的第一次loop执行的
			    	tA = e2.getKey();
					A = e2.getValue(); //get a the coordinate of a stop (for mapping it to a line of shape)
					iA1 = tr.getNearestIndex(tA)[0];
					iA2 = tr.getNearestIndex(tA)[1];
		    	}else
		    	{//这里都是正常的部分
		    	
		    	tB = e2.getKey();
				B = e2.getValue();
				iB1 = tr.getNearestIndex(tB)[0];
				iB2 = tr.getNearestIndex(tB)[1];
				if ((iA1+iA2)<(iB1+iB2)){direction = 1;}else{direction = -1;}
				
				tAB = tB-tA;
				indexArray = new int[]{iA1,iA2,iB1,iB2};
				Arrays.sort(indexArray);
				startIndex = indexArray[1];
				endIndex = indexArray[2];//到这里就已经找到始终点的index了
				
				Coordinate[] cArray = new Coordinate[1000];
				int aj=1;
				if (direction == 1){cArray[0]=A;}else{cArray[0]=B;}
				for (int j=startIndex; j<=endIndex;j++)
				{
					cArray[aj]=shapeMap.get(tr.shapeId).coordinates.get(j);
					aj++;
				}
				//System.out.println("aj="+aj);
				//System.out.println("Point A: time: "+tA+" Coordinate: "+"x:"+A.x+" y:"+A.y);
				//System.out.println("Point B: time: "+tB+" Coordinate: "+"x:"+B.x+" y:"+B.y);
				if (direction == 1){cArray[aj]=B;}else{cArray[aj]=A;}
				//这个loop结束之后cArray就装好了包括A,B在内的所有AB之间的路径点了。
				
				cArray = Arrays.copyOfRange(cArray,0,aj+1);//有这一步是因为之前尝试的时候发现cArray必须不能有空的部分，有的话就会报错
				CoordinateArraySequence cas1 = new CoordinateArraySequence(cArray);
				
				LineString ls1=new LineString(cas1,new GeometryFactory());
				double dAB = ls1.getLength();//这样就取得了dAB，也就是AB间的距离。
				
				//////////inbetween:speedSize testing code//////////
				if (tAB!=0&&dAB!=0){tr.speeds.put(tA,(int)(dAB/tAB*100000));}
				//System.out.println("Trip ID: "+tr.tripId+ "Speed: "+(int)(dAB/tAB*100000));
				//////////inbetween:speedSize testing code//////////
				
				double dCurrent = 0.0;
				tr.addWaypoint(tA,cArray[0]);//这是先把cArray里面的第一个Coordinate加入waypoints，因为下一步里面，第一个点会被漏掉
				for (int k=0;k<aj;k++){//0->(aj-1)每次把p1，p2中的p2的坐标加到waypoints里面，所以第一个点是会漏掉的
					Coordinate p1 = cArray[k];
					Coordinate p2 = cArray[k+1];
					
					double d1 = p1.distance(p2);
					dCurrent += d1;
					long tAA;
					if (direction==1){tAA=tA;}else{tAA=tB;}
					long t1 = (long) (tAA+(dCurrent/dAB)*tAB);
					tr.addWaypoint(t1, p2);
					//System.out.println("Add new waypoint: time: "+t1+" Coordinate: "+"x:"+p2.x+" y:"+p2.y);
				}
				
				//System.out.println(dAB);//###TEST###
				
				A=B;//这里是把B的值给A这样下一轮的时候B就变成了下一个点，A则是上一次的第二个点
				tA=tB;//总之这一步是必要的
				iA1=iB1;
				iA2=iB2;
		    	}//这里是最上面那个else的结束括号
			}
		    nOfTrip+=1;
		    wait += 1;
		    if (wait >200){
		    	System.out.println("%"+(nOfTrip/110)+" is finished.");
		    	wait=0;
		    }
		    tr.setStartEnd();//这个可以让每个trip都设定好起止时间。
		}//
	}
	
	public static HashMap<String,Shape> returnShapeMap(){//这里可以把shapeMap返回给board这样board就可以用来画shape了，不过这一般用于测试。
		return shapeMap;
	}
	
	public static HashMap<String,Coordinate> returnStopMap(){
		return stopMap;
	}

}

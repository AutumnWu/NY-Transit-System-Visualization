package oopVisualize;

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
	//The major class for processing raw data. Will store all the processed data in an
	//ArrayList filled with Trajectory objects and return it to board.
	////the following maps store the processed data
	static HashMap<String,Trajectory> transit = new HashMap<String,Trajectory>();//give back a list of trajectories		
	static HashMap<String,Coordinate> stopMap = new HashMap<String,Coordinate>(); //to store stops
	static HashMap<String,Shape> shapeMap = new HashMap<String,Shape>();
	static HashMap<String,Route> routeMap = new HashMap<String,Route>();
	static HashMap<String,Service> serviceMap = new HashMap<String,Service>();
	
	static double minX=99999, minY=99999,maxX=-9999,maxY=-9999,middleX=0,middleY=0;//for testing
	
	public static HashMap<String,Trajectory> parseTrips(){ // main method for parsing trajectories.
		ArrayList<String[]> stops = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/stops.txt");
		ArrayList<String[]> trips = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/trips.txt");
		ArrayList<String[]> stopTimes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/stop_times.txt");
		ArrayList<String[]> shapes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/shapes.txt");
		ArrayList<String[]> calendar = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/calendar.txt");
		ArrayList<String[]> calendar_dates = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/calendar_dates.txt");
		ArrayList<String[]> transfers = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/transfers.txt");
		ArrayList<String[]> routes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/routes.txt");
		
		calendarInitialize(serviceMap,calendar);
		routeInitialize(routeMap,routes);
		tripInitialize(transit,trips);//finish trips.txt //trips has to go after route initialize
		Trajectory.getRouteMap(routeMap);//this enables all tr to refer to routes.
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
	public static void projectStop(HashMap<String,Shape> shapeMap,HashMap<String,Trajectory> transit)
	{//project all stops to the corret positions on the shape lines!!
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
		int index1=0,index2=0;//
		//refer to the nearest two shape points to the stop
		
		for (Map.Entry<String, Trajectory> entry : transit.entrySet()) //############EEERROORORRRR
		{
		    tr = entry.getValue(); //get a trip
		    coordinates = shapeMap.get(tr.shapeId).coordinates;//get the poins in a shape.
		    pointCount = shapeMap.get(tr.shapeId).pointCount;
		    long time=0;//use this to refer to a stop in the trajectory
		    for (Map.Entry<Long, Coordinate> e2 : tr.trajectory.entrySet()) //
			{
		    	time = e2.getKey();
				C0 = e2.getValue(); //get a the coordinate of a stop (for mapping it to a line of shape)
				//System.out.println("Inaccurate Stop Location: x: "+C0.x+" y: "+C0.y);
				minDistance = 99999;//refresh minDistance
				
				for (int i=0;i<pointCount-1;i++)//i point in a shape
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
				}//get the nearest 2 points to a stop
				
			    Cnew = (new LineSegment(Cmin1,Cmin2)).project(C0); //finish projecting
			    //System.out.println("NEW Stop moved: +x: "+(Cnew.x-C0.x)+" +y: "+(Cnew.y-C0.y));
			    C0=Cnew;//
			    tr.addNearestIndex(time,index1,index2);//this is helpful for the second math equation in PDF later.
			}//for stop
		}//for trip
	}

	public static void shapeInitialize(HashMap<String,Shape> shapeMap,ArrayList<String[]> shapes){
		//initialize shape file
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
			currentX=Double.parseDouble(currentRawShape[2]);
			currentY=Double.parseDouble(currentRawShape[1]);
			//newX=(int)((currentX+xBuffer)*xScale); //get
			//newY=(int)((currentY+yBuffer)*yScale);// get
			currentShape.addPoint(currentX,currentY); // add a new point to current shape
		
			if (currentX<minX)
			{minX = currentX;}
			if (currentY<minY)
			{minY = currentY;}
			
			if (currentX>maxX)
			{maxX = currentX;}
			if (currentY>maxY)
			{maxY = currentY;}
			
		}
	}
	
	public static void stopTimeInitialize(HashMap<String,Trajectory> transit,
			HashMap<String,Coordinate> stopMap,ArrayList<String[]> stopTimes){ //has to be called after trips initialized
		//initialize all the stop times. first step in getting timestamp
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
		//process stops and put them into stopmap
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
			currentX=Double.parseDouble(currentRawStop[5]);
			currentY=Double.parseDouble(currentRawStop[4]);

			//newX=(int)((currentX+xBuffer)*xScale);
			//newY=(int)((currentY+yBuffer)*yScale);
			//currentStop.addStop(newX,newY,currentStopId);
			stopMap.put(currentStopId,new Coordinate(currentX,currentY)); //put <*key=stopid*,*value=Coordinate*> into map
			//currentX&Y are values on planet Earth
		}
	}
	
	public static void calendarInitialize(HashMap<String,Service> serviceMap,ArrayList<String[]> calendar){//把stopMap填好
		//so that we display subways according to weekdays.
		String currentServiceId="zzz";
		int i = 1;
		Service currentService;
		String[] currentRawCalendar;
	
		int Mon,Tue,Wed,Thu,Fri,Sat,Sun;
		
		
		for (i=1;i<calendar.size();i++){
			
			currentRawCalendar=calendar.get(i);
			currentServiceId = currentRawCalendar[0];//update currentShapeId
			Mon = Integer.parseInt(currentRawCalendar[1]);
			Tue = Integer.parseInt(currentRawCalendar[2]);
			Wed = Integer.parseInt(currentRawCalendar[3]);
			Thu = Integer.parseInt(currentRawCalendar[4]);
			Fri = Integer.parseInt(currentRawCalendar[5]);
			Sat = Integer.parseInt(currentRawCalendar[6]);
			Sun = Integer.parseInt(currentRawCalendar[7]);
			currentService = new Service(currentServiceId,Mon,Tue,Wed,Thu,Fri,Sat,Sun);
			

			serviceMap.put(currentServiceId,currentService); //put
			
		}
	}
	
	public static void routeInitialize(HashMap<String,Route> routeMap,ArrayList<String[]> routes){//把stopMap填好
		String currentId="zzz";
		String currentName = "zzz";
		int i = 1;
		Route currentRoute;
		String[] currentRawRoute;
	
		for (i=1;i<routes.size();i++){
			
			currentRawRoute=routes.get(i);
			currentId = currentRawRoute[0];//update currentShapeId
			currentName = currentRawRoute[3];
			currentRoute = new Route(currentId,currentName);
			routeMap.put(currentId,currentRoute); //put <*key=stopid*,*value=Coordinate*> into map
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
	{//
		Trajectory tr;
		int nOfTrip=0;//record program process
		int wait=0;
		for (Map.Entry<String, Trajectory> entry : transit.entrySet()) //####
		{//for every tr.
		    tr = entry.getValue(); //get a new tr Object
		    Coordinate A = null;
		    Coordinate B = null;
		    long tA=0,tB=0;//time of A, time of B
		    long tAB; //time interval between 2 stops
		    int iA1=0,iA2=0,iB1=0,iB2=0;
		    int startIndex,endIndex; //这两个是AB两个stop之间那些shape点的开始和结束点的index
		    int[] indexArray; //put 4 indexes inside, use sort to find #2,3
		    int direction; //important! will decide A first or B first
		    for (Map.Entry<Long, Coordinate> e2 : tr.trajectory.entrySet()) //
			{//For every B point (for every B,A point)
		    	if (A==null)
		    	{//only execute on first time
			    	tA = e2.getKey();
					A = e2.getValue(); //get a the coordinate of a stop (for mapping it to a line of shape)
					iA1 = tr.getNearestIndex(tA)[0];
					iA2 = tr.getNearestIndex(tA)[1];
		    	}else
		    	{//nomal parts
		    	tB = e2.getKey();
				B = e2.getValue();
				iB1 = tr.getNearestIndex(tB)[0];
				iB2 = tr.getNearestIndex(tB)[1];
				if ((iA1+iA2)<(iB1+iB2)){direction = 1;}else{direction = -1;}
				
				tAB = tB-tA;
				indexArray = new int[]{iA1,iA2,iB1,iB2};
				Arrays.sort(indexArray);
				startIndex = indexArray[1];
				endIndex = indexArray[2];//now we find the indexes of start and end points
				
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
					//the first point will be missed so we design it in this way.
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
				
				A=B;//necessary at the end
				tA=tB;//
				iA1=iB1;
				iA2=iB2;
		    	}//else
			}
		    nOfTrip+=1;
		    wait += 1;
		    if (wait >200){
		    	System.out.println("%"+(nOfTrip/110)+" is finished.");
		    	wait=0;
		    }
		    tr.setStartEnd();//set trip start and end time.
		}//
	}
	
	public static HashMap<String,Shape> returnShapeMap(){//return shapes to board
		return shapeMap;
	}

	public static HashMap<String,Coordinate> returnStopMap(){
		return stopMap;
	}


}

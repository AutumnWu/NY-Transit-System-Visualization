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
	
	public static HashMap<String,Trajectory> parseTrips(){ // ÿ����һ���µ�tripid�ͼ�һ���µ�trajectory
		ArrayList<String[]> stops = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/stops.txt");
		ArrayList<String[]> trips = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/trips.txt");
		ArrayList<String[]> stopTimes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/stop_times.txt");
		ArrayList<String[]> shapes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/shapes.txt");
		ArrayList<String[]> calendar = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/calendar.txt");
		ArrayList<String[]> calendar_dates = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/calendar_dates.txt");
		ArrayList<String[]> transfers = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/transfers.txt");
		ArrayList<String[]> routes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/routes.txt");
		
		tripInitialize(transit,trips);//������trips.txt
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
	public static HashMap<String,Trajectory> parseTrips2(){ // ÿ����һ���µ�tripid�ͼ�һ���µ�trajectory
		ArrayList<String[]> stops = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/stops.txt");
		ArrayList<String[]> trips = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/trips2.txt");
		ArrayList<String[]> stopTimes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/stop_times.txt");
		ArrayList<String[]> shapes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/shapes.txt");
		ArrayList<String[]> calendar = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/calendar.txt");
		ArrayList<String[]> calendar_dates = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/calendar_dates.txt");
		ArrayList<String[]> transfers = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/transfers.txt");
		ArrayList<String[]> routes = ReadCSV.read("C:/Users/Administrator/Desktop/OOP/OOP FINAL/mtaNYtransit/routes.txt");
		
		tripInitialize(transit,trips);//������trips.txt
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
	{//���ÿ��stop��λ�ö�ͶӰ��shape��·������
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
		int index1=0,index2=0;//����������ָ����ĳһ��stop���������·�����ڶ�Ӧ��shape���������index
		
		for (Map.Entry<String, Trajectory> entry : transit.entrySet()) //############EEERROORORRRR
		{
		    tr = entry.getValue(); //get a trip
		    coordinates = shapeMap.get(tr.shapeId).coordinates;//�õ���trip��Ӧ��shape�����һ����
		    pointCount = shapeMap.get(tr.shapeId).pointCount;
		    long time=0;//use this to refer to a stop in the trajectory
		    for (Map.Entry<Long, Coordinate> e2 : tr.trajectory.entrySet()) //
			{
		    	time = e2.getKey();
				C0 = e2.getValue(); //get a the coordinate of a stop (for mapping it to a line of shape)
				//System.out.println("Inaccurate Stop Location: x: "+C0.x+" y: "+C0.y);
				minDistance = 99999;//refresh minDistance
				
				for (int i=0;i<pointCount-1;i++)//i ��ָ��ĳһ��shape���������i��·����
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
				}//���for��������֮�����ĳstop��̵���������ҵ���
				
			    Cnew = (new LineSegment(Cmin1,Cmin2)).project(C0); //���project
			    //System.out.println("NEW Stop moved: +x: "+(Cnew.x-C0.x)+" +y: "+(Cnew.y-C0.y));
			    C0=Cnew;//������Դ�������һ��ô..?
			    //tr.trajectory.put(time,Cnew);//������һ��Trajectory�������һ��stop��λ��
			    tr.addNearestIndex(time,index1,index2);//�����������·�����index�ӵ����Trajectory���棬�Ժ���õ����ڶ�����ѧ���̣�
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
	
	public static void stopInitialize(HashMap<String,Coordinate> stopMap,ArrayList<String[]> stops){//��stopMap���
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
	
	public static void tripInitialize(HashMap<String,Trajectory> transit ,ArrayList<String[]> trips){//��transit list������ã�
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
	{//������������еڶ����ֵ��㷨���������ֻ�û�м��������
		Trajectory tr;
		int nOfTrip=0;//����ǲ���ʱ������¼������ȵ�
		int wait=0;
		for (Map.Entry<String, Trajectory> entry : transit.entrySet()) //####
		{//���for�ṹ������ÿһ��tr
		    tr = entry.getValue(); //get a new tr Object
		    Coordinate A = null;
		    Coordinate B = null;
		    long tA=0,tB=0;//time of A, time of B
		    long tAB; //��վ֮������ʱ��
		    int iA1=0,iA2=0,iB1=0,iB2=0;
		    int startIndex,endIndex; //��������AB����stop֮����Щshape��Ŀ�ʼ�ͽ������index
		    int[] indexArray; //��������4��indexȻ��ͨ��sort�ҵ���2��3��
		    int direction; //���������������A�Ȼ���B�� ����������AȻ����һ��shape�㻹��˵��BȻ����һ��shape��
		    for (Map.Entry<Long, Coordinate> e2 : tr.trajectory.entrySet()) //
			{//���for�ṹ������ÿһ��B(Ҳ����A,B�����)��������ǶԵ�
		    	if (A==null)
		    	{//�����ֻ�����forloop�ĵ�һ��loopִ�е�
			    	tA = e2.getKey();
					A = e2.getValue(); //get a the coordinate of a stop (for mapping it to a line of shape)
					iA1 = tr.getNearestIndex(tA)[0];
					iA2 = tr.getNearestIndex(tA)[1];
		    	}else
		    	{//���ﶼ�������Ĳ���
		    	
		    	tB = e2.getKey();
				B = e2.getValue();
				iB1 = tr.getNearestIndex(tB)[0];
				iB2 = tr.getNearestIndex(tB)[1];
				if ((iA1+iA2)<(iB1+iB2)){direction = 1;}else{direction = -1;}
				
				tAB = tB-tA;
				indexArray = new int[]{iA1,iA2,iB1,iB2};
				Arrays.sort(indexArray);
				startIndex = indexArray[1];
				endIndex = indexArray[2];//��������Ѿ��ҵ�ʼ�յ��index��
				
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
				//���loop����֮��cArray��װ���˰���A,B���ڵ�����AB֮���·�����ˡ�
				
				cArray = Arrays.copyOfRange(cArray,0,aj+1);//����һ������Ϊ֮ǰ���Ե�ʱ����cArray���벻���пյĲ��֣��еĻ��ͻᱨ��
				CoordinateArraySequence cas1 = new CoordinateArraySequence(cArray);
				
				LineString ls1=new LineString(cas1,new GeometryFactory());
				double dAB = ls1.getLength();//������ȡ����dAB��Ҳ����AB��ľ��롣
				
				//////////inbetween:speedSize testing code//////////
				if (tAB!=0&&dAB!=0){tr.speeds.put(tA,(int)(dAB/tAB*100000));}
				//System.out.println("Trip ID: "+tr.tripId+ "Speed: "+(int)(dAB/tAB*100000));
				//////////inbetween:speedSize testing code//////////
				
				double dCurrent = 0.0;
				tr.addWaypoint(tA,cArray[0]);//�����Ȱ�cArray����ĵ�һ��Coordinate����waypoints����Ϊ��һ�����棬��һ����ᱻ©��
				for (int k=0;k<aj;k++){//0->(aj-1)ÿ�ΰ�p1��p2�е�p2������ӵ�waypoints���棬���Ե�һ�����ǻ�©����
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
				
				A=B;//�����ǰ�B��ֵ��A������һ�ֵ�ʱ��B�ͱ������һ���㣬A������һ�εĵڶ�����
				tA=tB;//��֮��һ���Ǳ�Ҫ��
				iA1=iB1;
				iA2=iB2;
		    	}//�������������Ǹ�else�Ľ�������
			}
		    nOfTrip+=1;
		    wait += 1;
		    if (wait >200){
		    	System.out.println("%"+(nOfTrip/110)+" is finished.");
		    	wait=0;
		    }
		    tr.setStartEnd();//���������ÿ��trip���趨����ֹʱ�䡣
		}//
	}
	
	public static HashMap<String,Shape> returnShapeMap(){//������԰�shapeMap���ظ�board����board�Ϳ���������shape�ˣ�������һ�����ڲ��ԡ�
		return shapeMap;
	}
	
	public static HashMap<String,Coordinate> returnStopMap(){
		return stopMap;
	}

}

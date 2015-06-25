package oopVisualize;

public class Route {//light weight class to store name of a route.
	//0-route_id //3-routeLongName
	
	double totalSpeed=0;
	int nOfSpeed=0;
	
	String id;
	String name;
	
	public Route(String id,String name){
		this.id = id;
		this.name = name;
		//System.out.println(id);
		//System.out.println(name);
	}
	
	public String getName(){
		return name;
	}
	
	public void updateSpeed(double speed){
		if (totalSpeed < 200000){
		this.totalSpeed += speed;
		nOfSpeed +=1;
		}
	}
	
	public double getSpeed(){
		//will return the average speed of this route.
		if (nOfSpeed != 0)
			return totalSpeed/nOfSpeed;
		return 0;
	}
	
}

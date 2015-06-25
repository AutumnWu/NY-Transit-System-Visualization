package oopVisualize;

public class Service {
	//Helper class to store the services and calendars.
	//
	int[] weekdays = new int[7];
	String serviceId;
	public Service(String id, int Mon, int Tue, int Wed, int Thu, int Fri, int Sat, int Sun){
		this.serviceId = id;
		weekdays[0] = Mon;
		weekdays[1] = Tue;
		weekdays[2] = Wed;
		weekdays[3] = Thu;
		weekdays[4] = Fri;
		weekdays[5] = Sat;
		weekdays[6] = Sun;
	}
	
	public boolean checkActive(int weekday){//if the trip is active on this day then return true.
		if (weekdays[weekday] == 1)
		{return true;}else{return false;}
	}

}

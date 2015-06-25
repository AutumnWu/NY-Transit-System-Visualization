package oopVisualize;


import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Stop {//Helper class to temporarily store information of the stops when processing raw data.
	String stopId;
	int x;
	int y;
	
	public Stop(){
	}
	
	public void addStop(int x,int y,String id){
		this.x = x;
		this.y = y;
		stopId = id;
	}

	public void paint(Graphics2D g)
	{
		g.setColor(Color.BLACK);
		g.drawRect(this.x, this.y, 15, 15);
	}
	
	public void reset(int x, int y){//reset the display position of Stop.
		this.x = x;
		this.y = y;
	}
	
	
}
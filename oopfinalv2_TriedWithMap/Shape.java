package oopfinalv2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

public class Shape {
	int pointCount=0;
	ArrayList<Coordinate> coordinates;
	String shapeId;
	int x1,y1,x2,y2;
	
	static double xBuffer = 74.14;
	static double yBuffer = 40.91;
	static double bufferChangeAmount = 0.01;
	static int xScale = 2100;
	static int yScale = 2100;
	
	public Shape(String shapeId){
		this.shapeId = shapeId; //set shape Id
		coordinates = new ArrayList<Coordinate>(); //save all the coordinates in this shape here
	}
	
	public void addPoint(double x,double y){
		pointCount+=1;
		coordinates.add(new Coordinate(x,y));
	}
	
	public void paint(Graphics2D g)
	{
		g.setColor(Color.BLACK);
		for (int i=0;i<coordinates.size()-1;i++){
			x1=(int)((coordinates.get(i).x+xBuffer)*xScale);
			y1=(int)((yBuffer-coordinates.get(i).y)*yScale);
			x2=(int)((coordinates.get(i+1).x+xBuffer)*xScale);
			y2=(int)((yBuffer-coordinates.get(i+1).y)*yScale);
			g.drawLine(x1,y1,x2,y2);
			}
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
			
		}
		if (e.getKeyCode() == KeyEvent.VK_MINUS) //
		{
			xScale=(int)(xScale*0.8);
			yScale=(int)(yScale*0.8);
			bufferChangeAmount *= 1.1;
			yBuffer+=bufferChangeAmount;
			xBuffer+=bufferChangeAmount;
			
		}

	}//end of keypressed
}

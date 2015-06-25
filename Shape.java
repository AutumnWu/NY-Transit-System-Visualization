package oopVisualize;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;

public class Shape {//Helper class to store the waypoints in shape.txt file for each route.
	int pointCount=0;
	ArrayList<Coordinate> coordinates;
	String shapeId;
	int x1,y1,x2,y2;
	
	static double xBuffer = 74.31089000000005;
	static double yBuffer = 40.99619999999998;
	static double bufferChangeAmount = 0.02;
	static int xScale = 1075;
	static int yScale = 1075;
	Random r;
	Color randomColor;
	
	public Shape(String shapeId){
		r = new Random();
		randomColor = new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255));
		this.shapeId = shapeId; //set shape Id
		coordinates = new ArrayList<Coordinate>(); //save all the coordinates in this shape here
	}
	
	public void addPoint(double x,double y){//add a point to this shape object
		pointCount+=1;
		coordinates.add(new Coordinate(x,y));
	}
	
	public void paint(Graphics2D g)//paint this shape to the canvas
	{
		g.setColor(randomColor);
		for (int i=0;i<coordinates.size()-1;i++){
			x1=(int)((coordinates.get(i).x+xBuffer)*xScale);
			y1=(int)((yBuffer-coordinates.get(i).y)*yScale);
			x2=(int)((coordinates.get(i+1).x+xBuffer)*xScale);
			y2=(int)((yBuffer-coordinates.get(i+1).y)*yScale);
			g.drawLine(x1,y1,x2,y2);
			}
	}
	
	public void paintHighlight(Graphics2D g)//if the shape is highlighted
	{
		g.setColor(Color.RED);
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

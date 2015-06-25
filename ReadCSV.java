package oopVisualize;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

//////////////////////////////////////////
// We will use another main method to call this ReadCSV class and give it a path (That's all it takes!!!)
// Will return the csv file as an ArrayList.
////////////////////////////////////////

public class ReadCSV { //use this file to read data in stops.txt in NY data folder
public static ArrayList<String[]> read(String path) {
	 ReadCSV obj = new ReadCSV(); ///create a new ReadCSV
	 return obj.run(path); ///this basically just run the run() method 
	 //SO whoever calls ReadNYStops method will get an ArrayList of NY stops data in return!!
}

public ArrayList<String[]> run(String path) {
	String csvFile = path; //so we set path to be the path of this particular CSV file that needs to be read.
	BufferedReader br = null;
	String line = ""; ////line is later used to refer to a new line of data
	String csvSplitBy = ","; ////setting the parameter that will split a line, in our case it's ","
	ArrayList<String[]> csv = new ArrayList<String[]>();
	try {
		br = new BufferedReader(new FileReader(csvFile)); ////use br to read file
		while ((line = br.readLine()) != null) { ///while we still have a new line
		        // use comma as separator
			String[] aCsv = line.split(csvSplitBy); ///ths split function seem convenient
			csv.add(aCsv); //add this stop to stops!
		}
	} catch (FileNotFoundException e) { //just to deal with errors. You can ignore this part
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if (br != null) {
			try {
				br.close(); ///close the scanner(bufferedreader) when file comes to end
			} catch (IOException e) {
				e.printStackTrace(); 
			}
		}
	}
	System.out.println(path + " Read Successfully!!!");
	return csv;
 }//end of run method
}


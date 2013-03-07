package krsystem.utility;

import java.util.HashMap;

public class StringIDMap {

	protected HashMap<String, Integer> stringIDMap;
	protected int counter;
	
	public StringIDMap() {
		stringIDMap = new HashMap<String, Integer>();
		counter = 0;
	}
	
	public int Add(String str) {		
		if (stringIDMap.containsKey(str))
			return stringIDMap.get(str);
		else {
			stringIDMap.put(str, counter);
			counter++;
			return counter-1;
		}
	}
	
	public boolean Contains(String str) {
		return stringIDMap.containsKey(str) ? true : false;
	}
	
	public int Size() {
		return counter;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		StringIDMap map = new StringIDMap();
		map.Add("ranjan");
		map.Add("Bhagwani");
		System.out.println(map.Contains("ranjan"));
		System.out.println(map.Contains("Bhagwani"));
		System.out.println(map.Contains("pattu"));
		
	}

}

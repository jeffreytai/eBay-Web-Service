package edu.ucla.cs.cs144;

import java.util.Calendar;
import java.util.Date;

import edu.ucla.cs.cs144.AuctionSearch;
import edu.ucla.cs.cs144.SearchRegion;
import edu.ucla.cs.cs144.SearchResult;

public class AuctionSearchTest {
	public static void main(String[] args1)
	{
		try {
			AuctionSearch as = new AuctionSearch();

			String message = "Test message";
			String reply = as.echo(message);
			System.out.println("Reply: " + reply);
			
			// String query = "superman";
			// SearchResult[] basicResults = as.basicSearch(query, 0, 20);
			// System.out.println("Basic Search Query: " + query);
			// System.out.println("Received " + basicResults.length + " results");
			// for(SearchResult result : basicResults) {
			// 	System.out.println(result.getItemId() + ": " + result.getName());
			// }

			// String query = "superman";
			// SearchResult[] basicResults = as.basicSearch(query, 0, 68);
			// System.out.println("Basic Search Query: " + query);
			// System.out.println("Received " + basicResults.length + " results");
			// for(SearchResult result : basicResults) {
			// 	System.out.println(result.getItemId() + ": " + result.getName());
			// }
			
			SearchRegion region =
			    new SearchRegion(33.774, -118.63, 34.201, -117.38); 
			SearchResult[] spatialResults = as.spatialSearch("camera", region, 0, 194);
			System.out.println("Spatial Search");
			System.out.println("Received " + spatialResults.length + " results");
			for(SearchResult result : spatialResults) {
				System.out.println(result.getItemId() + ": " + result.getName());
			}
			
			//String itemId = "1497595357";
			// System.out.println("Contains bids");
			// String itemId = "1043495702";
			// String item = as.getXMLDataForItemId(itemId);
			// System.out.println("XML data for ItemId: " + itemId);
			// System.out.println(item);

			// System.out.println("listing with null latitude/longitude");
			// String itemId = "1043608482";
			// String item = as.getXMLDataForItemId(itemId);
			// System.out.println("XML data for ItemId: " + itemId);
			// System.out.println(item);

			// String itemId = "1497595357";
			// String item = as.getXMLDataForItemId(itemId);
			// System.out.println("XML data for ItemId: " + itemId);
			// System.out.println(item);

			// Add your own test here
		} catch (Exception ex) {
			System.out.println("Exception caught: " + ex);
		}
	}
}
package edu.ucla.cs.cs144;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.ucla.cs.cs144.DbManager;
import edu.ucla.cs.cs144.SearchRegion;
import edu.ucla.cs.cs144.SearchResult;

public class AuctionSearch implements IAuctionSearch {

	/* 
         * You will probably have to use JDBC to access MySQL data
         * Lucene IndexSearcher class to lookup Lucene index.
         * Read the corresponding tutorial to learn about how to use these.
         *
	 * You may create helper functions or classes to simplify writing these
	 * methods. Make sure that your helper functions are not public,
         * so that they are not exposed to outside of this class.
         *
         * Any new classes that you create should be part of
         * edu.ucla.cs.cs144 package and their source files should be
         * placed at src/edu/ucla/cs/cs144.
         *
         */
	private IndexSearcher searcher = null;
	private QueryParser parser = null;

	public AuctionSearch() throws IOException {
		searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File("/var/lib/lucene/index1"))));
        parser = new QueryParser("content", new StandardAnalyzer());
	}

	private TopDocs performSearch(String queryString, int n)
	throws IOException, ParseException {
		Query query = parser.parse(queryString);
		return searcher.search(query, n);
	}
	
	public SearchResult[] basicSearch(String query, int numResultsToSkip, 
			int numResultsToReturn) {
		// TODO: Your code here!
		if (numResultsToReturn <= 0) return new SearchResult[0];

		try {
			AuctionSearch ae = new AuctionSearch();
			TopDocs topDocs = ae.performSearch(query, numResultsToSkip + numResultsToReturn);
			ScoreDoc[] hits = topDocs.scoreDocs;
			SearchResult[] searchResult = new SearchResult[numResultsToReturn];
			int index=0;
			for (int i=numResultsToSkip; i<numResultsToSkip+numResultsToReturn; i++) {
				Document doc = ae.getDocument(hits[i].doc);
				searchResult[index] = new SearchResult(doc.get("id"), doc.get("name"));
				index++;
			}
			return searchResult;

		} catch (Exception ex) {
			System.out.println(ex);
		}

		return new SearchResult[0];
	}

	public SearchResult[] spatialSearch(String query, SearchRegion region,
			int numResultsToSkip, int numResultsToReturn) {
		// TODO: Your code here!

		// old result
		if (numResultsToReturn <= 0) return new SearchResult[0];
		Connection conn = null;
		List<SearchResult> sr = new ArrayList<SearchResult>();
		SearchResult[] basicResults = basicSearch(query, numResultsToSkip, numResultsToReturn);
		try {
			conn = DbManager.getConnection(true);
		} catch (SQLException ex) {
			System.out.println("Error connecting: " + ex);
		}

		try {
			for (int i=0; i<basicResults.length; i++) {
				PreparedStatement stmt = conn.prepareStatement("SELECT Item.Name, Location.itemID, x(coordinates) AS xcord, y(coordinates) AS ycord FROM Location, Item WHERE Location.itemID = Item.ItemID AND Location.itemID = ?");
				stmt.setInt(1, Integer.parseInt(basicResults[i].getItemId()));
				ResultSet rs = stmt.executeQuery();
				if (!rs.next()) continue;
				double x_cord, y_cord;
				String itemID, name;
				x_cord = Double.parseDouble(rs.getString("xcord"));
				y_cord = Double.parseDouble(rs.getString("ycord"));
				itemID = rs.getString("Location.itemID");
				name = rs.getString("Item.Name");

				if ( (x_cord > region.getLx()) &&
						(x_cord < region.getRx()) &&
						(y_cord > region.getLy()) &&
						(y_cord < region.getRy()) ) {
					sr.add(new SearchResult(itemID, name));
				}
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
		System.out.println("Size of arraylist: " + sr.size());
		SearchResult[] advancedResults = sr.toArray(new SearchResult[sr.size()]);
		sr.toArray(advancedResults);
		return advancedResults;
	}

	private String escapeCharacters(String unescaped) {
		String escaped = unescaped.replace("&", "&amp;");
		escaped = escaped.replace("<", "&lt;");
		escaped = escaped.replace(">", "&gt;");
		return escaped;
	}

	public String getXMLDataForItemId(String itemId) {
		// TODO: Your code here!
		String xmlData = "";
		Connection conn = null;
		try {
			conn = DbManager.getConnection(true);
		} catch (SQLException ex) {
			System.out.println("Error connecting: " + ex);
		}

		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT Name, Currently, First_Bid, Buy_Price, Number_of_bids, SellerID, Started, Ends, Description FROM Item WHERE ItemID = ?");
			stmt.setInt(1, Integer.parseInt(itemId));
			ResultSet rs = stmt.executeQuery();
			// itemId is not valid
			if (!rs.next()) return "";

			xmlData = "<Item ItemID=\"" + itemId + "\">";
			
			String name = rs.getString("Name");
			String currently = rs.getString("Currently");
			String first_bid = rs.getString("First_Bid");
			String buy_price = rs.getString("Buy_Price");
			String number_of_bids = rs.getString("Number_of_bids");
			String sellerID = rs.getString("SellerID");
			String description = rs.getString("Description");
			String started = rs.getString("Started");
			String ends = rs.getString("Ends");

			xmlData = xmlData + "\n" + "\t" + "<Name>" + escapeCharacters(name) + "</Name>";

			PreparedStatement stmt2 = conn.prepareStatement("SELECT CategoryID FROM ItemCategory WHERE ItemID = ?");
			stmt2.setInt(1, Integer.parseInt(itemId));
			ResultSet categorySet = stmt2.executeQuery();

			while (categorySet.next()) {
				String categoryid = categorySet.getString("CategoryID");
				PreparedStatement catName = conn.prepareStatement("SELECT CategoryName FROM Category WHERE CategoryID = ?");
				catName.setInt(1, Integer.parseInt(categoryid));
				ResultSet catNameSet = catName.executeQuery();
				catNameSet.next();
				xmlData = xmlData + "\n" + "\t" + "<Category>" + escapeCharacters(catNameSet.getString("CategoryName")) + "</Category>";
			}

			xmlData = xmlData + "\n" + "\t" + "<Currently>$" + currently + "</Currently>";
			if (buy_price != null) {
				xmlData = xmlData + "\n" + "\t" + "<Buy_Price>$" + buy_price + "</Buy_Price>";
			}
			xmlData = xmlData + "\n" + "\t" + "<First_Bid>$" + first_bid + "</First_Bid>";
			xmlData = xmlData + "\n" + "\t" + "<Number_of_Bids>" + number_of_bids + "</Number_of_Bids>";

			// bid section
			PreparedStatement bidStmt = conn.prepareStatement("SELECT BuyerID, Time, Amount FROM Bids WHERE ItemID = ?");
			bidStmt.setInt(1, Integer.parseInt(itemId));
			ResultSet bidSet = bidStmt.executeQuery();
			DateFormat source = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			source.setLenient(false);
			DateFormat target = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
			target.setLenient(false);
			if (!bidSet.next()) {
				xmlData = xmlData + "\n" + "\t" + "<Bids />";
			} else {
				do {
					xmlData = xmlData + "\n" + "\t" + "<Bids>";
					xmlData = xmlData + "\n" + "\t  " + "<Bid>";
					PreparedStatement bidderInfoStmt = conn.prepareStatement("SELECT Rating, Location, Country FROM User WHERE UserID = ?");
					bidderInfoStmt.setString(1, bidSet.getString("BuyerID"));
					ResultSet bidderInfoSet = bidderInfoStmt.executeQuery();
					bidderInfoSet.next();
					xmlData = xmlData + "\n" + "\t    " + "<Bidder Rating=\"" + bidderInfoSet.getString("Rating") + "\" UserID=\"" + bidSet.getString("BuyerID") + "\">";
					xmlData = xmlData + "\n" + "\t      " + "<Location>" + bidderInfoSet.getString("Location") + "</Location>";
					xmlData = xmlData + "\n" + "\t      " + "<Country>" + bidderInfoSet.getString("Country") + "</Country>";
					xmlData = xmlData + "\n" + "\t    " + "</Bidder>";
					Date d = source.parse(bidSet.getString("Time"));
					String date = target.format(d);
					xmlData = xmlData + "\n" + "\t    " + "<Time>" + date + "</Time>";
					xmlData = xmlData + "\n" + "\t    " + "<Amount>$" + bidSet.getString("Amount") + "</Amount>";
					xmlData = xmlData + "\n" + "\t  " + "</Bid>";
				} while (bidSet.next());
				xmlData = xmlData + "\n" + "\t" + "</Bids>";
			}

			// Location section
			PreparedStatement locStmt = conn.prepareStatement("SELECT Location, Country, Latitude, Longitude FROM Locate WHERE ItemID = ?");
			locStmt.setInt(1, Integer.parseInt(itemId));
			ResultSet locSet = locStmt.executeQuery();
			locSet.next();
			if (locSet.getString("Latitude") == null) {
				xmlData = xmlData + "\n" + "\t" + "<Location>" + escapeCharacters(locSet.getString("Location")) + "</Location>";
			} else {
				xmlData = xmlData + "\n" + "\t" + "<Location Latitude=\"" + locSet.getString("Latitude") + "\" Longitude=\"" + locSet.getString("Longitude") + "\">" + escapeCharacters(locSet.getString("Location")) + "</Location>";
			}
			
			xmlData = xmlData + "\n" + "\t" + "<Country>" + escapeCharacters(locSet.getString("Country")) + "</Country>";

			// TIMESTAMP section
			Date d = source.parse(started);
			String formattedStart = target.format(d);
			d = source.parse(ends);
			String formattedEnd = target.format(d);
			xmlData = xmlData + "\n" + "\t" + "<Started>" + formattedStart + "</Started>";
			xmlData = xmlData + "\n" + "\t" + "<Ends>" + formattedEnd + "</Ends>";

			PreparedStatement userStmt = conn.prepareStatement("SELECT Rating FROM User WHERE UserID = ?");
			userStmt.setString(1, sellerID);
			ResultSet userSet = userStmt.executeQuery();
			userSet.next();
			xmlData = xmlData + "\n" + "\t" + "<Seller Rating=\"" + userSet.getString("Rating") + "\" UserID=\"" + sellerID + "\" />";

			xmlData = xmlData + "\n" + "\t" + "<Description>" + escapeCharacters(description) + "</Description>";
			xmlData = xmlData + "\n" + "</Item>";

			return xmlData;
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return "";
	}
	
	public String echo(String message) {
		return message;
	}

	public Document getDocument(int docId) throws IOException {
		return searcher.doc(docId);
	}

}

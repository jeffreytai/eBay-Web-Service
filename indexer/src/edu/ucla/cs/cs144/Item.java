package edu.ucla.cs.cs144;

import java.io.IOException;
import java.io.StringReader;
import java.io.File;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Item {

	private String itemID;
	private String name;
	private String category;
	private String description;

	public Item(String itemID, String name, String category, String description) {
		this.itemID = itemID;
		this.name = name;
		this.category = category;
		this.description = description;
	}

	public String getItemID() {
		return itemID;
	}
	public String getName() {
		return name;
	}
	public String getCategory() {
		return category;
	}
	public String getDescription() {
		return description;
	}


}
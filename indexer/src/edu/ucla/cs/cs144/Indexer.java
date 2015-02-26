package edu.ucla.cs.cs144;

import java.io.IOException;
import java.io.StringReader;
import java.io.File;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
    
    /** Creates a new instance of Indexer */
    public Indexer() {
    }

    private IndexWriter indexWriter = null;

    public IndexWriter getIndexWriter(boolean create) throws IOException {
        if (indexWriter == null) {
            Directory indexDir = FSDirectory.open(new File("/var/lib/lucene/index1"));
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, new StandardAnalyzer());
            // used to create new index???
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            indexWriter = new IndexWriter(indexDir, config);
        }
        return indexWriter;
    }

    public void closeIndexWriter() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
    }

    public void indexItem(Item item) throws IOException {
        IndexWriter writer = getIndexWriter(false);
        Document doc = new Document();
        doc.add(new StringField("id", item.getItemID(), Field.Store.YES));
        doc.add(new StringField("name", item.getName(), Field.Store.YES));
        doc.add(new StringField("category", item.getCategory(), Field.Store.NO));
        doc.add(new StringField("description", item.getDescription(), Field.Store.NO));
        String fullSearchableText = item.getName() + " " + item.getCategory() + " " + item.getDescription();
        doc.add(new TextField("content", fullSearchableText, Field.Store.NO));
        writer.addDocument(doc);
    }
 
    public void rebuildIndexes() {

        Connection conn = null;

        // create a connection to the database to retrieve Items from MySQL
    	try {
    	    conn = DbManager.getConnection(true);
    	} catch (SQLException ex) {
    	    System.out.println(ex);
    	}

        Statement stmt = null;
        String itemID;
        String name;
        String description;
        try {
            stmt = conn.createStatement();
            PreparedStatement stmt2 = conn.prepareStatement("SELECT CategoryID FROM ItemCategory WHERE ItemID = ?");
            PreparedStatement stmt3 = conn.prepareStatement("SELECT CategoryName FROM Category WHERE CategoryID = ?");
            ResultSet rs = stmt.executeQuery("SELECT ItemID, Name, Description FROM Item");

            while ( rs.next() ) {
                int i_itemID = rs.getInt("ItemID");
                itemID = Integer.toString(i_itemID);
                name = rs.getString("Name");
                description = rs.getString("Description");
                if (description.length() > 4000) {
                    description = description.substring(0,4000);
                }
                stmt2.setInt(1, i_itemID);
                ResultSet rs2 = stmt2.executeQuery();
                int i_categoryID;
                StringBuilder categories = new StringBuilder();
                while ( rs2.next() ) {
                    i_categoryID = rs2.getInt("CategoryID");

                    stmt3.setInt(1, i_categoryID);
                    ResultSet rs3 = stmt3.executeQuery();
                    rs3.next();
                    String s_categoryName = rs3.getString("CategoryName");
                    // places a space between category names
                    if (categories.length() == 0) { categories.append(s_categoryName); }
                    else { categories.append(" " + s_categoryName); }
                }

                Item item = new Item(itemID, name, categories.toString(), description);
                indexItem(item);
            }

            closeIndexWriter();

        } catch (SQLException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }

        

            // close the database connection
    	try {
    	    conn.close();
    	} catch (SQLException ex) {
    	    System.out.println(ex);
    	}
    /*
     * Add your code here to retrieve Items using the connection
     * and add corresponding entries to your Lucene inverted indexes.
         *
         * You will have to use JDBC API to retrieve MySQL data from Java.
         * Read our tutorial on JDBC if you do not know how to use JDBC.
         *
         * You will also have to use Lucene IndexWriter and Document
         * classes to create an index and populate it with Items data.
         * Read our tutorial on Lucene as well if you don't know how.
         *
         * As part of this development, you may want to add 
         * new methods and create additional Java classes. 
         * If you create new classes, make sure that
         * the classes become part of "edu.ucla.cs.cs144" package
         * and place your class source files at src/edu/ucla/cs/cs144/.
     * 
     */
    }

    public static void main(String args[]) {
        Indexer idx = new Indexer();
        idx.rebuildIndexes();
    }   
}

# eBay-Web-Service

Using the database created in the eBay-Database repository, I created a Lucene index on the data to speed up retrieval of the data.

I created several methods for searching:

1) a basic search that returned matches for the query, allowing the user to specify how many results to skip and how many to return,

2) a spatial search that incorporated the basic search in addition to making sure that the item's location was within the specified region,

3) a function to return the original XML information in its respective format given an item ID

CREATE TABLE Location (
	itemID      INTEGER NOT NULL,
	coordinates POINT NOT NULL,
	PRIMARY KEY(itemID)
) ENGINE=MyISAM;

INSERT INTO Location (itemID, coordinates)
SELECT Locate.ItemID, GeomFromText(CONCAT('POINT(',Locate.Latitude,' ',Locate.longitude,')'))
FROM Locate
WHERE Locate.Latitude <> "NULL"
	AND Locate.Longitude <> "NULL";

CREATE SPATIAL INDEX locIndex ON Location (coordinates);
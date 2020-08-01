/*
NOTES:
	- 'DBLP-relationships.csv' must be in '<neo4j-home>\import' directory.
	- MERGE statement is an alternative to CREATE statement. MERGE statement checks if the data exists before inserting. IMPORTANT: using MERGE can have significant performance overhead (especially in terms of memory requirements). So, it is better to avoid it; rather, make sure that the file to be loaded does not have duplicates, and use CREATE.
	- USING PERIODIC COMMIT: the underlying transaction is committed every 1000 lines ('1000' can be customized: just add the desired number right after the USING PERIODIC COMMIT statement)
	- For more details on how to import CSV Data with Neo4j Desktop, see https://neo4j.com/developer/desktop-csv-import/
*/

:auto USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///DBLP-relationships.csv' AS line
MATCH (origin:Author {id: line.author1})
MATCH (destination:Author {id: line.author2})
// WITH line LIMIT 50000
CREATE (origin)-[:COAUTHORSHIP]->(destination)
// MERGE (origin)-[:COAUTHORSHIP]->(destination)
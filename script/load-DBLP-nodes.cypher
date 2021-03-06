/*
NOTES:
	- 'DBLP-nodes.csv' must be in '<neo4j-home>\import' directory.
	- MERGE statement is an alternative to CREATE statement. MERGE statement checks if the data exists before inserting. IMPORTANT: using MERGE can have significant performance overhead (especially in terms of memory requirements). So, it is better to avoid it; rather, make sure that the file to be loaded does not have duplicates, and use CREATE.
	- USING PERIODIC COMMIT: the underlying transaction is committed every 1000 lines ('1000' can be customized: just add the desired number right after the USING PERIODIC COMMIT statement)
	- IMPORTANT: it is highly suggested to create an index for nodes; otherwise, all subsequent tasks involving them (including, e.g., loading relationships) may be significantly slowed down. TO create an index for all loaded nodes, use this:
	CREATE CONSTRAINT ON (a:Author) ASSERT a.id IS UNIQUE
	- For more details on how to import CSV Data with Neo4j Desktop, see https://neo4j.com/developer/desktop-csv-import/
*/

:auto USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///DBLP-nodes.csv' AS line
// WITH line LIMIT 50000
CREATE (:Author { id: line.name })
// MERGE (:Author { id: line.name })
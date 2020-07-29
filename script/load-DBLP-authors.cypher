//'DBLP-authors.csv' must be in '<neo4j-home>\import directory'

:auto USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///DBLP-authors.csv' AS line
// WITH line LIMIT 50000
CREATE (:Author { id: line.name })
//'DBLP-topics.csv' must be in '<neo4j-home>\import directory'

:auto USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///DBLP-topics.csv' AS line
MERGE (:Topic { id: line.id })
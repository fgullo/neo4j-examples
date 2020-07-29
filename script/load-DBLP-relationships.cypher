//'DBLP-relationships.csv' must be in '<neo4j-home>\import directory'

:auto USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///DBLP-relationships.csv' AS line
MATCH (origin:Author {id: line.author1})
MATCH (destination:Author {id: line.author2})
// WITH line LIMIT 50000
CREATE (origin)-[:CoAuthorship]->(destination)
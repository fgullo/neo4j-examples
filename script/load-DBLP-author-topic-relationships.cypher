//'DBLP-author-topic-relationships.csv' must be in '<neo4j-home>\import directory'

:auto USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///DBLP-author-topic-relationships.csv' AS line
MATCH (origin:Author {id: line.author})
MATCH (destination:Topic {id: line.topic})
// WITH line LIMIT 50000
CREATE (origin)-[:AuthorTopic {probability: toFloat(line.probability)}]->(destination)
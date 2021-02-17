CALL gds.graph.list();

CALL gds.graph.create('DBLP', 'Author', 'COAUTHORSHIP')
YIELD graphName, nodeCount, relationshipCount, createMillis;

CALL gds.graph.create('DBLP', '*', '*') 
YIELD graphName, nodeCount, relationshipCount, createMillis;

CALL gds.pageRank.stream('DBLP',{concurrency: 1})
YIELD nodeId, score
RETURN gds.util.asNode(nodeId).id AS name, score
ORDER BY score DESC, name ASC
LIMIT 10;

CALL gds.pageRank.stats('DBLP',{concurrency: 1, maxIterations: 100})
YIELD ranIterations, didConverge, createMillis, computeMillis, postProcessingMillis, centralityDistribution;

CALL gds.wcc.stats('DBLP',{concurrency: 1})
YIELD componentCount, createMillis, computeMillis, postProcessingMillis, componentDistribution;
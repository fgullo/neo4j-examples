# neo4j-examples
Examples to extend Neo4j with user-defined procedures and/or functions 

Main steps:

* Follow [Neo4j Java Developer Reference](https://neo4j.com/docs/pdf/neo4j-java-reference-4.1.pdf) to set up a Neo4j plugin project
* Create a jar. If you use maven, edit pom.xml accordingly, and just run mvn package
* if you use Neo4j API 4.0+, be sure to compile with Java 11+
* Deploy the jar to the Neo4j database by just dropping the jar file into the Neo4j plugins directory ()
* Call procedures/functions via Cypher (e.g., for densest sugraph, type CALL org.neo4j.aidna.examples.densestSubgraph()) 

# neo4j-examples
Examples to extend [Neo4j](https://neo4j.com/) with user-defined procedures and/or functions 

Main steps:

* Follow [Neo4j Java Developer Reference](https://neo4j.com/docs/pdf/neo4j-java-reference-4.1.pdf) to set up a Neo4j plugin project.
* Create a jar. If you use `Maven`, edit `pom.xml` accordingly, and just run `mvn package`.
* If you use Neo4j API 4.0+, be sure to compile with Java 11+.
* Deploy the jar to the Neo4j database by just dropping the jar file into the Neo4j plugins directory (for the location of Neo4j plugins directory, refer to [Operations Manual](https://neo4j.com/docs/pdf/neo4j-operations-manual-4.1.pdf), *File locations*). Note that the database must be re-started (on each server) to pick up new procedures and functions.
* Call procedures/functions via [Cypher](https://neo4j.com/developer/cypher-basics-i/), Neo4j’s graph query language (e.g., for densest sugraph, type `CALL org.neo4j.aidna.examples.densestSubgraph()`).

package org.neo4j.aidna;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Stream;

import org.neo4j.logging.Log;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Relationship;
import org.neo4j.procedure.*;

public class Examples{

    @Context
    public GraphDatabaseService db;

	@Context
	public Log log;

//    @Procedure(name = "org.neo4j.aidna.examples.hello", mode = Mode.READ)
    @Procedure
    @Description("Just a simple 'Hello World!'")
    public Stream<StringOutput> hello(@Name("nodeId") long nodeId) {
 		
 		String nodeName = "Null";

 		try ( Transaction tx = db.beginTx() ) {
        	Node node = tx.getNodeById(nodeId);
			nodeName = node.getProperty("name", new String("Node ID not existing")).toString();

     		tx.close();
 		}

        String msg = "Hello " + nodeName + "!";
        log.info(msg);

        return Arrays.stream(new StringOutput[]{new StringOutput(msg)});
    }

    @Procedure(name = "org.neo4j.aidna.examples.densestSubgraph", mode = Mode.READ)
//    @Procedure
    @Description("Charikar's 2-approximation greedy algorithm for the densest-subgraph problem")
    public Stream<DensestSubgraphOutput> densestSubgraph() {

        long start = System.currentTimeMillis();
        Set<Long>[] nodeByDegree = null;
        Map<Long, Integer> currentDegrees = new HashMap();
        int nodes = 0;
        int edges = 0;

        try ( Transaction tx = db.beginTx() ) {

            ResourceIterable<Node> allNodes = tx.getAllNodes();

            int maxDegree = 0;
            for ( Node n : allNodes ){
                nodes++;
                int degree = n.getDegree();
                edges += degree;
                currentDegrees.put(n.getId(),degree);
                if (degree > maxDegree){
                    maxDegree = degree;
                }
            }
            edges /= 2;

            nodeByDegree = new Set[maxDegree+1];
            allNodes = tx.getAllNodes();
            for ( Node n : allNodes ){
                int degree = n.getDegree();
                if ( nodeByDegree[degree] == null ){
                    nodeByDegree[degree] = new HashSet<Long>();
                }
                nodeByDegree[degree].add(n.getId());
            }

            tx.close();
        }

        long runningTimeVisit = System.currentTimeMillis() - start;

        long[] removedNodes = new long[nodes+1];
        removedNodes[0] = -1;

        double[] densities = new double[nodes+1];
        densities[0] = ((double)edges)/nodes; //densities[0] contains the density of the whole graph

        double currentEdges = edges;
        int currentNodes = nodes;

        int minDegree = 0;

        //main cycle lasts until all nodes have been deleted
        for ( int i=1; i<removedNodes.length; i++ ) {

            while ( minDegree < nodeByDegree.length && (minDegree < 0 || nodeByDegree[minDegree] == null || nodeByDegree[minDegree].isEmpty()) ){
                minDegree++;
            }

            long n = nodeByDegree[minDegree].iterator().next();
            nodeByDegree[minDegree].remove(n);
            currentDegrees.remove(n);
            removedNodes[i] = n;

            currentEdges -= minDegree;
            currentNodes--;
            densities[i] = (currentNodes == 0) ? 0.0 : currentEdges/currentNodes;

            //Collection<Long> neighbors = new LinkedList();
            Map<Long,Integer> neighbors = new HashMap();
            try ( Transaction tx = db.beginTx() ) {
                Node node = tx.getNodeById(n);
                for ( Relationship rel : node.getRelationships() ){
                    long neighbor = rel.getOtherNode(node).getId();
                    if ( currentDegrees.containsKey(neighbor) ){
                        int currentAdjacentEdges = (neighbors.containsKey(neighbor)) ? neighbors.get(neighbor) : 0;
                        currentAdjacentEdges++;
                        neighbors.put(neighbor,currentAdjacentEdges);
                    }
                } 

                tx.close();
            }

            for ( long x : neighbors.keySet() ){
                int degreeReduction = neighbors.get(x);
                int currentDegree = currentDegrees.get(x);
                int newDegree = currentDegree - degreeReduction;
                currentDegrees.put(x,newDegree);

                nodeByDegree[currentDegree].remove(x);
                if ( nodeByDegree[newDegree] == null ){
                    nodeByDegree[newDegree] = new HashSet();
                }
                nodeByDegree[newDegree].add(x);

                //preparing for the next iteration
                //minDegree can decrease by at most 1 in every iteration on simple graphs, but it can decrease by more than 1 on multigraphs
                if ( newDegree < minDegree ){
                    minDegree = newDegree;
                }
            }
        }

        double maxDensity = 0;
        int iMaxDensity = -1;
        for ( int i=0; i<densities.length; i++ ){
            double density = densities[i];
            if ( density > maxDensity ){
                maxDensity = density;
                iMaxDensity = i;
            }
        }
        double minDensity = densities[densities.length-2]; //sanity check, it should be zero

        //the ultimate densest subgraph to be returned is composed of all nodes in removedNodes from iMaxDensity on
        Collection<Long> densestSubgraph = new LinkedList();
        for ( int i=iMaxDensity+1; i<removedNodes.length; i++){
            densestSubgraph.add(removedNodes[i]);
        }

        Iterator<Long> it = densestSubgraph.iterator();
        String densestSubgraphString = "{" + it.next();
        while ( it.hasNext() ){
            densestSubgraphString += ", " + it.next();
        }
        densestSubgraphString += "}";

        //DEBUG
        /*
        String msg = "Everything's fine till here";
        msg += " --- Nodes: " + nodes;
        msg += ", Edges: " + edges;
        msg += ", Max degree: " + (nodeByDegree.length - 1);
        msg += ", Min degree: " + minDegree;
        msg += ", Initial density: " + densities[0];
        msg += ", Max density: " + maxDensity;
        msg += ", Min density: " + minDensity;
        msg += ", Densest subgraph: " + densestSubgraphString;

        return Arrays.stream(new StringOutput[]{new StringOutput(msg)});
        */

        long runningTime = System.currentTimeMillis() - start;

        return Arrays.stream(new DensestSubgraphOutput[]{new DensestSubgraphOutput(densestSubgraphString, maxDensity, runningTime, runningTimeVisit)});
    }

    @Procedure(name = "org.neo4j.aidna.examples.densestSubgraph_singleTx", mode = Mode.READ)
//    @Procedure
    @Description("Charikar's 2-approximation greedy algorithm for the densest-subgraph problem")
    public Stream<DensestSubgraphOutput> densestSubgraph_singleTx() {

        long start = System.currentTimeMillis();
        Set<Long>[] nodeByDegree = null;
        Map<Long, Integer> currentDegrees = new HashMap();
        int nodes = 0;
        int edges = 0;

        String densestSubgraphString = "";
        double maxDensity = 0;
        long runningTimeVisit = 0;

        try ( Transaction tx = db.beginTx() ) {

            ResourceIterable<Node> allNodes = tx.getAllNodes();

            int maxDegree = 0;
            for ( Node n : allNodes ){
                nodes++;
                int degree = n.getDegree();
                edges += degree;
                currentDegrees.put(n.getId(),degree);
                if (degree > maxDegree){
                    maxDegree = degree;
                }
            }
            edges /= 2;

            nodeByDegree = new Set[maxDegree+1];
            allNodes = tx.getAllNodes();
            for ( Node n : allNodes ){
                int degree = n.getDegree();
                if ( nodeByDegree[degree] == null ){
                    nodeByDegree[degree] = new HashSet<Long>();
                }
                nodeByDegree[degree].add(n.getId());
            }

            runningTimeVisit = System.currentTimeMillis() - start;


            long[] removedNodes = new long[nodes+1];
            removedNodes[0] = -1;

            double[] densities = new double[nodes+1];
            densities[0] = ((double)edges)/nodes; //densities[0] contains the density of the whole graph

            double currentEdges = edges;
            int currentNodes = nodes;

            int minDegree = 0;

            //main cycle lasts until all nodes have been deleted
            for ( int i=1; i<removedNodes.length; i++ ) {
                while ( minDegree < nodeByDegree.length && (minDegree < 0 || nodeByDegree[minDegree] == null || nodeByDegree[minDegree].isEmpty()) ){
                    minDegree++;
                }

                long n = nodeByDegree[minDegree].iterator().next();
                nodeByDegree[minDegree].remove(n);
                currentDegrees.remove(n);
                removedNodes[i] = n;

                currentEdges -= minDegree;
                currentNodes--;
                densities[i] = (currentNodes == 0) ? 0.0 : currentEdges/currentNodes;

                //Collection<Long> neighbors = new LinkedList();
                Map<Long,Integer> neighbors = new HashMap();
                Node node = tx.getNodeById(n);
                for ( Relationship rel : node.getRelationships() ){
                    long neighbor = rel.getOtherNode(node).getId();
                    if ( currentDegrees.containsKey(neighbor) ){
                        int currentAdjacentEdges = (neighbors.containsKey(neighbor)) ? neighbors.get(neighbor) : 0;
                        currentAdjacentEdges++;
                        neighbors.put(neighbor,currentAdjacentEdges);
                    }
                }

                for ( long x : neighbors.keySet() ){
                    int degreeReduction = neighbors.get(x);
                    int currentDegree = currentDegrees.get(x);
                    int newDegree = currentDegree - degreeReduction;
                    currentDegrees.put(x,newDegree);

                    nodeByDegree[currentDegree].remove(x);
                    if ( nodeByDegree[newDegree] == null ){
                        nodeByDegree[newDegree] = new HashSet();
                    }
                    nodeByDegree[newDegree].add(x);

                    //preparing for the next iteration
                    //minDegree can decrease by at most 1 in every iteration on simple graphs, but it can decrease by more than 1 on multigraphs
                    if ( newDegree < minDegree ){
                        minDegree = newDegree;
                    }
                }
            }

            int iMaxDensity = -1;
            for ( int i=0; i<densities.length; i++ ){
                double density = densities[i];
                if ( density > maxDensity ){
                    maxDensity = density;
                    iMaxDensity = i;
                }
            }
            double minDensity = densities[densities.length-2]; //sanity check, it should be zero

            //the ultimate densest subgraph to be returned is composed of all nodes in removedNodes from iMaxDensity on
            Collection<Long> densestSubgraph = new LinkedList();
            for ( int i=iMaxDensity+1; i<removedNodes.length; i++){
                densestSubgraph.add(removedNodes[i]);
            }

            Iterator<Long> it = densestSubgraph.iterator();
            densestSubgraphString += "{" + it.next();
            while ( it.hasNext() ){
                densestSubgraphString += ", " + it.next();
            }
            densestSubgraphString += "}";

            //DEBUG
            /*
            String msg = "Everything's fine till here";
            msg += " --- Nodes: " + nodes;
            msg += ", Edges: " + edges;
            msg += ", Max degree: " + (nodeByDegree.length - 1);
            msg += ", Min degree: " + minDegree;
            msg += ", Initial density: " + densities[0];
            msg += ", Max density: " + maxDensity;
            msg += ", Min density: " + minDensity;
            msg += ", Densest subgraph: " + densestSubgraphString;

            return Arrays.stream(new StringOutput[]{new StringOutput(msg)});
            */
            tx.close();
        }

        long runningTime = System.currentTimeMillis() - start;

        return Arrays.stream(new DensestSubgraphOutput[]{new DensestSubgraphOutput(densestSubgraphString, maxDensity, runningTime, runningTimeVisit)});
    }

    public static class StringOutput {
        // This records contain a single field named 'nodeId'
        public String output;

        public StringOutput(String output) {
            this.output = output;
        }
    }

    public static class DensestSubgraphOutput {
        // This records contain a single field named 'nodeId'
        public String densestSubgraph;
        public double density;
        public long runningTime_ms;
        public long runningTimeVisit_ms;

        public DensestSubgraphOutput(String densestSubgraph, double density, long runningTime, long runningTimeVisit) {
            this.densestSubgraph = densestSubgraph;
            this.density = density;
            this.runningTime_ms = runningTime;
            this.runningTimeVisit_ms = runningTimeVisit;
        }
    }	
}
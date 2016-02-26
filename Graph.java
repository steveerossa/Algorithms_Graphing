/*TCSS 342 HW 5. Graph.java
 * Author Steve Onyango
 * Date: 6/5/2014 */
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Scanner;


// Represents an edge in the graph.
class Edge
{
    public Vertex     dest;   // Second vertex in Edge
    public double     cost;   // Edge cost
    
    public Edge( Vertex d, double c )
    {
        dest = d;
        cost = c;
    }
}


// Represents a vertex in the graph.
class Vertex
{
    public String     name;   // Vertex name
    public List<Edge> adj;    // Adjacent vertices
    public double     dist;   // Cost
    public Vertex     prev;   // Previous vertex on shortest path
    public int        scratch;// Extra variable used in algorithm

    public Vertex( String nm )
      { name = nm; adj = new LinkedList<Edge>( ); reset( ); }

    public void reset( )
      { dist = Graph.INFINITY; prev = null; scratch = 0; }    
      
}

class FullEdge 
{
    String start;
    String dest;
    double cost;
    
    FullEdge(String st, String dst, double cst) 
    {
        start = st;
        dest = dst;
        cost = cst;
    }
    
    public String toString()
    {
        return "("+start+","+dest+","+cost+")";
    }
}



// Graph class: evaluate shortest paths.
//
// CONSTRUCTION: with no parameters.
//
// ******************PUBLIC OPERATIONS**********************
// void addEdge( String v, String w, double cvw )
//                              --> Add additional edge
// void printPath( String w )   --> Print path after alg is run
// void unweighted( String s )  --> Single-source unweighted

// ******************ERRORS*********************************
// Some error checking is performed to make sure graph is ok,
// and to make sure graph satisfies properties needed by each
// algorithm.  Exceptions are thrown if errors are detected.

public class Graph
{
    public static final double INFINITY = Double.MAX_VALUE;
    private Map<String,Vertex> vertexMap = new HashMap<String,Vertex>( );


     
    public Graph(String the_file_name)
    {
        try
        {
            FileReader fin = new FileReader( the_file_name );
            Scanner graphFile = new Scanner( fin );
            String line;
            int lineCount  = 0;
            while( graphFile.hasNextLine( ) )
            {
                line = graphFile.nextLine( );
                lineCount++;
                StringTokenizer st = new StringTokenizer( line );

                try
                {
                    if( st.countTokens( ) != 3 )
                    {
                        System.err.println( "Skip ill-formatted line " + line +",  Line "+lineCount);
                        continue;
                    }
                    String source  = st.nextToken( );
                    String dest    = st.nextToken( );
                    int    cost    = Integer.parseInt( st.nextToken( ) );
                    addEdge( source, dest, cost );
                }
                catch( NumberFormatException e )
                {
                	System.err.println( "Skipping ill-formatted line " + line +",  Line " + lineCount); 
                }
             }
         }
         catch( IOException e )
           { System.err.println( e ); }

    }

    public Graph() 
    {
		// TODO Auto-generated constructor stub
	}

	private boolean isConnected()
    {
        
        LinkedList<Vertex> q = new LinkedList<Vertex>();
        HashSet<Vertex> discovered = new HashSet<Vertex>();

        
        
        Iterator it = vertexMap.entrySet().iterator();        
        if (it.hasNext())
        {
	    Map.Entry pairs = (Map.Entry)it.next();
            Vertex v = (Vertex)pairs.getValue();

	    q.add(v);
            discovered.add(v);

            while(!q.isEmpty())
            {
                Vertex t = q.poll();

                for(Edge e : t.adj)
                {
                    if (!discovered.contains(e.dest))
                    {
                        discovered.add(e.dest);
                        q.add(e.dest);
                    }
                }
            }

            
            
            
            
            if (discovered.size() == vertexMap.size())
            {
                return true;
            }
        }            
        return false;
    }

    /** 
     * Method that produces and returns the least expensive redundantly connected subgraph
     */
    public Graph rcSubgraph()
    {
        Set<FullEdge> edges = new HashSet<FullEdge>();

        Graph resultGraph = this;

        Iterator it = vertexMap.entrySet().iterator();        
        while (it.hasNext())
        {
	    Map.Entry pairs = (Map.Entry)it.next();
            Vertex v = (Vertex)pairs.getValue();
            for(Edge e : v.adj)
            {
                edges.add(new FullEdge(v.name, e.dest.name, e.cost));
            }
        }

        double minCost = totalEdgeCost();

        for (Set<FullEdge> s : SetMaker.powerSet(edges)) 
        {
            Graph g = new Graph();
            Graph gc = new Graph();

            int fullEdges = 0;

            for(FullEdge fe : s)
            {
                g.addEdge(fe.start, fe.dest, fe.cost);
                gc.addEdge(fe.start, fe.dest, fe.cost);
                gc.addEdge(fe.dest, fe.start, fe.cost);
                fullEdges++;
            }
            if (gc.vertexMap.size() == vertexMap.size())
            {
                if (gc.isConnected())
                {
                    boolean reduntant = true;
                    //System.out.println(s);
                    for(int i=0; i<fullEdges; i++)
                    {
                        Graph ge = new Graph();
                        int index = 0;
                        for(FullEdge fe : s)
                        {                    
                             if (index!=i)
                             {
                                ge.addEdge(fe.start, fe.dest, fe.cost);
                                ge.addEdge(fe.dest, fe.start, fe.cost);
                             }
                             index++;
                        }

                        if (ge.vertexMap.size()<vertexMap.size())
                        {
                           reduntant = false;
                        }
                        else if (!ge.isConnected())
                        {
                           reduntant = false;
                        }
                    }

                    if (reduntant)
                    {
                        if (minCost>g.totalEdgeCost())
                        {
                            minCost = g.totalEdgeCost();
                            resultGraph = g;
                        }
                    }
                }
            }
        }                                 

        return resultGraph; 
    }

    /**
     * a method to return the total cost of a graph
     */
    public double totalEdgeCost()
    {
        Iterator it = vertexMap.entrySet().iterator();
        double totalCost = 0.0;

        while (it.hasNext())
        {
	    Map.Entry pairs = (Map.Entry)it.next();
            Vertex v = (Vertex)pairs.getValue();
            for(Edge e : v.adj)
            {
                totalCost += e.cost;
            }
        }

        return totalCost;
    }
    /**
     * This method summarizes the graph.
     */
    public String toString()
    {
        Iterator it = vertexMap.entrySet().iterator();
        String result = "";

        while (it.hasNext())
        {
        	
        	
        	Map.Entry pairs = (Map.Entry)it.next();
            Vertex v = (Vertex)pairs.getValue();
            for(Edge e : v.adj)
            {
                result += v.name + " " + e.dest.name + " " + e.cost + "\n"; 
            }            
        }

        return result;
    }


    /**
     * Add a new edge to the graph.
     */
    public void addEdge( String the_source_name, String destination, double the_cost )
    {
        Vertex v = getVertex( the_source_name );
        Vertex w = getVertex( destination );
        v.adj.add( new Edge( w, the_cost ) );
    }

    /**
     * Driver routine to handle unreachables and print total cost.
     * It calls recursive routine to print shortest path to
     * destNode after a shortest path algorithm has run.
     */
    public void printPath( String destName )
    {
        Vertex w = vertexMap.get( destName );
        if( w == null )
            throw new NoSuchElementException( "Destination vertex not found" );
        else if( w.dist == INFINITY )
            System.out.println( destName + " is unreachable" );
        else
        {
            System.out.print( "(Cost is: " + w.dist + ") " );
            printPath( w );
            System.out.println( );
        }
    }

    /**
     * If vertexName is not present, add it to vertexMap.
     * In either case, return the Vertex.
     */
    private Vertex getVertex( String vertexName )
    {
        Vertex v = vertexMap.get( vertexName );
        if( v == null )
        {
            v = new Vertex( vertexName );
            vertexMap.put( vertexName, v );
        }
        return v;
    }

    /**
     * Recursive routine to print shortest path to dest
     * after running shortest path algorithm. The path
     * is known to exist.
     */
    private void printPath( Vertex dest )
    {
        if( dest.prev != null )
        {
            printPath( dest.prev );
            System.out.print( " to " );
        }
        System.out.print( dest.name );
    }
    
    /**
     * Initializes the vertex output info prior to running
     * any shortest path algorithm.
     */
    private void clearAll( )
    {
        for( Vertex v : vertexMap.values( ) )
            v.reset( );
    }

    /**
     * Single-source unweighted shortest-path algorithm.
     */
    public void unweighted( String startName )
    {
        clearAll( ); 

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        Queue<Vertex> q = new LinkedList<Vertex>( );
        q.add( start ); start.dist = 0;

        while( !q.isEmpty( ) )
        {
            Vertex v = q.remove( );

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                if( w.dist == INFINITY )
                {
                    w.dist = v.dist + 1;
                    w.prev = v;
                    q.add( w );
                }
            }
        }
    }
    /**
     * Process a request; return false if end of file.
     */
    public static boolean processRequest( Scanner in, Graph g )
    {
        try
        {
        
            System.out.println( "Unweighted shortest distance:" );        
            System.out.print( "Enter start node:" );
            String startName = in.nextLine( );

            System.out.print( "Enter destination node:" );
            String destName = in.nextLine( );

            g.unweighted( startName );
            g.printPath( destName );
        }
        catch( NoSuchElementException e )
        { 
            return false; 
        }
        return true;
    }



    /**
     * A main routine that:
     * 1. Reads a file containing edges (supplied as a command-line parameter);
     * 2. Forms the graph;
     * 3. Repeatedly prompts for two vertices and
     *    runs the shortest path algorithm.
     * The data file is a sequence of lines of the format
     *    source destination cost
     */
    public static void main( String [ ] args )
    {
        Graph g = new Graph( );
        try
        {
            FileReader fin = new FileReader( args[0] );
            Scanner graphFile = new Scanner( fin );

            // Read the edges and insert
            String line;
            while( graphFile.hasNextLine( ) )
            {
                line = graphFile.nextLine( );
                StringTokenizer st = new StringTokenizer( line );

                try
                {
                    if( st.countTokens( ) != 3 )
                    {
                        System.err.println( "Skipping ill-formatted line " + line );
                        continue;
                    }
                    String source  = st.nextToken( );
                    String dest    = st.nextToken( );
                    int    cost    = Integer.parseInt( st.nextToken( ) );
                    g.addEdge( source, dest, cost );
                }
                catch( NumberFormatException e )
                  { System.err.println( "Skipping ill-formatted line " + line ); }
             }
         }
         catch( IOException e )
           { System.err.println( e ); }

         System.out.println( "File read..." );
         System.out.println( g.vertexMap.size( ) + " vertices" );

         Scanner in = new Scanner( System.in );
         while( processRequest( in, g ) )
             ;
    }
}



class SetMaker 
{

    // http://stackoverflow.com/questions/1670862/obtaining-powerset-of-a-set-in-java

    public static <T> Set<Set<T>> powerSet(Set<T> originalSet) 
    {
        Set<Set<T>> sets = new HashSet<Set<T>>();
        
        if (originalSet.isEmpty())
        {    // an empty set has only one subset
            sets.add(new HashSet<T>()); // and that's the empty set
            return sets;   // sets contains only the empty set
        }
        
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);   // first element
        Set<T> rest = new HashSet<T>(list.subList(1, list.size())); // all the rest
        
        for (Set<T> set : powerSet(rest))
        {
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }      
        return sets;
    }
}
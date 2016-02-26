
public class TestGraph
{
    public static void main(String[] args) 
    {
        //Graph myGraph = new Graph("MRCGTest1.txt");
        Graph myGraph = new Graph("graph1.txt");
        Graph subGraph = myGraph.rcSubgraph();
        System.out.println("Total cost = "+subGraph.totalEdgeCost());
        System.out.println(subGraph);
    }
}
import org.jgrapht.Graph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


//using https://code.google.com/archive/p/json-simple/downloads for JSON
//using http://jgrapht.org/ for GRAPHS
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class JsonSolution
{
    static String jsonItems;
    public static class JSONRunnable implements Runnable
    {
        private String url;
        public JSONRunnable(String url)
        {
            this.url = url;
        }
        public void run()
        {
            jsonItems = getJSON(this.url);
        }
    }

    public static String getJSON(String url)
    {
        HttpsURLConnection con = null;
        try
        {
            URL u = new URL(url);
            con = (HttpsURLConnection) u.openConnection();
            con.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            br.close();
            return sb.toString();
        }
        catch (MalformedURLException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (con != null)
            {
                try
                {
                    con.disconnect();
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }
    public static void main(String[] args)
    {
        //copied the json as strings instead because they were relatively small
        JSONRunnable myRunnable = new JSONRunnable("https://backend-challenge-summer-2018.herokuapp.com/challenges.json?id=1&page=1");
        Thread thread = new Thread(myRunnable);
        thread.start();
        while(thread.isAlive())
        { }
        String input = jsonItems;

        //create a graph with nodes having the ids of the products from the json file
        Graph<Long, DefaultEdge> jsonGraph = createJSONGraph(input);
        Set<Long> allVerticies = jsonGraph.vertexSet();
        Iterator vertIterator = allVerticies.iterator();
        CycleDetector cycleDetector = new CycleDetector(jsonGraph);

        //loops stores hashsets of menus that are invalid
        ArrayList<HashSet> loops = new ArrayList<>();
        while(vertIterator.hasNext())
        {
          Object temp = vertIterator.next();
          HashSet tempSet = (HashSet) cycleDetector.findCyclesContainingVertex((temp));
          loops.add(tempSet);
        }


        //gets the relation between nodes and shows how they are linked
        ConnectivityInspector connectivityInspector = new ConnectivityInspector(jsonGraph);
        List goodSets = connectivityInspector.connectedSets();
        Iterator iterator1 = goodSets.iterator();
        ArrayList<HashSet> goodCyclesList = new ArrayList<>();
        while(iterator1.hasNext())
        {
            HashSet temp = (HashSet) iterator1.next();
            goodCyclesList.add(temp);
            //System.out.println(temp);
        }

        //cross referencing the looped menus with the menus that are connected to eliminate the invalid menus
        ArrayList<HashSet> badMenus = new ArrayList<>();

        for (int i = 0; i < loops.size(); i++)
        {
            for (int j = 0; j < goodCyclesList.size(); j++)
            {
                if (loops.get(i).equals(goodCyclesList.get(j)))
                {
                    badMenus.add(goodCyclesList.get(j));
                    goodCyclesList.remove(j);
                }
            }
        }
        //formatting the output
        JSONObject output = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        output.put("valid_menus",jsonArray);
        output.put("invalid_menus",jsonArray);

        addToOutput(output,goodCyclesList,"valid_menus");
        addToOutput(output,badMenus,"invalid_menus");
        System.out.println(output);
    }

    //just formatting the ouput to be be in the correct manner
    public static void addToOutput(JSONObject output, ArrayList<HashSet> inputSets, String valid)
    {
        JSONArray validItems = new JSONArray();

        for (int i = 0; i < inputSets.size(); i ++)
        {
            ArrayList children = new ArrayList();
            HashSet temp = inputSets.get(i);
            JSONObject root = new JSONObject();
            JSONObject child = new JSONObject();
            Iterator iterator = temp.iterator();
            boolean first = true;
            int counter = 0;
            while (iterator.hasNext())
            {
                if (first)
                {
                    root.put("root_id",iterator.next());
                    first = false;
                }
                else
                {
                    //  System.out.println(iterator.next());
                    children.add(counter, iterator.next());
                    counter++;
                }
            }
            child.put("children",children);
            validItems.add(0,root);
            validItems.add(1,child);
            output.put(valid,validItems);
        }

    }

    //creates a graph that uses the pIDs as node values
    private static Graph<Long, DefaultEdge> createJSONGraph(String input)
    {
        Graph<Long, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        JSONParser parser = new JSONParser();
        ArrayList<Object> listOfSubmenus = new ArrayList<>();
        try
        {
            JSONObject jsonResponse = (JSONObject) parser.parse(input);
            JSONArray arrayOfSpecificMenus = (JSONArray) jsonResponse.get("menus");
            for (int i = 0; i < arrayOfSpecificMenus.size(); i++)
            {
                listOfSubmenus.add(arrayOfSpecificMenus.get(i));
                JSONObject individualMenu = (JSONObject) arrayOfSpecificMenus.get(i);
                JSONArray childIDs = (JSONArray) individualMenu.get("child_ids");

                if (!graph.containsVertex((Long) individualMenu.get("id")))
                {
                    graph.addVertex((Long) individualMenu.get("id"));
                }
                if ((childIDs.size() != 0))
                {
                    for (int j = 0; j < childIDs.size(); j ++)
                    {
                        if (!graph.containsVertex((Long) childIDs.get(j)))
                        {
                            graph.addVertex((Long) childIDs.get(j));
                        }
                        graph.addEdge((Long) individualMenu.get("id"), (Long) childIDs.get(j));
                    }
                }
            }
        }
        catch(ParseException pe)
        {
            System.out.println("position: " + pe.getPosition());
            System.out.println(pe);
        }
        return graph;
    }
}

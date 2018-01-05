import org.jgrapht.Graph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

public class JsonSolution
{
    public static void main(String[] args)
    {
        String input = "{\n" +
                "  \"menus\":[\n" +
                "    {\n" +
                "      \"id\":1,\n" +
                "      \"data\":\"House\",\n" +
                "      \"child_ids\":[3]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\":2,\n" +
                "      \"data\":\"Company\",\n" +
                "      \"child_ids\":[4]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\":3,\n" +
                "      \"data\":\"Kitchen\",\n" +
                "      \"parent_id\":1,\n" +
                "      \"child_ids\":[5]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\":4,\n" +
                "      \"data\":\"Meeting Room\",\n" +
                "      \"parent_id\":2,\n" +
                "      \"child_ids\":[6]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\":5,\n" +
                "      \"data\":\"Sink\",\n" +
                "      \"parent_id\":3,\n" +
                "      \"child_ids\":[1]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\":6,\n" +
                "      \"data\":\"Chair\",\n" +
                "      \"parent_id\":4,\n" +
                "      \"child_ids\":[]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"pagination\":{\n" +
                "    \"current_page\":1,\n" +
                "    \"per_page\":5,\n" +
                "    \"total\":19\n" +
                "  }\n" +
                "}";
        Graph<Long, DefaultEdge> jsonGraph = createJSONGraph(input);
        Set<Long> allVerticies = jsonGraph.vertexSet();
        Iterator vertIterator = allVerticies.iterator();
        CycleDetector cycleDetector = new CycleDetector(jsonGraph);

        ArrayList<HashSet> loops = new ArrayList<>();
        while(vertIterator.hasNext())
        {
          Object temp = vertIterator.next();
          HashSet tempSet = (HashSet) cycleDetector.findCyclesContainingVertex((temp));
          loops.add(tempSet);
        }


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



        JSONObject output = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        output.put("valid_menus",jsonArray);
        output.put("invalid_menus",jsonArray);

        addToOutput(output,goodCyclesList,"valid_menus");
        addToOutput(output,badMenus,"invalid_menus");

        System.out.println(output);

    }

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

package Route_Optimization_System;

import java.util.*;

public class RouteManager {

	public static void main(String[] args) {
		Graph g = new Graph();

		// Sample nodes (locations)
		Node A = new Node("A_Restaurant");
		Node B = new Node("B");
		Node C = new Node("C");
		Node D = new Node("D");
		Node E = new Node("E_Customer");

		g.addNode(A);
		g.addNode(B);
		g.addNode(C);
		g.addNode(D);
		g.addNode(E);

		// Sample edges with weights (distance or time)
		g.addEdge(A, B, 4);
		g.addEdge(A, C, 2);
		g.addEdge(B, C, 1);
		g.addEdge(B, D, 5);
		g.addEdge(C, D, 8);
		g.addEdge(C, E, 10);
		g.addEdge(D, E, 2);

		String sourceId = "A_Restaurant";
		String targetId = "E_Customer";
		if (args.length >= 2) {
			sourceId = args[0];
			targetId = args[1];
		}

		DijkstraAlgorithm.Result res = DijkstraAlgorithm.shortestPath(g, sourceId, targetId);

		if (res.getPath().isEmpty()) {
			System.out.println("No path found between " + sourceId + " and " + targetId + ".");
		} else {
			System.out.println("Shortest path from " + sourceId + " to " + targetId + ":");
			StringJoiner sj = new StringJoiner(" -> ");
			for (Node n : res.getPath())
				sj.add(n.getId());
			System.out.println(sj.toString());
			System.out.printf("Total distance: %.2f\n", res.getDistance());
		}
	}
}

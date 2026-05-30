package Route_Optimization_System;

import java.util.*;

public class Graph {
	private final Map<Node, List<Edge>> adj = new HashMap<>();

	public void addNode(Node node) {
		adj.putIfAbsent(node, new ArrayList<>());
	}

	public void addEdge(Node from, Node to, double weight) {
		addNode(from);
		addNode(to);
		adj.get(from).add(new Edge(to, weight));
	}

	public List<Edge> getEdges(Node node) {
		return adj.getOrDefault(node, Collections.emptyList());
	}

	public Set<Node> getNodes() {
		return Collections.unmodifiableSet(adj.keySet());
	}

	public Node getNodeById(String id) {
		for (Node n : adj.keySet()) {
			if (n.getId().equals(id))
				return n;
		}
		return null;
	}
}

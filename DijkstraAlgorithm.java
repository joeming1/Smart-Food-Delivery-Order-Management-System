import java.util.*;

public class DijkstraAlgorithm {

	public static class Result {
		private final double distance;
		private final List<Node> path;

		public Result(double distance, List<Node> path) {
			this.distance = distance;
			this.path = path;
		}

		public double getDistance() {
			return distance;
		}

		public List<Node> getPath() {
			return path;
		}
	}

	public static Result shortestPath(Graph graph, String sourceId, String targetId) {
		Node source = graph.getNodeById(sourceId);
		Node target = graph.getNodeById(targetId);
		if (source == null || target == null) {
			return new Result(Double.POSITIVE_INFINITY, Collections.emptyList());
		}

		Map<Node, Double> dist = new HashMap<>();
		Map<Node, Node> prev = new HashMap<>();

		for (Node n : graph.getNodes()) {
			dist.put(n, Double.POSITIVE_INFINITY);
		}
		dist.put(source, 0.0);

		PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
		pq.add(source);

		while (!pq.isEmpty()) {
			Node u = pq.poll();
			if (u.equals(target)) break;
			double du = dist.get(u);
			for (Edge e : graph.getEdges(u)) {
				Node v = e.getTarget();
				double alt = du + e.getWeight();
				if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
					dist.put(v, alt);
					prev.put(v, u);
					// update priority queue
					pq.remove(v);
					pq.add(v);
				}
			}
		}

		double distance = dist.getOrDefault(target, Double.POSITIVE_INFINITY);
		if (distance == Double.POSITIVE_INFINITY) {
			return new Result(distance, Collections.emptyList());
		}

		LinkedList<Node> path = new LinkedList<>();
		for (Node at = target; at != null; at = prev.get(at)) {
			path.addFirst(at);
		}

		return new Result(distance, path);
	}
}

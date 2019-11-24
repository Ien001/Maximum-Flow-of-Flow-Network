/**
* @author Ian Ren
* @Time 2019/11/20
* 
*/
package emp_proj.graphCode;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import emp_proj.graphCode.FF.GraphValue;

import java.util.HashMap;;

public class FF {
    /**
     * Return source node of SimpleGraph g.
     * if node name is "s", return it.
     * @param g a SimpleGraph 
     * @returns s the source node
     */
    public static Vertex source(SimpleGraph g){
    	Vertex s = new Vertex(null,null);
    	Iterator i = g.vertices();
    	while(i.hasNext()) {
    		s = (Vertex)i.next();
    		if(s.getName().equals("s")) return s;
    	}
    	return s;
    }
	
	/**
	 * convert integer to nearest smaller value which is 2^power
	 * @param d
	 * @return
	 */
	static int convert(int d) {
		if(d==1) return 1;
		int power = 0;
		while(d!=1) {
			d = d >>1;
			power += 1;
		}
		Double a = Math.pow(2, power);
		return a.intValue();
		
	}
	
	// return value of F.F. and Scaling
	public static class ReturnValue{
		long runningtime;
		int maxflow;
	}
	
	// data structure to store curr_flow_value, capacity, forward/backward
	public static class GraphValue{
		int curr_flow_value = 0;
		int capacity = 0;
		boolean if_forward = true;
	}
	
	/**
	 * deep first search algorithm to find s-t path in the residual graph
	 * @param s the begining vertex of searching
	 * @param G_residual the residual graph
	 * @param st_path a vector consist of vertex, which is s-t path
	 * @param seen a vector consist of vertices which have been seen 
	 * @return
	 */
	static Vector<Vertex> dfs(Vertex s, HashMap<Vertex,HashMap<Vertex,GraphValue>> G_residual, Vector<Vertex> st_path, Vector<Vertex> seen) {
		Iterator iterator_s_incidentEdgeList = s.incidentEdgeList.listIterator();
		while(iterator_s_incidentEdgeList.hasNext()) {
			Edge e = (Edge)iterator_s_incidentEdgeList.next();
			Vertex v =  e.getSecondEndpoint();
			
			// already add
			if(st_path.contains(v)) continue;
			// already seen
			if(seen.contains(v)) {
				continue;
			}else {
				seen.add(v);
			}

			GraphValue gv = G_residual.get(s).get(v);
			// join s-t path
			if(gv.if_forward) { 
				if(gv.capacity-gv.curr_flow_value>=1) {
					// join s-t path
					st_path.add(v);
					// already find a s-t path
					if(v.getName().equals("t")) return st_path;
		
					return dfs(v,G_residual,st_path,seen);
				}
			}else {
				if(gv.capacity>=1) {
					// join s-t path
					st_path.add(v);
					// already find a s-t path
					if(v.getName().equals("t")) return st_path;
		
					return dfs(v,G_residual,st_path,seen);
				}
			}
		}
		// there is no s-t path in this level, go back to upper level
		st_path.remove(s);
		// size = 0 : no s-t path 
		if(st_path.size()==0) return new Vector<Vertex>();
		// go back to upper level
		return dfs(st_path.lastElement(),G_residual,st_path,seen);
	}
	
	/**
	 * deep first search algorithm to find s-t path in the residual graph for scaling
	 * @param s the begining vertex of searching
	 * @param G_residual the residual graph
	 * @param st_path a vector consist of vertex, which is s-t path
	 * @param seen a vector consist of vertices which have been seen 
	 * @param d delta value of scaling
	 * @return
	 */
	static Vector<Vertex> dfs_scaling(Vertex s, HashMap<Vertex,HashMap<Vertex,GraphValue>> G_residual, Vector<Vertex> st_path, Vector<Vertex> seen, int d) {
		Iterator iterator_s_incidentEdgeList = s.incidentEdgeList.listIterator();
		while(iterator_s_incidentEdgeList.hasNext()) {
			Edge e = (Edge)iterator_s_incidentEdgeList.next();
			Vertex v =  e.getSecondEndpoint();
			
			// already add
			if(st_path.contains(v)) continue;
			// already seen
			if(seen.contains(v)) {
				continue;
			}else {
				seen.add(v);
			}

			GraphValue gv = G_residual.get(s).get(v);
			
			// if residual capacity bigger than delta, add
			if(gv.if_forward) { // forward edge
				if((gv.capacity-gv.curr_flow_value)>=d) { //
					// join s-t path
					st_path.add(v);
					// already find a s-t path
					if(v.getName().equals("t")) return st_path;
		
					return dfs_scaling(v,G_residual,st_path,seen,d);
				}
			}else { // backward edge
				if(gv.capacity>=d) {
					// join s-t path
					st_path.add(v);
					// already find a s-t path
					if(v.getName().equals("t")) return st_path;
		
					return dfs_scaling(v,G_residual,st_path,seen,d);
				}
			}
		}
		// there is no s-t path in this level, go back to upper level
		st_path.remove(s);
		// size = 0 : no s-t path 
		if(st_path.size()==0) return new Vector<Vertex>();
		// go back to upper level
		return dfs_scaling(st_path.lastElement(),G_residual,st_path,seen,d);
	}
	
	/**
	 * FordFulkerson algorithm
	 * @param FN a flow network
	 * @return rv  a data structure contains running time and maxflow value 
	 */
	static ReturnValue FordFulkerson(SimpleGraph FN) {
		// return value structure
		ReturnValue rv = new ReturnValue();
		
		// compute time
		Calendar calendar1 = Calendar.getInstance();
		long starttime = calendar1.getTimeInMillis();
		
		// construct Residual graph
		HashMap<Vertex,HashMap<Vertex,GraphValue>> m = new HashMap<Vertex,HashMap<Vertex,GraphValue>>();
		// traverse all vertices of Flow Network
		Iterator iterator_FN_vertices = FN.vertices();
		while(iterator_FN_vertices.hasNext()){
			// current node
			Vertex curr_v = (Vertex) iterator_FN_vertices.next();
			// to store the connection between this node and the others if there is an edge between the two
			HashMap<Vertex,GraphValue> curr_map = new HashMap<Vertex,GraphValue>();
			Iterator iterator_FN_incidentEdges = FN.incidentEdges(curr_v);
			while(iterator_FN_incidentEdges.hasNext()) {
				Edge curr_e = (Edge) iterator_FN_incidentEdges.next();
				Vertex next_v = curr_e.getSecondEndpoint();
				if(curr_v.equals(next_v)) continue;
				GraphValue gv = new GraphValue();
				// middle value
				Double a = (Double)curr_e.getData();
				gv.capacity =  a.intValue();
				// load to current map
				curr_map.put(next_v, gv);				
			}
			// load to the Residual graph
			m.put(curr_v, curr_map);			
		}
		System.out.println("Construct Residual graph successfully!");
		
		// while there is a s-t path
		while(true) { 
			Vertex source = source(FN);
			// if the source node is empty 
			if(source.getName()==null) break;
			
			// find s-t path
			Vector<Vertex> s_t_path_begin = new Vector<Vertex>(); // pass a empty vector
			Vector<Vertex> se = new Vector<Vertex>(); // record the vertex seen
			s_t_path_begin.add(source);
			se.add(source);
			
			Vector<Vertex> s_t_path_finish = new Vector<Vertex>(); // serve as final vector
			
			s_t_path_finish = dfs(source, m, s_t_path_begin, se);
			
			// there is no s-t path
			if(s_t_path_finish.isEmpty()) break;
			
			System.out.println("Find a s-t path!");
			// if there is a s-t path, find minimum capacity of edges of s-t path
			int minimum = Integer.MAX_VALUE;
			Vertex curr_v = s_t_path_finish.get(0);
			for(int i = 1; i<s_t_path_finish.size(); i++) {
				Vertex next_v = s_t_path_finish.get(i);
				// find minimum
				minimum = Math.min(m.get(curr_v).get(next_v).capacity, minimum);
				
				// update current vertex 
				curr_v = next_v;
			}
			
			// augment flow and update residual graph
			curr_v = s_t_path_finish.get(0);
			for(int i = 1; i<s_t_path_finish.size(); i++) {
				Vertex next_v = s_t_path_finish.get(i);
				// if forward edge:
				if(m.get(curr_v).get(next_v).if_forward) {
					// 1. push minimum unit of flow to edge
					HashMap<Vertex,GraphValue> curr_m = m.remove(curr_v);
					GraphValue gv_build = curr_m.get(next_v);
					gv_build.curr_flow_value += minimum;
					// load back
					curr_m.put(next_v, gv_build);
					m.put(curr_v, curr_m);
					
					// 2. build backward edge
					curr_m = m.remove(next_v);
					gv_build = new GraphValue();
					gv_build.capacity = minimum;
					gv_build.curr_flow_value = minimum;
					gv_build.if_forward = false;
					// load back
					curr_m.put(curr_v, gv_build);
					m.put(next_v, curr_m);
					
				}else { // if backward edge
					// 1. cancel minimum unit of flow on it
					HashMap<Vertex,GraphValue> curr_m = m.remove(curr_v);
					GraphValue gv_build = curr_m.get(next_v);
					gv_build.capacity -= minimum;
					gv_build.curr_flow_value -= minimum;
					// load back
					curr_m.put(next_v, gv_build);
					m.put(curr_v, curr_m);
					
					// 2. cancel minimum unit of flow on forward edge
					curr_m = m.remove(next_v);
					gv_build = curr_m.get(curr_v);
					gv_build.curr_flow_value -= minimum;
					// load back
					curr_m.put(curr_v, gv_build);
					m.put(next_v, curr_m);
					
				}
				// go for the next edge
				curr_v = next_v;
			}
			rv.maxflow += minimum;
			//System.out.println(minimum);
		}
		
		Calendar calendar2 = Calendar.getInstance();
		long totaltime = calendar2.getTimeInMillis() - starttime;
		rv.runningtime = totaltime;
		
		return rv;
	}
	
	/**
	 * Scaling algorithm
	 * @param FN a flow network
	 * @return rv return value contains running time and max flow value
	 */
	static ReturnValue Scaling(SimpleGraph FN) {
		// return value structure
		ReturnValue rv = new ReturnValue();
		
		// compute time
		Calendar calendar1 = Calendar.getInstance();
		long starttime = calendar1.getTimeInMillis();
		
		// construct Residual graph
		HashMap<Vertex,HashMap<Vertex,GraphValue>> m = new HashMap<Vertex,HashMap<Vertex,GraphValue>>();
		// traverse all vertices of Flow Network
		Iterator iterator_FN_vertices = FN.vertices();
		while(iterator_FN_vertices.hasNext()){
			// current node
			Vertex curr_v = (Vertex) iterator_FN_vertices.next();
			// to store the connection between this node and the others if there is an edge between the two
			HashMap<Vertex,GraphValue> curr_map = new HashMap<Vertex,GraphValue>();
			Iterator iterator_FN_incidentEdges = FN.incidentEdges(curr_v);
			while(iterator_FN_incidentEdges.hasNext()) {
				Edge curr_e = (Edge) iterator_FN_incidentEdges.next();
				Vertex next_v = curr_e.getSecondEndpoint();
				if(curr_v.equals(next_v)) continue;
				GraphValue gv = new GraphValue();
				// middle value to avoid error
				Double a = (Double)curr_e.getData();
				gv.capacity =  a.intValue();
				// load to current map
				curr_map.put(next_v, gv);				
			}
			// load to the Residual graph
			m.put(curr_v, curr_map);			
		}
		System.out.println("Construct Residual graph successfully!");
		
		// compute delta
		int delta = 0;
		Iterator iterator_FN_source_incidentEdgeList = source(FN).incidentEdgeList.iterator();
		while(iterator_FN_source_incidentEdgeList.hasNext()) {
			Edge e = (Edge)iterator_FN_source_incidentEdgeList.next();
			// middle value to avoid error
			Double a = (Double)e.getData();
			delta = Math.max(delta, a.intValue());
		}
		// to nearest 2^power
		delta = convert(delta);
		
		// while there is a s-t path
		while(delta>=1) {
			Vertex source = source(FN);
			// if the source node is empty 
			if(source.getName()==null) break;
			
			// find s-t path
			Vector<Vertex> s_t_path_begin = new Vector<Vertex>(); // pass a empty vector
			Vector<Vertex> se = new Vector<Vertex>(); // record the vertex seen
			s_t_path_begin.add(source);
			se.add(source);
			Vector<Vertex> s_t_path_finish = new Vector<Vertex>(); // serve as final vector
			
			// find
			s_t_path_finish = dfs_scaling(source, m, s_t_path_begin, se, delta);
			
			// there is no s-t path, delta / 2
			if(s_t_path_finish.isEmpty()) {
				delta=delta>>1;
				continue;
			}
			
			System.out.println("Find a s-t path!");
			// if there is a s-t path, find minimum capacity of edges of s-t path
			int minimum = Integer.MAX_VALUE;
			Vertex curr_v = s_t_path_finish.get(0);
			for(int i = 1; i<s_t_path_finish.size(); i++) {
				Vertex next_v = s_t_path_finish.get(i);
				// find minimum
				minimum = Math.min(m.get(curr_v).get(next_v).capacity, minimum);
				
				// update current vertex 
				curr_v = next_v;
			}
			
			// update residual graph
			curr_v = s_t_path_finish.get(0);
			for(int i = 1; i<s_t_path_finish.size(); i++) {
				Vertex next_v = s_t_path_finish.get(i);
				// if forward edge:
				if(m.get(curr_v).get(next_v).if_forward) {
					// 1. push minimum unit of flow to edge
					HashMap<Vertex,GraphValue> curr_m = m.remove(curr_v);
					GraphValue gv_build = curr_m.get(next_v);
					gv_build.curr_flow_value += minimum;
					// load back
					curr_m.put(next_v, gv_build);
					m.put(curr_v, curr_m);
					
					// 2. build backward edge
					curr_m = m.remove(next_v);
					gv_build = new GraphValue();
					gv_build.capacity = minimum;
					gv_build.curr_flow_value = minimum;
					gv_build.if_forward = false;
					// load back
					curr_m.put(curr_v, gv_build);
					m.put(next_v, curr_m);
					
				}else { // if backward edge
					// 1. cancel minimum unit of flow on it
					HashMap<Vertex,GraphValue> curr_m = m.remove(curr_v);
					GraphValue gv_build = curr_m.get(next_v);
					gv_build.capacity -= minimum;
					gv_build.curr_flow_value -= minimum;
					// load back
					curr_m.put(next_v, gv_build);
					m.put(curr_v, curr_m);
					
					// 2. cancel minimum unit of flow on forward edge
					curr_m = m.remove(next_v);
					gv_build = curr_m.get(curr_v);
					gv_build.curr_flow_value -= minimum;
					// load back
					curr_m.put(curr_v, gv_build);
					m.put(next_v, curr_m);
					
				}
				// go for the next edge
				curr_v = next_v;
			}
			rv.maxflow += minimum;
		}
		
		Calendar calendar2 = Calendar.getInstance();
		long totaltime = calendar2.getTimeInMillis() - starttime;
		rv.runningtime = totaltime;
		
		return rv;
	}
	
	
	// Driver program to test above functions
	public static void main(String[] args) throws java.lang.Exception {
		// graph file path
		String filepath = args[0];
		SimpleGraph G = new SimpleGraph();
		// load this graph to a FlowNetwork, return SimpleGraph and the sum of capacities of edges which come out of "s"
		GraphInput.Result res = GraphInput.LoadGraph(G, filepath);
		SimpleGraph FlowNetwork = res.simplegraph;
		
		// compute edge number and capacity
		int m = FlowNetwork.numEdges();
		int C = res.Capacity;
		
		// run FF
		System.out.println("Begin FordFulkerson..");
		ReturnValue RV1 = new ReturnValue();
		RV1 = FordFulkerson(FlowNetwork);
		int FF_maxflow = RV1.maxflow;
		long FF_runningtime = RV1.runningtime;
		System.out.println("FordFulkerson Done!");
		
		// run Scaling
		System.out.println("Begin Scaling..");
		ReturnValue RV2 = new ReturnValue(); 
		RV2 = Scaling(FlowNetwork); 
		int SC_maxflow = RV2.maxflow; 
		long SC_runningtime = RV2.runningtime;
		System.out.println("Scaling Done!");
		 
		
		System.out.println("Edge number m : " + m);
		System.out.println("Capacity C : " + C);
		System.out.println("The value of maximum flow generated by F.F. is " + FF_maxflow);
		System.out.println("The value of maximum flow generated by Scaling is " + SC_maxflow);
		System.out.println("The running time of F.F. is " + FF_runningtime);
		System.out.println("The running time of Scaling is " + SC_runningtime);
	}
}

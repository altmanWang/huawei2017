import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class MinFlow {
    public static int V = 6;

    /**
     *
     * @param rGraph 残留网络
     * @param s 源点
     * @param t 终点
     * @param path 路径
     * @return 是否可以在rGraph中找到一条从 s 到 t 的路径
     */
    public static boolean hasPath(int rGraph[][], int s, int t, int[] path, boolean[] visited,int[] dis) {
        visited[s] = true;
        for(int i = 0; i < rGraph.length; i++){
            if(rGraph[s][i] > 0){
                if(dis[i] > dis[s] + rGraph[s][i]){
                    dis[i] = dis[s] + rGraph[s][i];
                    path[i] = s;
                    if(!visited[i]){
                        if(hasPath(rGraph, i, t, path,visited,dis)){
                            return true;
                        }
                    }else
                        return true;
                }
            }
        }
        visited[s] = false;
        return false;
    }

    /**
     *
     * @param graph 有向图的矩阵表示
     * @param s 源点
     * @param t 终点
     * @return 最大流量
     */
    private static int maxFlow(int[][] graph,int s, int t) {
        int rGraph[][] = new int[V][V];
        for(int i=0; i<V; i++)
            for(int j=0; j<V; j++)
                rGraph[i][j] = graph[i][j];

        int maxFlow = 0;

        int path[] = new int[V];
        boolean[] visited = new boolean[V];
        int[] dis = new int[V];
        for(int i = 0; i < dis.length; i ++){
            dis[i] = Integer.MAX_VALUE;
        }
        dis[s] = 0;
        hasPath(rGraph, s, t, path,visited,dis);
        for(int i = 0 ; i<path.length; i ++){
            System.out.println(path[i]);
        }
        while(hasPath(rGraph, s, t, path,visited,dis)){
            visited = new boolean[V];
            for(int i = 0; i < dis.length; i ++){
                dis[i] = Integer.MAX_VALUE;
            }
            dis[s] = 0;


            int min_flow = Integer.MAX_VALUE;

            //更新路径中的每条边,找到最小的流量
            for(int v=t; v != s; v=path[v]){
                int u = path[v];
                min_flow = Math.min(min_flow, rGraph[u][v]);
            }

            //更新路径中的每条边
            for(int v=t; v != s; v=path[v]){
                int u = path[v];
                rGraph[u][v] -= min_flow;
                rGraph[v][u] += min_flow;
            }
            maxFlow += min_flow;
        }

        return maxFlow;
    }

    public static void main(String[] args) {
        //创建例子中的有向图
        int graph[][] = { { 0, 1, 13, 0, 0, 0 },
                { 0, 0, 1, 12, 0, 0 },
                { 0, 1, 0, 0, 1, 0 },
                { 0, 0, 9, 0, 0, 1 },
                { 0, 0, 0, 7, 0, 1 },
                { 0, 0, 0, 0, 0, 0 } };
        V = graph.length;
        int flow = maxFlow(graph, 0, 5);
        System.out.println("The maximum possible flow is :" + flow);
    }
}
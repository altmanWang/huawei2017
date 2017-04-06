package com.cacheserverdeploy.deploy;

import javafx.scene.input.ScrollEvent;
import sun.awt.image.ImageWatched;

import javax.swing.undo.StateEdit;
import java.util.*;

/**
 * Created by altman on 17-3-12.
 */

public class Deploy {

    public static String[] deployServer(String[] graphContent) {

        /** do your work here **/


        return display(graphContent);
    }

    public static Graph read(String[] graphContent) {
        System.out.println(graphContent[0]);
        String[] data = graphContent[0].split(" ");
        int server_cost = Integer.parseInt(graphContent[2]);
        Graph G = new Graph(Integer.parseInt(data[0]) + Integer.parseInt(data[2]), Integer.parseInt(data[1]),
                Integer.parseInt(data[0]), Integer.parseInt(data[2]), server_cost);
        boolean node_client = true;
        for (int i = 4; i < graphContent.length; i++) {

            if (graphContent[i].equals("")) {
                node_client = false;
                continue;
            }
            if (node_client) {
                String[] parameter = graphContent[i].split(" ");
                G.addNodeEdge(new Edge(Integer.parseInt(parameter[0]), Integer.parseInt(parameter[1]),
                                Integer.parseInt(parameter[2]), Integer.parseInt(parameter[3]), Integer.parseInt(parameter[2])),
                        true);
            } else {
                String[] parameter = graphContent[i].split(" ");
                int self = Integer.parseInt(parameter[0]);
                int other = Integer.parseInt(parameter[1]);
                double value = 0;

                G.addNodeEdge(new Edge(self+G.getNumberOfNodes(),
                                Integer.parseInt(parameter[1]),
                                Integer.parseInt(parameter[2]),
                                0, Integer.parseInt(parameter[2])),
                        false);
            }

        }
        return G;
    }

    public static String[] display(String[] graphContent) {

        int par;
        Chromosome best_individual = null;
        Chromosome tmp_individual = null;
        int best_fitness_score = Integer.MAX_VALUE;
        long deadline = 85000;
        Long end;
        Long begin = System.currentTimeMillis();
        Graph G = Deploy.read(graphContent);


        Optimization opt = new Optimization(G, begin, deadline,graphContent);

        ArrayList<Vertex<Integer>> candidates = opt.influenceMax((int) (G.getNumberOfNodes() * 1));

        opt.setup(candidates);
        //opt.test();

        if(G.getNumberOfNodes()>600){
            //高级
            par = 1;
        }else if(G.getNumberOfNodes()>200){
            //中级
            par = 0;
        }else {
            //低级
            par = -1;
        }
        for (int i = 0; i < 10; i++) {
            opt.init(par);
            tmp_individual = opt.train();
            System.out.println("第"+i+"次成本："+tmp_individual.fitness_score);

            if (tmp_individual.fitness_score < best_fitness_score) {
                best_fitness_score = tmp_individual.fitness_score;
                best_individual = Chromosome.clone(tmp_individual);
            }
            end = System.currentTimeMillis();
            if (end - begin > deadline) {
                break;
            }
        }
        String[] result = opt.getFinalResult(best_individual);
        System.out.println(best_fitness_score);
        System.out.println("耗时:"+(System.currentTimeMillis()-begin));

        return result;
    }


    public static String stringReverse(String s, Graph g) {
        String result = "";
        String[] ss = s.split(" ");

		// 倒着遍历字符串数组，得到每一个元素
        for (int x = 0; x <ss.length-1; x++) {
            // 用新字符串把每一个元素拼接起来
            result += ss[x] + " ";
        }
        // 消费节点的id减去网络节点的数目，得到其真实的id
        int bw = Integer.parseInt(ss[ss.length-1]) - g.getNumberOfNodes();
        result = result + bw;
        return result;
    }


    //运用网络流的方法
    public static String[] methodOfFlow(String[] graphContent,HashSet<Integer> client) {

        ResidualGraph g = new ResidualGraph();

        String[] dataNum = graphContent[0].split(" ");
        g.numOfNode = Integer.parseInt(dataNum[0]);
        g.numOfEdge = Integer.parseInt(dataNum[1]);
        g.numOfClient = Integer.parseInt(dataNum[2]);
        g.costOfServer = Integer.parseInt(graphContent[2]);

        for (int i = 0; i < g.numOfClient + g.numOfNode + 2; i++) {
            g.graph.put(i, new ArrayList<resEdge>());
        }

        // 初始化普通节点及边
        for (int i = 4; i < g.numOfEdge + 4; i++) {
            if (graphContent[i].split(" ").length == 4) {
                String[] data = graphContent[i].split(" ");

                //无向图
                resEdge edge = new resEdge(Integer.parseInt(data[0]), Integer.parseInt(data[1]),
                        Integer.parseInt(data[2]), Integer.parseInt(data[3]), true);
                resEdge edge1 = new resEdge(Integer.parseInt(data[1]), Integer.parseInt(data[0]),
                        Integer.parseInt(data[2]), Integer.parseInt(data[3]), true);

                g.graph.get(edge.start).add(edge);
                g.graph.get(edge1.start).add(edge1);

            }
        }

        // 初始化超级源点及消费节点。
        // g.numOfNode + g.numOfClient + 1作为超级源点
        int demand = 0;
        g.graph.put(g.numOfNode + g.numOfClient + 1, new ArrayList<resEdge>());// 节点为超级源点
        for (int i = 0; i < g.numOfClient; i++) {
            if (graphContent[i + g.numOfEdge + 5].split(" ").length == 3) {
                String[] data = graphContent[i + g.numOfEdge + 5].split(" ");
                // 超级源点
                resEdge edg = new resEdge(g.numOfNode + g.numOfClient + 1, Integer.parseInt(data[0]) + g.numOfNode,
                        Integer.parseInt(data[2]), 0, true);
                g.graph.get(g.numOfNode + g.numOfClient + 1).add(edg);
                // 消费节点
                resEdge edge = new resEdge(Integer.parseInt(data[0]) + g.numOfNode, Integer.parseInt(data[1]),
                        Integer.parseInt(data[2]), 0, true);
                g.graph.get(edge.start).add(edge);

                demand += Integer.parseInt(data[2]);
            }

        }

        // 节点为超级汇点
        // g.numOfNode + g.numOfClient为超级汇点
        // ————————————需添加边到超级汇点——————这些边的起点为服务器部署的节点——————————
        // ----------后续可以试试先把所有点都认为是服务器----------------------
//		List<Integer> client = new ArrayList<>();

//		高级案例case 8
//		 client=Arrays.asList(470, 591, 110, 111, 597, 478, 599, 237, 239, 14, 16, 18, 19, 363, 484, 124, 245, 487, 246, 126, 247, 369, 7, 128, 249, 29, 491, 251, 372, 494, 253, 375, 376, 497, 259, 35, 37, 38, 380, 260, 141, 262, 263, 143, 266, 146, 267, 147, 389, 705, 41, 42, 707, 44, 45, 49, 390, 391, 271, 153, 398, 157, 710, 712, 714, 51, 54, 55, 56, 57, 58, 281, 161, 162, 284, 165, 166, 288, 169, 600, 607, 170, 298, 730, 610, 612, 734, 70, 735, 736, 75, 79, 185, 187, 501, 622, 624, 81, 746, 626, 83, 506, 628, 85, 509, 88, 190, 192, 195, 197, 750, 630, 751, 510, 512, 633, 754, 90, 635, 637, 94, 518, 519, 760, 520, 523, 644, 524, 404, 646, 649, 651, 411, 774, 413, 655, 536, 779, 417, 538, 419, 661, 782, 420, 421, 301, 302, 787, 426, 668, 669, 308, 790, 672, 793, 311, 312, 433, 554, 313, 434, 797, 798, 678, 316, 679, 560, 440, 561, 562, 442, 684, 443, 323, 444, 203, 324, 566, 687, 325, 447, 568, 689, 206, 449, 570, 571, 692, 210, 213, 576, 698, 578, 340, 221, 464, 223, 344, 103, 224, 225, 467, 347, 228, 229);

//		case demo
//		client = Arrays.asList(0, 3, 22, 27);

//		case0
//		client = Arrays.asList(7, 13, 15, 22,37,38,43);

//		case1
//		client = Arrays.asList(6, 7, 13, 17,35,41,48);

//		case2
//		client = Arrays.asList(12, 18, 23, 29,31,38,48);

//		case3
//		client = Arrays.asList(10, 26, 22, 29, 35);

//		case4
//		client = Arrays.asList(12, 15, 20, 22, 26 ,37 ,48);

//		case99
//		client=Arrays.asList(49, 190, 150, 271, 590, 393, 396, 595, 672, 311, 597, 313, 119, 50, 319, 13, 17, 121, 682, 122, 167, 289, 246, 763, 205, 22, 68, 290, 291, 173, 571, 133, 530, 730, 137, 218, 537, 735, 75, 77, 141, 185, 582, 781, 100, 585, 147, 148, 348, 624);
        for (int k : client) {
            resEdge edg = new resEdge(k, g.numOfNode + g.numOfClient, Integer.MAX_VALUE, 0, true);
            g.graph.get(k).add(edg);// 注意这里的i是否就是对应与节点i呢？
        }

        // 获取最大流
        g.findMaxFlow(g.numOfNode + g.numOfClient + 1, g.numOfNode + g.numOfClient);

        //// -------测试输出---------
        System.out.println("候选服务器的个数：" + client.size());
//		Collections.sort(client);
        System.out.println(client);

        System.out.println("使用服务器的个数：" + g.useServer.size());
        Collections.sort(g.useServer);
        System.out.println(g.useServer);
        System.out.println("总需求: " + demand);
        System.out.println("最大流： "+g.maxFlow);
        System.out.println("最小费用流： "+g.minCost);
        System.out.println("总耗费：" + (g.minCost + g.costOfServer * g.useServer.size()));
        System.out.println(g.flowPath);

        //--------数据输出-------
        String[] data = g.flowPath.toString().split(" ;");
        String[] cont = new String[data.length + 2];
        cont[0] = String.valueOf(data.length);
        cont[1] = "";

        for (int i = 0; i < data.length; i++) {
            cont[i + 2] = data[i];
            // System.out.println(cont[i+2]);
        }
        return cont;
    }

    public static int methodOfFlowCost(String[] graphContent,HashSet<Integer> client){
        ResidualGraph g = new ResidualGraph();

        String[] dataNum = graphContent[0].split(" ");
        g.numOfNode = Integer.parseInt(dataNum[0]);
        g.numOfEdge = Integer.parseInt(dataNum[1]);
        g.numOfClient = Integer.parseInt(dataNum[2]);
        g.costOfServer = Integer.parseInt(graphContent[2]);

        for (int i = 0; i < g.numOfClient + g.numOfNode + 2; i++) {
            g.graph.put(i, new ArrayList<resEdge>());
        }

        // 初始化普通节点及边
        for (int i = 4; i < g.numOfEdge + 4; i++) {
            if (graphContent[i].split(" ").length == 4) {
                String[] data = graphContent[i].split(" ");

                //无向图
                resEdge edge = new resEdge(Integer.parseInt(data[0]), Integer.parseInt(data[1]),
                        Integer.parseInt(data[2]), Integer.parseInt(data[3]), true);
                resEdge edge1 = new resEdge(Integer.parseInt(data[1]), Integer.parseInt(data[0]),
                        Integer.parseInt(data[2]), Integer.parseInt(data[3]), true);

                g.graph.get(edge.start).add(edge);
                g.graph.get(edge1.start).add(edge1);

            }
        }

        // 初始化超级源点及消费节点。
        // g.numOfNode + g.numOfClient + 1作为超级源点
        int demand = 0;
        g.graph.put(g.numOfNode + g.numOfClient + 1, new ArrayList<resEdge>());// 节点为超级源点
        for (int i = 0; i < g.numOfClient; i++) {
            if (graphContent[i + g.numOfEdge + 5].split(" ").length == 3) {
                String[] data = graphContent[i + g.numOfEdge + 5].split(" ");
                // 超级源点
                resEdge edg = new resEdge(g.numOfNode + g.numOfClient + 1, Integer.parseInt(data[0]) + g.numOfNode,
                        Integer.parseInt(data[2]), 0, true);
                g.graph.get(g.numOfNode + g.numOfClient + 1).add(edg);
                // 消费节点
                resEdge edge = new resEdge(Integer.parseInt(data[0]) + g.numOfNode, Integer.parseInt(data[1]),
                        Integer.parseInt(data[2]), 0, true);
                g.graph.get(edge.start).add(edge);

                demand += Integer.parseInt(data[2]);
            }

        }

        for (int k : client) {
            resEdge edg = new resEdge(k, g.numOfNode + g.numOfClient, Integer.MAX_VALUE, 0, true);
            g.graph.get(k).add(edg);// 注意这里的i是否就是对应与节点i呢？
        }

        // 获取最大流
        g.findMaxFlow(g.numOfNode + g.numOfClient + 1, g.numOfNode + g.numOfClient);

        return (g.minCost + g.costOfServer * g.useServer.size());
    }
}

/*
 * 子路径
 */
class SubWalks{
    static int[][] weights;
    private int value;
    LinkedList<Vertex<Integer>> sub_walks;
    private Vertex<Integer> start;
    //true:一个节点
    //false:多个节点
    private boolean one_two;
    public SubWalks(Vertex<Integer> start, int value,boolean flag){
        this.start = start;
        one_two = flag;
        this.value = value;
        sub_walks = new LinkedList<Vertex<Integer>>();
    }
    public int getWeight(){
        int support = Integer.MAX_VALUE;
        Vertex<Integer> last = start;
        for(Vertex<Integer> node : sub_walks){
            support = Math.min(support,weights[last.id][node.id]);
            last = node;
        }
        return support;
    }
    public int getValue(){
        return value;
    }
    public void setValue(int value){
        this.value = value;
    }
    public void setStart(Vertex<Integer> start){
        this.start = start;
    }
    public Vertex<Integer> getStart(){
        return start;
    }
    public boolean isOneTwo(){
        return one_two;
    }
    public void add(Vertex<Integer> node){
        sub_walks.add(node);
    }
    public Vertex<Integer> getDestination(){
        return sub_walks.getLast();
    }
    public Vertex<Integer> get(int idx){
        return sub_walks.get(idx);
    }
    public String toString(){
        String result = ""+start.id+"->";
        for(Vertex<Integer> walk : sub_walks){
            result +=(walk.id+"->");
        }
        result += ("  "+"价格为"+value);
        result += ("  "+"终点为"+getDestination());
        result += ("  "+"判断"+isOneTwo());
        result += ("  "+"权值"+getWeight());
        return result;
    }

}

/**
 * 边类
 * */
class Edge {
    // (v,e)
    final int v;
    final int e;
    final int weight;
    final int value;
    int remain;

    public Edge(int v, int e, int w, int val, int remain) {
        this.weight = w;
        this.value = val;
        this.v = v;
        this.e = e;
        this.remain = remain;
    }


    public int getSelf() {
        return this.v;
    }

    public int getOther() {
        return this.e;
    }

    public int getWeight() {
        return weight;
    }

    public int getValue() {
        return value;
    }

    public int getRemain() {
        return remain;
    }
}
/**
 * 节点类
 * */
class Vertex<Item> implements Comparable<Vertex<Item>> {
    //性价比
    private double performance = 0.0;
    //每个节点对应一个字符串编码，方便对Vertex类hascode
    private String code;
    //节点id
    final int id;
    //该属性用于影响力最大化
    private double priority = 0;
    //度
    private int degrees = 0;
    //节点的输出值
    private int outputs = 0;
    //该节点拥有的边
    private HashMap<Integer, Edge> edges;
    //该节点的邻居节点
    private LinkedList<Vertex<Item>> neighbor;

    //该节点是否为普通节点
    private boolean node_flag = false;
    //该节点是否为服务器
    private boolean client_flag = false;
    //如果是服务器，则该服务器的需求带宽为demand
    private int demand = 0;
    //记录被采样概率
    float visited_p = (float) 0.0;
    //
    LinkedList<SubWalks> sub_walk_set = new LinkedList<SubWalks>();

    public Vertex(int id) {
        neighbor = new LinkedList<Vertex<Item>>();
        this.id = id;
        edges = new HashMap<Integer, Edge>();
        this.code = id + "";
    }

    public LinkedList<SubWalks> getSubWalksSet(){
        return sub_walk_set;
    }


    public void sortNeighbors(){
        Collections.sort(neighbor, new Comparator<Vertex<Item>>() {
            public int compare(Vertex<Item> o1, Vertex<Item> o2) {
                if (o1.performance > o2.performance)
                    return -1;
                else if (o1.performance < o2.performance)
                    return 1;
                return 0;
            }
        });
    }

    public void sortSubWalks(){
        Collections.sort(sub_walk_set, new Comparator<SubWalks>() {
            public int compare(SubWalks o1, SubWalks o2) {
                if(o1.getValue() > o2.getValue())
                    return 1;
                else if(o1.getValue() < o2.getValue())
                    return -1;
                return 0;
            }
        });
    }

    public void addNeighbors(Vertex<Item> node) {
        if(node.isNode())
            neighbor.add(degrees, node);
        degrees += 1;
        if (!this.isClient() && !node.isClient())
            performance += (double) this.getWeight(node) / this.getValue(node);
        if (!this.isClient())
            priority += (double) this.getWeight(node) / this.getValue(node);

    }

    //如果是消费节点，设置其需求带宽
    public void setDemand(int demand) {
        this.demand = demand;
    }

    //获得消费节点的需求带宽
    public int getDemand() {
        return demand;
    }

    //是否为服务器
    public boolean isClient() {
        return client_flag;
    }

    //是否为普通节点
    public boolean isNode() {
        return node_flag;
    }

    //设置该节点为消费节点
    public void setClient() {
        client_flag = true;
    }

    //设置该节点为普通节点
    public void setNode() {
        node_flag = true;
    }

    //
    public String toString() {
        return this.id + "";
    }

    //获得该节点的所有邻居节点
    public LinkedList<Vertex<Item>> getNeighbors() {
        return neighbor;
    }

    //获得度
    public int getDegree() {
        return this.degrees;
    }

    //为该节点添加边
    public void addEdge(Edge edge) {
        outputs += edge.weight;
        this.edges.put(edge.getOther(), edge);
    }

    //获得指定边上的权重（带宽）
    public int getWeight(int other) {
        return this.edges.get(other).weight;
    }

    public int getWeight(Vertex<Item> other) {
        return getWeight(other.id);
    }

    //获得指定边上的单价
    public int getValue(int other) {
        return this.edges.get(other).value;
    }

    public int getValue(Vertex<Item> other) {
        return getValue(other.id);
    }

    //获得该节点的性价比
    public double getPerformance() {
        return performance;
    }

    public int getOutputs() {
        return outputs;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double dv,int id){
        Edge edge = edges.get(id);
        double tv = edge.weight / edge.value;

        priority = (dv - 2 * tv - (dv - tv) * tv * 0.01);
    }

    public int compareTo(Vertex obj) {
        if (!(obj instanceof Vertex))
            new RuntimeException("cuowu");
        Vertex v = (Vertex) obj;

        if (this.getPriority() < v.getPriority())
            return 1;
        else if (this.getPriority() == v.getPriority())
            return this.code.compareTo(v.code);
        return -1;
    }


}
/*
步骤
用来存储路径中的每一步
node：该步骤中选择的节点
demand：该步骤提供了需求
value:该步骤单价
cost：该步骤成本
*/
class Step {
    Vertex<Integer> node;
    int demand = 0;
    double value = 0;
    int cost = 0;

    public Step(Vertex<Integer> node, int demand, double value) {
        this.node = node;
        this.demand = demand;
        this.value = value;
    }

    public String toString() {
        return "该节点为" + node + " 承担需求： " + demand + " 价格为:" + value;
    }
}

/*
图
用来构造图
V：节点数量（普通节点+消费节点）
num_nodes：普通节点的数量
server_cost：服务器成本
E:边的数量
num_client:服务器数量
vertexs：图中所有的节点
Edges:图中所有的边
clients:图中服务器的集合
*/

class Graph {

    private final int V, num_nodes, server_cost;
    int E;
    private int num_client = 0;
    int edge_num = 0;
    private Vertex<Integer>[] vertexs;
    private Edge[] edges;
    private LinkedList<Vertex<Integer>> clients = new LinkedList<Vertex<Integer>>();

    /*
    初始化图
    给定图中，节点数量，边数量，普通节点数量，服务器数量，服务器成本
    */
    public Graph(int V, int E, int num_nodes, int num_client, int server_cost) {
        this.V = V;
        this.E = E * 2;
        this.num_nodes = num_nodes;
        this.server_cost = server_cost;
        vertexs = (Vertex<Integer>[]) new Vertex[this.V];
        for (int v = 0; v < this.V; v++) {
            vertexs[v] = new Vertex<Integer>(v);
        }
        edges = (Edge[]) new Edge[this.E];
    }

    //返回服务器成本
    public int getServerCost() {
        return server_cost;
    }

    /*
    为给定节点添加边
    node_client:用来判断给节点是否为消费节点
    True:普通节点
    False:消费节点
    */
    public void addNodeEdge(Edge edge, boolean node_client) {
        int v = edge.getSelf();
        int e = edge.getOther();
        if (node_client) {
            vertexs[v].setNode();
            vertexs[e].setNode();
        } else {
            vertexs[v].setClient();
            vertexs[v].setDemand(edge.getWeight());
            clients.add(num_client, vertexs[v]);
            num_client += 1;
        }
        vertexs[v].addEdge(edge);
        vertexs[v].addNeighbors(vertexs[e]);

        Edge other_edge = new Edge(edge.getOther(), edge.getSelf(), edge.getWeight(), edge.getValue(),
                edge.getRemain());
        vertexs[e].addEdge(other_edge);
        vertexs[e].addNeighbors(vertexs[v]);
        if (node_client) {
            edges[edge_num] = edge;
            edge_num += 1;
            edges[edge_num] = other_edge;
            edge_num += 1;
        }
    }

    //返回图中所有节点
    public Vertex<Integer>[] getVertexs() {
        return vertexs;
    }

    //返回图中所有消费节点
    public LinkedList<Vertex<Integer>> getClients() {
        return clients;
    }

    //返回给定节点的度
    public int getNodeDegree(int node) {
        return vertexs[node].getDegree();
    }

    //返回给定边上的权重（带宽）
    public int getEdgeWeight(int self, int other) {
        return vertexs[self].getWeight(other);
    }

    //获得给定边上的单价
    public double getEdgeValue(int self, int other) {
        return vertexs[self].getValue(other);
    }

    //获得给定节点的，邻居节点
    public LinkedList<Vertex<Integer>> getNodeNeighbors(int node) {
        return vertexs[node].getNeighbors();
    }

    public LinkedList<Vertex<Integer>> getNodeNeighbors(Vertex<Integer> node) {
        return getNodeNeighbors(node.id);
    }

    //获得所有节点的数量
    public int getNumberOfVertexes() {
        return V;
    }

    //获得边的数量
    public int getNumberOfEdges() {
        return E;
    }

    //给定节点的id，获得该节点类
    public Vertex<Integer> getNode(int id) {
        return vertexs[id];
    }

    //获得普通节点的数量
    public int getNumberOfNodes() {
        return this.num_nodes;
    }

    //获得消费节点的数量
    public int getNumberOfClient() {
        return this.num_client;
    }
}

/*
染色体（解）
boolean[] gen:存储服务器组合方式
例：
gen[1] = {true,false,false,true,false}
则，候选一号和四号节点为服务器，其他为普通节点。

fitness_score:该解得适应度值
size:gen的大小。其与candidate（候选集合）集合大小保持一致
 */
class Chromosome {
    boolean[] gene;
    int fitness_score = 0;
    int size = 0;

    public Chromosome() {
    }

    public Chromosome(int size) {
        if (size <= 0) {
            return;
        }
        initGeneSize(size);
        for (int i = 0; i < size; i++) {
            gene[i] = Math.random() >= 0.5;
        }
    }


    public Chromosome(int size, double[] performance, LinkedList<Vertex<Integer>> clients,ArrayList<Vertex<Integer>> candidates, float rate) {

        if (size <= 0) {
            return;
        }

        initGeneSize(size);
        int len = (int)(size*0.6);
        int idx;
        Random random = new Random();
        for(Vertex<Integer> client : clients){
            idx = client.getNeighbors().getFirst().id;
            if(performance[idx] > Math.random()){
                gene[idx] = true;
            }else {
                gene[idx] = false;
                idx = candidates.get(random.nextInt(len)).id;
                gene[idx] = performance[idx] > Math.random();
            }
        }
    }

    public Chromosome(int size, double[] performance, LinkedList<Vertex<Integer>> clients,ArrayList<Vertex<Integer>> candidates, float rate,
                      Chromosome sample) {
        if (size <= 0) {
            return;
        }
        initGeneSize(size);
        Random random = new Random();
        int len = (int)(candidates.size()*rate);
        int idx;
        for(int i = 0; i < sample.gene.length; i++){
            if(sample.gene[i]){
                if(Math.random() > 0.5)
                    this.gene[i] = true;
                else {
                    this.gene[i] = false;
                    idx = candidates.get(random.nextInt(len)).id;
                    this.gene[idx] = performance[idx] > Math.random();
                }
            }
        }
    }

    void initGeneSize(int size) {
        if (size <= 0) {
            return;
        }
        this.size = size;
        gene = new boolean[size];
    }

    public boolean[] getGene() {
        return gene;
    }

    //对染色体克隆
    public static Chromosome clone(final Chromosome target) {
        if (target == null || target.gene == null)
            return null;
        Chromosome result = new Chromosome();
        result.initGeneSize(target.size);
        for (int i = 0; i < target.size; i++) {
            result.gene[i] = target.gene[i];
        }
        result.fitness_score = target.fitness_score;
        return result;
    }
}

/*
优化寻找服务器
 */
class Optimization {

    /*
    种群基本信息配置
    best_individual:最优个体
    global_best_fitness：最有适应度值
    gen_size：染色体大小
    pop_size:种群大小
    max_gen:最大迭代次数
    mutation_rate:变异概率
    candidates：候选种子集
    population:种群
     */
    String[] graphContent;
    private Chromosome sample_individual;
    private Chromosome best_individual;
    private int global_best_fitness = Integer.MAX_VALUE;
    private int gen_size;
    private int pop_size = 200;
    private final int max_gen = 1000;
    private int mate_size;
    private final double mutation_rate = 1.0;
    static private ArrayList<Vertex<Integer>> candidates;
    private ArrayList<Chromosome> population;
    private int end_condition;

    // 寻找路径所需变量
    /*
    performance_init：性价比
    outputs_init:每个节点的输出带宽
    weights_init：两个边上的带宽
    clients_init:消费节点
     */
    static private double max_performance = 0;
    static private double min_performance = Integer.MAX_VALUE;
    static private double[] normalization_performance;

    static private int[][] values_init;
    static private double[] performance_init;
    static private int[] outputs_init;
    static private int[][] weights_init;
    static private LinkedList<Vertex<Integer>> clients_init;
    static Graph G;

    // 计时器
    long start_time;
    long deadline;

    //
    public Optimization(Graph G, long start_time, long deadline,String[] graphContent) {
        this.deadline = deadline;
        this.start_time = start_time;
        this.G = G;
        this.graphContent=graphContent;
    }

    //测试
    public void test(){
        SubWalks.weights = weights_init;
        Vertex<Integer> node;
        LinkedList<Vertex<Integer>> one_hop;
        LinkedList<Vertex<Integer>> two_hop;
        int value;
        SubWalks sub_walk;
        for(int i = 0; i < G.getNumberOfNodes(); i++){
            node = G.getNode(i);
            one_hop = node.getNeighbors();
            for(Vertex<Integer> node1:one_hop){
                value = node.getValue(node1);
                sub_walk = new SubWalks(node ,value,true);
                sub_walk.add(node1);
                node.sub_walk_set.add(sub_walk);
                two_hop = node1.getNeighbors();
                for(Vertex<Integer> node2:two_hop){
                    if(node2.id != node.id){
                        sub_walk = new SubWalks(node ,value + node1.getValue(node2),false);
                        sub_walk.add(node1);
                        sub_walk.add(node2);
                        node.sub_walk_set.add(sub_walk);
                    }
                }
            }
            LinkedList<SubWalks> sub_walks = node.getSubWalksSet();
            node.sortSubWalks();
            for(SubWalks walk : sub_walks){
                System.out.println(walk);
            }
        }
    }

    public void setup(ArrayList<Vertex<Integer>> candidates){
        int min_cost = Integer.MAX_VALUE;
        int tmp_cost = 0;
        this.candidates = candidates;
        this.gen_size = G.getNumberOfNodes();

        best_individual = null;
        normalization_performance = new double[G.getNumberOfNodes()];
        performance_init = new double[G.getNumberOfVertexes()];
        outputs_init = new int[G.getNumberOfVertexes()];
        weights_init = new int[G.getNumberOfVertexes()][G.getNumberOfVertexes()];
        values_init = new int[G.getNumberOfVertexes() ][G.getNumberOfVertexes()];
        clients_init = G.getClients();

        //对消费者排序
        Collections.sort(clients_init, new Comparator<Vertex<Integer>>() {
            public int compare(Vertex<Integer> o1, Vertex<Integer> o2) {
                if (o1.getDemand() > o2.getDemand())
                    return -1;
                else if (o1.getDemand() < o2.getDemand())
                    return 1;
                else
                    return 0;
            }
        });
        //
        LinkedList<Vertex<Integer>> two_hop;
        int value;
        SubWalks sub_walk;
        Vertex<Integer> node;
        for (int i = 0; i < G.getNumberOfVertexes(); i++) {
            node = G.getNode(i);
            performance_init[i] = node.getPerformance() ;
            outputs_init[i] = node.getOutputs();
            LinkedList<Vertex<Integer>> neighbors = node.getNeighbors();
            for (Vertex<Integer> neighbor : neighbors) {
                if (neighbor.isNode()){
                    weights_init[i][neighbor.id] = node.getWeight(neighbor);
                    values_init[i][neighbor.id] = node.getValue(neighbor);
                }
                if (node.isClient())
                    weights_init[node.id][neighbor.id] = node.getDemand();
                value = node.getValue(neighbor);
                sub_walk = new SubWalks(node ,value,true);
                sub_walk.add(neighbor);
                node.sub_walk_set.add(sub_walk);
                two_hop = neighbor.getNeighbors();
                for(Vertex<Integer> node2:two_hop){
                    if(node2.id != node.id){
                        sub_walk = new SubWalks(node ,value + neighbor.getValue(node2),false);
                        sub_walk.add(neighbor);
                        sub_walk.add(node2);
                        node.sub_walk_set.add(sub_walk);
                    }
                }
                node.sortSubWalks();

            }
            if(node.isNode()){
                node.sortNeighbors();
                if(performance_init[i] > max_performance){
                    max_performance = performance_init[i];
                }else if(performance_init[i] < min_performance){
                    min_performance = performance_init[i];
                }
            }
        }
        //归一化
        for(int i = 0; i < normalization_performance.length; i++){
            normalization_performance[i] = (performance_init[i]-min_performance)/(max_performance-min_performance);
        }

        //生成样本解
        sample_individual = new Chromosome();
        sample_individual.initGeneSize(gen_size);

        int idx;
        LinkedList<Vertex<Integer>> set = new LinkedList<Vertex<Integer>>();
        for(Vertex<Integer> client : clients_init) {
            idx = client.getNeighbors().getFirst().id;
            sample_individual.gene[idx] = true;
            set.add(client.getNeighbors().getFirst());
        }
        Collections.sort(set, new Comparator<Vertex<Integer>>() {
            public int compare(Vertex<Integer> o1, Vertex<Integer> o2) {
                if(o1.getPerformance() > o2.getPerformance())
                    return 1;
                else if(o1.getPerformance() < o2.getPerformance())
                    return -1;
                return 0;
            }
        });
        for(int i=0; i<set.size();i++){
            sample_individual.gene[set.get(i).id] = false;
            tmp_cost = anotherGetCostNew(sample_individual);
            if(tmp_cost < min_cost){
                min_cost = tmp_cost;
            }else {
                sample_individual.gene[set.get(i).id] = true;
            }
        }
    }

    // 种群初始化
    public void init(int par) {
        long end_time;
        int min_cost = Integer.MAX_VALUE;
        int tmp_cost = 0;
        best_individual = null;

        //初始化种群
        population = new ArrayList<Chromosome>();
        int idx = 0;
        min_cost = Integer.MAX_VALUE;
        tmp_cost = 0;
        System.out.println("初始化");
        if(par==1){
            pop_size = 10;
            end_condition = 200;
            mate_size = (int) (pop_size * 0.5);
        }else if(par==0){
            pop_size = 100;
            end_condition = 100;
            mate_size = (int) (pop_size * 0.5);
        }else {
            pop_size = 200;
            end_condition = 50;
            mate_size = (int) (pop_size * 0.5);
        }
        for (int i = 0; i < pop_size; i++) {
            Chromosome individual;
            if(i < (int)(pop_size*1.0)){
                individual = new Chromosome();
                individual.initGeneSize(gen_size);
                for(int j = 0; j < sample_individual.gene.length; j++){
                    individual.gene[j] = sample_individual.gene[j];
                }
            }else if(i < (int)(pop_size*0.5)){
                individual = new Chromosome(gen_size,
                        normalization_performance,
                        clients_init,
                        candidates,
                        (float) 0.6,
                        sample_individual);
            }else {
                individual = new Chromosome(gen_size,
                        normalization_performance,
                        clients_init,
                        candidates,
                        (float) 0.6);
            }
            tmp_cost = getCostNew(individual);
            individual.fitness_score = tmp_cost;
            population.add(individual);
            if (min_cost > tmp_cost) {
                min_cost = tmp_cost;
                best_individual = Chromosome.clone(individual);
                global_best_fitness = min_cost;
            }
        }
    }

    // 选择
    public ArrayList<Chromosome> selection() {
        ArrayList<Chromosome> next_population = new ArrayList<Chromosome>();
        next_population.add(best_individual);
        while (next_population.size() < mate_size) {
            Random rand = new Random();
            int i = rand.nextInt(pop_size);
            int j = rand.nextInt(pop_size);
            if (population.get(i).fitness_score < population.get(j).fitness_score)
                next_population.add(Chromosome.clone(population.get(i)));
            else
                next_population.add(Chromosome.clone(population.get(j)));
        }
        return next_population;
    }

    // 交叉
    public void crossover(ArrayList<Chromosome> next_population) {
        Chromosome a;
        Chromosome b;
        int start_index;
        int end_index;
        int tmp;
        Chromosome a_next;
        Chromosome b_next;
        while (next_population.size() < pop_size) {
            Random random = new Random();
            a = next_population.get(random.nextInt(mate_size));
            b = next_population.get(random.nextInt(mate_size));
            start_index = random.nextInt(gen_size);
            end_index = random.nextInt(gen_size);
            if(start_index > end_index){
                tmp = start_index;
                start_index = end_index;
                end_index = start_index;
            }

            a_next = new Chromosome();
            b_next = new Chromosome();
            a_next.initGeneSize(gen_size);
            b_next.initGeneSize(gen_size);

            for (int i = 0; i < start_index; i++) {
                a_next.gene[i] = a.gene[i];
                b_next.gene[i] = b.gene[i];
            }
            for (int i = start_index; i < end_index; i++) {
                a_next.gene[i] = b.gene[i];
                b_next.gene[i] = a.gene[i];
            }
            for (int i = end_index; i < gen_size; i++) {
                a_next.gene[i] = a.gene[i];
                b_next.gene[i] = b.gene[i];
            }
            next_population.add(a_next);
            next_population.add(b_next);
        }
    }

    // 变异
    public void mutate(ArrayList<Chromosome> next_population) {
        Chromosome individual;
        Random random = new Random();
        Vertex<Integer> node;
        int idx;
        for (int i = 1; i < next_population.size(); i++) {
            individual = next_population.get(i);
            if (Math.random() < mutation_rate) {
                for(int t = 0; t < 1; t++){
                    node = candidates.get(random.nextInt(candidates.size()));
                    idx = node.id;
                    individual.gene[idx] = !individual.gene[idx];
                }
            }
        }
    }

    //局部搜索
    public void localSearch() {
        Chromosome tmp = Chromosome.clone(best_individual);
        int min_cost = global_best_fitness;
        int tmp_cost = 0;
        System.out.println("-----");
        for(int i = 0; i < tmp.gene.length; i++){
            if(tmp.gene[i]){
                tmp.gene[i] = !tmp.gene[i];
                tmp_cost = anotherGetCostNew(tmp);
                if (tmp_cost < min_cost) {
                    min_cost = tmp_cost;
                    System.out.println("+++++");
                    tmp.fitness_score = tmp_cost;
                    best_individual = Chromosome.clone(tmp);
                    best_individual.fitness_score = min_cost;
                    global_best_fitness = min_cost;
                } else {
                    tmp.gene[i] = !tmp.gene[i];
                }
            }
        }
    }

    public void anotherlocalSearch() {
        Chromosome tmp = Chromosome.clone(best_individual);
        boolean flag = true;
        Random random;
        int idx;
        int min_cost = global_best_fitness;
        int tmp_cost = 0;
        while (flag) {
            random = new Random();
            for(int i=0; i < 7; i++){
                idx = random.nextInt(gen_size);
                if(normalization_performance[idx]>Math.random())
                    tmp.gene[idx] = true;
                else
                    tmp.gene[idx] = false;
            }
            tmp_cost = getCostNew(tmp);
            if (tmp_cost < min_cost) {
                System.out.println("******");
                min_cost = tmp_cost;
                tmp.fitness_score = tmp_cost;
                best_individual = Chromosome.clone(tmp);
                best_individual.fitness_score = min_cost;
                global_best_fitness = min_cost;
            } else {
                flag = false;
            }
        }
    }

    //获得下一代
    public ArrayList<Chromosome> genNextPopulation() {
        ArrayList<Chromosome> next_population = selection();
        crossover(next_population);
        mutate(next_population);
        anotherlocalSearch();
        return next_population;
    }

    //训练
    public Chromosome train() {
        System.out.println("训练");
        int count = 0;
        int last_min_cost = 0;
        int min_cost, tmp_cost;
        Long end_time;
        for (int t = 0; t < max_gen; t++) {

            min_cost = global_best_fitness;
            tmp_cost = 0;
            population = genNextPopulation();

            for (Chromosome individual : population) {
                tmp_cost = getCostNew(individual);
                individual.fitness_score = tmp_cost;
                if (min_cost > tmp_cost) {
                    min_cost = tmp_cost;
                    best_individual = Chromosome.clone(individual);
                    global_best_fitness = min_cost;
                    localSearch();
                }
                end_time = System.currentTimeMillis();
                //判断是否超时
                if (end_time - start_time > deadline) {
                    return best_individual;
                }
            }

            //判断是否收敛
            if (last_min_cost == global_best_fitness) {
                count += 1;
                if (count == end_condition) {
                    return best_individual;
                }
            } else {
                count = 0;
            }
            last_min_cost = min_cost;
            end_time = System.currentTimeMillis();
            //判断是否超时
            if (end_time - start_time > deadline) {
                return best_individual;
            }
            System.out.println(t+"   "+global_best_fitness);
        }
        return best_individual;
    }


    //获得结果
    public String[] getResult(Chromosome individual) {
        int idx = 0;
        String[] result;
        String tmp;
        boolean[] gen = individual.getGene();
        HashSet<Vertex<Integer>> server_set = new HashSet<Vertex<Integer>>();
        for (int i = 0; i < gen.length; i++) {
            if (gen[i])
                server_set.add(G.getNode(i));
        }
        LinkedList<LinkedList<Step>> walks = findWalksNew(G, server_set, clients_init, performance_init, outputs_init,
                weights_init, G.getNumberOfVertexes());
        result = new String[walks.size()];

        for (LinkedList<Step> walk : walks) {
            tmp = "";
            for (Step step : walk) {
                tmp += (step.node + " ");
                if (server_set.contains(step.node)) {
                    tmp += (";" + walk.getLast().demand);
                    break;
                }
            }
            result[idx] = tmp;
            idx += 1;
        }


        return result;
    }
    //目标函数，获得每个解得成本
    public int getCostNew(Chromosome individual) {
        int result = 0;
        boolean[] gen = individual.getGene();
        //server_set：服务器集合
        HashSet<Vertex<Integer>> server_set = new HashSet<Vertex<Integer>>();
        HashSet<Integer> sets = new HashSet<Integer>();
        for (int idx = 0; idx < gen.length; idx++) {
            if (gen[idx])
                server_set.add(G.getNode(idx));
        }
        //寻找该解下的所有路径
        LinkedList<LinkedList<Step>> walks = findWalksNew(G, server_set, clients_init, performance_init, outputs_init,
                weights_init, G.getNumberOfVertexes());

        //计算成本
        individual.gene = new boolean[individual.size];
        for (LinkedList<Step> walk : walks) {
            for (Step step : walk) {
                //确保当路径到达新增服务器时，停止计算成本
                /*
                例如：
                0->1->2->5->4
                其中，2号节点在之后被选择为服务器。则计算成本是，应当到达2号节点，停止。
                 */
                if (server_set.contains(step.node)) {
                    sets.add(step.node.id);
                    result += step.cost;
                    break;
                }
            }
        }

        for(Integer i : sets){
            individual.gene[i] = true;
        }
        result += G.getServerCost() * sets.size();
        return result;
    }

    public int anotherGetCostNew(Chromosome individual) {
        int result = 0;
        boolean[] gen = individual.getGene();
        //server_set：服务器集合
        HashSet<Vertex<Integer>> server_set = new HashSet<Vertex<Integer>>();
        HashSet<Integer> sets = new HashSet<Integer>();
        for (int idx = 0; idx < gen.length; idx++) {
            if (gen[idx])
                server_set.add(G.getNode(idx));
        }
        //寻找该解下的所有路径
        LinkedList<LinkedList<Step>> walks = findWalksNew(G, server_set, clients_init, performance_init, outputs_init,
                weights_init, G.getNumberOfVertexes());

        //计算成本
        for (LinkedList<Step> walk : walks) {
            for (Step step : walk) {
                if (server_set.contains(step.node)) {
                    sets.add(step.node.id);
                    result += step.cost;
                    break;
                }
            }
        }
        result += G.getServerCost() * sets.size();
        return result;
    }

    //寻找所有路径
    public static LinkedList<LinkedList<Step>> findWalksNew(Graph G, HashSet<Vertex<Integer>> server_set,
                                                         LinkedList<Vertex<Integer>> clients, double[] performance, int[] outputs, int[][] weights, int len) {

        LinkedList<LinkedList<Step>> walks = new LinkedList<LinkedList<Step>>();
        //outputs_neighbor_server，该节点的邻居节点有服务器，计算其与服务器的带宽和
        //neighbor_has_server：判断该节点的邻居节点是否有服务器
        boolean[] neighbor_has_server = new boolean[len];
        int[] outputs_neighbor_server = new int[len];
        //克隆信息
        double[] performance_tmp = performance;
        int[] outputs_tmp = outputs.clone();
        int[][] weights_tmp = new int[len][];
        for (int i = 0; i < len; i++) {
            weights_tmp[i] = weights[i].clone();
        }
        //根据服务器，更新网络中各个节点的性价比
        for (Vertex<Integer> server : server_set) {
            for (Vertex<Integer> neighbor : server.getNeighbors()) {
                if (neighbor.isNode() && !server_set.contains(neighbor)) {
                    neighbor_has_server[neighbor.id] = true;
                    outputs_neighbor_server[neighbor.id] += neighbor.getWeight(server);
                }
            }
        }
        //为每一个服务器，寻找路径
        SubWalks.weights = weights_tmp;
        for (Vertex<Integer> client : clients) {
            int demand = client.getDemand();
            int remain_demand = demand;
            //确保满足每一个服务器的需求带宽
            while (remain_demand != 0) {
                LinkedList<Step> walk = findNew(client, client.getDemand(), server_set, neighbor_has_server,
                        performance_tmp, weights_tmp, outputs_neighbor_server, outputs_tmp);

                walks.addLast(walk);
                remain_demand -= walk.getLast().demand;
            }
        }
        return walks;
    }
    //给定服务器和需要承担的带宽，寻找路径.每次只寻找一次路径
    public static LinkedList<Step> findNew(Vertex<Integer> client, int demand, final HashSet<Vertex<Integer>> server_set,
                                        final boolean[] neighbor_has_server, final double[] performance, int[][] weights,
                                        int[] outputs_neighbor_server, int[] outputs) {

        HashSet<Vertex<Integer>> selected_nodes = new HashSet<Vertex<Integer>>();

        LinkedList<Step> walk = new LinkedList<Step>();
        walk.addLast(new Step(client, demand, 0));
        boolean flag = true;
        //
        int cost = 0;
        int support;
        Step step_tmp;
        Vertex<Integer> last;

        while (flag) {
            Step current_step = walk.getLast();
            Vertex<Integer> current_node = current_step.node;
            int current_demand = current_step.demand;

            //生成新的邻居节点集
            LinkedList<SubWalks> raw_sub_walks_set = current_node.getSubWalksSet();
            LinkedList<SubWalks> sub_walks_set = new LinkedList<SubWalks>();

            //根据规则，排序
            Vertex<Integer> end;
            for (SubWalks sub_walk : raw_sub_walks_set) {
                end = sub_walk.getDestination();
                if (end.isNode() && sub_walk.getWeight() > 0
                        && !selected_nodes.contains(end)
                        && !selected_nodes.contains(sub_walk.get(0))){
                    sub_walks_set.addLast(sub_walk);
                }
            }

            Collections.sort(sub_walks_set, new Comparator<SubWalks>() {
                public int compare(SubWalks o1, SubWalks o2) {
                    Vertex<Integer> end1 = o1.getDestination();
                    Vertex<Integer> end2 = o2.getDestination();
                    if(server_set.contains(end1) && !server_set.contains(end2))
                        return -1;
                    else if((!server_set.contains(end1) && server_set.contains(end2)))
                        return 1;

                    if ((neighbor_has_server[end1.id] && !neighbor_has_server[end2.id]) )
                       return -1;
                    else if((!neighbor_has_server[end1.id] && neighbor_has_server[end2.id]))
                        return 1;

                    if(o1.getValue() > o2.getValue())
                        return 1;
                    else if(o1.getValue() < o2.getValue())
                        return -1;

                    if(performance[end1.id] > performance[end2.id])
                        return -1;
                    else if(performance[end1.id] < performance[end2.id])
                        return 1;
                    return 0;
                }
            });

            LinkedList<Step>  next_steps = findNextStepNew(current_node, current_demand, sub_walks_set, server_set, neighbor_has_server,
                    weights, outputs_neighbor_server, performance, outputs);
            if (next_steps != null) {
                for(Step next_step : next_steps){
                    walk.addLast(next_step);
                    selected_nodes.add(next_step.node);
                    if (server_set.contains(next_step.node)) {
                        flag = false;
                        last = client;
                        support = walk.getLast().demand;
                        for (int i = 1; i < walk.size(); i++) {
                            step_tmp = walk.get(i);
                            cost += support * step_tmp.value;
                            step_tmp.cost = cost;
                            weights[last.id][step_tmp.node.id] -= support;
                            if (neighbor_has_server[step_tmp.node.id]){
                                outputs_neighbor_server[step_tmp.node.id] -= support;
                            }
                            outputs[step_tmp.node.id] -= support;
                            last = step_tmp.node;
                        }
                    }
                    break;
                }
            } else {
                flag = false;
                last = client;
                support = walk.getLast().demand;
                for (int i = 1; i < walk.size(); i++) {
                    step_tmp = walk.get(i);
                    cost += support * step_tmp.value;
                    step_tmp.cost = cost;
                    weights[last.id][step_tmp.node.id] -= support;
                    if (neighbor_has_server[step_tmp.node.id]){
                        outputs_neighbor_server[step_tmp.node.id] -= support;
                    }
                    outputs[step_tmp.node.id] -= support;
                    last = step_tmp.node;
                }

                //更新，新增服务器
                Vertex<Integer> new_server = walk.getLast().node;
                server_set.add(new_server);
                //更新新增服务器的邻居节点的配置信息，如perforamnce，outputs_neighbor_server
                for (Vertex<Integer> neighbor : new_server.getNeighbors()) {
                    if (neighbor.isNode() && !server_set.contains(neighbor)) {
                        neighbor_has_server[neighbor.id] = true;
                        outputs_neighbor_server[neighbor.id] += weights[neighbor.id][new_server.id];
                    }
                }
            }

        }
        return walk;
    }
    //寻找一条路径中的下一步骤
    public static LinkedList<Step> findNextStepNew(Vertex<Integer> current_node, int current_demand,
                                    LinkedList<SubWalks> neighbors, final HashSet<Vertex<Integer>> server_set,
                                    boolean[] neighbor_has_server, int[][] weights, int[] outputs_neighbor_server, double[] performance,
                                    int[] outputs) {

        if (neighbors.isEmpty())
            return null;
        SubWalks sub_walk = neighbors.removeFirst();
        int sub_walk_support = sub_walk.getWeight();
        if(sub_walk_support > 0){
            LinkedList<Step> next_steps = new LinkedList<Step>();
            int support = Math.min(sub_walk_support,current_demand);
            /*
            for(Vertex<Integer> node : sub_walk.sub_walks){
                Step step = new Step(node,support,current_node.getValue(node));
                next_steps.addLast(step);
                current_node = node;
            }*/
            Vertex<Integer> node = sub_walk.get(0);
            Step step = new Step(node,support,current_node.getValue(node));
            next_steps.addLast(step);
            return next_steps;
        }
        return findNextStepNew(current_node, current_demand, neighbors, server_set, neighbor_has_server, weights,
                outputs_neighbor_server, performance, outputs);
    }



    //影响力最大化，选择候选集

    public ArrayList<Vertex<Integer>> influenceMax(int keyNum){


        ArrayList<Vertex<Integer>> maxVertexs = new ArrayList<Vertex<Integer>>();

        TreeSet<Vertex> sortVertexsByPriority = new TreeSet<Vertex>();
        //此处是原方法的基于度的节点的存储方式
        ArrayList<Integer> vertexsOfNotClient = new ArrayList<Integer>();

        Vertex<Integer>[] vertexs = this.G.getVertexs();
        LinkedList<Vertex<Integer>> clients = this.G.getClients();


        for(Vertex v : vertexs){
            if(clients.contains(v)){
                continue;
            }
            sortVertexsByPriority.add(v);
            vertexsOfNotClient.add(v.id);
        }

        for(int j=0;j < keyNum;j++){
            Vertex v = sortVertexsByPriority.pollFirst();
            maxVertexs.add(v);
            LinkedList<Vertex> neighbor = v.getNeighbors();
            int value = 0;
            for(Vertex vn : neighbor){
                if(!maxVertexs.contains(vn) && !clients.contains(vn)){
                    sortVertexsByPriority.remove(vn);
                    vn.setPriority(vn.getPriority(), v.id);
                    sortVertexsByPriority.add(vn);
                }
            }

        }

        return maxVertexs;
    }


    public ArrayList<Vertex<Integer>> influenceMax(){
        ArrayList<Vertex<Integer>> candidates = new ArrayList<Vertex<Integer>>();
        int[][] value_init = new int[G.getNumberOfVertexes()][G.getNumberOfVertexes()];
        int[] visited_num_tmp = new int[G.getNumberOfVertexes()];
        int[] values = new int[G.getNumberOfVertexes()];
        LinkedList<Vertex<Integer>> neighbors;
        LinkedList<Vertex<Integer>> current_neighbors;
        LinkedList<Vertex<Integer>> next_neighbors;

        Vertex<Integer> node;
        for(int i = 0; i < G.getNumberOfNodes(); i++){
            node = G.getNode(i);
            neighbors = node.getNeighbors();
            for(Vertex<Integer> neighbor : neighbors){
                value_init[node.id][neighbor.id] = (int)node.getValue(neighbor);
            }
        }
        //
        for(Vertex<Integer> client : G.getClients()){
            for(int idx = 0; idx < values.length; idx++){
                values[idx] = Integer.MAX_VALUE;
            }
            current_neighbors = new LinkedList<Vertex<Integer>>();
            values[client.id] = 0;
            current_neighbors.add(client);
            for(int i = 0; i < 3; i++){
                next_neighbors = new LinkedList<Vertex<Integer>>();
                while (!current_neighbors.isEmpty()){
                    node = current_neighbors.removeFirst();
                    neighbors = node.getNeighbors();
                    for(Vertex<Integer> neighbor : neighbors){
                        /*
                        if(values[neighbor.id] > values[node.id]+node.getValue(neighbor)){
                            visited_num_tmp[neighbor.id] +=1;
                            next_neighbors.add(neighbor);
                        }*/
                        visited_num_tmp[neighbor.id] +=1;
                        next_neighbors.add(neighbor);
                    }
                }
                current_neighbors = next_neighbors;
            }
        }
        int max = 0;
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < G.getNumberOfNodes(); i++){
            if(visited_num_tmp[i]!=0){
                if(visited_num_tmp[i] > max){
                    max = visited_num_tmp[i];
                }
                if(visited_num_tmp[i] < min){
                    min = visited_num_tmp[i];
                }
            }
        }
        for(int i = 0; i < G.getNumberOfNodes(); i++){
            G.getNode(i).visited_p = ((visited_num_tmp[i] - min)/(float)(max-min));
            candidates.add(G.getNode(i));

        }
        Collections.sort(candidates, new Comparator<Vertex<Integer>>() {
            public int compare(Vertex<Integer> o1, Vertex<Integer> o2) {
                if(o1.visited_p > o2.visited_p)
                    return -1;
                else if(o1.visited_p < o2.visited_p)
                    return 1;
                if(o1.getPerformance() > o2.getPerformance())
                    return -1;
                else if(o1.getPerformance() < o2.getPerformance())
                    return 1;
                return 0;
            }
        });

        return candidates;
    }

    public String[] getFinalResult(Chromosome individual) {
        String tmp;
        int idx = 0;
        String[] result;
        int support;
        Vertex<Integer> server;
        HashSet<Integer> server_set = new HashSet<Integer>();
        HashSet<Vertex<Integer>> final_server_set = new HashSet<Vertex<Integer>>();
        for(int i = 0; i < individual.gene.length; i++){
            if(individual.gene[i])
                server_set.add(G.getNode(i).id);
        }
        System.out.println(server_set);
        result=Deploy.methodOfFlow(graphContent, server_set);

        return result;
    }
}

/**
 * 残留边类
 * 	主要方法：addFlow(int amount) 	更新残留网络中边的流
 * 			。。。。
 *
 * */
class resEdge {
    public final int start;//边的起始节点
    public final int end;//目标节点
    public final boolean isResidual;//是否为残留边   false表示该边是原始边
    public resEdge reverse;//反向边
    public int capacity;//容量，即带宽
    public int unitcost;//单位带宽费用
    public int flow;//流经该边的流

    public resEdge(int start, int end, int capacity, int unitcost, boolean isOriginal) {
        this.start = start;
        this.end = end;
        this.capacity = capacity;
        this.unitcost = unitcost;
        this.isResidual = !isOriginal;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getUnitcost() {
        return unitcost;
    }

    //更新残留网络后，用于更新图中边的流的大小
    public void addFlow(int amount) {
        if (amount < 0) {
            reverse.addFlow(-amount);
            return;
        }
        capacity -= amount;
        flow += amount;
        reverse.capacity += amount;
        // reverse.unitcost -= amount;
    }

//	//更新边的费用，及方向边的费用
//	public void addCost(int amount) {
//		if (amount > 0) {
//			reverse.addCost(-amount);
//			return;
//		}
//		// capacity -= amount;
//		reverse.unitcost -= amount;
//	}

    public resEdge getReverse() {
        return reverse;
    }

    public void setReverse(resEdge reverse) {
        this.reverse = reverse;
    }

    public boolean isOriginal() {
        return !isResidual;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setUnitcost(int unitcost) {
        this.unitcost = unitcost;
    }

    public void setFlow(int flow) {
        this.flow = flow;
    }
}
/**
 * 残留网络
 * 	主要方法：
 * 		findMaxFlow(int s, int t)				获取源s到汇点t的最大流
 * 		augmentPath(ArrayList<resEdge> path)	根据增广路径path来更新残留网络的属性值
 * 		shortestPaths(int source, int t)		寻找最小费用路径
 * */
class ResidualGraph {
    int numOfNode;// 普通网络节点的数量，不包括消费节点
    int numOfEdge;// 边的数量
    int costOfServer;// 服务器的费用
    int numOfClient;//消费节点的费用
    int maxFlow;//最大流
    int minCost;//最小费用
    StringBuilder flowPath = new StringBuilder();//记录所有增广路径上节点及该路径的带宽
    ArrayList<Integer> useServer = new ArrayList<Integer>();//记录使用的服务器

    Map<Integer, ArrayList<resEdge>> graph = new HashMap<Integer, ArrayList<resEdge>>();//残留图

//	public void maxFlow(int s, int t) {
//		findMaxFlow(s, t);
//		for (int i = 0; i < graph.size(); i++)
//			for (int j = 0; j < graph.get(i).size(); j++) {
//
//				// System.out.println(graph.get(i).get(j).start + "-> " + graph.get(i).get(j).end);
//				// System.out.println(graph.get(i).get(j).flow);
//			}
//	}

    public void findMaxFlow(int s, int t) {
        if (s == t)
            return;
        ////始终循环去找增广路径，直到路径为null退出循环
        while (true) {
            ArrayList<resEdge> pathEdge = new ArrayList<resEdge>();
            Deque<Integer> path = shortestPaths(s, t);
            if (path.isEmpty())
                break;
            // displayPath(s);
//			for (int i : path) {
//				System.out.println("第214行:    " + i);
//			}

            path.addLast(numOfNode + numOfClient);// 超级汇点
            path.addFirst(numOfNode + numOfClient + 1);// 超级源点

            while (!path.isEmpty()) {
                int i = path.poll();
                Integer k = path.poll();
                if (k != null) {
                    path.addFirst(k);
                    for (resEdge edg : graph.get(i)) {
                        if (edg.end == k && edg.capacity > 0)
                            pathEdge.add(edg);
                    }
                }
            }
            // if (path.isEmpty())
            // break;
            augmentPath(pathEdge);
        }
    }

    // 根据增广路径来修改带宽值，及添加反向边
    public void augmentPath(ArrayList<resEdge> path) {
        int capacity = Integer.MAX_VALUE;
        for (resEdge edge : path)
            capacity = Math.min(capacity, edge.getCapacity());
        maxFlow += capacity;
        for (resEdge edge : path)
            minCost += (capacity * edge.unitcost);

        flowPath.append(capacity + " ;");
        for (resEdge edge : path) {

            // edge.reverse.reverse = edge;
            if (edge.reverse == null) {
                resEdge reverse = new resEdge(edge.end, edge.start, 0, -edge.unitcost, false);

                edge.setReverse(reverse);
                reverse.setReverse(edge);
                graph.get(edge.end).add(reverse);
            }
            edge.addFlow(capacity);
        }

    }

    // 最小费用路径。。（还有一点问题,用队列解决）。。。已解决
    public Deque<Integer> shortestPaths(int source, int t) {
        Queue<Integer> queue = new ArrayDeque<Integer>();

        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        for (int node : graph.keySet())
            result.put(node, Integer.MAX_VALUE);
        result.put(source, 0);

        int[] pre = new int[graph.size()];// 前向节点，用于记录节点的最小费用路径的前向节点，默认初始化为超级源点的的位置
        for (int i = 0; i < pre.length; i++) {
            pre[i] = (numOfClient + numOfNode + 1);
        }

        queue.add(source);

        while (!queue.isEmpty()) {

            int i = queue.poll();
            // int[] copyPre = Arrays.copyOf(pre, pre.length);
            for (resEdge edge : graph.get(i)) {

                if (edge.isOriginal() && edge.capacity > 0) {

                    if (result.get(edge.end) > edge.getUnitcost() + result.get(i)) {
                        queue.add(edge.end);
                        result.put(edge.end, edge.getUnitcost() + result.get(i));
                        pre[edge.end] = i;

                    } else {
                        result.put(edge.end, result.get(edge.end));

                    }

                }

            }
            // if(Arrays.equals(pre, copyPre)){
            // p++;
            // }

            // -----------------这里还需要判断是否存在负环的状况，增加程序健壮性-------------------------

        }
        Deque<Integer> path = new ArrayDeque<Integer>();
        // path.addFirst(4);
        int tt = t;
        //循环前向数组来记录增广路径
        while (pre[t] != (numOfClient + numOfNode + 1)) {////////////////////////
            if (!useServer.contains(pre[tt])) {
                useServer.add(pre[tt]);//// 记录使用的服务器
            }
            path.addFirst(pre[t]);

            if (pre[pre[t]] == (numOfClient + numOfNode + 1)) {
                flowPath.append((pre[t] - numOfNode) + " ");
            } else {
                flowPath.append(pre[t] + " ");
            }

            t = pre[t];
//			System.out.println("第405行:    " + t);
        }

        // path.addFirst(0);
        // System.out.println(pre[t]);
        return path;

    }

}

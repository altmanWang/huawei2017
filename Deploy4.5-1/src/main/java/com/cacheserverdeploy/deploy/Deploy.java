package com.cacheserverdeploy.deploy;


import com.sun.org.apache.regexp.internal.RE;
import org.omg.CORBA.BAD_CONTEXT;

import java.util.*;


public class Deploy {

    public static String[] deployServer(String[] graphContent) {

        /** do your work here **/

        return display(graphContent);
    }

    public static Graph read(String[] graphContent) {

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
                int value = 0;

                G.addNodeEdge(new Edge(self + G.getNumberOfNodes(), Integer.parseInt(parameter[1]),
                        Integer.parseInt(parameter[2]), value, Integer.parseInt(parameter[2])), false);
            }

        }
        return G;
    }

    public static String[] display(String[] graphContent) {
        int par;
        Chromosome best_individual = null;
        Chromosome tmp_individual = null;
        int best_fitness_score = Integer.MAX_VALUE;
        int temp = Integer.parseInt(graphContent[0].split(" ")[0]);
        long deadline;
        if (temp < 400) {
            deadline = 82000;
        } else {
            deadline = 85000;
        }

        Long end;
        Long begin = System.currentTimeMillis();
        Graph G = Deploy.read(graphContent);

        Optimization opt = new Optimization(G, begin, deadline, graphContent);

        ArrayList<Vertex<Integer>> candidates = opt.influenceMax((int) (G.getNumberOfNodes() * 1));
        opt.test(candidates);
        /*
        opt.setup(candidates);

        if (G.getNumberOfNodes() > 600) {
            // 高级
            par = 1;
        } else if (G.getNumberOfNodes() > 200) {
            // 中级
            par = 0;
        } else {
            // 低级
            par = -1;
        }
        for (int i = 0; i < 10; i++) {
            opt.init(par);
            tmp_individual = opt.train();
            // System.out.println("第"+i+"次成本："+tmp_individual.fitness_score);

            if (tmp_individual.fitness_score < best_fitness_score) {
                best_fitness_score = tmp_individual.fitness_score;
                best_individual = Chromosome.clone(tmp_individual);
            }
            end = System.currentTimeMillis();
            if (end - begin > deadline) {
                break;
            }
        }
        System.out.println(System.currentTimeMillis()-begin);
        */
        //System.out.println("最終結果"+opt.getCost(best_individual));

        //String[] result = opt.getResult(best_individual);
        String[] result = new String[1];
        return result;
    }

    public static String stringReverse(String s, Graph g) {
        String result = "";
        String[] ss = s.split(" ");

        // 倒着遍历字符串数组，得到每一个元素
        for (int x = ss.length - 1; x > 0; x--) {
            // 用新字符串把每一个元素拼接起来
            result += ss[x] + " ";
        }
        // 消费节点的id减去网络节点的数目，得到其真实的id
        int bw = Integer.parseInt(ss[0]) - g.getNumberOfNodes();
        result = result + bw;
        return result;
    }

    // 运用网络流的方法
    public static String[] methodOfFlow(String[] graphContent, ArrayList<Integer> client) {
        ResidualGraph g = new ResidualGraph();
        // System.out.println(client);
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

                // 无向图
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
        // List<Integer> client = new ArrayList<>();

        // 高级案例case 8
        // client=Arrays.asList(470, 591, 110, 111, 597, 478, 599, 237, 239, 14,
        // 16, 18, 19, 363, 484, 124, 245, 487, 246, 126, 247, 369, 7, 128, 249,
        // 29, 491, 251, 372, 494, 253, 375, 376, 497, 259, 35, 37, 38, 380,
        // 260, 141, 262, 263, 143, 266, 146, 267, 147, 389, 705, 41, 42, 707,
        // 44, 45, 49, 390, 391, 271, 153, 398, 157, 710, 712, 714, 51, 54, 55,
        // 56, 57, 58, 281, 161, 162, 284, 165, 166, 288, 169, 600, 607, 170,
        // 298, 730, 610, 612, 734, 70, 735, 736, 75, 79, 185, 187, 501, 622,
        // 624, 81, 746, 626, 83, 506, 628, 85, 509, 88, 190, 192, 195, 197,
        // 750, 630, 751, 510, 512, 633, 754, 90, 635, 637, 94, 518, 519, 760,
        // 520, 523, 644, 524, 404, 646, 649, 651, 411, 774, 413, 655, 536, 779,
        // 417, 538, 419, 661, 782, 420, 421, 301, 302, 787, 426, 668, 669, 308,
        // 790, 672, 793, 311, 312, 433, 554, 313, 434, 797, 798, 678, 316, 679,
        // 560, 440, 561, 562, 442, 684, 443, 323, 444, 203, 324, 566, 687, 325,
        // 447, 568, 689, 206, 449, 570, 571, 692, 210, 213, 576, 698, 578, 340,
        // 221, 464, 223, 344, 103, 224, 225, 467, 347, 228, 229);

        // case demo
        // client = Arrays.asList(0, 3, 22, 27);

        // case0
        // client = Arrays.asList(7, 13, 15, 22,37,38,43);

        // case1
        // client = Arrays.asList(6, 7, 13, 17,35,41,48);

        // case2
        // client = Arrays.asList(12, 18, 23, 29,31,38,48);

        // case3
        // client = Arrays.asList(10, 26, 22, 29, 35);

        // case4
        // client = Arrays.asList(12, 15, 20, 22, 26 ,37 ,48);

        // case99
        // client=Arrays.asList(49, 190, 150, 271, 590, 393, 396, 595, 672, 311,
        // 597, 313, 119, 50, 319, 13, 17, 121, 682, 122, 167, 289, 246, 763,
        // 205, 22, 68, 290, 291, 173, 571, 133, 530, 730, 137, 218, 537, 735,
        // 75, 77, 141, 185, 582, 781, 100, 585, 147, 148, 348, 624);
        for (int k : client) {
            resEdge edg = new resEdge(k, g.numOfNode + g.numOfClient, Integer.MAX_VALUE, 0, true);
            g.graph.get(k).add(edg);// 注意这里的i是否就是对应与节点i呢？
        }

        // 获取最大流
        g.findMaxFlow(g.numOfNode + g.numOfClient + 1, g.numOfNode + g.numOfClient);

        //// -------测试输出---------
        System.out.println("候选服务器的个数：" + client.size());
        // Collections.sort(client);
        // System.out.println(client);

        System.out.println("使用服务器的个数：" + g.useServer.size());
        // Collections.sort(g.useServer);
        System.out.println(g.useServer);
        System.out.println("总需求: " + demand);
        System.out.println("最大流： " + g.maxFlow);
        System.out.println("最小费用流： " + g.minCost);
        System.out.println("总耗费：" + (g.minCost + g.costOfServer * g.useServer.size()));
        System.out.println(g.flowPath);

        // --------数据输出-------
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
}

/**
 * 边类
 */
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
 */
class Vertex<Item> implements Comparable<Vertex<Item>> {
    // 性价比
    private double performance = 0.0;
    // 每个节点对应一个字符串编码，方便对Vertex类hascode
    private String code;
    // 节点id
    final int id;
    // 该属性用于影响力最大化
    private double priority = 0;
    // 度
    private int degrees = 0;
    // 节点的输出值
    private int outputs = 0;
    // 该节点拥有的边
    private HashMap<Integer, Edge> edges;
    // 该节点的邻居节点
    private LinkedList<Vertex<Item>> neighbor;

    // 该节点是否为普通节点
    private boolean node_flag = false;
    // 该节点是否为服务器
    private boolean client_flag = false;
    // 如果是服务器，则该服务器的需求带宽为demand
    private int demand = 0;
    // 记录被采样概率
    float visited_p = (float) 0.0;

    public Vertex(int id) {
        neighbor = new LinkedList<Vertex<Item>>();
        this.id = id;
        edges = new HashMap<Integer, Edge>();
        this.code = id + "";
    }

    public void sortNeighbors() {
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

    public void addNeighbors(Vertex<Item> node) {
        neighbor.add(degrees, node);
        degrees += 1;
        if (!this.isClient() && !node.isClient())
            performance += (double) this.getWeight(node) / this.getValue(node);
        if (!this.isClient())
            priority += (double) this.getWeight(node) / this.getValue(node);

    }

    // 如果是消费节点，设置其需求带宽
    public void setDemand(int demand) {
        this.demand = demand;
    }

    // 获得消费节点的需求带宽
    public int getDemand() {
        return demand;
    }

    // 是否为服务器
    public boolean isClient() {
        return client_flag;
    }

    // 是否为普通节点
    public boolean isNode() {
        return node_flag;
    }

    // 设置该节点为消费节点
    public void setClient() {
        client_flag = true;
    }

    // 设置该节点为普通节点
    public void setNode() {
        node_flag = true;
    }

    //
    public String toString() {
        return this.id + "";
    }

    // 获得该节点的所有邻居节点
    public LinkedList<Vertex<Item>> getNeighbors() {
        return neighbor;
    }

    // 获得度
    public int getDegree() {
        return this.degrees;
    }

    // 为该节点添加边
    public void addEdge(Edge edge) {
        outputs += edge.weight;
        this.edges.put(edge.getOther(), edge);
    }

    // 获得指定边上的权重（带宽）
    public int getWeight(int other) {
        return this.edges.get(other).weight;
    }

    public int getWeight(Vertex<Item> other) {
        return getWeight(other.id);
    }

    // 获得指定边上的单价
    public int getValue(int other) {
        return this.edges.get(other).value;
    }

    public int getValue(Vertex<Item> other) {
        return getValue(other.id);
    }

    // 获得该节点的性价比
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

    public void setPriority(double dv, int id) {
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
 * 步骤 用来存储路径中的每一步 node：该步骤中选择的节点 demand：该步骤提供了需求 value:该步骤单价 cost：该步骤成本
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
 * 图 用来构造图 V：节点数量（普通节点+消费节点） num_nodes：普通节点的数量 server_cost：服务器成本 E:边的数量
 * num_client:服务器数量 vertexs：图中所有的节点 Edges:图中所有的边 clients:图中服务器的集合
 *
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
     * 初始化图 给定图中，节点数量，边数量，普通节点数量，服务器数量，服务器成本
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

    // 返回服务器成本
    public int getServerCost() {
        return server_cost;
    }

    /*
     * 为给定节点添加边 node_client:用来判断给节点是否为消费节点 True:普通节点 False:消费节点
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

    // 返回图中所有节点
    public Vertex<Integer>[] getVertexs() {
        return vertexs;
    }

    // 返回图中所有消费节点
    public LinkedList<Vertex<Integer>> getClients() {
        return clients;
    }

    // 返回给定节点的度
    public int getNodeDegree(int node) {
        return vertexs[node].getDegree();
    }

    // 返回给定边上的权重（带宽）
    public int getEdgeWeight(int self, int other) {
        return vertexs[self].getWeight(other);
    }

    // 获得给定边上的单价
    public double getEdgeValue(int self, int other) {
        return vertexs[self].getValue(other);
    }

    // 获得给定节点的，邻居节点
    public LinkedList<Vertex<Integer>> getNodeNeighbors(int node) {
        return vertexs[node].getNeighbors();
    }

    public LinkedList<Vertex<Integer>> getNodeNeighbors(Vertex<Integer> node) {
        return getNodeNeighbors(node.id);
    }

    // 获得所有节点的数量
    public int getNumberOfVertexes() {
        return V;
    }

    // 获得边的数量
    public int getNumberOfEdges() {
        return E;
    }

    // 给定节点的id，获得该节点类
    public Vertex<Integer> getNode(int id) {
        return vertexs[id];
    }

    // 获得普通节点的数量
    public int getNumberOfNodes() {
        return this.num_nodes;
    }

    // 获得消费节点的数量
    public int getNumberOfClient() {
        return this.num_client;
    }
}

/*
 * 染色体（解） boolean[] gen:存储服务器组合方式 例： gen[1] = {true,false,false,true,false}
 * 则，候选一号和四号节点为服务器，其他为普通节点。
 *
 * fitness_score:该解得适应度值 size:gen的大小。其与candidate（候选集合）集合大小保持一致
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

    public Chromosome(int size, double[] performance, LinkedList<Vertex<Integer>> clients,
                      ArrayList<Vertex<Integer>> candidates, float rate) {

        if (size <= 0) {
            return;
        }

        initGeneSize(size);
        int len = (int) (size * 0.6);
        int idx;
        Random random = new Random();
        for (Vertex<Integer> client : clients) {
            idx = client.getNeighbors().getFirst().id;
            if (performance[idx] > Math.random()) {
                gene[idx] = true;
            } else {
                gene[idx] = false;
                idx = candidates.get(random.nextInt(len)).id;
                gene[idx] = performance[idx] > Math.random();
            }
        }
    }

    public Chromosome(int size, double[] performance, LinkedList<Vertex<Integer>> clients,
                      ArrayList<Vertex<Integer>> candidates, float rate, Chromosome sample,
                      HashSet<Integer> certain_servers) {
        if (size <= 0) {
            return;
        }
        initGeneSize(size);
        Random random = new Random();
        int len = (int) (candidates.size() * rate);
        int idx;
        for (int i = 0; i < sample.gene.length; i++) {
            if (sample.gene[i]) {
                this.gene[i] = true;
            }
            for(int t = 0; t < 5; t++){
                idx = random.nextInt((int)(candidates.size()*0.2));
                if(!certain_servers.contains(candidates.get(idx).id))
                    this.gene[candidates.get(idx).id] = performance[candidates.get(idx).id] > Math.random();
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

    // 对染色体克隆
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
 * 优化寻找服务器
 */
class Optimization {

    /*
     * 种群基本信息配置 best_individual:最优个体 global_best_fitness：最有适应度值 gen_size：染色体大小
     * pop_size:种群大小 max_gen:最大迭代次数 mutation_rate:变异概率 candidates：候选种子集
     * population:种群
     */
    private HashSet<Integer> certain_servers = new HashSet<Integer>();
    private Chromosome sample_individual;
    static String[] graphContent;
    private Chromosome best_individual;
    private int global_best_fitness = Integer.MAX_VALUE;
    private int gen_size;
    private int pop_size;
    private final int max_gen = 100000;
    private int mate_size;
    private final double mutation_rate = 1.0;
    static private ArrayList<Vertex<Integer>> candidates;
    private ArrayList<Chromosome> population;
    private int end_condition;
    /*
     * performance_init：性价比 outputs_init:每个节点的输出带宽 weights_init：两个边上的带宽
     * clients_init:消费节点
     *
     */


    static private double max_performance = 0;
    static private double min_performance = Integer.MAX_VALUE;
    static private double[] normalization_performance;

    static private int total_visited_num = 0;
    static private int[] visited_num;
    static double[] visited_p;
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
    public Optimization(Graph G, long start_time, long deadline, String[] graphContent) {
        this.deadline = deadline;
        this.start_time = start_time;
        this.G = G;
        this.graphContent = graphContent;
    }

    // 测试
    public void test(ArrayList<Vertex<Integer>> candidates) {
        int min_cost = Integer.MAX_VALUE;
        int tmp_cost = 0;
        this.candidates = candidates;
        this.gen_size = G.getNumberOfNodes();
        visited_num = new int[G.getNumberOfNodes()];
        visited_p = new double[G.getNumberOfNodes()];
        best_individual = null;
        normalization_performance = new double[G.getNumberOfNodes()];
        performance_init = new double[G.getNumberOfVertexes()];
        outputs_init = new int[G.getNumberOfVertexes()];
        weights_init = new int[G.getNumberOfVertexes()][G.getNumberOfVertexes()];
        clients_init = G.getClients();

        // 对消费者排序
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
        for (int i = 0; i < G.getNumberOfVertexes(); i++) {
            Vertex<Integer> node = G.getNode(i);
            performance_init[i] = node.getPerformance();
            outputs_init[i] = node.getOutputs();
            LinkedList<Vertex<Integer>> neighbors = node.getNeighbors();
            for (Vertex<Integer> neighbor : neighbors) {
                if (neighbor.isNode())
                    weights_init[i][neighbor.id] = node.getWeight(neighbor);
                if (node.isClient())
                    weights_init[node.id][neighbor.id] = node.getDemand();
            }
            if (node.isNode()) {
                node.sortNeighbors();
                if (performance_init[i] > max_performance) {
                    max_performance = performance_init[i];
                } else if (performance_init[i] < min_performance) {
                    min_performance = performance_init[i];
                }
            }
        }
        // 归一化
        for (int i = 0; i < normalization_performance.length; i++) {
            normalization_performance[i] = (performance_init[i] - min_performance)
                    / (max_performance - min_performance);
        }

        //
        sample_individual = new Chromosome();
        sample_individual.initGeneSize(gen_size);
        /*
        int idx;
        LinkedList<Vertex<Integer>> set = new LinkedList<Vertex<Integer>>();
        for (Vertex<Integer> client : clients_init) {

            idx = client.getNeighbors().getFirst().id;

            sample_individual.gene[idx] = true;
            set.add(client.getNeighbors().getFirst());

        }
        // 此处基于performance与priotiry相同
        Collections.sort(set, new Comparator<Vertex<Integer>>() {
            public int compare(Vertex<Integer> o1, Vertex<Integer> o2) {
                if (o1.getPerformance() > o2.getPerformance())
                    return 1;
                else if (o1.getPerformance() < o2.getPerformance())
                    return -1;
                return 0;
            }
        });
        for (int i = 0; i < set.size(); i++) {
            sample_individual.gene[set.get(i).id] = false;
            tmp_cost = getCostBk(sample_individual);
            if (tmp_cost < min_cost) {
                min_cost = tmp_cost;
                certain_servers.add(set.get(i).id);
            } else {
                sample_individual.gene[set.get(i).id] = true;
            }
        }

        for(int i=0; i<sample_individual.gene.length;i++){
            if(sample_individual.gene[i]){
                sample_individual.gene[i] = false;
                tmp_cost = getCostBk(sample_individual);
                if(tmp_cost < min_cost){
                    min_cost = tmp_cost;
                }else {
                    sample_individual.gene[i] = true;
                }
            }
        }
        */
        greedy(sample_individual);

    }
    public void greedy(Chromosome individual){
        Chromosome tmp_individual = new Chromosome();
        tmp_individual.initGeneSize(G.getNumberOfNodes());
        ArrayList<Integer> servers =new ArrayList<Integer>();
        Result base = ZKW.methodOfzkw2(graphContent,servers);
        int demand = base.demand;
        int support = base.support;
        int last_support = 0;
        int max_support = 0;
        int selected_idx = -1;
        float max_performance;
        /*
        while (true){
            selected_idx = -1;
            min_cost = Integer.MAX_VALUE;
            for(int i =0; i < individual.gene.length; i++){
                int idx = i;
                if(individual.gene[idx] && !servers.contains(idx)){
                    servers.add(idx);
                    Result tmp_result = ZKW.methodOfzkw2(graphContent,servers);
                    if(tmp_result.support > max_support){
                        if(tmp_result.cost < min_cost){
                            max_support = tmp_result.support;
                            selected_idx = idx;
                            min_cost = tmp_result.cost;
                        }
                    }
                    servers.remove((Integer)idx);
                }
            }
            support = max_support;
            if(selected_idx==-1)
                break;
            tmp_individual.gene[selected_idx] = true;
            servers.add(selected_idx);
            System.out.println(servers.size());
            System.out.println(support+":"+demand);
        }*/

        while (support != demand) {
            selected_idx = -1;
            max_performance = (float)0.0;
            for (int i = 0; i < candidates.size() * 1.0; i++) {
                int idx = candidates.get(i).id;
                if (!servers.contains(idx)) {
                    servers.add(idx);
                    Result tmp_result = ZKW.methodOfzkw2(graphContent, servers);
                    if (tmp_result.support > last_support && (float)((float)tmp_result.support/tmp_result.cost) > max_performance) {
                        max_support = tmp_result.support;
                        selected_idx = idx;
                        max_performance = (float)((float)tmp_result.support/tmp_result.cost);
                        support = tmp_result.support;
                    }
                    servers.remove((Integer) idx);
                }
            }
            if (selected_idx == -1)
                break;

            tmp_individual.gene[selected_idx] = true;
            last_support = max_support;
            servers.add(selected_idx);
            System.out.println(servers.size());
            System.out.println(servers);
            System.out.println(support + ":" + demand);
        }
        //getResult(tmp_individual);
        sample_individual = tmp_individual;
        getResult(tmp_individual);
    }



    public void setup(ArrayList<Vertex<Integer>> candidates) {
        int min_cost = Integer.MAX_VALUE;
        int tmp_cost = 0;
        this.candidates = candidates;
        this.gen_size = G.getNumberOfNodes();
        visited_num = new int[G.getNumberOfNodes()];
        visited_p = new double[G.getNumberOfNodes()];
        best_individual = null;
        normalization_performance = new double[G.getNumberOfNodes()];
        performance_init = new double[G.getNumberOfVertexes()];
        outputs_init = new int[G.getNumberOfVertexes()];
        weights_init = new int[G.getNumberOfVertexes()][G.getNumberOfVertexes()];
        clients_init = G.getClients();

        // 对消费者排序
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
        for (int i = 0; i < G.getNumberOfVertexes(); i++) {
            Vertex<Integer> node = G.getNode(i);
            performance_init[i] = node.getPerformance();
            outputs_init[i] = node.getOutputs();
            LinkedList<Vertex<Integer>> neighbors = node.getNeighbors();
            for (Vertex<Integer> neighbor : neighbors) {
                if (neighbor.isNode())
                    weights_init[i][neighbor.id] = node.getWeight(neighbor);
                if (node.isClient())
                    weights_init[node.id][neighbor.id] = node.getDemand();
            }
            if (node.isNode()) {
                node.sortNeighbors();
                if (performance_init[i] > max_performance) {
                    max_performance = performance_init[i];
                } else if (performance_init[i] < min_performance) {
                    min_performance = performance_init[i];
                }
            }
        }
        // 归一化
        for (int i = 0; i < normalization_performance.length; i++) {
            normalization_performance[i] = (performance_init[i] - min_performance)
                    / (max_performance - min_performance);
        }

        //
        sample_individual = new Chromosome();
        sample_individual.initGeneSize(gen_size);

        int idx;
        // int[] v=new int[800];
        LinkedList<Vertex<Integer>> set = new LinkedList<Vertex<Integer>>();
        for (Vertex<Integer> client : clients_init) {

            idx = client.getNeighbors().getFirst().id;

            sample_individual.gene[idx] = true;
            set.add(client.getNeighbors().getFirst());

        }
        // 此处基于performance与priotiry相同
        Collections.sort(set, new Comparator<Vertex<Integer>>() {
            public int compare(Vertex<Integer> o1, Vertex<Integer> o2) {
                if (o1.getPerformance() > o2.getPerformance())
                    return 1;
                else if (o1.getPerformance() < o2.getPerformance())
                    return -1;
                return 0;
            }
        });
        for (int i = 0; i < set.size(); i++) {
            sample_individual.gene[set.get(i).id] = false;
            tmp_cost = getCostBk(sample_individual);
            if (tmp_cost < min_cost) {
                min_cost = tmp_cost;
                certain_servers.add(set.get(i).id);
            } else {
                sample_individual.gene[set.get(i).id] = true;
            }
        }

        for(int i=0; i<sample_individual.gene.length;i++){
            if(sample_individual.gene[i]){
                sample_individual.gene[i] = false;
                tmp_cost = getCostBk(sample_individual);
                if(tmp_cost < min_cost){
                    System.out.println(min_cost);
                    min_cost = tmp_cost;
                }else {
                    sample_individual.gene[i] = true;
                }
            }
        }
    }

    // 种群初始化
    public void init(int par) {
        long end_time;
        int min_cost = Integer.MAX_VALUE;
        int tmp_cost = 0;
        best_individual = null;

        // 初始化种群
        population = new ArrayList<Chromosome>();
        int idx = 0;
        min_cost = Integer.MAX_VALUE;
        tmp_cost = 0;
        if (par == 1) {
            pop_size = 20;
            mate_size = (int) (pop_size * 0.5);
            end_condition = 1000000;
        } else if (par == 0) {
            pop_size = 50;
            mate_size = (int) (pop_size * 0.5);
            end_condition = 10000000;
        } else {
            pop_size = 100;
            mate_size = (int) (pop_size * 0.5);
            end_condition = 10000000;
        }
        for (int i = 0; i < pop_size; i++) {
            Chromosome individual = null;
            if (i < (int) (pop_size * 0.5)) {
                individual = new Chromosome();
                individual.initGeneSize(gen_size);
                for (int j = 0; j < sample_individual.gene.length; j++) {
                    individual.gene[j] = sample_individual.gene[j];
                }
            }
            else{
                individual = new Chromosome(gen_size,
                normalization_performance,
                clients_init,
                candidates,
                (float) 0.6,
                sample_individual, certain_servers);
            }
            tmp_cost = getCostBk(individual);
            // System.out.println(tmp_cost);
            individual.fitness_score = tmp_cost;
            population.add(individual);
            if (min_cost > tmp_cost) {
                min_cost = tmp_cost;
                best_individual = Chromosome.clone(individual);
                best_individual.fitness_score = min_cost;
                global_best_fitness = min_cost;
            }
        }
    }

    // 选择
    public ArrayList<Chromosome> selection() {
        Collections.sort(population, new Comparator<Chromosome>() {
            public int compare(Chromosome o1, Chromosome o2) {
                if(o1.fitness_score > o2.fitness_score)
                    return 1;
                else if(o1.fitness_score < o2.fitness_score)
                    return -1;
                return 0;
            }
        });
        ArrayList<Chromosome> next_population = new ArrayList<Chromosome>();
        next_population.add(best_individual);
        for(int i =0; i <pop_size*0.1; i++){
            next_population.add(Chromosome.clone(population.get(i)));
        }
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

    // 变异
    public void mutate(ArrayList<Chromosome> next_population) {
        Chromosome individual;
        Random random = new Random();
        Vertex<Integer> node;
        int idx1,idx2;
        while(next_population.size()!=pop_size){
            individual = Chromosome.clone(next_population.get(random.nextInt(mate_size)));
            idx1 = candidates.get(random.nextInt((int)(G.getNumberOfNodes()*0.4))).id;
            idx2 = candidates.get(random.nextInt((int)(G.getNumberOfNodes()*0.4))).id;
            individual.gene[idx1] = !individual.gene[idx1];
            individual.gene[idx2] = !individual.gene[idx2];
            next_population.add(individual);
        }
    }

    // 局部搜索
    public void localSearch() {
        Chromosome tmp = Chromosome.clone(best_individual);

        int min_cost = global_best_fitness;
        int tmp_cost = 0;
        for(int i =0; i < tmp.gene.length; i++){
            tmp.gene[i] = false;
            tmp_cost = getCostBk(tmp);
            if(tmp_cost < min_cost){
                System.out.println("+++++");
                min_cost = getCost(tmp);
                best_individual = Chromosome.clone(tmp);
                best_individual.fitness_score = min_cost;
                global_best_fitness = min_cost;
            }else {
                tmp.gene[i] = true;
            }
        }

    }
    // 获得下一代
    public ArrayList<Chromosome> genNextPopulation() {
        ArrayList<Chromosome> next_population = selection();
        mutate(next_population);
        //localSearch();
        return next_population;
    }

    // 训练
    public Chromosome train() {
        LinkedList<Integer> test;
        int count = 0;
        int last_min_cost = 0;
        int min_cost = Integer.MAX_VALUE;
        int tmp_cost;
        for (int t = 0; t < max_gen; t++) {
            tmp_cost = 0;
            population = genNextPopulation();

            for (Chromosome individual : population) {
                tmp_cost = getCost(individual);
                individual.fitness_score = tmp_cost;
                if (min_cost > tmp_cost) {
                    min_cost = tmp_cost;
                    best_individual = Chromosome.clone(individual);
                    best_individual.fitness_score = min_cost;
                    global_best_fitness = min_cost;
                    localSearch();
                }
            }
            // 判断是否收敛
            if (last_min_cost == global_best_fitness) {
                count += 1;
                if (count == end_condition) {
                    return best_individual;
                }
            } else {
                count = 0;
            }
            last_min_cost = min_cost;
            Long end_time = System.currentTimeMillis();
            // 判断是否超时
            if (end_time - start_time > deadline) {
                return best_individual;
            }
            System.out.println(t + "   " + global_best_fitness);
        }

        return best_individual;
    }

    // 获得结果
    public static String[] getResult(Chromosome individual) {
        int idx = 0;

        String tmp;
        boolean[] gen = individual.getGene();
        ArrayList<Integer> server_set = new ArrayList<Integer>();
        for (int i = 0; i < gen.length; i++) {
            if (gen[i])
                server_set.add(G.getNode(i).id);///// 获取最优服务器位置
        }
        //String[] result = ZKW.getWalk(graphContent, ZKW.methodOfzkw(graphContent, server_set), weights_init,
                //clients_init);
        String[] result = ZKW.getWalk(graphContent, server_set, weights_init, clients_init);
        return result;
    }

    // 目标函数，获得每个解得成本
    public int getCost(Chromosome individual) {
        int result = 0;
        boolean[] gen = individual.getGene();
        // server_set：服务器集合
        HashSet<Vertex<Integer>> server_set = new HashSet<Vertex<Integer>>();
        HashSet<Integer> sets = new HashSet<Integer>();
        for (int idx = 0; idx < gen.length; idx++) {
            if (gen[idx])
                server_set.add(G.getNode(idx));
        }

        // 寻找该解下的所有路径

        LinkedList<LinkedList<Step>> walks = findWalks(G, server_set, clients_init, performance_init, outputs_init,
                weights_init, G.getNumberOfVertexes());

        individual.gene = new boolean[individual.size];


        // 计算成本

        for (LinkedList<Step> walk : walks) {
            for (Step step : walk) {
                if (server_set.contains(step.node)) {
                    sets.add(step.node.id);
                    result += step.cost;
                    break;
                }
            }
        }
        for (Integer i : sets) {
            individual.gene[i] = true;
        }
        result += G.getServerCost() * sets.size();

        return result;
    }

    public int getCostBk(Chromosome individual) {
        int result = 0;
        boolean[] gen = individual.getGene();
        // server_set：服务器集合
        HashSet<Vertex<Integer>> server_set = new HashSet<Vertex<Integer>>();
        HashSet<Integer> sets = new HashSet<Integer>();
        for (int idx = 0; idx < gen.length; idx++) {
            if (gen[idx])
                server_set.add(G.getNode(idx));
        }
        // 寻找该解下的所有路径
        LinkedList<LinkedList<Step>> walks = findWalks(G, server_set, clients_init, performance_init, outputs_init,
                weights_init, G.getNumberOfVertexes());
        ArrayList<Integer> servers=new ArrayList<Integer>();
        for(Vertex<Integer> i:server_set){
            servers.add(i.id);
        }
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

    // 寻找所有路径
    public static LinkedList<LinkedList<Step>> findWalks(Graph G, HashSet<Vertex<Integer>> server_set,
                                                         LinkedList<Vertex<Integer>> clients, double[] performance, int[] outputs, int[][] weights, int len) {

        LinkedList<LinkedList<Step>> walks = new LinkedList<LinkedList<Step>>();
        // outputs_neighbor_server，该节点的邻居节点有服务器，计算其与服务器的带宽和
        // neighbor_has_server：判断该节点的邻居节点是否有服务器
        boolean[] neighbor_has_server = new boolean[len];
        int[] outputs_neighbor_server = new int[len];
        // 克隆信息
        double[] performance_tmp = performance;
        int[] outputs_tmp = outputs.clone();
        int[][] weights_tmp = new int[len][];
        for (int i = 0; i < len; i++) {
            weights_tmp[i] = weights[i].clone();
        }
        // 根据服务器，更新网络中各个节点的性价比
        for (Vertex<Integer> server : server_set) {
            for (Vertex<Integer> neighbor : server.getNeighbors()) {
                if (neighbor.isNode() && !server_set.contains(neighbor)) {
                    neighbor_has_server[neighbor.id] = true;
                    outputs_neighbor_server[neighbor.id] += neighbor.getWeight(server);
                }
            }
        }
        // 为每一个服务器，寻找路径
        for (Vertex<Integer> client : clients) {
            int demand = client.getDemand();
            int remain_demand = demand;
            // 确保满足每一个服务器的需求带宽
            while (remain_demand != 0) {
                LinkedList<Step> walk = find(client, client.getDemand(), server_set, neighbor_has_server,
                        performance_tmp, weights_tmp, outputs_neighbor_server, outputs_tmp);

                walks.addLast(walk);
                remain_demand -= walk.getLast().demand;
            }
        }

        return walks;
    }

    // 给定服务器和需要承担的带宽，寻找路径
    public static LinkedList<Step> find(Vertex<Integer> client, int demand, final HashSet<Vertex<Integer>> server_set,
                                        final boolean[] neighbor_has_server, final double[] performance, int[][] weights,
                                        int[] outputs_neighbor_server, int[] outputs) {

        HashSet<Vertex<Integer>> selected_nodes = new HashSet<Vertex<Integer>>();
        int cost = 0;
        LinkedList<Step> walk = new LinkedList<Step>();
        walk.addLast(new Step(client, demand, 0));

        // 用于加快排序的参数
        int idx_0 = 0;
        int idx_1 = 0;
        boolean flag_sort = true;
        Vertex<Integer> tmp_node;
        // 用于控制路径寻找深度
        int count = 0;
        final int max_find_count = 6;

        boolean flag = true;
        while (flag) {
            Step current_step = walk.getLast();
            final Vertex<Integer> current_node = current_step.node;
            int current_demand = current_step.demand;
            // 生成新的邻居节点集
            LinkedList<Vertex<Integer>> raw_neighbors = current_node.getNeighbors();
            LinkedList<Vertex<Integer>> neighbors = new LinkedList<Vertex<Integer>>();

            for (Vertex<Integer> neighbor : raw_neighbors) {
                if (neighbor.isNode() && weights[current_node.id][neighbor.id] > 0
                        && !selected_nodes.contains(neighbor)) {

                    if (server_set.contains(neighbor)) {
                        for (int i = 0; i < neighbors.size(); i++) {
                            tmp_node = neighbors.get(i);
                            if (server_set.contains(tmp_node)) {
                                if (neighbor.getValue(current_node) < tmp_node.getValue(current_node)) {
                                    neighbors.add(i, neighbor);
                                    flag_sort = false;
                                    break;
                                }
                            } else {
                                neighbors.add(i, neighbor);
                                flag_sort = false;
                                break;
                            }
                        }
                        if (flag_sort)
                            neighbors.addLast(neighbor);
                        flag_sort = true;
                        idx_0 += 1;
                        idx_1 += 1;
                    } else if (neighbor_has_server[neighbor.id]) {
                        for (int i = idx_0; i < neighbors.size(); i++) {
                            tmp_node = neighbors.get(i);
                            if (server_set.contains(tmp_node)) {
                                ;
                            } else if (neighbor_has_server[tmp_node.id]) {
                                if (neighbor.getValue(current_node) < tmp_node.getValue(current_node)) {
                                    neighbors.add(i, neighbor);
                                    flag_sort = false;
                                    break;
                                }
                            } else {
                                neighbors.add(i, neighbor);
                                flag_sort = false;
                                break;
                            }
                        }
                        if (flag_sort)
                            neighbors.addLast(neighbor);
                        idx_1 += 1;
                        flag_sort = true;
                    } else {
                        for (int i = idx_1; i < neighbors.size(); i++) {
                            tmp_node = neighbors.get(i);
                            if (server_set.contains(tmp_node)) {
                                ;
                            } else if (neighbor_has_server[tmp_node.id]) {
                                ;
                            } else {
                                if (neighbor.getValue(current_node) < tmp_node.getValue(current_node)) {
                                    neighbors.add(i, neighbor);
                                    flag_sort = false;
                                    break;
                                }
                            }
                        }
                        if (flag_sort)
                            neighbors.addLast(neighbor);
                        flag_sort = true;
                    }
                }
            }
			/*
			 * System.out.println("开始"); System.out.println(server_set);
			 * System.out.println(selected_nodes);
			 * System.out.println(raw_neighbors);
			 * System.out.println(current_node); System.out.println("测试一");
			 * for(Vertex<Integer> neighbor : neighbors){
			 * System.out.println(neighbor+" "+neighbor.getPerformance()+"  "
			 * +neighbor.getValue(current_node)); }
			 */
            // 根据规则，排序
			/*
			 * neighbors = new LinkedList<Vertex<Integer>>(); for
			 * (Vertex<Integer> neighbor : raw_neighbors) { if
			 * (neighbor.isNode() && weights[current_node.id][neighbor.id] > 0
			 * && !selected_nodes.contains(neighbor)){
			 * neighbors.addLast(neighbor); } } Collections.sort(neighbors, new
			 * Comparator<Vertex<Integer>>() { public int
			 * compare(Vertex<Integer> o1, Vertex<Integer> o2) {
			 * if(server_set.contains(o1) && !server_set.contains(o2)) return
			 * -1; else if((!server_set.contains(o1) &&
			 * server_set.contains(o2))) return 1;
			 *
			 * if ((o1.isNode() && neighbor_has_server[o1.id] && o2.isNode() &&
			 * !neighbor_has_server[o2.id]) ) return -1; else if((o1.isNode() &&
			 * !neighbor_has_server[o1.id] && o2.isNode() &&
			 * neighbor_has_server[o2.id])) return 1;
			 *
			 * if(o1.getValue(current_node) > o2.getValue(current_node)) return
			 * 1; if(o1.getValue(current_node) < o2.getValue(current_node))
			 * return -1;
			 *
			 * if(performance[o1.id] > performance[o2.id]) return -1; else
			 * if(performance[o1.id] < performance[o2.id]) return 1; return 0; }
			 * });
			 */
			/*
			 * //System.out.println("测试二"); //for(Vertex<Integer> neighbor :
			 * neighbors){ //
			 * System.out.println(neighbor+" "+neighbor.getPerformance()+"  "
			 * +neighbor.getValue(current_node)); //}
			 */
            // 寻找下一步
            Step next_step = findNextStep(current_node, current_demand, neighbors, server_set, neighbor_has_server,
                    weights, outputs_neighbor_server, performance, outputs);
            // 判断你是否停止寻找节点，终止搜索。并且计算每一步的成本。
            // 在终止前，应该更新网络属性。例如，outputs，weights和perforamnce
            if (next_step != null) {
                walk.addLast(next_step);
                selected_nodes.add(next_step.node);
                count += 1;
                if (server_set.contains(next_step.node)) {
                    flag = false;
                    int size = walk.size();
                    Vertex<Integer> last = client;
                    for (int i = 1; i < size; i++) {
                        Step step = walk.get(i);
                        if (i > 1) {
                            cost += walk.getLast().demand * step.value;
                            step.cost = cost;
                        }
                        weights[last.id][step.node.id] -= walk.getLast().demand;
                        if (neighbor_has_server[step.node.id])
                            outputs_neighbor_server[step.node.id] -= walk.getLast().demand;
                        outputs[step.node.id] -= walk.getLast().demand;
                        last = step.node;
                    }
                } else if (count > max_find_count) {
                    flag = false;
                    int size = walk.size();
                    Vertex<Integer> last = client;
                    for (int i = 1; i < size; i++) {
                        Step step = walk.get(i);
                        if (i > 1) {
                            cost += walk.getLast().demand * step.value;
                            step.cost = cost;
                        }
                        weights[last.id][step.node.id] -= walk.getLast().demand;
                        if (neighbor_has_server[step.node.id])
                            outputs_neighbor_server[step.node.id] -= walk.getLast().demand;
                        outputs[step.node.id] -= walk.getLast().demand;
                        last = step.node;
                    }
                    // 更新，新增服务器
                    Vertex<Integer> new_server = walk.getLast().node;
                    server_set.add(new_server);
                    // 更新新增服务器的邻居节点的配置信息，如perforamnce，outputs_neighbor_server
                    for (Vertex<Integer> neighbor : new_server.getNeighbors()) {
                        if (neighbor.isNode() && !server_set.contains(neighbor)) {
                            neighbor_has_server[neighbor.id] = true;
                            outputs_neighbor_server[neighbor.id] += weights[neighbor.id][new_server.id];
                        }
                    }
                }
            } else {
                flag = false;
                int size = walk.size();
                Vertex<Integer> last = client;
                for (int i = 1; i < size; i++) {
                    Step step = walk.get(i);
                    if (i > 1) {
                        cost += walk.getLast().demand * step.value;
                        step.cost = cost;
                    }
                    weights[last.id][step.node.id] -= walk.getLast().demand;
                    if (neighbor_has_server[step.node.id])
                        outputs_neighbor_server[step.node.id] -= walk.getLast().demand;
                    outputs[step.node.id] -= walk.getLast().demand;
                    last = step.node;
                }
                // 更新，新增服务器
                Vertex<Integer> new_server = walk.getLast().node;
                server_set.add(new_server);
                // 更新新增服务器的邻居节点的配置信息，如perforamnce，outputs_neighbor_server
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

    // 寻找一条路径中的下一步骤
    public static Step findNextStep(Vertex<Integer> current_node, int current_demand,
                                    LinkedList<Vertex<Integer>> neighbors, final HashSet<Vertex<Integer>> server_set,
                                    boolean[] neighbor_has_server, int[][] weights, int[] outputs_neighbor_server, double[] performance,
                                    int[] outputs) {

        if (neighbors.isEmpty())
            return null;
        Vertex<Integer> neighbor = neighbors.removeFirst();

        if (server_set.contains(neighbor) && weights[current_node.id][neighbor.id] > 0) {
            int support = Math.min(current_demand, weights[current_node.id][neighbor.id]);
            return new Step(neighbor, support, current_node.getValue(neighbor));
        } else if (neighbor_has_server[neighbor.id] && outputs_neighbor_server[neighbor.id] > 0) {
            int server_support = Math.min(outputs_neighbor_server[neighbor.id], weights[current_node.id][neighbor.id]);
            int support = Math.min(current_demand, server_support);

            return new Step(neighbor, support, current_node.getValue(neighbor));
        } else if (performance[neighbor.id] > performance[current_node.id]
                && weights[current_node.id][neighbor.id] > 0) {
            int node_support = Math.min(outputs[neighbor.id], weights[current_node.id][neighbor.id]);
            int support = Math.min(current_demand, node_support);
            return new Step(neighbor, support, current_node.getValue(neighbor));
        }
        return findNextStep(current_node, current_demand, neighbors, server_set, neighbor_has_server, weights,
                outputs_neighbor_server, performance, outputs);
    }

    // 影响力最大化，选择候选集

    public ArrayList<Vertex<Integer>> influenceMax(int keyNum) {

        ArrayList<Vertex<Integer>> maxVertexs = new ArrayList<Vertex<Integer>>();

        TreeSet<Vertex> sortVertexsByPriority = new TreeSet<Vertex>();
        // 此处是原方法的基于度的节点的存储方式
        ArrayList<Integer> vertexsOfNotClient = new ArrayList<Integer>();

        Vertex<Integer>[] vertexs = this.G.getVertexs();
        LinkedList<Vertex<Integer>> clients = this.G.getClients();

        for (Vertex v : vertexs) {
            if (clients.contains(v)) {
                continue;
            }
            sortVertexsByPriority.add(v);
            vertexsOfNotClient.add(v.id);
        }

        for (int j = 0; j < keyNum; j++) {
            Vertex v = sortVertexsByPriority.pollFirst();
            maxVertexs.add(v);
            LinkedList<Vertex> neighbor = v.getNeighbors();
            int value = 0;
            for (Vertex vn : neighbor) {
                if (!maxVertexs.contains(vn) && !clients.contains(vn)) {
                    sortVertexsByPriority.remove(vn);
                    vn.setPriority(vn.getPriority(), v.id);
                    sortVertexsByPriority.add(vn);
                }
            }

        }

        return maxVertexs;
    }

    public ArrayList<Vertex<Integer>> influenceMax() {
        ArrayList<Vertex<Integer>> candidates = new ArrayList<Vertex<Integer>>();
        int[][] value_init = new int[G.getNumberOfVertexes()][G.getNumberOfVertexes()];
        int[] visited_num_tmp = new int[G.getNumberOfVertexes()];
        int[] values = new int[G.getNumberOfVertexes()];
        LinkedList<Vertex<Integer>> neighbors;
        LinkedList<Vertex<Integer>> current_neighbors;
        LinkedList<Vertex<Integer>> next_neighbors;

        Vertex<Integer> node;
        for (int i = 0; i < G.getNumberOfNodes(); i++) {
            node = G.getNode(i);
            neighbors = node.getNeighbors();
            for (Vertex<Integer> neighbor : neighbors) {
                value_init[node.id][neighbor.id] = (int) node.getValue(neighbor);
            }
        }
        //
        for (Vertex<Integer> client : G.getClients()) {
            for (int idx = 0; idx < values.length; idx++) {
                values[idx] = Integer.MAX_VALUE;
            }
            current_neighbors = new LinkedList<Vertex<Integer>>();
            values[client.id] = 0;
            current_neighbors.add(client);
            for (int i = 0; i < 3; i++) {
                next_neighbors = new LinkedList<Vertex<Integer>>();
                while (!current_neighbors.isEmpty()) {
                    node = current_neighbors.removeFirst();
                    neighbors = node.getNeighbors();
                    for (Vertex<Integer> neighbor : neighbors) {

                        visited_num_tmp[neighbor.id] += 1;
                        next_neighbors.add(neighbor);
                    }
                }
                current_neighbors = next_neighbors;
            }
        }
        int max = 0;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < G.getNumberOfNodes(); i++) {
            if (visited_num_tmp[i] != 0) {
                if (visited_num_tmp[i] > max) {
                    max = visited_num_tmp[i];
                }
                if (visited_num_tmp[i] < min) {
                    min = visited_num_tmp[i];
                }
            }
        }
        for (int i = 0; i < G.getNumberOfNodes(); i++) {
            G.getNode(i).visited_p = ((visited_num_tmp[i] - min) / (float) (max - min));
            candidates.add(G.getNode(i));

        }
        Collections.sort(candidates, new Comparator<Vertex<Integer>>() {
            public int compare(Vertex<Integer> o1, Vertex<Integer> o2) {
                if (o1.visited_p > o2.visited_p)
                    return -1;
                else if (o1.visited_p < o2.visited_p)
                    return 1;
                if (o1.getPerformance() > o2.getPerformance())
                    return -1;
                else if (o1.getPerformance() < o2.getPerformance())
                    return 1;
                return 0;
            }
        });

        return candidates;
    }
}

/**
 * 残留边类 主要方法：addFlow(int amount) 更新残留网络中边的流 。。。。
 *
 */
class resEdge {
    public final int start;// 边的起始节点
    public final int end;// 目标节点
    public final boolean isResidual;// 是否为残留边 false表示该边是原始边
    public resEdge reverse;// 反向边
    public int capacity;// 容量，即带宽
    public int unitcost;// 单位带宽费用
    public int flow;// 流经该边的流

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

    // 更新残留网络后，用于更新图中边的流的大小
    public void addFlow(int amount) {
        if (amount < 0) {
            reverse.addFlow(-amount);
            return;
        }
        capacity -= amount;
        flow += amount;
        reverse.capacity += amount;
    }

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
 * 残留网络 主要方法： findMaxFlow(int s, int t) 获取源s到汇点t的最大流
 * augmentPath(ArrayList<resEdge> path) 根据增广路径path来更新残留网络的属性值 shortestPaths(int
 * source, int t) 寻找最小费用路径
 */
class ResidualGraph {
    int numOfNode;// 普通网络节点的数量，不包括消费节点
    int numOfEdge;// 边的数量
    int costOfServer;// 服务器的费用
    int numOfClient;// 消费节点的费用
    int maxFlow;// 最大流
    int minCost;// 最小费用
    StringBuilder flowPath = new StringBuilder();// 记录所有增广路径上节点及该路径的带宽
    ArrayList<Integer> useServer = new ArrayList<Integer>();// 记录使用的服务器

    Map<Integer, ArrayList<resEdge>> graph = new HashMap<Integer, ArrayList<resEdge>>();// 残留图

    public void findMaxFlow(int s, int t) {
        if (s == t)
            return;
        //// 始终循环去找增广路径，直到路径为null退出循环
        while (true) {
            ArrayList<resEdge> pathEdge = new ArrayList<resEdge>();
            Deque<Integer> path = shortestPaths(s, t);
            if (path.isEmpty())
                break;

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
        // 循环前向数组来记录增广路径
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

        }

        return path;

    }

}

class EDGE1 {
    int cost, cap, v; // cost是边的费用，cap是边的容量，v是边终点的标号
    int next, re; // next是，re是反边终点的标号
    int current;
    boolean isPos;
}

class ZKW {

    final int MAXN = 2000;
    final int MAXM = 20000;
    final int INF = 10000000;

    static int DEMAND;

    EDGE1[] edge = new EDGE1[MAXM];
    int[] head = new int[MAXN]; // head表示的是从汇点出发到源点走过的路径，没走过的为-1，走过的为其他值
    int[] vis = new int[MAXN];
    int[] d = new int[MAXN]; // vis表示从源点出发到汇点走过的路径，走过的点标记为1，没走过的标记为0.
    int e;
    int ans, cost, src, des, n;
    int nt, m;
    int[][] dis = new int[MAXN][MAXN];
    char[][] s = new char[MAXN][MAXN];

    void init() {
        Arrays.fill(head, -1);
        e = 0;
        this.DEMAND = 0;
        ans = cost = 0;
    }

    void add(int u, int v, int cap, int cost) {
        edge[e].current = u;
        edge[e].v = v;
        edge[e].cap = cap;
        edge[e].cost = cost;
        edge[e].re = e + 1;
        edge[e].next = head[u];
        edge[e].isPos = true;
        head[u] = e++;
        edge[e].current = v;
        edge[e].v = u;
        edge[e].cap = 0;
        edge[e].cost = -cost;
        edge[e].re = e - 1;
        edge[e].next = head[v];
        head[v] = e++;
        edge[e].isPos = false;

    }

    // f表示的是容量
    int aug(int u, int f) {
        if (u == des) {
            ans += cost * f;
            DEMAND += f;
            return f;
        }
        vis[u] = 1; // vis表示从源点出发，走过的点标记为1，没走过的标记为0.
        int tmp = f;
        for (int i = head[u]; i != -1; i = edge[i].next) {
            if (edge[i].cap != 0 && edge[i].cost == 0 && vis[edge[i].v] == 0) {
                int delta = aug(edge[i].v, tmp < edge[i].cap ? tmp : edge[i].cap);
                edge[i].cap -= delta;
                edge[edge[i].re].cap += delta;
                tmp -= delta;
                if (tmp == 0)
                    return f;
            }
        }
        return f - tmp;
    }

    int augNew(int u, int f) {
        if (u == des) {
            ans += cost * f;
            DEMAND += f;
            return f;
        }
        vis[u] = 1; // vis表示从源点出发，走过的点标记为1，没走过的标记为0.
        int tmp = f;
        for (int i = head[u]; i != -1; i = edge[i].next) {
            if (edge[i].cap != 0 && edge[i].cost == 0 && vis[edge[i].v] == 0) {
                int delta = aug(edge[i].v, tmp < edge[i].cap ? tmp : edge[i].cap);
                edge[i].cap -= delta;
                edge[edge[i].re].cap += delta;
                tmp -= delta;
                if (tmp == 0)
                    return f;
            }
        }
        return f - tmp;
    }

    // 从汇点到源点的路径入队列的顺序
    boolean modlabel() {
        for (int i = 0; i <= n; i++)
            d[i] = INF; // d变量是Cij，即此刻的状态到下一刻状态的费用
        d[des] = 0;
        Deque<Integer> Q = new ArrayDeque<Integer>();
        Q.addLast(des);// (des);
        while (!Q.isEmpty()) {
            int u = Q.getFirst(), tmp;
            Q.pollFirst();

            for (int i = head[u]; i != -1; i = edge[i].next) {
                tmp = d[u] - edge[i].cost;
                if (((edge[edge[i].re].cap) != 0) && (tmp < d[edge[i].v]))// 判断是否满足ZKW算法的条件一
                {
                    d[edge[i].v] = tmp;
                    if ((d[edge[i].v]) <= d[Q.isEmpty() ? src : Q.getFirst()]) {
                        Q.addFirst(edge[i].v);
                    } else {
                        Q.addLast(edge[i].v);
                    }
                }
            }
        }
        // 对走过路径的费用清零
        for (int u = 1; u <= n; u++)
            for (int i = head[u]; i != -1; i = edge[i].next)
                edge[i].cost += d[edge[i].v] - d[u];
        cost += d[src];
        return d[src] < INF; // 如果为真，从汇点到源点找到一条可行路径
    }

    void costflow() {
        while (modlabel()) {
            do {
                Arrays.fill(vis, 0);
            } while (aug(src, INF) == 0);
        }
    }


    String[] findWalk(int[][] weight,
                      boolean[] servers,
                      LinkedList<Vertex<Integer>> clients,
                      int count,
                      int num) {

        int[][] current_weight = new int[weight.length][weight.length];
        int[][] source_weight = new int[weight.length][weight.length];

        int[] path = new int[weight.length];
        boolean[] visited = new boolean[weight.length];
        boolean[] isClient = new boolean[weight.length];
        LinkedList<Integer> stack = new LinkedList<Integer>();
        for (int i = 0; i < weight.length; i++) {
            source_weight[i] = weight[i].clone();
        }

        for (Vertex<Integer> client : clients){
            isClient[client.id] = true;
        }
        LinkedList<String> walks_set = new LinkedList<String>();

        while (modlabel()) {
            do {
                Arrays.fill(vis, 0);
            } while (augNew(src, INF) == 0);
        }

        for(int i = 0; i < count; i ++){
            current_weight[edge[i*2].current-1][edge[i*2].v-1] =
                    source_weight[edge[i*2].current-1][edge[i*2].v-1] - edge[i*2].cap;
        }
        int demand;
        int support;
        for(Vertex<Integer> client : clients){
            demand = client.getDemand();
            while (demand > 0){
                visited = new boolean[weight.length];
                path = new int[weight.length];
                dfs(client.id,
                        current_weight,
                        visited,
                        path,
                        demand,
                        0,
                        servers,
                        isClient,
                        walks_set,
                        num);
                String[] tmp = walks_set.getLast().split(" ");
                support = Integer.parseInt(tmp[tmp.length-1]);
                demand -= support;
            }
        }
        for(Vertex<Integer> client:clients){
            for(int i =0; i < weight.length; i++){
                if(current_weight[client.id][i]!=0)
                    System.out.println("error");
            }
        }
        String[] result = new String[walks_set.size()+2];
        result[0] = ""+walks_set.size();
        result[1] = "";
        for(int i = 0; i < walks_set.size(); i++){
            result[i+2] = walks_set.get(i);
        }
        return result;
    }
    public boolean dfs(int node, int[][] weights, boolean[] visited, int[] path, int demand, int support,
                       boolean[] servers,
                       boolean[] isClient,
                       LinkedList<String> walks_set,
                       int num){

        if(servers[node]){
            String tmp = node +" ";
            weights[path[node]][node] -= support;
            int u;
            for(u = path[node]; !isClient[u]; u = path[u]){
                tmp += u +" ";
                weights[path[u]][u] -= support;
            }
            tmp += (u-num+" "+support);
            walks_set.addLast(tmp);
//            System.out.println(tmp);
            return true;

        }else {
            visited[node] = true;
            for(int i = 0; i < weights.length; i ++){
                if(weights[node][i]!=0 && !visited[i]){
                    path[i] = node;
                    support = Math.min(demand,weights[node][i]);
                    if(dfs(i, weights, visited, path, support, support, servers, isClient, walks_set,num)){
                        visited[node] = false;
                        return true;
                    }
                }
            }
        }
        return true;
    }

    public static String[] getWalk(String[] graphContent, ArrayList<Integer> individual, int[][] weight,
                                   LinkedList<Vertex<Integer>> clients_init) {
        int count = 0;

        ZKW zkw = new ZKW();

        for (int i = 0; i < zkw.MAXM; i++) {
            EDGE1 e = new EDGE1();
            zkw.edge[i] = e;
        }

        zkw.init();

        // 添加边

        String[] nums = graphContent[0].split(" ");
        count = Integer.parseInt(nums[2]) + Integer.parseInt(nums[1]) * 2;
        int num = Integer.parseInt(nums[0]);
        int costOfserver = Integer.parseInt(graphContent[2]);
        zkw.n = Integer.parseInt(nums[2]) + Integer.parseInt(nums[0]) + 2;
        zkw.src = Integer.parseInt(nums[2]) + Integer.parseInt(nums[0]) + 1;
        zkw.des = zkw.n;

        for (int i = 4; i < Integer.parseInt(nums[1]) + 4; i++) {

            String[] data = graphContent[i].split(" ");

            zkw.add(Integer.parseInt(data[0]) + 1, Integer.parseInt(data[1]) + 1, Integer.parseInt(data[2]),
                    Integer.parseInt(data[3]));
            zkw.add(Integer.parseInt(data[1]) + 1, Integer.parseInt(data[0]) + 1, Integer.parseInt(data[2]),
                    Integer.parseInt(data[3]));
        }
        for (int i = 0; i < Integer.parseInt(nums[2]); i++) {

            String[] data = graphContent[Integer.parseInt(nums[1]) + 5 + i].split(" ");

            zkw.add(Integer.parseInt(data[0]) + Integer.parseInt(nums[0]) + 1, Integer.parseInt(data[1]) + 1,
                    Integer.parseInt(data[2]), 0);

        }
        for (int i = 0; i < Integer.parseInt(nums[2]); i++) {

            String[] data = graphContent[Integer.parseInt(nums[1]) + 5 + i].split(" ");

            zkw.add(zkw.src, Integer.parseInt(data[0]) + Integer.parseInt(nums[0]) + 1, Integer.parseInt(data[2]), 0);

        }

        boolean[] servers = new boolean[weight.length];
        for (Integer i : individual) {
            servers[i] = true;
            zkw.add(i + 1, zkw.des, zkw.INF, 0);
        }

        String[] content = zkw.findWalk(weight, servers, clients_init, count, num);
        ArrayList<Integer> useOfServers = new ArrayList<Integer>();
        for (int i : individual) {
            if (zkw.edge[zkw.head[i+1]].cap != zkw.INF) {
                useOfServers.add(i);
            }
        }

        System.out.println(zkw.ans + costOfserver * (useOfServers.size()));
        System.out.println(useOfServers);
        zkw.DEMAND = 0;
        return content;

    }

    public static int methodOfzkw1(String[] graphContent, ArrayList<Integer> servers) {
        ZKW zkw = new ZKW();
        // 初始化
        for (int i = 0; i < zkw.MAXM; i++) {
            EDGE1 e = new EDGE1();
            zkw.edge[i] = e;
        }
        zkw.init();
        Arrays.fill(zkw.head, -1);
        zkw.e = 0;
        zkw.ans = zkw.cost = 0;

        // Arrays.fill(zkw.rlx, 0);

        // 添加边
        int demands = 0;
        String[] nums = graphContent[0].split(" ");
        int costOfserver = Integer.parseInt(graphContent[2]);
        zkw.n = Integer.parseInt(nums[2]) + Integer.parseInt(nums[0]) + 2;
        zkw.src = Integer.parseInt(nums[2]) + Integer.parseInt(nums[0]) + 1;

        zkw.des = zkw.n;

        for (int i = 4; i < Integer.parseInt(nums[1]) + 4; i++) {
            String[] data = graphContent[i].split(" ");

            zkw.add(Integer.parseInt(data[0]) + 1, Integer.parseInt(data[1]) + 1, Integer.parseInt(data[2]),
                    Integer.parseInt(data[3]));
            zkw.add(Integer.parseInt(data[1]) + 1, Integer.parseInt(data[0]) + 1, Integer.parseInt(data[2]),
                    Integer.parseInt(data[3]));
        }
        for (int i = 0; i < Integer.parseInt(nums[2]); i++) {

            String[] data = graphContent[Integer.parseInt(nums[1]) + 5 + i].split(" ");

            zkw.add(Integer.parseInt(data[0]) + Integer.parseInt(nums[0]) + 1, Integer.parseInt(data[1]) + 1,
                    Integer.parseInt(data[2]), 0);
            demands += Integer.parseInt(data[2]);
            zkw.add(zkw.src, Integer.parseInt(data[0]) + Integer.parseInt(nums[0]) + 1, Integer.parseInt(data[2]), 0);

        }

        for (int i : servers) {
            zkw.add(i + 1, zkw.des, zkw.INF, 0);
        }

        zkw.costflow();
        ArrayList<Integer> useOfServers = new ArrayList<Integer>();
        ArrayList<Integer> tmp = (ArrayList<Integer>) servers.clone();
        for (Integer i : tmp) {
            if (zkw.edge[zkw.head[i + 1]].cap != zkw.INF) {
                useOfServers.add(i);
            }else {
                servers.remove(i);
            }
        }

        int cost;

        if (demands != zkw.DEMAND) {
            cost = -1;
        } else {
            cost = zkw.ans + costOfserver * (useOfServers.size());
        }
        zkw.DEMAND = 0;
        return cost;
    }

    public static ArrayList<Integer> methodOfzkw(String[] graphContent, ArrayList<Integer> servers) {

        int min = methodOfzkw1(graphContent, servers);
        int s = -1;
        for (int i = 0; i < servers.size(); i++) {
            int temp = servers.get(i);
            servers.remove(i);
            // long start = System.currentTimeMillis();

            int t = methodOfzkw1(graphContent, servers);
            if (t == -1) {
                servers.add(i, temp);
                continue;
            }
            // min=Math.min(t, min);

            if (t < min) {
                min = t;
                s = i;

            }
            servers.add(i, temp);
        }

        if (s != -1) {
            servers.remove(s);
        }
        return servers;

    }

    public static int methodOfzkwNew(String[] graphContent,ArrayList<Integer> servers) {

        ZKW zkw = new ZKW();

        // 初始化
        for (int i = 0; i < zkw.MAXM; i++) {
            EDGE1 e = new EDGE1();
            zkw.edge[i] = e;
        }
        zkw.init();
        Arrays.fill(zkw.head, -1);
        zkw.e = 0;
        zkw.ans = zkw.cost = 0;


        // 添加边
        int demands=0;
        String[] nums = graphContent[0].split(" ");
        int costOfserver=Integer.parseInt(graphContent[2]);
        zkw.n = Integer.parseInt(nums[2]) + Integer.parseInt(nums[0]) + 2;
        zkw.src = Integer.parseInt(nums[2]) + Integer.parseInt(nums[0]) + 1;

        zkw.des = zkw.n;

        for (int i = 4; i < Integer.parseInt(nums[1]) + 4; i++) {
            String[] data = graphContent[i].split(" ");

            zkw.add(Integer.parseInt(data[0]) + 1, Integer.parseInt(data[1]) + 1, Integer.parseInt(data[2]),
                    Integer.parseInt(data[3]));
            zkw.add(Integer.parseInt(data[1]) + 1, Integer.parseInt(data[0]) + 1, Integer.parseInt(data[2]),
                    Integer.parseInt(data[3]));
        }
        for (int i = 0; i < Integer.parseInt(nums[2]); i++) {

            String[] data = graphContent[Integer.parseInt(nums[1]) + 5 + i].split(" ");

            zkw.add(Integer.parseInt(data[0]) + Integer.parseInt(nums[0]) + 1, Integer.parseInt(data[1]) + 1,
                    Integer.parseInt(data[2]), 0);
            demands+=Integer.parseInt(data[2]);
            zkw.add(zkw.src, Integer.parseInt(data[0]) + Integer.parseInt(nums[0]) + 1, Integer.parseInt(data[2]), 0);

        }

        for(int i:servers){
            zkw.add(i+1,zkw.des, zkw.INF, 0);
        }

        zkw.costflow();
        ArrayList<Integer> useOfServers=new ArrayList<Integer>();
        for(int i:servers){
            if(zkw.edge[zkw.head[i+1]].cap!=zkw.INF){;
                useOfServers.add(i);
            }
        }
        int cost;
        if(demands != zkw.DEMAND){
            cost = -1;
        }else {
            cost = zkw.ans+costOfserver*(useOfServers.size());
        }
        zkw.DEMAND = 0;
        return cost;
    }


    public static Result methodOfzkw2(String[] graphContent, ArrayList<Integer> servers) {
        ZKW zkw = new ZKW();
        // 初始化
        for (int i = 0; i < zkw.MAXM; i++) {
            EDGE1 e = new EDGE1();
            zkw.edge[i] = e;
        }
        zkw.init();
        Arrays.fill(zkw.head, -1);
        zkw.e = 0;
        zkw.ans = zkw.cost = 0;

        // Arrays.fill(zkw.rlx, 0);

        // 添加边
        int demands = 0;
        String[] nums = graphContent[0].split(" ");
        int costOfserver = Integer.parseInt(graphContent[2]);
        zkw.n = Integer.parseInt(nums[2]) + Integer.parseInt(nums[0]) + 2;
        zkw.src = Integer.parseInt(nums[2]) + Integer.parseInt(nums[0]) + 1;

        zkw.des = zkw.n;

        for (int i = 4; i < Integer.parseInt(nums[1]) + 4; i++) {
            String[] data = graphContent[i].split(" ");

            zkw.add(Integer.parseInt(data[0]) + 1, Integer.parseInt(data[1]) + 1, Integer.parseInt(data[2]),
                    Integer.parseInt(data[3]));
            zkw.add(Integer.parseInt(data[1]) + 1, Integer.parseInt(data[0]) + 1, Integer.parseInt(data[2]),
                    Integer.parseInt(data[3]));
        }
        for (int i = 0; i < Integer.parseInt(nums[2]); i++) {

            String[] data = graphContent[Integer.parseInt(nums[1]) + 5 + i].split(" ");

            zkw.add(Integer.parseInt(data[0]) + Integer.parseInt(nums[0]) + 1, Integer.parseInt(data[1]) + 1,
                    Integer.parseInt(data[2]), 0);
            demands += Integer.parseInt(data[2]);
            zkw.add(zkw.src, Integer.parseInt(data[0]) + Integer.parseInt(nums[0]) + 1, Integer.parseInt(data[2]), 0);

        }

        for (int i : servers) {
            zkw.add(i + 1, zkw.des, zkw.INF, 0);
        }

        zkw.costflow();
        ArrayList<Integer> useOfServers = new ArrayList<Integer>();
        ArrayList<Integer> tmp = (ArrayList<Integer>) servers.clone();
        for (Integer i : tmp) {
            if (zkw.edge[zkw.head[i + 1]].cap != zkw.INF) {
                useOfServers.add(i);
            }else {
                servers.remove(i);
            }
        }

        int cost;

        if (demands != zkw.DEMAND) {
            cost = zkw.ans + costOfserver * (useOfServers.size());
            return new Result(false,zkw.DEMAND,cost,demands);
        } else {
            cost = zkw.ans + costOfserver * (useOfServers.size());
            return new Result(true,demands,cost,zkw.DEMAND);
        }
    }

}

class Result{
    boolean flag;
    int support;
    int cost;
    int demand;
    public Result(boolean flag, int support, int cost, int demand){
        this.flag = flag;
        this.support = support;
        this.cost = cost;
        this.demand = demand;
    }
}
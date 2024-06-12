package sjq;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Hex;

class TextGraphAnalyzer {
  private final Pattern nonLetterPattern = Pattern.compile("[^a-zA-Z\\s]");
  private final Map<String, Map<String, Integer>> graph;
  SecureRandom random = new SecureRandom();

  public TextGraphAnalyzer(String filePath) {
    this.graph = buildGraphFromFile(filePath);
  }


  @SuppressFBWarnings("PATH_TRAVERSAL_IN")
  private Map<String, Map<String, Integer>> buildGraphFromFile(String filePath) {
    Map<String, Map<String, Integer>> graph = new HashMap<>(); // 初始化图
    Path normalPath = Paths.get(filePath).toAbsolutePath().normalize();
    try (BufferedReader br = new BufferedReader(new
        InputStreamReader(new FileInputStream(normalPath.toFile()), "UTF-8"))) {
      String line;
      String previousWord = null; // 保存上一行最后一个单词

      // 逐行读取文件
      while ((line = br.readLine()) != null) {
        // 将非字母字符替换为空格，转换为小写并按空格分割
        String[] words = nonLetterPattern
            .matcher(line.toLowerCase()).replaceAll(" ").trim().split("\\s+");

        // 如果有前一行的最后一个单词，将其与当前行的第一个单词相连
        if (previousWord != null && words.length > 0) {
          graph.computeIfAbsent(previousWord, k -> new HashMap<>())
              .merge(words[0], 1, Integer::sum);
        }

        // 遍历当前行的单词，构建图的边
        for (int i = 0; i < words.length - 1; i++) {
          String wordFrom = words[i];
          String wordTo = words[i + 1];
          graph.computeIfAbsent(wordFrom, k -> new HashMap<>())
              .merge(wordTo, 1, Integer::sum); // 更新边的权重
        }

        // 保存当前行的最后一个单词，以便与下一行的第一个单词相连
        if (words.length > 0) {
          previousWord = words[words.length - 1];
        }
      }
      // 确保最后一个单词被加入到图中
      if (previousWord != null && !graph.containsKey(previousWord)) {
        graph.put(previousWord, new HashMap<>());
      }
    } catch (FileNotFoundException e) {
      // 处理文件读取错误
      System.err.println("Error reading file: " + e.getMessage());
    } catch (UnsupportedEncodingException e) {
      // 处理文件读取错误
      System.err.println("Error reading file: " + e.getMessage());
    } catch (IOException e) {
      // 处理文件读取错误
      System.err.println("Error reading file: " + e.getMessage());
    }

    return graph; // 返回构建好的图
  }


  public Map<String, Map<String, Integer>> getGraph() {
    //printGraph();
    return graph;
  }

  public void printGraph() {
    for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
      System.out.print(entry.getKey() + " -> {");
      List<String> edges;
      edges = new ArrayList<>();
      for (Map.Entry<String, Integer> neighborEntry : entry.getValue().entrySet()) {
        edges.add(neighborEntry.getKey() + "(" + neighborEntry.getValue() + ")");
      }
      System.out.println(String.join(", ", edges) + "}");
    }
  }

  public String queryBridgeWords(String word1, String word2) {

    if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
      return "";
    }

    List<String> bridgeWords = new ArrayList<>();
    for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
      String node = entry.getKey();
      Map<String, Integer> neighbors = entry.getValue();

      if (graph.get(word1).containsKey(node) && neighbors.containsKey(word2)) {
        bridgeWords.add(node);
      }
    }

    String joinResult;
    if (bridgeWords.isEmpty()) {
      //System.out.println("No bridge words from " + word1 + " to " + word2 + "!");
      return " ";
    } else {
      joinResult = String.join(", ", bridgeWords);
    }
    return joinResult; // 直接返回所有桥接词，以逗号分隔
  }

  public void showBridgeWords(String word1, String word2) {
    String result = queryBridgeWords(word1, word2);
    if (result.isEmpty()) {
      if (graph.containsKey(word1)) {
        System.out.printf("No %s in the graph!", word2);
      } else if (graph.containsKey(word2)) {
        System.out.printf("No %s in the graph!", word1);
      } else {
        System.out.printf("No %s and %s in the graph!", word1, word2);
      }
    } else if (result.equals(" ")) {
      System.out.println("No bridge words from " + word1 + " to " + word2 + "!");
    } else {
      System.out.println("The bridge words from " + word1 + " to " + word2 + " are: " + result);
    }
  }

  public String generateNewText(String inputText) {
    // 使用正则表达式分割输入文本为单词列表
    String[] words = inputText.toLowerCase().split("\\s+");
    /*SecureRandom random = new SecureRandom();*/

    List<String> newText = new ArrayList<>();

    for (int i = 0; i < words.length - 1; i++) {
      // 尝试查询桥接词
      String result = queryBridgeWords(words[i].toLowerCase(), words[i + 1].toLowerCase());

      // 如果找到了桥接词，随机选择一个插入；否则直接添加原单词对
      if (!result.isEmpty() && !result.equals(" ")) {
        String[] bridgeWords = result.split(",");
        int randomIndex = random.nextInt(bridgeWords.length);
        String chosenBridgeWord = bridgeWords[randomIndex];
        newText.add(words[i]);
        newText.add(chosenBridgeWord);
      } else {
        newText.add(words[i]);
      }
    }

    // 添加最后一个单词到结果列表
    newText.add(words[words.length - 1]);

    // 将列表转换回字符串并返回
    return String.join(" ", newText);
  }

  public String calcShortestPath(String word1, String word2) {
    // 检查word1和word2是否在图中
    if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
      System.out.println("Either word1 or word2 is not in the graph!");
      return null;
    }

    final int Inf = Integer.MAX_VALUE; // 定义一个无穷大值，用于初始化距离
    Map<String, Integer> dist = new HashMap<>(); // 存储从word1到各节点的最短距离
    Map<String, String> prev = new HashMap<>(); // 存储最短路径上的前驱节点
    Comparator<Map.Entry<String, Integer>> comparator = Comparator
        .comparingInt(Map.Entry::getValue);
    // 使用TreeSet实现优先队列，以便在更新距离时能够删除旧的条目
    TreeSet<Map.Entry<String, Integer>> pq = new TreeSet<>(comparator
        .thenComparing(Map.Entry::getKey));

    // 初始化dist和prev，并将所有节点加入优先队列
    for (String vertex : graph.keySet()) {
      int distance = vertex.equals(word1) ? 0 : Inf; // 起点距离为0，其他点为无穷大
      dist.put(vertex, distance); // 初始化距离表
      prev.put(vertex, null); // 初始化前驱节点表
      pq.add(new AbstractMap.SimpleEntry<>(vertex, distance)); // 将节点加入优先队列
    }

    // 主循环，直到优先队列为空
    while (!pq.isEmpty()) {
      // 取出优先队列中最小距离的节点
      Map.Entry<String, Integer> entry = pq.pollFirst();
      String currentNode = null;
      if (entry != null) {
        currentNode = entry.getKey();
      }

      // 如果当前节点是目标节点，重建路径并返回
      if (currentNode != null && currentNode.equals(word2)) {
        StringBuilder path = new StringBuilder(word2); // 从目标节点开始
        int totalDistance = dist.get(word2); // 获取最短路径的总距离

        // 通过前驱节点表重建路径
        while (prev.get(currentNode) != null) {
          path.insert(0, "→").insert(0, prev.get(currentNode));
          currentNode = prev.get(currentNode);
        }
        System.out.println("Shortest Path Length: " + totalDistance);
        return path.toString(); // 返回路径字符串
      }

      // 遍历当前节点的所有邻居节点
      for (Map.Entry<String, Integer> neighborEntry
          : graph.getOrDefault(currentNode, Collections.emptyMap()).entrySet()) {
        String neighbor = neighborEntry.getKey();
        int edgeWeight = neighborEntry.getValue(); // 获取边的权值

        // 计算从currentNode到neighbor的新距离
        int newDist = dist.get(currentNode) + edgeWeight;
        // 如果新距离更短，更新dist和prev，并更新优先队列
        if (newDist < dist.get(neighbor)) {
          pq.remove(new AbstractMap.SimpleEntry<>(neighbor, dist.get(neighbor))); // 移除旧的距离条目
          dist.put(neighbor, newDist); // 更新距离表
          prev.put(neighbor, currentNode); // 更新前驱节点表
          pq.add(new AbstractMap.SimpleEntry<>(neighbor, newDist)); // 添加新的距离条目到优先队列
        }
      }
    }

    // 如果没有找到路径，返回提示信息
    return "No path found from " + word1 + " to " + word2;
  }

  public void calcShortestMulPaths(String startWord) {
    // 检查 startWord 是否在图中
    if (!graph.containsKey(startWord)) {
      System.out.println("The word is not in the graph!");
      return;
    }

    final int Inf = Integer.MAX_VALUE; // 定义一个无穷大值，用于初始化距离
    Map<String, Integer> dist = new HashMap<>(); // 存储从 startWord 到各节点的最短距离
    Map<String, String> prev = new HashMap<>(); // 存储最短路径上的前驱节点
    Comparator<Map.Entry<String, Integer>> comparator
        = Comparator.comparingInt(Map.Entry::getValue);
    // 使用 TreeSet 实现优先队列，以便在更新距离时能够删除旧的条目
    TreeSet<Map.Entry<String, Integer>> pq
        = new TreeSet<>(comparator.thenComparing(Map.Entry::getKey));

    // 初始化 dist 和 prev，并将所有节点加入优先队列
    for (String vertex : graph.keySet()) {
      int distance = vertex.equals(startWord) ? 0 : Inf; // 起点距离为 0，其他点为无穷大
      dist.put(vertex, distance); // 初始化距离表
      prev.put(vertex, null); // 初始化前驱节点表
      pq.add(new AbstractMap.SimpleEntry<>(vertex, distance)); // 将节点加入优先队列
    }

    // 主循环，直到优先队列为空
    while (!pq.isEmpty()) {
      // 取出优先队列中最小距离的节点
      Map.Entry<String, Integer> entry = pq.pollFirst();
      String currentNode = null;
      if (entry != null) {
        currentNode = entry.getKey();
      }

      if (currentNode != null) {
        // 更新邻居节点的距离
        Map<String, Integer> neighbors = graph.get(currentNode);
        for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
          String neighborNode = neighbor.getKey();
          int weight = neighbor.getValue();
          int newDist = dist.get(currentNode) + weight;

          // 如果找到更短的路径，则更新优先队列和距离表
          if (newDist < dist.get(neighborNode)) {
            pq.remove(new AbstractMap.SimpleEntry<>(neighborNode, dist.get(neighborNode)));
            dist.put(neighborNode, newDist);
            prev.put(neighborNode, currentNode);
            pq.add(new AbstractMap.SimpleEntry<>(neighborNode, newDist));
          }
        }
      }
    }

    // 打印从 startWord 到所有其他节点的路径
    for (String vertex : graph.keySet()) {
      if (!vertex.equals(startWord)) {
        StringBuilder path = new StringBuilder(vertex);
        String currentNode = vertex;

        // 通过前驱节点表重建路径
        while (prev.get(currentNode) != null) {
          path.insert(0, "→").insert(0, prev.get(currentNode));
          currentNode = prev.get(currentNode);
        }

        System.out.println("最短路径从 " + startWord + " 到 " + vertex + ": " + path);
      }
    }
  }

  @SuppressFBWarnings({"DM_DEFAULT_ENCODING", "PREDICTABLE_RANDOM"})
  public String randomWalk() {
    Random random = new Random();
    Scanner scanner = new Scanner(System.in);
    String startNode = getRandomNode(graph.keySet()); // 假设这是获取图中随机起点的函数
    Set<String> visitedEdges = new HashSet<>();
    StringBuilder walkStringBuilder = new StringBuilder(startNode);

    while (!startNode.isEmpty()) {
      // 获取当前节点的所有出边，如果没有出边则结束
      Map<String, Integer> edges = graph.getOrDefault(startNode, Collections.emptyMap());
      if (edges.isEmpty()) {
        break;
      }
      // 随机选择一个出边的目标节点
      List<String> neighborNodes = new ArrayList<>(edges.keySet());
      String nextNode = neighborNodes.get(random.nextInt(neighborNodes.size()));
      String edge = startNode + "→" + nextNode;
      // 检查是否重复，如果是则结束
      if (!visitedEdges.add(edge)) {
        break;
      }
      // 记录节点和边
      walkStringBuilder.append(" ").append(nextNode);
      startNode = nextNode; // 移动到下一个节点
      // 提示用户是否停止遍历
      System.out.println("键入“q”停止遍历：");
      String input = scanner.nextLine();
      if (input.equalsIgnoreCase("q")) {
        break;
      }
    }
    // 写入文件
    writeWalkToFile(walkStringBuilder.toString());

    return walkStringBuilder.toString();
  }

  // 获取图中的随机节点
  private String getRandomNode(Set<String> nodes) {
    /*SecureRandom random = new SecureRandom();*/
    //产生随机数
    int index = random.nextInt(nodes.size());
    Iterator<String> iterator = nodes.iterator();
    for (int i = 0; i < index; i++) {
      iterator.next();
    }
    return iterator.next();
  }

  // 将游走结果写入文件
  @SuppressFBWarnings("DM_DEFAULT_ENCODING")
  private void writeWalkToFile(String content) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter("random_walk_output.txt"))) {
      writer.write(content);
    } catch (IOException e) {
      System.err.println("Error writing to file: " + e.getMessage());
    }
  }

}

package sjq;

import java.util.Map;
import java.util.Scanner;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

/**
 * .
 * 主类。
 *
 * @author 时景琦
 * @since 2024.6.11
 */
public class Main {
  /**
   * .
   * 主函数入口，显示交互界面。
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: java Main <file_path>");
      return;
    }

    String filePath = args[0];
    TextGraphAnalyzer analyzer = new TextGraphAnalyzer(filePath);
    System.out.println("文件读入，有向图已生成！");
    Scanner scanner = new Scanner(System.in, "ISO-8859-1");
    while (true) {
      System.out.println();
      System.out.println("-----------------------------------");
      System.out.println("功能选择：");
      System.out.println("0.退出");
      System.out.println("1.有向图命令行展示");
      System.out.println("2.有向图可视化");
      System.out.println("3.查询桥接词");
      System.out.println("4.根据桥接词生成新文本");
      System.out.println("5.计算两单词间最短路径");
      System.out.println("6.计算某一单词最短路径");
      System.out.println("7.随机游走");
      System.out.println("-----------------------------------");
      System.out.println("请输入您选择的功能序号：");
      int choice = scanner.nextInt();
      scanner.nextLine(); //读取换行
      switch (choice) {
        case 0:
          System.exit(0);
          break;
        case 1:
          System.out.println("有向图的节点边文字描述为：");
          analyzer.printGraph();
          break;
        case 2:
          showDirectedGraph(analyzer.getGraph());
          break;
        case 3:
          System.out.println("第一个单词：");
          String word1 = scanner.nextLine();
          System.out.println("第二个单词：");
          String word2 = scanner.nextLine();
          analyzer.showBridgeWords(word1, word2); // 替换为实际单词进行查询
          break;
        case 4:
          System.out.println("请输入文本：");
          String text = scanner.nextLine();
          String newText = analyzer.generateNewText(text);
          System.out.println("新文本：" + newText);
          break;
        case 5:
          System.out.println("第一个单词：");
          String word3 = scanner.nextLine();
          System.out.println("第二个单词：");
          String word4 = scanner.nextLine();
          String distance = analyzer.calcShortestPath(word3, word4);
          System.out.printf("%s 和 %s 的最短路径为:%s%n", word3, word4, distance);
          break;
        case 6:
          System.out.println("输入单词：");
          String word5 = scanner.nextLine();

          analyzer.calcShortestMulPaths(word5);
          break;
        case 7:

          String randomPath = analyzer.randomWalk();
          System.out.println("随机游走路径:\"" + randomPath + "\"已写入文件");
          break;
        default:
          System.out.println("无效输入！请输入0~6的数字");
      }
    }


  }

  static void showDirectedGraph(Map<String, Map<String, Integer>> graph) {
    System.setProperty("org.graphstream.ui", "swing");

    // 创建有向图对象
    Graph directedGraph = new SingleGraph("Directed Graph");

    // 添加节点和边到图中
    for (Map.Entry<String, Map<String, Integer>> entrySource : graph.entrySet()) {
      String source = entrySource.getKey();
      Map<String, Integer> targets = entrySource.getValue();

      // 添加源节点（如果不存在）
      if (directedGraph.getNode(source) == null) {
        directedGraph.addNode(source).setAttribute("ui.label", source);
        directedGraph.getNode(source)
            .setAttribute("ui.style", "text-size: 20px; text-color: blue;");
      }

      // 遍历目标节点并添加节点和边
      for (Map.Entry<String, Integer> entryTarget : targets.entrySet()) {
        String target = entryTarget.getKey();
        int weight = entryTarget.getValue();

        // 添加目标节点（如果不存在）
        if (directedGraph.getNode(target) == null) {
          directedGraph.addNode(target).setAttribute("ui.label", target);
          directedGraph.getNode(target)
              .setAttribute("ui.style", "text-size: 20px; text-color: blue;");
        }

        // 添加边（如果不存在）
        String edgeId = source + "_" + target;
        if (directedGraph.getEdge(edgeId) == null) {
          Edge edge = directedGraph.addEdge(edgeId, source, target, true);
          edge.setAttribute("weight", weight);
          edge.setAttribute("ui.style", "text-size: 20px; text-color: red; arrow-size: 10px;");
        }
      }
    }

    // 设置边标签
    directedGraph.edges()
        .forEach(edge -> edge.setAttribute("ui.label", edge.getAttribute("weight")));

    // 显示图形
    Viewer viewer = directedGraph.display();
    viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
  }
}



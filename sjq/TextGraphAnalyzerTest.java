package sjq;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextGraphAnalyzerTest {
  String path="E://soft_engineering//lab1//lab1//sjq//input.txt";
  private final TextGraphAnalyzer analyzer = new TextGraphAnalyzer(path);
  @org.junit.jupiter.api.Test
  void  calcShortestPathCase1() {
    String word1 = "after";
    String word2 = "the";
    String expectedPath = "after→morning→light→the";
    // 调用calcShortestPath方法
    String result = analyzer.calcShortestPath(word1, word2);
    // 验证结果
    assertEquals(expectedPath, result);
  }

  @org.junit.jupiter.api.Test
  void calcShortestPathCase2() {
    String word1 = "";
    String word2 = "to";
    String expectedPath = null;
    // 调用calcShortestPath方法
    String result = analyzer.calcShortestPath(word1, word2);
    // 验证结果
    assertEquals(expectedPath, result, "The calculated path does not match the expected path.");
  }

  @org.junit.jupiter.api.Test
  void calcShortestPathCase3() {
    String word1 = "morning";
    String word2 = "";
    String expectedPath = null;
    // 调用calcShortestPath方法
    String result = analyzer.calcShortestPath(word1, word2);
    // 验证结果
    assertEquals(expectedPath, result, "The calculated path does not match the expected path.");
  }

  @org.junit.jupiter.api.Test
  void calcShortestPathCase4() {
    String word1 = "new";
    String word2 = "hate";
    String expectedPath = null;
    // 调用calcShortestPath方法
    String result = analyzer.calcShortestPath(word1, word2);
    // 验证结果
    assertEquals(expectedPath, result, "The calculated path does not match the expected path.");
  }

  @org.junit.jupiter.api.Test
  void calcShortestPathCase5() {
    String word1 = "data";
    String word2 = "explore";
    String expectedPath = null;
    // 调用calcShortestPath方法
    String result = analyzer.calcShortestPath(word1, word2);
    // 验证结果
    assertEquals(expectedPath, result, "The calculated path does not match the expected path.");
  }

}
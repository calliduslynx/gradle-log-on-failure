import org.junit.Assert;
import org.junit.Test;

public class ExampleTest {
  private static void log(String msg) {
    System.out.println(msg);
  }

  @Test
  public void test_1_ok() {
    log("test_1_ok: line 1");
    log("test_1_ok: line 2");
    log("test_1_ok: line 3");
  }

  @Test
  public void test_2_failing() {
    log("test_2_failing: line 1");
    log("test_2_failing: line 2");
    Assert.fail("Let's fail the test and see if we get the logs");
    log("test_2_failing: line 3");
  }

  @Test
  public void test_3_exception() {
    log("test_3_failing: line 1");
    log("test_3_failing: line 2");
    log("test_3_failing: line 3\nand line 3");
    if (Math.random() < 2)
      throw new IllegalStateException("Let's throw an exception and see if we get the logs");
    log("test_3_failing: line 3");
  }

  @Test
  public void test_4_ok() {
    log("test_4_ok: line 1");
    log("test_4_ok: line 2");
    log("test_4_ok: line 3");
  }
}

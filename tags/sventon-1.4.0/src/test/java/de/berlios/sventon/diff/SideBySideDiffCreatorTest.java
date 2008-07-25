package de.berlios.sventon.diff;

import de.berlios.sventon.model.SideBySideDiffRow;
import de.berlios.sventon.model.TextFile;
import junit.framework.TestCase;

import java.util.List;
import java.util.Iterator;

public class SideBySideDiffCreatorTest extends TestCase {

  public void testCreateFromDiffResult() throws Exception {
    final String diffResult = "1d1\r\n<\r\n";
    final TextFile fromFile = new TextFile("\r\ntest\r\nfile\r\n");
    final TextFile toFile = new TextFile("test\r\nfile\r\n");

    final SideBySideDiffCreator diffCreator = new SideBySideDiffCreator(
        fromFile, null, "UTF-8", toFile, null, "UTF-8");

    final List<SideBySideDiffRow> result = diffCreator.createFromDiffResult(diffResult);
    assertEquals(3, result.size());
    final Iterator<SideBySideDiffRow> iterator = result.iterator();
    assertFalse(iterator.next().getIsUnchanged());
    assertTrue(iterator.next().getIsUnchanged());
    assertTrue(iterator.next().getIsUnchanged());

    for (SideBySideDiffRow row : result) {
      assertEquals(row.getLeft().getLine(), row.getRight().getLine());
    }
  }
}

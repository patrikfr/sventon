package org.sventon.advice;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StopWatchAroundAdviceTest {

  @Test
  public void testCreateMessage() throws Exception {
    final StopWatchAroundAdvice advice = new StopWatchAroundAdvice();
    assertEquals("Method [null] took [0] ms", advice.createMessage(null, 0));
    assertEquals("Method [test] took [0] ms", advice.createMessage("test", 0));
    assertEquals("Method [test] took [99] ms", advice.createMessage("test", 99));
    assertEquals("Method [test] took [1000] ms", advice.createMessage("test", 1000));
  }
}

package com.twitter.heron.localmode.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * RotatingMap Tester.
 */
public class RotatingMapTest {

  @Before
  public void before() throws Exception {
  }

  @After
  public void after() throws Exception {
  }

  /**
   * // Test the remove and rotate
   */
  @Test
  public void testRemoveAndRotate() throws Exception {
    int nBuckets = 3;
    RotatingMap g = new RotatingMap(nBuckets);

    // Create some items
    for (int i = 0; i < 100; i++) {
      g.create(i, 1);
    }

    // Make sure that removes work
    for (int i = 0; i < 100; i++) {
      Assert.assertTrue(g.remove(i));
    }

    // Unknown items cant be removed
    for (int i = 0; i < 100; i++) {
      Assert.assertFalse(g.remove(i));
    }

    // Create some more
    for (int i = 0; i < 100; ++i) {
      g.create(i, 1);
    }

    // Rotate once
    g.rotate();

    // Make sure that removes work
    for (int i = 0; i < 100; ++i) {
      Assert.assertTrue(g.remove(i));
    }

    // Unknown items cant be removed
    for (int i = 0; i < 100; ++i) {
      Assert.assertFalse(g.remove(i));
    }

    // Create some more
    for (int i = 0; i < 100; ++i) {
      g.create(i, 1);
    }

    // Rotate nBuckets times
    for (int i = 0; i < nBuckets; ++i) {
      g.rotate();
    }

    // removes dont work
    for (int i = 0; i < 100; ++i) {
      Assert.assertFalse(g.remove(i));
    }
  }

  /**
   * Test the anchor logic
   */
  @Test
  public void testAnchor() throws Exception {
    int nBuckets = 3;
    RotatingMap g = new RotatingMap(nBuckets);

    // Create some items
    for (int i = 0; i < 100; ++i) {
      g.create(i, 1);
    }

    // basic Anchor works
    for (int i = 0; i < 100; ++i) {
      Assert.assertEquals(g.anchor(i, 1), true);
      Assert.assertEquals(g.remove(i), true);
    }

    // layered anchoring
    List<Long> things_added = new ArrayList<>();
    long first_key = new Random().nextLong();
    g.create(1, first_key);
    things_added.add(first_key);
    for (int j = 1; j < 100; ++j) {
      long key = new Random().nextLong();
      things_added.add(key);
      Assert.assertEquals(g.anchor(1, key), false);
    }

    // xor ing works
    for (int j = 0; j < 99; ++j) {
      Assert.assertEquals(g.anchor(1, things_added.get(j)), false);
    }

    Assert.assertEquals(g.anchor(1, things_added.get(99)), true);
    Assert.assertEquals(g.remove(1), true);

    // Same test with some rotation
    things_added.clear();
    first_key = new Random().nextLong();
    g.create(1, first_key);
    things_added.add(first_key);
    for (int j = 1; j < 100; ++j) {
      long key = new Random().nextLong();
      things_added.add(key);
      Assert.assertEquals(g.anchor(1, key), false);
    }

    g.rotate();

    for (int j = 0; j < 99; ++j) {
      Assert.assertEquals(g.anchor(1, things_added.get(j)), false);
    }

    Assert.assertEquals(g.anchor(1, things_added.get(99)), true);
    Assert.assertEquals(g.remove(1), true);

    // Too much rotation
    things_added.clear();
    first_key = new Random().nextLong();
    g.create(1, first_key);
    things_added.add(first_key);
    for (int j = 1; j < 100; ++j) {
      long key = new Random().nextLong();
      things_added.add(key);
      Assert.assertEquals(g.anchor(1, key), false);
    }

    for (int i = 0; i < nBuckets; ++i) {
      g.rotate();
    }

    for (int j = 0; j < 100; ++j) {
      Assert.assertEquals(g.anchor(1, things_added.get(j)), false);
    }

    Assert.assertEquals(g.remove(1), false);
  }
} 
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import lsmTree.LSMTree;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LSMTreeTest {
  private final LSMTree<Integer, Integer> tree = new LSMTree<>(6, 2, 3);

  @Before
  public void initTree() {
    for (int i = 0; i < 100; i++) {
      tree.insert(i, i * 2);
    }
  }

  @Test
  public void get() {
    for (int i = 0; i < 100; i++) {
      Assert.assertEquals(0, tree.get(i) - 2 * i);
    }
  }

  @Test
  public void delete() {
    for (int i = 0; i < 100; i++) {
      tree.remove(i);
      Assert.assertNull(tree.get(i));
    }
  }

  @Test
  public void deleteAndADD() {
    for (int i = 0; i < 50; i++) {
      tree.remove(i);
      Assert.assertNull(tree.get(i));
    }
    for (int i = 0; i < 50; i++) {
      tree.insert(i + 100, (i + 100) * 2);
      Assert.assertEquals(0, tree.get(i + 100) - 2 * (i + 100));
    }
  }

  @Test
  public void removeAndInsert() {
    tree.remove(0);
    tree.insert(0, 0);
    Assert.assertEquals(0, (int) tree.get(0));
  }

  @Test
  public void multiPos() {
    tree.remove(0);
    for (int i = 0; i < 50; i++) {
      tree.insert(i + 100, (i + 100) * 2);
      Assert.assertEquals(0, tree.get(i + 100) - 2 * (i + 100));
    }
    Assert.assertNull(tree.get(0));
  }
}

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

import bplustree.BPlusRecord;
import bplustree.BPlusTree;

import java.util.Iterator;

public class BPlusTreeTest {

  public static void main(String[] args) {
    testInitAndDelete();
    testPrint();
  }

  private static void testInitAndDelete() {
    BPlusTree<Integer, Integer> tree = new BPlusTree<>(3);
    for (int i = 0; i < 4; i++) {
      tree.insertOrUpdate(i, i);
    }
    System.out.println("Init BPlusTree:");
    tree.print();
    tree.remove(2);
    System.out.println("Remove 2:");
    tree.print();
  }

  private static void testPrint() {
    System.out.println("Test Iterator:");
    BPlusTree<Integer, Integer> tree = new BPlusTree<>(3);
    for (int i = 0; i < 10; i++) {
      tree.insertOrUpdate(i, i);
    }
    for (Iterator<BPlusRecord<Integer, Integer>> iterator = tree.iterator(); iterator.hasNext(); ) {
      System.out.print(iterator.next().getKey() + " ");
    }
  }
}

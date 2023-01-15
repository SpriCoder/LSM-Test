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

package lsmTree;

import bplustree.BPlusRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LSMTree<K extends Comparable<K>, V> {
  // B+ 树阶数
  protected final int order;
  // L0 层数的树的大小
  protected final int L0Size;
  // 下一层树最大比上一层大多少
  protected final int k;
  // 各层 LSM 树
  protected List<LSMNode<K, V>> nodes;

  public LSMTree(int L0Size, int k, int order) {
    this.order = order;
    this.L0Size = L0Size;
    this.k = k;
    nodes = new ArrayList<>();
    nodes.add(new BPlusLSMNode<>(order, L0Size));
  }

  /** 插入键值 */
  public void insert(K key, V value) {
    LSMNode<K, V> level0Tree = nodes.get(0);
    level0Tree.insert(new BPlusRecord<>(key, value));
    if (level0Tree.needUnion()) {
      merge(level0Tree, 0);
    }
  }

  /** 查询键值为 key 的值 */
  public V get(K key) {
    for (LSMNode<K, V> node : nodes) {
      BPlusRecord<K, V> record = node.get(key);
      if (record != null) {
        return (record.isDeleted()) ? null : record.getValue();
      }
    }
    return null;
  }

  /** 为键值 key 的记录添加墓碑标记 */
  public void remove(K key) {
    LSMNode<K, V> level0Tree = nodes.get(0);
    level0Tree.insert(new BPlusRecord<>(key, true));
    if (level0Tree.needUnion()) {
      merge(level0Tree, 0);
    }
  }

  /**
   * 合并相邻层级
   *
   * @param node 待合并的树
   * @param level 待合并的树所处的层级
   */
  private void merge(LSMNode<K, V> node, int level) {
    if (level <= nodes.size()) {
      int greatLevelTreeSize = (int) (L0Size * Math.pow(k, level + 1));
      if (level + 1 == nodes.size()) {
        // 如果 level + 1 层为空，则直接替换。
        node.setMaxSize(greatLevelTreeSize);
        nodes.add(node);
        nodes.set(level, new BPlusLSMNode<>(order, (int) (L0Size * Math.pow(k, level))));
      } else if (!nodes.get(level + 1).isEmpty()) {
        // 如果 level + 1 层非空，则合并两层
        LSMNode<K, V> newNode = new BPlusLSMNode<>(order, greatLevelTreeSize);
        mergeNode(newNode, node, nodes.get(level + 1));
        nodes.set(level + 1, newNode);
        nodes.set(level, new BPlusLSMNode<>(order, (int) (L0Size * Math.pow(k, level))));
        if (newNode.needUnion()) {
          merge(newNode, level + 1);
        }
      } else {
        // 如果 level + 1 层为空，则直接替换
        node.setMaxSize(greatLevelTreeSize);
        nodes.set(level + 1, node);
        nodes.set(level, new BPlusLSMNode<>(order, (int) (L0Size * Math.pow(k, level))));
      }
    }
  }

  /**
   * 合并两层
   *
   * @param node 结果
   * @param node1 层级比较低的一层
   * @param node2 层级比较高的一层
   */
  private void mergeNode(LSMNode<K, V> node, LSMNode<K, V> node1, LSMNode<K, V> node2) {
    Iterator<BPlusRecord<K, V>> iterator1 = node1.iterator();
    Iterator<BPlusRecord<K, V>> iterator2 = node2.iterator();
    // if any one is empty then
    if (!iterator1.hasNext()) {
      node = node2;
    } else if (!iterator2.hasNext()) {
      node = node1;
    }
    // both not empty
    BPlusRecord<K, V> entry1 = iterator1.next(), entry2 = iterator2.next();
    do {
      if (entry1 == null && entry2 == null) {
        break;
      } else if (entry1 == null) {
        node.insert(entry2);
        entry2 = iterator2.hasNext() ? iterator2.next() : null;
      } else if (entry2 == null) {
        node.insert(entry1);
        entry1 = iterator1.hasNext() ? iterator1.next() : null;
      } else {
        int result = entry1.getKey().compareTo(entry2.getKey());
        switch (result) {
          case -1:
            // <
            node.insert(entry1);
            entry1 = iterator1.hasNext() ? iterator1.next() : null;
            break;
          case 1:
            // >
            node.insert(entry2);
            entry2 = iterator2.hasNext() ? iterator2.next() : null;
            break;
          default:
            // equal
            node.insert(entry1);
            entry1 = iterator1.hasNext() ? iterator1.next() : null;
            entry2 = iterator2.hasNext() ? iterator2.next() : null;
            break;
        }
      }
    } while (iterator1.hasNext() || iterator2.hasNext());
    // check left entry
    if (entry1 != null && entry2 != null) {
      int result = entry1.getKey().compareTo(entry2.getKey());
      switch (result) {
        case -1:
          // <
          node.insert(entry1);
          node.insert(entry2);
          break;
        case 1:
          // >
          node.insert(entry2);
          node.insert(entry1);
          break;
        default:
          // equal
          node.insert(entry1);
          break;
      }
    } else if (entry1 != null) {
      node.insert(entry1);
    } else if (entry2 != null) {
      node.insert(entry2);
    }
  }

  public int getMaxLevel() {
    return nodes.size();
  }

  public int getOrder() {
    return order;
  }

  public void print() {
    System.out.printf("LSM Tree: MaxLevel=%d, K=%d, Order=%d\n", nodes.size(), k, order);
    for (int level = 0; level < nodes.size(); level++) {
      LSMNode<K, V> tree = nodes.get(level);
      System.out.printf(
          "Level-%d Tree: MaxSize=%d, Size=%d\n", level, tree.getMaxSize(), tree.getSize());
      tree.print();
    }
  }
}

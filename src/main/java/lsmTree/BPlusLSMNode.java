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
import bplustree.BPlusTree;
import bplustree.BPlusTreeIterator;

import java.util.Iterator;

public class BPlusLSMNode<K extends Comparable<K>, V> implements LSMNode<K, V> {
  protected BPlusTree<K, V> tree;
  protected int size = 0;
  protected int maxSize;

  public BPlusLSMNode(int order, int size) {
    tree = new BPlusTree<>(order);
    this.maxSize = size;
  }

  @Override
  public void insert(BPlusRecord<K, V> record) {
    size++;
    if (record.isDeleted()) {
      tree.insertRemoveFlag(record.getKey());
    } else {
      tree.insertOrUpdate(record.getKey(), record.getValue());
    }
  }

  @Override
  public BPlusRecord<K, V> get(K key) {
    return tree.get(key);
  }

  @Override
  public void print() {
    tree.print();
  }

  @Override
  public boolean needUnion() {
    return size >= maxSize;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public int getSize() {
    return size;
  }

  @Override
  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

  @Override
  public int getMaxSize() {
    return maxSize;
  }

  @Override
  public Iterator<BPlusRecord<K, V>> iterator() {
    return new BPlusTreeIterator<>(tree);
  }
}

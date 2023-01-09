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

package bplustree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BPlusTreeIterator<K extends Comparable<K>, V> implements Iterator<BPlusRecord<K, V>> {
  private BPlusNode<K, V> node;
  private List<BPlusRecord<K, V>> records;
  private int index = 0;

  public BPlusTreeIterator(BPlusTree<K, V> tree) {
    BPlusNode<K, V> head = tree.getHead();
    if (head != null) {
      this.records = head.records;
      this.node = head.next;
    } else {
      this.node = null;
      this.records = new ArrayList<>();
    }
  }

  @Override
  public boolean hasNext() {
    if (index >= records.size()) {
      if (node == null) {
        return false;
      } else {
        this.records = node.records;
        node = node.next;
        index = 0;
      }
    }
    return records.size() > 0;
  }

  @Override
  public BPlusRecord<K, V> next() {
    try {
      return records.get(index++);
    } catch (Exception e) {
      System.out.println(records + " " + index);
      throw e;
    }
  }
}

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

import java.util.Iterator;

/**
 * B+树的定义： 1.任意非叶子结点最多有M个子节点；且M>2；M为B+树的阶数 2.除根结点以外的非叶子结点至少有 (M+1)/2个子节点； 3.根结点至少有2个子节点；
 * 4.除根节点外每个结点存放至少（M-1）/2和至多M-1个关键字；（至少1个关键字） 5.非叶子结点的子树指针比关键字多1个；
 * 6.非叶子节点的所有key按升序存放，假设节点的关键字分别为K[0], K[1] … K[M-2],指向子女的指针分别为P[0], P[1]…P[M-1]。则有： 　　　P[0] < K[0]
 * <= P[1] < K[1] …..< K[M-2] <= P[M-1] 7.所有叶子结点位于同一层； 8.为所有叶子结点增加一个链指针； 9.所有关键字都在叶子结点出现
 */
public class BPlusTree<K extends Comparable<K>, V> implements Iterable<BPlusRecord<K, V>> {

  // 根节点
  protected BPlusNode<K, V> root;

  // 阶数，M值
  protected int order;

  // 叶子节点的链表头
  protected BPlusNode<K, V> head;

  // 树高
  protected int height = 0;

  public BPlusNode<K, V> getHead() {
    return head;
  }

  public void setHead(BPlusNode<K, V> head) {
    this.head = head;
  }

  public BPlusNode<K, V> getRoot() {
    return root;
  }

  public void setRoot(BPlusNode<K, V> root) {
    this.root = root;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getHeight() {
    return height;
  }

  public BPlusRecord<K, V> get(K key) {
    return root.get(key);
  }

  public V remove(K key) {
    return root.remove(key, this);
  }

  public void insertOrUpdate(K key, V value) {
    root.insertOrUpdate(new BPlusRecord<>(key, value), this);
  }

  public void insertRemoveFlag(K key) {
    root.insertOrUpdate(new BPlusRecord<>(key, true), this);
  }

  public BPlusTree(int order) {
    if (order < 3) {
      System.out.print("order must be greater than 2");
      System.exit(0);
    }
    this.order = order;
    root = new BPlusNode<K, V>(true, true);
    head = root;
  }

  public void print() {
    this.root.print(0);
  }

  @Override
  public Iterator<BPlusRecord<K, V>> iterator() {
    return new BPlusTreeIterator<>(this);
  }
}

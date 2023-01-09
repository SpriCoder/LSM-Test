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
import java.util.List;

public class BPlusNode<K extends Comparable<K>, V> {

  // whether is leaf node
  protected boolean isLeaf;

  // whether is root node
  protected boolean isRoot;

  // the parent node of current node
  protected BPlusNode<K, V> parent;

  // the previous node of current node
  protected BPlusNode<K, V> previous;

  // the next node of current node
  protected BPlusNode<K, V> next;

  // the records of current node
  protected List<BPlusRecord<K, V>> records;

  // the children of current node
  protected List<BPlusNode<K, V>> children;

  public BPlusNode(boolean isLeaf) {
    this.isLeaf = isLeaf;
    records = new ArrayList<>();

    if (!isLeaf) {
      children = new ArrayList<>();
    }
  }

  public BPlusNode(boolean isLeaf, boolean isRoot) {
    this(isLeaf);
    this.isRoot = isRoot;
  }

  public BPlusRecord<K, V> get(K key) {
    // if current node is leaf node
    if (isLeaf) {
      int low = 0, high = records.size() - 1, mid;
      int comp;
      while (low <= high) {
        mid = (low + high) / 2;
        comp = records.get(mid).getKey().compareTo(key);
        if (comp == 0) {
          return records.get(mid);
        } else if (comp < 0) {
          low = mid + 1;
        } else {
          high = mid - 1;
        }
      }
      // not find target record
      return null;
    }
    // if current node is not leaf node
    if (key.compareTo(records.get(0).getKey()) < 0) {
      // if current key less than the left key, then search the first child
      return children.get(0).get(key);
    } else if (key.compareTo(records.get(records.size() - 1).getKey()) >= 0) {
      // if current key greater than the right key, then search the last child
      return children.get(children.size() - 1).get(key);
    } else {
      // otherwise, continue searching along the previous child node greater than key
      int low = 0, high = records.size() - 1, mid = 0;
      int comp;
      while (low <= high) {
        mid = (low + high) / 2;
        comp = records.get(mid).getKey().compareTo(key);
        if (comp == 0) {
          return children.get(mid + 1).get(key);
        } else if (comp < 0) {
          low = mid + 1;
        } else {
          high = mid - 1;
        }
      }
      return children.get(low).get(key);
    }
  }

  public void insertOrUpdate(BPlusRecord<K, V> record, BPlusTree<K, V> tree) {
    // if current node is leaf node
    if (isLeaf) {
      // no need to split, insert or update directly
      if (contains(record.getKey()) != -1 || records.size() < tree.getOrder()) {
        insertOrUpdate(record);
        if (tree.getHeight() == 0) {
          tree.setHeight(1);
        }
        return;
      }
      // need to split, split into two nodes (left node, right node)
      BPlusNode<K, V> left = new BPlusNode<>(true);
      BPlusNode<K, V> right = new BPlusNode<>(true);
      // update link
      if (previous != null) {
        previous.next = left;
        left.previous = previous;
      }
      if (next != null) {
        next.previous = right;
        right.next = next;
      }
      if (previous == null) {
        tree.setHead(left);
      }

      left.next = right;
      right.previous = left;
      previous = null;
      next = null;

      // copy origin records into new nodes
      copy2Nodes(record, left, right, tree);

      // if current node is not root node
      if (parent != null) {
        // update the relation between child node and parent node
        int index = parent.children.indexOf(this);
        parent.children.remove(this);
        left.parent = parent;
        right.parent = parent;
        parent.children.add(index, left);
        parent.children.add(index + 1, right);
        parent.records.add(index, right.records.get(0));
        // delete current node's records
        records = null;
        // delete current node's children nodes
        children = null;
        // update records into parent tree
        parent.updateInsert(tree);
        // delete current node's parent node
        parent = null;
      } else {
        // if current node is root node
        isRoot = false;
        BPlusNode<K, V> parent = new BPlusNode<K, V>(false, true);
        tree.setRoot(parent);
        left.parent = parent;
        right.parent = parent;
        parent.children.add(left);
        parent.children.add(right);
        parent.records.add(right.records.get(0));
        records = null;
        children = null;
      }
      return;
    }
    // if current node is not leaf node
    if (record.getKey().compareTo(records.get(0).getKey()) < 0) {
      // if current key less than the left key, then search the first child
      children.get(0).insertOrUpdate(record, tree);
    } else if (record.getKey().compareTo(records.get(records.size() - 1).getKey()) >= 0) {
      // if current key greater than the right key, then search the last child
      children.get(children.size() - 1).insertOrUpdate(record, tree);
    } else {
      // otherwise, continue searching along the previous child node greater than key
      int low = 0, high = records.size() - 1, mid = 0;
      int comp;
      while (low <= high) {
        mid = (low + high) / 2;
        comp = records.get(mid).getKey().compareTo(record.getKey());
        if (comp == 0) {
          children.get(mid + 1).insertOrUpdate(record, tree);
          break;
        } else if (comp < 0) {
          low = mid + 1;
        } else {
          high = mid - 1;
        }
      }
      if (low > high) {
        children.get(low).insertOrUpdate(record, tree);
      }
    }
  }

  private void copy2Nodes(
      BPlusRecord<K, V> record, BPlusNode<K, V> left, BPlusNode<K, V> right, BPlusTree<K, V> tree) {
    // The size of left node
    int leftSize = (tree.getOrder() + 1) / 2 + (tree.getOrder() + 1) % 2;
    // used to record whether a new element has been inserted
    boolean b = false;
    for (int i = 0; i < records.size(); i++) {
      BPlusNode<K, V> insertNodes;
      if (leftSize != 0) {
        leftSize--;
        insertNodes = left;
      } else {
        insertNodes = right;
      }
      if (!b && records.get(i).getKey().compareTo(record.getKey()) > 0) {
        insertNodes.records.add(record);
        b = true;
        i--;
      } else {
        insertNodes.records.add(records.get(i));
      }
    }
    if (!b) {
      right.records.add(record);
    }
  }

  protected void updateInsert(BPlusTree<K, V> tree) {
    // If the number of child nodes exceeds the order, the node needs to be split
    if (children.size() > tree.getOrder()) {
      // need to split, split into two nodes (left node, right node)
      BPlusNode<K, V> left = new BPlusNode<K, V>(false);
      BPlusNode<K, V> right = new BPlusNode<K, V>(false);
      // the size of left node and right node
      int leftSize = (tree.getOrder() + 1) / 2 + (tree.getOrder() + 1) % 2;
      int rightSize = (tree.getOrder() + 1) / 2;
      // copy the child node to the new node that is split, and update the records
      for (int i = 0; i < leftSize; i++) {
        left.children.add(children.get(i));
        children.get(i).parent = left;
      }
      for (int i = 0; i < rightSize; i++) {
        right.children.add(children.get(leftSize + i));
        children.get(leftSize + i).parent = right;
      }
      for (int i = 0; i < leftSize - 1; i++) {
        left.records.add(records.get(i));
      }
      for (int i = 0; i < rightSize - 1; i++) {
        right.records.add(records.get(leftSize + i));
      }
      // if current node is not leaf node
      if (parent != null) {
        // update the relation between child node and parent node
        int index = parent.children.indexOf(this);
        parent.children.remove(this);
        left.parent = parent;
        right.parent = parent;
        parent.children.add(index, left);
        parent.children.add(index + 1, right);
        parent.records.add(index, records.get(leftSize - 1));
        records = null;
        children = null;
        // update the parent's records
        parent.updateInsert(tree);
        parent = null;
      } else {
        // if current node is root node
        isRoot = false;
        BPlusNode<K, V> parent = new BPlusNode<K, V>(false, true);
        tree.setRoot(parent);
        tree.setHeight(tree.getHeight() + 1);
        left.parent = parent;
        right.parent = parent;
        parent.children.add(left);
        parent.children.add(right);
        parent.records.add(records.get(leftSize - 1));
        records = null;
        children = null;
      }
    }
  }

  protected void updateRemove(BPlusTree<K, V> tree) {
    // if the number of child nodes is less than M/2 or less than 2, you need to merge nodes
    if (children.size() < tree.getOrder() / 2 || children.size() < 2) {
      // if current node is root node
      if (isRoot) {
        // if the size of children equals or greater than 2, then return
        if (children.size() >= 2) {
          return;
        }
        // merge the child
        BPlusNode<K, V> root = children.get(0);
        tree.setRoot(root);
        tree.setHeight(tree.getHeight() - 1);
        root.parent = null;
        root.isRoot = true;
        records = null;
        children = null;
        return;
      }
      // if current node is not root node
      // calculate the previous and next node
      int currIdx = parent.children.indexOf(this);
      int prevIdx = currIdx - 1;
      int nextIdx = currIdx + 1;
      BPlusNode<K, V> previous = null, next = null;
      if (prevIdx >= 0) {
        previous = parent.children.get(prevIdx);
      }
      if (nextIdx < parent.children.size()) {
        next = parent.children.get(nextIdx);
      }

      // If the number of child nodes of the previous node is greater than M/2 and greater than 2,
      // the borrow node
      if (previous != null
          && previous.children.size() > tree.getOrder() / 2
          && previous.children.size() > 2) {
        // add the last record of previous leaf node into first record
        int idx = previous.children.size() - 1;
        BPlusNode<K, V> borrow = previous.children.get(idx);
        previous.children.remove(idx);
        borrow.parent = this;
        children.add(0, borrow);
        int preIndex = parent.children.indexOf(previous);

        records.add(0, parent.records.get(preIndex));
        parent.records.set(preIndex, previous.records.remove(idx - 1));
        return;
      }

      // If the number of child nodes of the post node is greater than M/2 and greater than 2,
      // then borrow node
      if (next != null && next.children.size() > tree.getOrder() / 2 && next.children.size() > 2) {
        // add the first record of next leaf node into last record
        BPlusNode<K, V> borrow = next.children.get(0);
        next.children.remove(0);
        borrow.parent = this;
        children.add(borrow);
        int preIndex = parent.children.indexOf(this);
        records.add(parent.records.get(preIndex));
        parent.records.set(preIndex, next.records.remove(0));
        return;
      }

      // merge previous node
      if (previous != null
          && (previous.children.size() <= tree.getOrder() / 2 || previous.children.size() <= 2)) {
        previous.children.addAll(children);
        for (int i = 0; i < previous.children.size(); i++) {
          previous.children.get(i).parent = this;
        }
        int indexPre = parent.children.indexOf(previous);
        previous.records.add(parent.records.get(indexPre));
        previous.records.addAll(records);
        children = previous.children;
        records = previous.records;

        // update parent's records
        parent.children.remove(previous);
        previous.parent = null;
        previous.children = null;
        previous.records = null;
        parent.records.remove(parent.children.indexOf(this));
        if ((!parent.isRoot
                && (parent.children.size() >= tree.getOrder() / 2 && parent.children.size() >= 2))
            || parent.isRoot && parent.children.size() >= 2) {
          return;
        }
        parent.updateRemove(tree);
        return;
      }

      // merge next node
      if (next != null
          && (next.children.size() <= tree.getOrder() / 2 || next.children.size() <= 2)) {
        for (int i = 0; i < next.children.size(); i++) {
          BPlusNode<K, V> child = next.children.get(i);
          children.add(child);
          child.parent = this;
        }
        int index = parent.children.indexOf(this);
        records.add(parent.records.get(index));
        records.addAll(next.records);
        parent.children.remove(next);
        next.parent = null;
        next.children = null;
        next.records = null;
        parent.records.remove(parent.children.indexOf(this));
        if ((!parent.isRoot
                && (parent.children.size() >= tree.getOrder() / 2 && parent.children.size() >= 2))
            || parent.isRoot && parent.children.size() >= 2) {
          return;
        }
        parent.updateRemove(tree);
      }
    }
  }

  public V remove(K key, BPlusTree<K, V> tree) {
    // if current node is leaf node
    if (isLeaf) {
      // if not contains target key, then return null
      if (contains(key) == -1) {
        return null;
      }
      // if current node is also root node, then delete directly
      if (isRoot) {
        if (records.size() == 1) {
          tree.setHeight(0);
        }
        return remove(key);
      }
      // if the size of records of current node greater than M / 2, then delete directly
      if (records.size() > tree.getOrder() / 2 && records.size() > 2) {
        return remove(key);
      }
      // if the size of records of current node less than M / 2 and the size of records
      // of previous node greater than M / 2, then borrow node
      if (previous != null
          && previous.parent == parent
          && previous.records.size() > tree.getOrder() / 2
          && previous.records.size() > 2) {
        // add the first place
        int size = previous.records.size();
        records.add(0, previous.records.remove(size - 1));
        int index = parent.children.indexOf(previous);
        parent.records.set(index, records.get(0));
        return remove(key);
      }
      // if the size of records of current node less than M / 2 and  the size of records
      // of next node greater than M / 2, then borrow node
      if (next != null
          && next.parent == parent
          && next.records.size() > tree.getOrder() / 2
          && next.records.size() > 2) {
        records.add(next.records.remove(0));
        int index = parent.children.indexOf(this);
        parent.records.set(index, next.records.get(0));
        return remove(key);
      }
      // merge previous node
      if (previous != null
          && previous.parent == parent
          && (previous.records.size() <= tree.getOrder() / 2 || previous.records.size() <= 2)) {
        V returnValue = remove(key);
        // add the records of current node into the last place of previous node
        previous.records.addAll(records);
        records = previous.records;
        parent.children.remove(previous);
        previous.parent = null;
        previous.records = null;
        // update the link of leaf node
        if (previous.previous != null) {
          BPlusNode<K, V> temp = previous;
          temp.previous.next = this;
          previous = temp.previous;
          temp.previous = null;
          temp.next = null;
        } else {
          tree.setHead(this);
          previous.next = null;
          previous = null;
        }
        parent.records.remove(parent.children.indexOf(this));
        if ((!parent.isRoot
                && (parent.children.size() >= tree.getOrder() / 2 && parent.children.size() >= 2))
            || parent.isRoot && parent.children.size() >= 2) {
          return returnValue;
        }
        parent.updateRemove(tree);
        return returnValue;
      }
      // merge next node
      if (next != null
          && next.parent == parent
          && (next.records.size() <= tree.getOrder() / 2 || next.records.size() <= 2)) {
        V returnValue = remove(key);
        // add the records into the end of records of current node
        records.addAll(next.records);
        next.parent = null;
        next.records = null;
        parent.children.remove(next);
        // update the link of leaf node
        if (next.next != null) {
          BPlusNode<K, V> temp = next;
          temp.next.previous = this;
          next = temp.next;
          temp.previous = null;
          temp.next = null;
        } else {
          next.previous = null;
          next = null;
        }
        // update the records of parent
        parent.records.remove(parent.children.indexOf(this));
        if ((!parent.isRoot
                && (parent.children.size() >= tree.getOrder() / 2 && parent.children.size() >= 2))
            || parent.isRoot && parent.children.size() >= 2) {
          return returnValue;
        }
        parent.updateRemove(tree);
        return returnValue;
      }
    }

    // if current node is not leaf node
    if (key.compareTo(records.get(0).getKey()) < 0) {
      // if current key less than the left key, then search the first child
      return children.get(0).remove(key, tree);
    } else if (key.compareTo(records.get(records.size() - 1).getKey()) >= 0) {
      // if current key greater than the right key, then search the last child
      return children.get(children.size() - 1).remove(key, tree);
    } else {
      // otherwise, continue searching along the previous child node greater than key
      int low = 0, high = records.size() - 1, mid = 0;
      int comp;
      while (low <= high) {
        mid = (low + high) / 2;
        comp = records.get(mid).getKey().compareTo(key);
        if (comp == 0) {
          return children.get(mid + 1).remove(key, tree);
        } else if (comp < 0) {
          low = mid + 1;
        } else {
          high = mid - 1;
        }
      }
      return children.get(low).remove(key, tree);
    }
  }

  protected int contains(K key) {
    int low = 0, high = records.size() - 1, mid;
    int comp;
    while (low <= high) {
      mid = (low + high) / 2;
      comp = records.get(mid).getKey().compareTo(key);
      if (comp == 0) {
        return mid;
      } else if (comp < 0) {
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    return -1;
  }

  protected void insertOrUpdate(BPlusRecord<K, V> record) {
    // 二叉查找，插入
    int low = 0, high = records.size() - 1, mid;
    int comp;
    while (low <= high) {
      mid = (low + high) / 2;
      comp = records.get(mid).getKey().compareTo(record.getKey());
      if (comp == 0) {
        records.get(mid).copy(record);
        break;
      } else if (comp < 0) {
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    if (low > high) {
      records.add(low, record);
    }
  }

  protected V remove(K key) {
    int low = 0, high = records.size() - 1, mid;
    int comp;
    while (low <= high) {
      mid = (low + high) / 2;
      comp = records.get(mid).getKey().compareTo(key);
      if (comp == 0) {
        return records.remove(mid).getValue();
      } else if (comp < 0) {
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    return null;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("isRoot=");
    sb.append(isRoot);
    sb.append(", ");
    sb.append("isLeaf=");
    sb.append(isLeaf);
    sb.append(", ");
    sb.append("keys: ");
    for (BPlusRecord<K, V> entry : records) {
      sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
    }
    return sb.toString();
  }

  public void print(int index) {
    StringBuilder prefix = new StringBuilder();
    for (int i = 0; i < index; i++) {
      prefix.append(" ");
    }
    if (records.size() != 0) {
      if (this.isLeaf) {
        System.out.printf("%s[Leaf-%d]:", prefix, index);
        for (BPlusRecord<K, V> entry : records) {
          System.out.print(entry + " ");
        }
        System.out.println();
      } else {
        System.out.printf("%s[Node-%d]:", prefix, index);
        for (BPlusRecord<K, V> entry : records) {
          System.out.print(entry + " ");
        }
        System.out.println();
        for (BPlusNode<K, V> child : children) {
          child.print(index + 1);
        }
      }
    }
  }
}

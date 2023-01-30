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

public class LSMTreeDemo {

  private static final LSMTree<Integer, Integer> tree = new LSMTree<>(6, 2, 3);

  public static void main(String[] args) {
    System.out.println("--------------------------------");
    System.out.println("初始化树，并插入到5:");
    System.out.println("--------------------------------");
    for (int i = 1; i <= 5; i++) {
      tree.insert(i, i * 2);
    }
    tree.print();
    System.out.println("--------------------------------");
    System.out.println("插入6:");
    System.out.println("--------------------------------");
    tree.insert(6, 12);
    tree.print();
    System.out.println("--------------------------------");
    System.out.println("插入到11:");
    System.out.println("--------------------------------");
    for (int i = 7; i < 12; i++) {
      tree.insert(i, i * 2);
    }
    tree.print();
    System.out.println("--------------------------------");
    System.out.println("插入12:");
    System.out.println("--------------------------------");
    tree.insert(12, 24);
    tree.print();
    System.out.println("--------------------------------");
    System.out.println("插到18:");
    System.out.println("--------------------------------");
    for (int i = 13; i < 19; i++) {
      tree.insert(i, i * 2);
    }
    tree.print();
    System.out.println("--------------------------------");
    System.out.println("删除5:");
    System.out.println("--------------------------------");
    tree.remove(5);
    tree.print();
    System.out.println("查询5:" + tree.get(5));
    System.out.println("--------------------------------");
    System.out.println("插入到23:");
    System.out.println("--------------------------------");
    for (int i = 19; i < 24; i++) {
      tree.insert(i, i * 2);
    }
    tree.print();
    System.out.println("查询5:" + tree.get(5));
  }
}

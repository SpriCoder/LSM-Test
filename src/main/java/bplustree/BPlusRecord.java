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

public class BPlusRecord<K extends Comparable<K>, V> {
  private final K key;
  private V value;
  private boolean deleted;

  public BPlusRecord(K key, boolean deleted) {
    this.key = key;
    this.value = null;
    this.deleted = deleted;
  }

  public BPlusRecord(K key, V value) {
    this.key = key;
    this.value = value;
    this.deleted = false;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void copy(BPlusRecord<K, V> newRecord) {
    this.value = newRecord.getValue();
    this.deleted = newRecord.isDeleted();
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    if (deleted) {
      stringBuilder.append(key).append("(delete)");
    } else {
      stringBuilder.append(key).append("=").append(value);
    }
    return stringBuilder.toString();
  }
}

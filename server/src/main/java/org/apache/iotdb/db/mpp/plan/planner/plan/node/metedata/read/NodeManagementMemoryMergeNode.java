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

package org.apache.iotdb.db.mpp.plan.planner.plan.node.metedata.read;

import org.apache.iotdb.db.mpp.plan.planner.plan.node.PlanNode;
import org.apache.iotdb.db.mpp.plan.planner.plan.node.PlanNodeId;
import org.apache.iotdb.db.mpp.plan.planner.plan.node.PlanNodeType;
import org.apache.iotdb.db.mpp.plan.planner.plan.node.PlanVisitor;
import org.apache.iotdb.db.mpp.plan.planner.plan.node.process.ProcessNode;
import org.apache.iotdb.tsfile.utils.ReadWriteIOUtils;

import com.google.common.collect.ImmutableList;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NodeManagementMemoryMergeNode extends ProcessNode {
  private final Set<String> data;

  private PlanNode child;

  public NodeManagementMemoryMergeNode(PlanNodeId id, Set<String> data) {
    super(id);
    this.data = data;
  }

  public Set<String> getData() {
    return data;
  }

  public PlanNode getChild() {
    return child;
  }

  @Override
  public List<PlanNode> getChildren() {
    return ImmutableList.of(child);
  }

  @Override
  public void addChild(PlanNode child) {
    this.child = child;
  }

  @Override
  public PlanNode clone() {
    return new NodeManagementMemoryMergeNode(getPlanNodeId(), this.data);
  }

  @Override
  public int allowedChildCount() {
    return ONE_CHILD;
  }

  @Override
  public List<String> getOutputColumnNames() {
    return child.getOutputColumnNames();
  }

  @Override
  public <R, C> R accept(PlanVisitor<R, C> visitor, C context) {
    return visitor.visitNodeManagementMemoryMerge(this, context);
  }

  @Override
  protected void serializeAttributes(ByteBuffer byteBuffer) {
    PlanNodeType.NODE_MANAGEMENT_MEMORY_MERGE.serialize(byteBuffer);
    int size = data.size();
    ReadWriteIOUtils.write(size, byteBuffer);
    data.forEach(node -> ReadWriteIOUtils.write(node, byteBuffer));
  }

  public static NodeManagementMemoryMergeNode deserialize(ByteBuffer byteBuffer) {
    Set<String> data = new HashSet<>();
    int size = byteBuffer.getInt();
    for (int i = 0; i < size; i++) {
      data.add(ReadWriteIOUtils.readString(byteBuffer));
    }
    PlanNodeId planNodeId = PlanNodeId.deserialize(byteBuffer);
    return new NodeManagementMemoryMergeNode(planNodeId, data);
  }
}

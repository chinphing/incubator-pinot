/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.spi.config.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import org.apache.pinot.spi.config.BaseJsonConfig;

import java.util.List;
import javax.annotation.Nullable;


public class StarTreeIndexConfig extends BaseJsonConfig {
  private final List<String> _dimensionsSplitOrder;
  private final List<String> _skipStarNodeCreationForDimensions;
  private final List<String> _functionColumnPairs;
  private final int _maxLeafRecords;

  @JsonCreator
  public StarTreeIndexConfig(
      @JsonProperty(value = "dimensionsSplitOrder", required = true) List<String> dimensionsSplitOrder,
      @JsonProperty("skipStarNodeCreationForDimensions") @Nullable List<String> skipStarNodeCreationForDimensions,
      @JsonProperty(value = "functionColumnPairs", required = true) List<String> functionColumnPairs,
      @JsonProperty("maxLeafRecords") int maxLeafRecords) {
    Preconditions.checkArgument(dimensionsSplitOrder != null, "'dimensionsSplitOrder' must be configured");
    Preconditions.checkArgument(functionColumnPairs != null, "'functionColumnPairs' must be configured");
    _dimensionsSplitOrder = dimensionsSplitOrder;
    _skipStarNodeCreationForDimensions = skipStarNodeCreationForDimensions;
    _functionColumnPairs = functionColumnPairs;
    _maxLeafRecords = maxLeafRecords;
  }

  public List<String> getDimensionsSplitOrder() {
    return _dimensionsSplitOrder;
  }

  @Nullable
  public List<String> getSkipStarNodeCreationForDimensions() {
    return _skipStarNodeCreationForDimensions;
  }

  public List<String> getFunctionColumnPairs() {
    return _functionColumnPairs;
  }

  public int getMaxLeafRecords() {
    return _maxLeafRecords;
  }
}

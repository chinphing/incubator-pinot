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
package org.apache.pinot.core.query.aggregation.function;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.pinot.common.function.AggregationFunctionType;
import org.apache.pinot.common.request.transform.TransformExpressionTree;
import org.apache.pinot.common.utils.DataSchema.ColumnDataType;
import org.apache.pinot.core.common.BlockValSet;
import org.apache.pinot.core.query.aggregation.AggregationResultHolder;
import org.apache.pinot.core.query.aggregation.ObjectAggregationResultHolder;
import org.apache.pinot.core.query.aggregation.groupby.GroupByResultHolder;
import org.apache.pinot.core.query.aggregation.groupby.ObjectGroupByResultHolder;


public class PercentileAggregationFunction implements AggregationFunction<DoubleArrayList, Double> {
  private static final double DEFAULT_FINAL_RESULT = Double.NEGATIVE_INFINITY;

  protected final int _percentile;
  protected final String _column;
  private final List<TransformExpressionTree> _inputExpressions;

  /**
   * Constructor for the class.
   *
   * @param arguments List of arguments.
   *                  <ul>
   *                  <li> Arg 0: Column name to aggregate.</li>
   *                  <li> Arg 1: Percentile to compute. </li>
   *                  </ul>
   */
  public PercentileAggregationFunction(List<String> arguments) {
    int numArgs = arguments.size();
    Preconditions.checkArgument(numArgs == 2, getType() + " expects two argument, got: " + numArgs);

    _column = arguments.get(0);
    _percentile = AggregationFunctionUtils.parsePercentile(arguments.get(1));
    _inputExpressions = Collections.singletonList(TransformExpressionTree.compileToExpressionTree(_column));
  }

  @Override
  public AggregationFunctionType getType() {
    return AggregationFunctionType.PERCENTILE;
  }

  @Override
  public String getColumnName() {
    return getType().getName() + _percentile + "_" + _column;
  }

  @Override
  public String getResultColumnName() {
    return getType().getName().toLowerCase() + _percentile + "(" + _column + ")";
  }

  @Override
  public List<TransformExpressionTree> getInputExpressions() {
    return _inputExpressions;
  }

  @Override
  public void accept(AggregationFunctionVisitorBase visitor) {
    visitor.visit(this);
  }

  @Override
  public AggregationResultHolder createAggregationResultHolder() {
    return new ObjectAggregationResultHolder();
  }

  @Override
  public GroupByResultHolder createGroupByResultHolder(int initialCapacity, int maxCapacity) {
    return new ObjectGroupByResultHolder(initialCapacity, maxCapacity);
  }

  @Override
  public void aggregate(int length, AggregationResultHolder aggregationResultHolder, Map<String, BlockValSet> blockValSetMap) {
    DoubleArrayList valueList = getValueList(aggregationResultHolder);
    double[] valueArray = blockValSetMap.get(_column).getDoubleValuesSV();
    for (int i = 0; i < length; i++) {
      valueList.add(valueArray[i]);
    }
  }

  @Override
  public void aggregateGroupBySV(int length, int[] groupKeyArray, GroupByResultHolder groupByResultHolder,
      Map<String, BlockValSet> blockValSetMap) {
    double[] valueArray = blockValSetMap.get(_column).getDoubleValuesSV();
    for (int i = 0; i < length; i++) {
      DoubleArrayList valueList = getValueList(groupByResultHolder, groupKeyArray[i]);
      valueList.add(valueArray[i]);
    }
  }

  @Override
  public void aggregateGroupByMV(int length, int[][] groupKeysArray, GroupByResultHolder groupByResultHolder,
      Map<String, BlockValSet> blockValSetMap) {
    double[] valueArray = blockValSetMap.get(_column).getDoubleValuesSV();
    for (int i = 0; i < length; i++) {
      double value = valueArray[i];
      for (int groupKey : groupKeysArray[i]) {
        DoubleArrayList valueList = getValueList(groupByResultHolder, groupKey);
        valueList.add(value);
      }
    }
  }

  @Override
  public DoubleArrayList extractAggregationResult(AggregationResultHolder aggregationResultHolder) {
    DoubleArrayList doubleArrayList = aggregationResultHolder.getResult();
    if (doubleArrayList == null) {
      return new DoubleArrayList();
    } else {
      return doubleArrayList;
    }
  }

  @Override
  public DoubleArrayList extractGroupByResult(GroupByResultHolder groupByResultHolder, int groupKey) {
    DoubleArrayList doubleArrayList = groupByResultHolder.getResult(groupKey);
    if (doubleArrayList == null) {
      return new DoubleArrayList();
    } else {
      return doubleArrayList;
    }
  }

  @Override
  public DoubleArrayList merge(DoubleArrayList intermediateResult1, DoubleArrayList intermediateResult2) {
    intermediateResult1.addAll(intermediateResult2);
    return intermediateResult1;
  }

  @Override
  public boolean isIntermediateResultComparable() {
    return false;
  }

  @Override
  public ColumnDataType getIntermediateResultColumnType() {
    return ColumnDataType.OBJECT;
  }

  @Override
  public ColumnDataType getFinalResultColumnType() {
    return ColumnDataType.DOUBLE;
  }

  @Override
  public Double extractFinalResult(DoubleArrayList intermediateResult) {
    int size = intermediateResult.size();
    if (size == 0) {
      return DEFAULT_FINAL_RESULT;
    } else {
      double[] values = intermediateResult.elements();
      Arrays.sort(values, 0, size);
      if (_percentile == 100) {
        return values[size - 1];
      } else {
        return values[(int) ((long) size * _percentile / 100)];
      }
    }
  }

  /**
   * Returns the value list from the result holder or creates a new one if it does not exist.
   *
   * @param aggregationResultHolder Result holder
   * @return Value list from the result holder
   */
  protected static DoubleArrayList getValueList(AggregationResultHolder aggregationResultHolder) {
    DoubleArrayList valueList = aggregationResultHolder.getResult();
    if (valueList == null) {
      valueList = new DoubleArrayList();
      aggregationResultHolder.setValue(valueList);
    }
    return valueList;
  }

  /**
   * Returns the value list for the given group key. If one does not exist, creates a new one and returns that.
   *
   * @param groupByResultHolder Result holder
   * @param groupKey Group key for which to return the value list
   * @return Value list for the group key
   */
  protected static DoubleArrayList getValueList(GroupByResultHolder groupByResultHolder, int groupKey) {
    DoubleArrayList valueList = groupByResultHolder.getResult(groupKey);
    if (valueList == null) {
      valueList = new DoubleArrayList();
      groupByResultHolder.setValueForKey(groupKey, valueList);
    }
    return valueList;
  }
}

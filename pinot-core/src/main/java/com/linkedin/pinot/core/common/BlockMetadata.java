/**
 * Copyright (C) 2014-2015 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.pinot.core.common;

import com.linkedin.pinot.common.data.FieldSpec.DataType;


public interface BlockMetadata {

  int getSize();

  //additional info about the docIdSet
  int getLength();

  int getStartDocId();

  int getEndDocId();

  //DocId set properties

  boolean isSorted();

  boolean isSparse();

  boolean hasInvertedIndex();

  boolean hasDictionary();

  boolean isSingleValue();

  com.linkedin.pinot.core.segment.index.readers.Dictionary getDictionary();

  int getMaxNumberOfMultiValues();

  DataType getDataType();

  //boolean getForwardIndexCompressionType();

  //boolean getInvertedIndexCompressionType();

}

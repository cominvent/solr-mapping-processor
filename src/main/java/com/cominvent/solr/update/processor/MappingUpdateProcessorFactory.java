package com.cominvent.solr.update.processor;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
import org.apache.solr.util.plugin.SolrCoreAware;

/**
 * Reads a field's value and maps it to another value based on a dictionary
 * The dictionary is an external file in the same format as Solr's synonym.txt
 */
public class MappingUpdateProcessorFactory extends
    UpdateRequestProcessorFactory implements SolrCoreAware {

  private boolean enabled;
  SolrParams params;
  private SolrCore core;

  @SuppressWarnings("unchecked")
  @Override
  public void init(final NamedList args) {
    if (args != null) {
      params = SolrParams.toSolrParams(args);
      setEnabled(params.getBool("enabled", true));
    }
  }

  public UpdateRequestProcessor getInstance(SolrQueryRequest solrQueryRequest, SolrQueryResponse solrQueryResponse, UpdateRequestProcessor updateRequestProcessor) {
    return new MappingUpdateProcessor(core, params, solrQueryRequest, solrQueryResponse, updateRequestProcessor);
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void inform(SolrCore core) {
    this.core = core;
  }
}

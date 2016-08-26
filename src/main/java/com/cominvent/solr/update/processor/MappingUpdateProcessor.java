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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a field's value and maps it to another value based on a dictionary
 * The dictionary is an external file in the same format as Solr's synonym.txt
 * 
 * Sample Configuration in UpdateProcessorChain:
 * <pre>
 * &lt;processor class="MappingUpdateProcessorFactory"&gt;
 *   &lt;str name="inputField"&gt;myInField&lt;/str&gt;
 *   &lt;str name="outputField"&gt;MyOutField&lt;/str&gt;
 *   &lt;str name="idField"&gt;id&lt;/str&gt;
 *   &lt;str name="fallback"&gt;&lt;/str&gt;
 *   &lt;str name="mappingFile"&gt;mappings.txt&lt;/str&gt;
 * &lt;/processor&gt;
 * </pre>
 */
public class MappingUpdateProcessor extends UpdateRequestProcessor {

  protected final static Logger log = LoggerFactory
      .getLogger(MappingUpdateProcessor.class);

  private static final String INPUT_FIELD_PARAM = "inputField";
  private static final String OUTPUT_FIELD_PARAM = "outputField";
  private static final String MAPPING_FILE_PARAM = "mappingFile";
  private static final String FALLBACK_VALUE_PARAM = "fallback";
  private static final String DOCID_PARAM = "idField";

  private static final String DOCID_FIELD_DEFAULT = "id";

  private boolean enabled;

  String inputField;
  String outputField;
  String fallbackValue;
  String docIdField;
  String mappingFile;
  
  SolrCore core;
  
  HashMap<String,String> map = new HashMap<String,String>();

  /**
   * The constructor initializes the processor by reading configuration
   * and loading a HashMap from the specified file name.
   */
  public MappingUpdateProcessor(SolrCore core, SolrParams params,
                                SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
    super(next);

    this.core = core;
    
    // TODO: Add support for caseSensitive and expand parameters, support full synonyms.txt format
    if (params != null) {
      setEnabled(params.getBool("enabled", true));

      inputField = params.get(INPUT_FIELD_PARAM, "").trim();
      outputField = params.get(OUTPUT_FIELD_PARAM, "").trim();
      docIdField = params.get(DOCID_PARAM, DOCID_FIELD_DEFAULT);
      fallbackValue = params.get(FALLBACK_VALUE_PARAM, null);
      mappingFile = params.get(MAPPING_FILE_PARAM, "").trim();
    }

    if (inputField.length() == 0) {
      log
          .error("Missing or faulty configuration of MappingUpdateProcessor. Input field must be specified");
      throw new SolrException(
          ErrorCode.NOT_FOUND,
          "Missing or faulty configuration of MappingUpdateProcessor. Input field must be specified");
    }
    
    try {
      log.info("Attempting to initialize mapping file "+mappingFile);
      InputStream is = core.getResourceLoader().openResource(mappingFile);
      BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      String line;
      while((line = br.readLine()) != null) {
        if(line.startsWith("#")) continue;
        String[] kv = line.split("=>");
        if(kv.length < 2) continue;
        map.put(kv[0].trim(), kv[1].trim());
      }
      log.info("Map initialized from "+mappingFile+": "+map);
    }catch (Exception e) {
      throw new SolrException(ErrorCode.NOT_FOUND, "Error when reading mapping file "+mappingFile+".");
    } 
  }

  @Override
  public void processAdd(AddUpdateCommand cmd) throws IOException {
    if (isEnabled()) {

      SolrInputDocument doc = cmd.getSolrInputDocument();

      // Fetch document id
      String docId = "";
      if (doc.containsKey(docIdField))
        docId = (String) doc.getFieldValue(docIdField);

      String inValue = (String) doc.getFieldValue(inputField);
      String outValue;
      if(map.containsKey(inValue)) {
        outValue = map.get(inValue); 
      } else {
        outValue = fallbackValue;
      }

      if(outValue != null && outValue.length() > 0) {
        log.debug("Mapping done for document "+docId+": " + inValue + " => " + outValue);
        doc.setField(outputField, outValue);
      }

    } else {
      log.debug("MappingUpdateProcessor is not enabled. Skipping");
    }
    
    super.processAdd(cmd);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

}

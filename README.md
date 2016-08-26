# Solr UpdateProcessors

[![Build Status](https://travis-ci.org/cominvent/solr-update-processors.svg?branch=master)](https://travis-ci.org/cominvent/solr-update-processors)

## MappingUpdateProcessor

This processor can map values of an input field to another value in the output field.
Config:

    <processor class="MappingUpdateProcessorFactory">
      <str name="inputField">myInField</str>
      <str name="outputField">MyOutField</str>
      <str name="idField">id</str>
      <str name="fallback"></str>
      <str name="mappingFile">mappings.txt</str>
    </processor>
    
The mapping file has same syntax as synonyms.txt, e.g. 

    fromValue => toValue
    colour => color
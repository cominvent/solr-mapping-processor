# Solr Mapping Update Processor

[![Build Status](https://travis-ci.org/cominvent/solr-update-processors.svg?branch=master)](https://travis-ci.org/cominvent/solr-update-processors)

This processor can map values of an input field to another value in the output field.

## Build

Build with maven:

    mvn package

Copy the jar to a place where Solr can find it:

    SOLR_HOME=/path/to/solr/home
    mkdir $SOLR_HOME/lib
    cp target/mapping-processor-1.1.0.jar $SOLR_HOME/lib/

## Install

**NB:** works only with [unreleased build](http://people.apache.org/~janhoy/dist/), see https://s.apache.org/solr-plugin:

    bin/solr plugin repo add cominvent https://github.com/cominvent/solr-plugins
    bin/solr plugin install mapping-processor

OR the old way:

Download a pre-built jar from [releases](https://github.com/cominvent/solr-mapping-processor/releases) section.
and drop it in your `$SOLR_HOME/lib/`

### solrconfig.xml

Define the Processor:

    <updateProcessor class="MappingUpdateProcessorFactory" name="mapping-processor>
      <str name="inputField">myInField</str>
      <str name="outputField">MyOutField</str>
      <str name="idField">id</str>
      <str name="fallback"></str>
      <str name="mappingFile">mappings.txt</str>
    </processor>

The mapping file has same syntax as synonyms.txt, e.g. 

    fromValue => toValue
    colour => color

Now you can refer to this processor by name in either an update chain
or directly on the request, e.g. http://localhost:8983/solr/foo/update?processor=mapping-processor
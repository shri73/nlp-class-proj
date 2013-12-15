#!/bin/bash

MEMORY="2g"

java -classpath target/classes:res/factorie-class-proj-1.0-SNAPSHOT.jar:$FAC_CP:$NER_RES_CP:$POS_CP:$PARSE_CP:$LEXICON_CP:$WORDNET_CP:$NER_CP -Xmx$MEMORY -Dfile.encoding=UTF-8 edu.umass.cs.iesl.strubell.ErrorAnalyzer 


#!/bin/bash
#
# Benchmark Factorie ForwardPOSTagger on Ontonotes test data
#
# First argument is the directory where models can be found
# Second argument is the directory where data (ontonotes-en-1.1.0) can be found
#
MEMORY="2g"


java -classpath target/classes:res/factorie-class-proj-1.0-SNAPSHOT.jar:$FAC_CP:$HOME/.m2/repository/cc/factorie/app/nlp/pos/1.0-RC5/pos-1.0-RC5.jar:$HOME/.m2/repository/cc/factorie/app/nlp/parse/1.0-RC5/parse-1.0-RC5.jar:$HOME/.m2/repository/cc/factorie/app/nlp/ner/1.0-RC5/ner-1.0-RC5.jar:$HOME/.m2/repository/cc/factorie/app/nlp/mention/1.0-RC5/mention-1.0-RC5.jar:$HOME/.m2/repository/cc/factorie/app/nlp/coref/1.0-RC5/coref-1.0-RC5.jar:$HOME/.m2/repository/cc/factorie/app/nlp/lexicon/1.0-RC5/lexicon-1.0-RC5.jar:$HOME/.m2/repository/cc/factorie/app/nlp/wordnet/1.0-RC5/wordnet-1.0-RC5.jar -Xmx$MEMORY -Dfile.encoding=UTF-8 edu.umass.cs.iesl.strubell.ErrorAnalyzer 


#!/bin/bash
#
#
#
MODEL_DIR="file:///Users/strubell/Documents/research/models"
#MODEL_DIR=""
POS_MODEL="OntonotesForwardPosTagger.factorie"
PARSE_MODEL="OntonotesTransitionBasedParser.factorie"
MEMORY="2g"

pos="$HOME/.m2/repository/cc/factorie/app/nlp/pos/1.0-RC5/pos-1.0-RC5.jar"
parse="$HOME/.m2/repository/cc/factorie/app/nlp/parse/1.0-RC5/parse-1.0-RC5.jar"
wordnet="$HOME/.m2/repository/cc/factorie/app/nlp/wordnet/1.0-RC5/wordnet-1.0-RC5.jar"

java -classpath $pos:$parse:$wordnet:res/factorie-class-proj-1.0-SNAPSHOT.jar:$FAC_CP:$HOME/.m2/repository/cc/factorie/app/nlp/factorie-nlp-resources-ner/0.1-SNAPSHOT/factorie-nlp-resources-ner-0.1-SNAPSHOT.jar -Xmx$MEMORY -ea -Djava.awt.headless=true -Dfile.encoding=UTF-8 -server cc.factorie.app.nlp.NLP --ontonotes-forward-pos --transition-based-parser=$MODEL_DIR/$PARSE_MODEL


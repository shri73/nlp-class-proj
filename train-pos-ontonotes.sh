#!/bin/bash
#
# Train a Factorie ForwardPOSTagger using Onotnotes training data
# Specify directory containing data (ontonotes-en-1.1.0) as command line paramter
#

MEMORY=16g
MODELNAME="OntonotesForwardPosTagger.factorie"

#trainDir="--trainDir=/iesl/canvas/jdchoi/clearnlp/dat/general-2.0.1/trn-pmd/"
#testDir="--testDir=/iesl/canvas/jdchoi/clearnlp/dat/general-2.0.1/dev-pmd/"
trainDir="--trainDir=/iesl/canvas/strubell/data/ontonotes-en-1.1.0/trn-pmd/"
testDir="--testDir=/iesl/canvas/strubell/data/ontonotes-en-1.1.0/dev-pmd/"
l1="--l1=0.07379024017066312"
l2="--l2=1.1481182114789412E-8"
hinge="--use-hinge-loss=false"
rate="--rate=1.1296435034705585E-4"
#rate="--rate=0.01"
delta="--delta=6.090780307103741"
#delta="--delta=0.1"
save="--save-model=true"
iters="--num-iterations=3"
cutoff="--cutoff=2"
model="--model=$MODELNAME"

wordnet="$HOME/.m2/repository/cc/factorie/app/nlp/wordnet/1.0-RC5/wordnet-1.0-RC5.jar"
lexicon="$HOME/.m2/repository/cc/factorie/app/nlp/lexicon/1.0-RC5/lexicon-1.0-RC5.jar"
pos="$HOME/.m2/repository/cc/factorie/app/nlp/pos/1.0-RC5/pos-1.0-RC5.jar"
ner="$HOME/.m2/repository/cc/factorie/app/nlp/ner/1.0-RC5/ner-1.0-RC5.jar"
nerres="$HOME/.m2/repository/cc/factorie/app/nlp/factorie-nlp-resources-ner/0.1-SNAPSHOT/factorie-nlp-resources-ner-0.1-SNAPSHOT.jar"

java -classpath res/factorie-class-proj-1.0-NER2POS.jar:$ner:$nerres:$wordnet:$lexicon:$HOME/.m2/repository/org/scala-lang/scala-library/2.10.1/scala-library-2.10.1.jar:$HOME/.m2/repository/com/typesafe/akka/akka-actor_2.10/2.1.4/akka-actor_2.10-2.1.4.jar:$HOME/.m2/repository/com/typesafe/config/1.0.0/config-1.0.0.jar:$HOME/.m2/repository/org/scala-lang/scala-compiler/2.10.1/scala-compiler-2.10.1.jar:$HOME/.m2/repository/org/scala-lang/scala-reflect/2.10.1/scala-reflect-2.10.1.jar:$HOME/.m2/repository/junit/junit/4.10/junit-4.10.jar:$HOME/.m2/repository/org/hamcrest/hamcrest-core/1.1/hamcrest-core-1.1.jar:$HOME/.m2/repository/com/fasterxml/jackson/module/jackson-module-scala_2.10/2.2.2/jackson-module-scala_2.10-2.2.2.jar:$HOME/.m2/repository/com/thoughtworks/paranamer/paranamer/2.3/paranamer-2.3.jar:$HOME/.m2/repository/com/google/code/findbugs/jsr305/2.0.1/jsr305-2.0.1.jar:$HOME/.m2/repository/com/google/guava/guava/13.0.1/guava-13.0.1.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.2.2/jackson-databind-2.2.2.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.2.2/jackson-annotations-2.2.2.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.2.2/jackson-core-2.2.2.jar:$HOME/.m2/repository/org/scalatest/scalatest_2.10/1.9.1/scalatest_2.10-1.9.1.jar:$HOME/.m2/repository/org/scala-lang/scala-actors/2.10.0/scala-actors-2.10.0.jar:$HOME/.m2/repository/org/mongodb/mongo-java-driver/2.11.1/mongo-java-driver-2.11.1.jar:$HOME/.m2/repository/net/sourceforge/jregex/jregex/1.2_01/jregex-1.2_01.jar:$HOME/.m2/repository/org/jblas/jblas/1.2.3/jblas-1.2.3.jar:$HOME/.m2/repository/org/apache/commons/commons-lang3/3.1/commons-lang3-3.1.jar -Xmx$MEMORY cc.factorie.app.nlp.pos.ForwardPosTrainer $trainDir $testDir $l1 $l2 $hinge $rate $delta $save $iters $cutoff $model

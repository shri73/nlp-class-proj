package edu.umass.cs.iesl.strubell

import cc.factorie._
import cc.factorie.util.FileUtils
import cc.factorie.app.nlp.load._
import java.io._

object ErrorAnalyzer {
  def main(args: Array[String]) {
    
	println("Loading docs...")
	
	val ontonotesTestFile = "data/nw-wsj-23.dep.pmd"
	val ontonotesTestDoc = LoadOntonotes5.fromFilename(ontonotesTestFile).head
	  
	val conllTestFile = "data/conll2003/eng.testa"
	val conllTestDoc = LoadConll2003.fromFilename(conllTestFile).head
	
	/* Load serialized models */
	println("Loading models... ")
	
	//print("\tparser: ")
    //val parser = app.nlp.parse.OntonotesTransitionBasedParser
	//val parserFile = "models/OntonotesTransitionBasedParser.factorie"
	//val parser = new app.nlp.parse.TransitionBasedParser(new File(parserFile))

	//print("\tpos: ")
	//val tagger = app.nlp.pos.OntonotesForwardPosTagger
	
	val namedent = app.nlp.ner.ConllStackedChainNer
	
    /* Run the pipeline */
    print("Running pipeline... ")
    val pipeline = app.nlp.DocumentAnnotatorPipeline(namedent)
    pipeline.process(conllTestDoc)
    
    /* Generate nicely formatted error output */
    println("Generating output...")
    //OutputFormatter.generateTikzFiles(doc, "mark_prep")
    //OutputFormatter.generateConfusionDataPos(doc)
    OutputFormatter.generateConfusionDataNer(conllTestDoc)
  }
}
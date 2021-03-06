package edu.umass.cs.iesl.strubell

import cc.factorie._
import cc.factorie.util.FileUtils
import cc.factorie.app.nlp.load._
import java.io._

object ErrorAnalyzer {
  def main(args: Array[String]) {
    
	println("Loading docs...")
	
	//val ontonotesDir = "/home/strubell/canvas/data/general-2.0.1/dev-pmd"
	val ontonotesDir = "/Users/strubell/Documents/research/data/general-2.0.1/dev-pmd"
	val ontonotesTestFiles = FileUtils.getFileListFromDir(ontonotesDir)
	val ontonotesTestDocs = ontonotesTestFiles.map(f => {LoadOntonotes5.fromFilename(f).head})
	  
	val conllTestFile = "data/conll2003/eng.testa"
	val conllTestDoc = LoadConll2003.fromFilename(conllTestFile).head
	
	/* Load serialized models */
	println("Loading models... ")
	
	//print("\tparser: ")
    //val parser = app.nlp.parse.OntonotesTransitionBasedParser
	//val parserFile = "models/OntonotesTransitionBasedParser.factorie"
	//val parser = new app.nlp.parse.TransitionBasedParser(new File(parserFile))

	//print("\tpos: ")
	val tagger = app.nlp.pos.OntonotesForwardPosTagger
	
	//val namedent = app.nlp.ner.ConllStackedChainNer
	
    /* Run the pipeline */
    print("Running pipeline... ")
    val pipeline = new app.nlp.DocumentAnnotationPipeline(Seq(tagger))
    ontonotesTestDocs.map(doc => pipeline.process(doc))
    
    //val ontonotesTestDoc = ontonotesTestDocs.head
    //val processedDoc = pipeline.process(ontonotesTestDoc)
    println(pipeline.profileReport)
    
    /* Generate nicely formatted error output */
    println("Generating output...")
    //OutputFormatter.generateTikzFiles(doc, "mark_prep")
    OutputFormatter.generateConfusionDataPos(ontonotesTestDocs)
    //OutputFormatter.generateConfusionDataPos(ontonotesTestDoc)
    //OutputFormatter.generateConfusionDataNer(conllTestDoc)
  }
}
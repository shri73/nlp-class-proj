package edu.umass.cs.iesl.strubell

import cc.factorie._
import cc.factorie.util.FileUtils
import cc.factorie.app.nlp.load._
import java.io._

object ErrorAnalyzer {
  def main(args: Array[String]) {
	println("Loading doc...")
	val testDoc = "data/nw-wsj-23.dep.pmd"
	val doc = LoadOntonotes5.fromFilename(testDoc).head
	  
	/* Load serialized models */
	println("Loading models... ")
	
	print("\tparser: ")
    //val parser = app.nlp.parse.OntonotesTransitionBasedParser
	val parserFile = "models/OntonotesTransitionBasedParser.factorie"
	val parser = new app.nlp.parse.TransitionBasedParser(new File(parserFile))

    /* Run the pipeline */
    print("Running pipeline... ")
    val pipeline = app.nlp.DocumentAnnotatorPipeline(parser)
    pipeline.process(doc)
    
    /* Generate nicely formatted error output */
    println("Generating output...")
    OutputFormatter.generateTikzFiles(doc)
  }
}
package edu.umass.cs.iesl.strubell

import cc.factorie._
import java.net.Socket
import java.io._
import cc.factorie.app.nlp.Section
import cc.factorie.util.FileUtils
import cc.factorie.app.nlp.load._
import scala.collection.mutable.ArrayBuffer
import cc.factorie.app.nlp.Sentence
import cc.factorie.app.nlp.coref._

object CorefPipeline {
  def main(args: Array[String]) {
	  println("Loading doc...")
	  val testDoc = "/Users/strubell/Documents/research/data/conll2011/conll-test-clean.txt"
	  val doc = ConllCorefLoader.loadWithParse(testDoc).head
	  
	  /* Load serialized models */
	  println("Loading models... ")
	  //val modelLoc = args(0)
	
	  // POS tagger
	  print("\tpos: ")
	  var t0 = System.currentTimeMillis()
	  val tagger = app.nlp.pos.OntonotesForwardPosTagger
	  //tagger.deserialize(new java.io.File(s"$modelLoc/OntonotesForwardPosTagger.factorie"))
	  println(s"${System.currentTimeMillis() - t0}ms")
	
	  // dependency parser
	  print("\tparse: ")
	  t0 = System.currentTimeMillis()
	  val parser = app.nlp.parse.OntonotesTransitionBasedParser
	  //parser.deserialize(new java.io.File(s"$modelLoc/OntonotesTransitionBasedParser.factorie"))
	  println(s"${System.currentTimeMillis() - t0}ms")
	
	  print("\tner: ")
	  t0 = System.currentTimeMillis()
	  val ner = app.nlp.ner.ConllStackedChainNer
	  //parser.deserialize(new java.io.File(s"$modelLoc/OntonotesTransitionBasedParser.factorie"))
	  println(s"${System.currentTimeMillis() - t0}ms")
	  
	  print("\tmention (gender): ")
	  t0 = System.currentTimeMillis()
	  val mentionGender = app.nlp.mention.MentionGenderLabeler
	  //parser.deserialize(new java.io.File(s"$modelLoc/OntonotesTransitionBasedParser.factorie"))
	  println(s"${System.currentTimeMillis() - t0}ms")
	  
	  print("\tmention (number): ")
	  t0 = System.currentTimeMillis()
	  val mentionNumber = app.nlp.mention.MentionNumberLabeler
	  //parser.deserialize(new java.io.File(s"$modelLoc/OntonotesTransitionBasedParser.factorie"))
	  println(s"${System.currentTimeMillis() - t0}ms")
	  
	  print("\tmention (entity type): ")
	  t0 = System.currentTimeMillis()
	  val mentionEntityType = app.nlp.mention.MentionEntityTypeLabeler
	  //parser.deserialize(new java.io.File(s"$modelLoc/OntonotesTransitionBasedParser.factorie"))
	  println(s"${System.currentTimeMillis() - t0}ms")
	  
	  print("\tcoref: ")
	  t0 = System.currentTimeMillis()
	  val coref = app.nlp.coref.ForwardCoref
	  //parser.deserialize(new java.io.File(s"$modelLoc/OntonotesTransitionBasedParser.factorie"))
	  println(s"${System.currentTimeMillis() - t0}ms")
	  
	  /* Run the pipeline */
	  print("Running pipeline... ")
	  t0 = System.currentTimeMillis()
	  val annotators = Seq(coref)//Seq(tagger, parser, ner, mentionGender, mentionNumber, mentionEntityType, coref)
	  val pipeline = app.nlp.DocumentAnnotatorPipeline(coref)
	  pipeline.process(doc)
	  println(s"${System.currentTimeMillis() - t0}ms")
	
	  /* Print formatted results */
	  println("Results: ")
	  val printers = for (ann <- pipeline.annotators) yield (t: app.nlp.Token) => ann.tokenAnnotationString(t)
	  println(doc.owplString(printers))
  }
}
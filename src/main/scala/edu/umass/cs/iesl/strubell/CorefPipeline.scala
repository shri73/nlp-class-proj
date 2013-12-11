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
import cc.factorie.app.nlp.ner._

object CorefPipeline {
  def main(args: Array[String]) {
	  println("Loading doc...")
	  val testDoc = "data/nw-wsj-23.dep.pmd"
	  val doc = LoadOntonotes5.fromFilename(testDoc).head

	  /* Load serialized models */
	  println("Loading models... ")
	  //val modelLoc = args(0)
	
	  // POS tagger
	  print("\tpos: ")
//	  var t0 = System.currentTimeMillis()
	  val tagger = app.nlp.pos.OntonotesForwardPosTagger
//	  println(s"${System.currentTimeMillis() - t0}ms")
//	
//	  // dependency parser
//	  print("\tparse: ")
//	  t0 = System.currentTimeMillis()
//	  val parser = app.nlp.parse.OntonotesTransitionBasedParser
//	  println(s"${System.currentTimeMillis() - t0}ms")
//	
	  print("\tner: ")
//	  t0 = System.currentTimeMillis()
	  val ner = app.nlp.ner.NoEmbeddingsConllStackedChainNer
//	  println(s"${System.currentTimeMillis() - t0}ms")
//	  
//	  print("\tmention (gender): ")
//	  t0 = System.currentTimeMillis()
//	  val mentionGender = app.nlp.mention.MentionGenderLabeler
//	  println(s"${System.currentTimeMillis() - t0}ms")
//	  
//	  print("\tmention (number): ")
//	  t0 = System.currentTimeMillis()
//	  val mentionNumber = app.nlp.mention.MentionNumberLabeler
//	  println(s"${System.currentTimeMillis() - t0}ms")
//	  
//	  print("\tmention (entity type): ")
//	  t0 = System.currentTimeMillis()
//	  val mentionEntityType = app.nlp.mention.MentionEntityTypeLabeler
//	  println(s"${System.currentTimeMillis() - t0}ms")
//	  
	  print("\tcoref: ")
	  var t0 = System.currentTimeMillis()
	  val coref = app.nlp.coref.NerForwardCoref
	  println(s"${System.currentTimeMillis() - t0}ms")
	  
	  /* Run the pipeline */
	  print("Running pipeline... ")
	  t0 = System.currentTimeMillis()
	  val annotators = Seq(coref)//Seq(tagger, parser, ner, mentionGender, mentionNumber, mentionEntityType, coref)
	  val pipeline = app.nlp.DocumentAnnotatorPipeline(tagger,ner)
	  pipeline.process(doc)
	  println(s"${System.currentTimeMillis() - t0}ms")
	
	  /* Print formatted results */
	  println("Results: ")
//    println(tagger.accuracy(doc.sentences).toString())


    // Get our error hashes
    val posErrorHash = tagger.detailedAccuracy(doc.sentences)
    val nerErrorHash = ner.testError(Seq(doc))


    // Print out the error hashes
    println("POS ERRORS:")
    for(key <- posErrorHash.keys) {
      println("Errors for " + key.replaceAllLiterally("~*~", " ") + ": " + posErrorHash(key).length)
    }

    println("NER ERRORS:")
    for(key <- nerErrorHash.keys) {
      println(key.replaceAllLiterally("~*~", " ") + ": " + nerErrorHash(key).length)
    }

//	  val printers = for (ann <- pipeline.annotators) yield (t: app.nlp.Token) => ann.tokenAnnotationString(t)
//	  println(doc.owplString(printers))
  }
}
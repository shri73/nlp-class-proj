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
import cc.factorie.app.nlp.pos.LabeledPennPosTag
import cc.factorie.app.chain._

class SecondPrediction(val value : String)

object CorefPipeline {
  def main(args: Array[String]) {
	  println("Loading doc...")
	  val testDoc = "data/conll2003/eng.testa.poop"
//	  val doc = LoadOntonotes5.fromFilename(testDoc).head
    val docs = app.nlp.load.LoadConll2003(true).fromFilename(testDoc)

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
//	  val ner = app.nlp.ner.NoEmbeddingsConllStackedChainNer
    val ner = app.nlp.ner.ConllStackedChainNer
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
	  docs.foreach( d => pipeline.process(d))
	  println(s"${System.currentTimeMillis() - t0}ms")
	
	  /* Print formatted results */
	  println("Results: ")
//    println(tagger.accuracy(doc.sentences).toString())


    // Get our error hashes
    val posErrorHash = tagger.detailedAccuracy(docs)
    val nerErrorHash = ner.testError(docs)


    // Print out the error hashes
    println("POS ERRORS:")
    for(key <- posErrorHash.keys) {
      println("Errors for " + key.replaceAllLiterally("~*~", " ") + ": " + posErrorHash(key).length)
    }

    println("NER ERRORS:")
    for(key <- nerErrorHash.keys) {
      println(key.replaceAllLiterally("~*~", " ") + ": " + nerErrorHash(key).length)
    }

    val numPosTags = app.nlp.pos.PennPosDomain.categories.size
    val numNerTags = app.nlp.ner.BilouConllNerDomain.categories.size
    var posMistakes = Array.fill(numPosTags, numPosTags){0}
    var nerMistakes = Array.fill(numNerTags, numNerTags){0}

    for(key <- posErrorHash.keys) {
      val split = key.split("~\\*~")
      posMistakes(app.nlp.pos.PennPosDomain.getIndex(split(0)))(app.nlp.pos.PennPosDomain.getIndex(split(1))) = posErrorHash(key).length
    }

    for(key <- nerErrorHash.keys) {
      val split = key.split("~\\*~")
      nerMistakes(app.nlp.ner.BilouConllNerDomain.getIndex(split(0)))(app.nlp.ner.BilouConllNerDomain.getIndex(split(1))) = nerErrorHash(key).length
    }

    val posConfusionWriter = new PrintWriter(new File("pos-confusion.dat"))
    val posLabelWriter = new PrintWriter(new File("pos-labels"))
    posMistakes.foreach(x => posConfusionWriter.println(x.mkString(" ")))
    app.nlp.pos.PennPosDomain.foreach(x => posLabelWriter.println(x))
    posLabelWriter.close
    posConfusionWriter.close

    val nerConfusionWriter = new PrintWriter(new File("ner-confusion.dat"))
    val nerLabelWriter = new PrintWriter(new File("ner-labels"))
    nerMistakes.foreach(x => nerConfusionWriter.println(x.mkString(" ")))
    app.nlp.ner.BilouConllNerDomain.foreach(x => nerLabelWriter.println(x))
    nerLabelWriter.close
    nerConfusionWriter.close

    val nerSentenceOutput = new PrintWriter(new File("ner-tag-output.txt"))


    for(d <- docs) {
      for(s <- d.sentences) {
        if(s.attr.contains(classOf[ViterbiResults])) {
          // Look at the scores. If pred label is 0, then select the second highest, map that to the result, and display it

//          print("MaxVal: ")
//          s.attr[ViterbiResults].mapValues.foreach(i => print(BilouConllNerDomain.dimensionName(i) + " "))
//          println("")
//          print("2MaxVa: ")
          for((t, i) <- s.tokens.zipWithIndex) {
            t.attr += new SecondPrediction(BilouConllNerDomain.dimensionName(s.attr[ViterbiResults].secondMapValues(i)))
          }
//          s.attr[ViterbiResults].secondMapValues.foreach(i => print(BilouConllNerDomain.dimensionName(i) + " "))
//          println("")
//          print("Scores: ")
//          s.attr[ViterbiResults].localScores.foreach(i => print(BilouConllNerDomain.dimensionName(i.toSeq.zipWithIndex.maxBy(_._1)._2) + " "))
//          println("")
//          print("Tags  : ")
//          s.tokens.foreach(t => print(t.attr[LabeledBilouConllNerTag].value + " "))
//          println("")
//          val maxIndex = s.attr[ViterbiResults].localScores.zipWithIndex.maxBy(_._1)._2
//          val maxName = BilouConllNerDomain.dimensionName(maxIndex)
//          println(maxName)
        }
        for(t <- s.tokens) {
          nerSentenceOutput.println(t.string + "~*~" + t.attr[LabeledPennPosTag].value + "~*~" + t.attr[LabeledPennPosTag].target.value + "~*~" + t.attr[LabeledBilouConllNerTag].value + "~*~" + t.attr[LabeledBilouConllNerTag].target.value + "~*~" + t.attr[SecondPrediction].value)
        }
        nerSentenceOutput.println("")
      }
    }
    nerSentenceOutput.close


//	  val printers = for (ann <- pipeline.annotators) yield (t: app.nlp.Token) => ann.tokenAnnotationString(t)
//	  println(doc.owplString(printers))
  }

}
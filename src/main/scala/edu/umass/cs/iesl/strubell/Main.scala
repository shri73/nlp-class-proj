package edu.umass.cs.iesl.strubell

import cc.factorie._
import java.net.Socket
import java.io._
import cc.factorie.app.nlp.Section
import cc.factorie.util.FileUtils
import cc.factorie.app.nlp.load._
import scala.collection.mutable.ArrayBuffer
import cc.factorie.app.nlp.Sentence

class SentenceData(val sentence : Sentence, val predictedPosLabels : Seq[Prediction], val predictedParseLabels : Seq[Prediction])
class Prediction(val label : String, val correct : Boolean)

object Main {

  final val GOOD_COLOR = "black"
  final val BAD_COLOR = "red"

  final val TEX_DIR = "latex"
  final val TEX_OUTPUT_DIR = TEX_DIR + "/output"
  final val DOT_OUTPUT_DIR = "dot"



  //  final val POS_TAGS = {
  //
  //  }

  def genDotTrees(document: String) = {
    //    val connection = new Socket("localhost", 3228)
    //    val server = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream)), true)
    //    server.println(document)
    //    connection.shutdownOutput
    //    val result = io.Source.fromInputStream(connection.getInputStream).getLines().toArray.map(_.split("\\s+"))
    //
    //    val emptyIndices = result.zipWithIndex.collect { case (line,idx) if line.size <= 1 => idx }
    //    val sentenceIndices = emptyIndices.zip(emptyIndices.tail)
    //    val sentences = sentenceIndices.map(x => result.slice(x._1,x._2))
    //    for((sentence,i) <- sentences.zipWithIndex){
    //      val name = s"s$i"
    //
    //      val outFile = new File(s"${DOT_OUTPUT_DIR}/${name}.dot")
    //      val dir = new File(outFile.getParentFile().getAbsolutePath())
    //      dir.mkdirs()
    //
    //      val writer = new PrintWriter(outFile)
    //      writer.write(s"digraph ${name}{")
    //      for (line <- sentence) {
    //    	if (line.size > 1) {
    //    	  if (line(4) == "0")
    //    	    writer.write(s""""${line(2)}/${line(3)}";""")
    //          else
    //            writer.write(s""""${line(2)}/${line(3)}" -> "${sentence(line(4).toInt)(2)}/${sentence(line(4).toInt)(3)}" [label=${line(5)}];""")
    //        }
    //      }
    //      writer.write("}")
    //      writer.close
    //    }
  }

  def main(args: Array[String]) {

    implicit val random = new scala.util.Random()

    val singleDoc = false
    val doc = "On a sultry day in late August, a dozen staff members of the Centers for Medicare and Medicaid Services gathered at the agency’s Baltimore headquarters with managers from the major contractors building HealthCare.gov to review numerous problems with President’s Obama’s online health insurance initiative. The mood was grim."
    if(singleDoc){
      genDotTrees(doc)
    }

    else{
      // TODO implement command line arguments for all this stuff
      var dotOutput = false
      var tikzOutput = false
      var recordTime = true

      //val testDir = "/Users/strubell/Documents/research/data/ontonotes-en-1.1.0/tst-pmd/"
      //val testFile = "/Users/strubell/Documents/research/data/wsj-test-clean/nw-wsj-23.dep.pmd"
      val testFile = "data/nw-wsj-23.dep.pmd"
      val sentencesToTake = -1

      /* Load documents */
      print("Loading sentences... ")
      var t0 = System.currentTimeMillis()

      //println("Loading file lists...")
      //val testFileList = FileUtils.getFileListFromDir(testDir, "pmd")

      //println("Loading documents...")
      //val testDocs = testFileList.map(LoadOntonotes5.fromFilename(_).head)

      //println("Getting sentences from documents...")
      //val testSentences = testDocs.flatMap(_.sentences).take(sentencesToTake)

      val testSentences = LoadOntonotes5.fromFilename(testFile).head // We're only loading 1 doc

      println(s"${System.currentTimeMillis() - t0}ms")

      val numDepLabels = app.nlp.parse.ParseTreeLabelDomain.categories.size
      val numPosTags = app.nlp.pos.PennPosDomain.categories.size

      var posMistakes = Array.fill(numPosTags, numPosTags){0}
      var parseMistakes = Array.fill(numDepLabels, numDepLabels){0}

      val startSentence = 1

      var sentenceData = Seq[SentenceData]()

      for ((sentence, i) <- testSentences.sentences.takeRight(testSentences.sentences.size-startSentence-1).zipWithIndex) {

        var parseLabels = Seq[Prediction]()
        var posLabels = Seq[Prediction]()

        // TODO fix this, use a proper regex?
        // also, could become a problem with messier (e.g. transcribed-from-spoken) corpora
        val sentenceString = sentence.string.replaceAll(" '", "'").replaceAll(" 'n", "'n").replaceAll(" ,", ",").replaceAll(" - ", "-")
        //val sentenceString = sentence
        //println(sentenceString)

        if(i < 500) {
        val connection = new Socket("localhost", 3228)
        val server = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream)), true)

        print(s"Processing sentence ${i + 1}... ")
        t0 = System.currentTimeMillis()
        server.println(sentenceString)
        connection.shutdownOutput
        val result = io.Source.fromInputStream(connection.getInputStream).getLines().toArray.map(_.split("\\s+"))
        val time = System.currentTimeMillis() - t0
        println(s"${time}ms")

        val name = s"parse${i}"

        val filteredResult = result.filter(_.size > 1)
        if(filteredResult.size != sentence.tokens.size){
          // sentences weren't tokenized to the same number of tokens -- print out some info and return
          println(s"WARNING: Skipping sentences not tokenized to same number of tokens")
        }
        else{
          // tabulate pos and parse mistakes
          for ((line,tok) <- filteredResult.zip(sentence.tokens)) {

            // parse mistakes
            val goldLabel = tok.parseLabel.value.toString
            val parserLabel = if (line(4) == "0") "root" else line(5)
            val parseError = goldLabel == parserLabel
            if(goldLabel != parserLabel){
              parseMistakes(app.nlp.parse.ParseTreeLabelDomain.getIndex(goldLabel))(app.nlp.parse.ParseTreeLabelDomain.getIndex(parserLabel)) += 1
            }

            // pos mistakes
            val goldTag = tok.posTag.value.toString
            val taggerTag = line(3)
            val tagError = goldTag == taggerTag
            if(goldTag != taggerTag){
              posMistakes(app.nlp.pos.PennPosDomain.getIndex(goldTag))(app.nlp.pos.PennPosDomain.getIndex(taggerTag)) += 1
            }

            parseLabels :+= new Prediction(parserLabel, parseError)
            posLabels :+= new Prediction(taggerTag, tagError)

//            val mistake = new Mistake(line, line, parseError, tagError)
//            errors :+ mistake
          }
        }


//        println("ADDING SENTENCE")
        if(posLabels.length > 0) sentenceData :+= new SentenceData(sentence, posLabels, parseLabels)


        if (dotOutput)
          generateDotFile(result, name)

        if (tikzOutput)
          generateTikzFile(result, sentence, name)

        //if(termOutput)
        //  printResult(result)

        connection.close
        }
      }

      // write confusion matrix output
      val posConfusionWriter = new PrintWriter(new File("pos-confusion.dat"))
      val posLabelWriter = new PrintWriter(new File("pos-labels"))
      posMistakes.foreach(x => posConfusionWriter.println(x.mkString(" ")))
      app.nlp.pos.PennPosDomain.foreach(x => posLabelWriter.println(x))
      posLabelWriter.close
      posConfusionWriter.close

      val parseConfusionWriter = new PrintWriter(new File("parse-confusion.dat"))
      val parseLabelWriter = new PrintWriter(new File("parse-labels"))
      parseMistakes.foreach(x => parseConfusionWriter.println(x.mkString(" ")))
      app.nlp.parse.ParseTreeLabelDomain.foreach(x => parseLabelWriter.println(x))
      parseLabelWriter.close
      parseConfusionWriter.close

      val errorDetector = new ErrorDetector
      println(sentenceData.length)
      println(sentenceData.slice(0,5).length)
      val (trainData, testData) = sentenceData.split(0.6)
      errorDetector.train(trainData, testData)
    }

  }

  def generateTikzFile(processedSentence: Array[Array[String]], inputSentence: app.nlp.Sentence, name: String) = {

    // make sure output directory exists
    val outFile = new File(s"${TEX_OUTPUT_DIR}/${name}.tex")
    val dir = new File(outFile.getParentFile().getAbsolutePath())
    dir.mkdirs()

    val writer = new PrintWriter(outFile)
    writer.write("\\begin{dependency}[edge style={<-}]\n")

    // TODO write a nice replace for cleaning stuff for latex
    val toks = processedSentence.filter(x => x.size > 1).map(y => y(2).replaceAll("\\$", "\\\\\\$").replaceAll("&", "\\\\&").replaceAll("%", "\\\\%"))
    var tags = processedSentence.filter(x => x.size > 1).map(y => y(3))

    val tagColors = (tags,inputSentence.tokens).zipped.map((x,y) => {if (x == y.posTag.value.toString) GOOD_COLOR else BAD_COLOR})
    tags = tags.map(_.replaceAll("\\$", "\\\\\\$").replaceAll("&", "\\\\&").replaceAll("%", "\\\\%"))

    writer.write("\\begin{deptext}\n")
    for (i <- 0 to toks.size - 2) writer.write(s"${toks(i)} \\& ")
    writer.write(s"${toks(toks.size - 1)}\\\\\n")

    //assert(tags.size == inputSentence.tokens.size, s"Sentences not tokenized to same number of tokens:\ntoks: ${toks.mkString(" ")}\ninputSentence.tokens: ${inputSentence.tokens.map(_.string).mkString(" ")}")
    if(tags.size != inputSentence.tokens.size){
      // sentences weren't tokenized to the same number of tokens -- print out some info and return
      println(s"WARNING: Skipping sentences not tokenized to same number of tokens:\nFactorie: ${toks.mkString(" ")}\nOntonotes: ${inputSentence.tokens.map(_.string).mkString(" ")}")
    }
    else{
      //println(s"tagColors.size: ${tagColors.size}, tags.size: ${tags.size}, inputSentence.tokens.size: ${inputSentence.tokens.size}")

      //println(s"tags: ${tags.mkString(" ")}")
      //println(s"toks: ${toks.mkString(" ")}")
      //println(s"inputSentence.tokens: ${inputSentence.tokens.map(_.string).mkString(" ")}")
      for (i <- 0 to tags.size - 2) writer.write(s"{\\color{${tagColors(i)}}${tags(i)}} \\& ")
      writer.write(s"${tags(tags.size - 1)}\\\\\n")
      writer.write("\\end{deptext}\n")

      // now dependencies
      val filteredProcessedSentence = processedSentence.filter(_.size > 1)

      // print server-processed sentence
      //print(s"${filteredProcessedSentence.size}: ")
      //filteredProcessedSentence.foreach(x => print(s"${x(2)} "))
      //println()

      // print sentence from Document
      //print(s"${inputSentence.tokens.size}: ")
      //inputSentence.tokens.foreach(x => print(s"${x.string} "))
      //println("\n\n")

      //println(s"${filteredProcessedSentence.zip(inputSentence.tokens).size}")

      for ((line,tok) <- filteredProcessedSentence.zip(inputSentence.tokens)) {
        var col = GOOD_COLOR

        //println("line: "+ line.mkString(" "))
        //println(s"parent line: ${if (line(4) == "0") "root" else filteredProcessedSentence(line(4).toInt-1).mkString(" ")}")
        val depParent = if (line(4) == "0") "root" else filteredProcessedSentence(line(4).toInt-1)(2)
        val depType = if (depParent == "root") "root" else line(5)

        // this situation is disgusting; fix
        if(depParent == "root" && tok.parseLabel.value.toString != "root"
          || depParent != "root" && tok.parseLabel.value.toString == "root"){
          col = BAD_COLOR
        }
        else if (depParent == "root" && tok.parseLabel.value.toString == "root"){

        }
        else if(depType != tok.parseLabel.value.toString ){
          //      println(s"tok.parseLabel: ${tok.parseLabel.value.toString}; tok.parseParent: ${tok.parseParent}")
          if(depParent != tok.parseParent.string){
            //println(s"WORD: ${tok.string}")
            if (depType != "root" && tok.parseLabel.value.toString != "root"){
              // println(s"depType: ${depType}")
              // println(s"depParent: ${depParent}")
              // println(s"tok.parseLabel.value: ${tok.parseLabel.value}")
              // if(tok.parseParent != null)
              //	  println(s"tok.parseParent.string: ${tok.parseParent.string}")
              // else
              //  println("tok.parseParent is null")
              // println(s"${line(2)}: ${depParent}!=${tok.parseParent.string} || ${depType} != ${tok.parseLabel.value}")
              col = BAD_COLOR
            }
            col = BAD_COLOR
          }
        }

        if (line(4) == "0")
          writer.write(s"\\deproot[edge style={${col},->}]{${line(1)}}{root}\n")
        else
          writer.write(s"\\depedge[color=${col}]{${line(1)}}{${filteredProcessedSentence(line(4).toInt-1)(1)}}{${line(5)}}\n")
      }
      writer.write("\\end{dependency}\n")
      writer.close
    }
  }

  def generateDotFile(processedSentence: Array[Array[String]], name: String) = {

    // make sure output directory exists
//    val outFile = new File(s"${DOT_OUTPUT_DIR}/${name}.dot")
//    val dir = new File(outFile.getParentFile().getAbsolutePath())
//    dir.mkdirs()
//
//    val writer = new PrintWriter(outFile)
//    writer.write(s"digraph ${name}{")
//    for (line <- processedSentence) {
//      if (line.size > 1) {
//        if (line(4) == "0")
//          writer.write(s""""${line(2)}/${line(3)}";""")
//        else
//          writer.write(s""""${line(2)}/${line(3)}" -> "${processedSentence(line(4).toInt)(2)}/${processedSentence(line(4).toInt)(3)}" [label=${line(5)}];""")
//      }
//    }
//    writer.write("}")
//    writer.close
  }

  /* Load serialized models */
  //    println("Loading models... ")
  //    t0 = System.currentTimeMillis()
  //    val modelLoc = args(0)
  //    
  //    // POS tagger
  //    print("\tpos: ")
  //    val tagger = new app.nlp.pos.ForwardPosTagger
  //    tagger.deserialize(new java.io.File(s"$modelLoc/OntonotesForwardPosTagger.factorie"))
  //    println(s"${System.currentTimeMillis()-t0}ms")

  // dependency parser
  //    print("\tparse: ")
  //    val parser = new app.nlp.parse.TransitionBasedParser
  //    parser.deserialize(new java.io.File(s"$modelLoc/OntonotesTransitionBasedParser.factorie"))
  //    println(s"${System.currentTimeMillis()-t0}ms")

  /* Run the pipeline */
  //    print("Running pipeline... ")
  //    t0 = System.currentTimeMillis()
  //    val pipeline = app.nlp.DocumentAnnotatorPipeline(tagger)//, parser)
  //    pipeline.process(doc)
  //    println(s"${System.currentTimeMillis()-t0}ms")
  //    
  //    /* Print formatted results */
  //    println("Results: ")
  //    val printers = for (ann <- pipeline.annotators) yield (t: app.nlp.Token) => ann.tokenAnnotationString(t)
  //    println(doc.owplString(printers))
  //    
  //    doc.annotators(classOf[pos.PennPosTag])

}
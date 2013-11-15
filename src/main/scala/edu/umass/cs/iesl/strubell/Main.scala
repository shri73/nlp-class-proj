package edu.umass.cs.iesl.strubell

import cc.factorie._
import java.net.Socket
import java.io._
import cc.factorie.util.FileUtils
import cc.factorie.app.nlp.load._

object Main {

  final val GOOD_COLOR = "black"
  final val BAD_COLOR = "red"
    
  final val TEX_DIR = "latex"
  final val TEX_OUTPUT_DIR = TEX_DIR + "/output"
  final val DOT_OUTPUT_DIR = "dot"

  def main(args: Array[String]) {

    // TODO implement command line arguments for all this stuff
    var dotOutput = false
    var tikzOutput = true
    var recordTime = true

    //val testDir = "/Users/strubell/Documents/research/data/ontonotes-en-1.1.0/tst-pmd/"
    val testFile = "/Users/strubell/Documents/research/data/ontonotes-en-1.1.0/tst-pmd/nw-wsj-23.dep.pmd"
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

    val testDoc = LoadOntonotes5.fromFilename(testFile).head
    var testSentences = LoadOntonotes5.fromFilename(testFile).head.sentences
    if(sentencesToTake > 0)
      testSentences = testSentences.take(sentencesToTake)
    println(s"${System.currentTimeMillis() - t0}ms")
    
    val numDepLabels = app.nlp.parse.ParseTreeLabelDomain.categories.size
    
    var posMistakes = Array.ofDim[Integer](numDepLabels, numDepLabels)
    var parseMistakes = Array.ofDim[Integer](numDepLabels, numDepLabels)
    
    val startSentence = 1610

    for ((sentence, i) <- testSentences.takeRight(testSentences.size-startSentence-1).zipWithIndex) {

      // TODO fix this, use a proper regex?
      // also, could become a problem with messier (e.g. transcribed-from-spoken) corpora
      val sentenceString = sentence.string.replaceAll(" '", "'").replaceAll(" 'n", "'n").replaceAll(" ,", ",").replaceAll(" - ", "-")
      println(sentenceString)
      
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
      
      // tabulate pos and parse mistakes
      val filteredResult = result.filter(_.size > 1)
//      for ((line,tok) <- filteredResult.zip(sentence.tokens)) {
//        
//      }
      
      if (dotOutput)
        generateDotFile(result, name)
      
      if (tikzOutput)
    	generateTikzFile(result, sentence, name)
      
      //if(termOutput)
      //  printResult(result)

      connection.close
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
    println(s"tagColors.size: ${tagColors.size}, tags.size: ${tags.size}, inputSentence.tokens.size: ${inputSentence.tokens.size}")
    
    println(s"tags: ${tags.mkString(" ")}")
    println(s"toks: ${toks.mkString(" ")}")
    println(s"inputSentence.tokens: ${inputSentence.tokens.map(_.string).mkString(" ")}")
    for (i <- 0 to tags.size - 2) writer.write(s"{\\color{${tagColors(i)}}${tags(i)}} \\& ")
    writer.write(s"${tags(tags.size - 1)}\\\\\n")
    writer.write("\\end{deptext}\n")

    // now dependencies
    val filteredProcessedSentence = processedSentence.filter(_.size > 1)
    
    // print server-processed sentence
    print(s"${filteredProcessedSentence.size}: ")
    filteredProcessedSentence.foreach(x => print(s"${x(2)} "))
    println()
    
    // print sentence from Document
    print(s"${inputSentence.tokens.size}: ")
    inputSentence.tokens.foreach(x => print(s"${x.string} "))
    println("\n\n")
    
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
  
  def generateDotFile(processedSentence: Array[Array[String]], name: String) = {
    
    // make sure output directory exists
    val outFile = new File(s"${DOT_OUTPUT_DIR}/${name}.dot")
    val dir = new File(outFile.getParentFile().getAbsolutePath())
    dir.mkdirs()
    
    val writer = new PrintWriter(outFile)
    writer.write(s"digraph ${name}{")
    for (line <- processedSentence) {
      if (line.size > 1) {
        if (line(4) == "0")
          writer.write(s""""${line(2)}/${line(3)}";""")
        else
          writer.write(s""""${line(2)}/${line(3)}" -> "${processedSentence(line(4).toInt)(2)}/${processedSentence(line(4).toInt)(3)}" [label=${line(5)}];""")
      }
    }
    writer.write("}")
    writer.close
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
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

    for ((sentence, i) <- testSentences.zipWithIndex) {

      // TODO fix this, use a proper regex?
      val sentenceString = sentence.string.replaceAll(" '", "'").replaceAll(" 'n", "'n").replaceAll(" ,", ",").replaceAll(" .", ".").replaceAll(" - ", "-")
      println(sentenceString)
      
      val connection = new Socket("localhost", 3228)
      val server = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream)), true)

      print(s"Processing sentence ${i + 1}... ")
      t0 = System.currentTimeMillis()
      server.println(sentenceString) // could become a problem 
      connection.shutdownOutput
      val result = io.Source.fromInputStream(connection.getInputStream).getLines().toArray.map(_.split("\\s+"))
      println(s"${System.currentTimeMillis() - t0}ms")
      val name = s"parse${i}"
      if (dotOutput)
        generateDotFile(result, name)
      if (tikzOutput)
        generateTikzFile(result, sentence, name)
      //if(termOutput)
      //  printResult(result)

      connection.close
    }

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

  def generateTikzFile(processedSentence: Array[Array[String]], inputSentence: app.nlp.Sentence, name: String) = {
    
    // make sure output directory exists
    val outFile = new File(s"${TEX_OUTPUT_DIR}/${name}.tex")
    val dir = new File(outFile.getParentFile().getAbsolutePath())
    dir.mkdirs()
    
    val writer = new PrintWriter(outFile)
    writer.write("\\begin{dependency}[edge style={<-}]\n")

    val toks = processedSentence.filter(x => x.size > 1).map(y => y(2).replaceAll("\\$", "\\\\\\$"))
    var tags = processedSentence.filter(x => x.size > 1).map(y => y(3))

    val tagColors = (tags,inputSentence.tokens).zipped.map((x,y) => {if (x == y.posTag.value.toString) GOOD_COLOR else BAD_COLOR})
    tags = tags.map(_.replaceAll("\\$", "\\\\\\$"))
    
    writer.write("\\begin{deptext}\n")
    for (i <- 0 to toks.size - 2) writer.write(s"${toks(i)} \\& ")
    writer.write(s"${toks(toks.size - 1)}\\\\\n")
    for (i <- 0 to tags.size - 2) writer.write(s"{\\color{${tagColors(i)}}${tags(i)}} \\& ")
    writer.write(s"${tags(tags.size - 1)}\\\\\n")
    writer.write("\\end{deptext}\n")

    // now dependencies
    val filteredProcessedSentence = processedSentence.filter(_.size > 1)
    
    //print(s"${filteredProcessedSentence.size} ")
    filteredProcessedSentence.foreach(x => print(s"${x(2)} "))
    println()
    print(s"${inputSentence.tokens.size} ")
    inputSentence.tokens.foreach(x => print(s"${x.string} "))
    println("\n\n")
    
    println(s"${filteredProcessedSentence.zip(inputSentence.tokens).size}")
    
    for ((line,tok) <- filteredProcessedSentence.zip(inputSentence.tokens)) {
      var col = GOOD_COLOR
      
      val depParent = if (line(4) == "0") "root" else processedSentence(line(4).toInt)(2)
      val depType = if (depParent == "root") "root" else line(5)
      
      // this situation is disgusting; think about and fix
      if(depParent == "root" && tok.parseLabel.value.toString == "root"){
        col = BAD_COLOR
      }
      
      else if(depType != tok.parseLabel.value.toString || depParent != tok.parseParent.string){
//        if (depType != "root" && tok.parseLabel.value.toString != "root")
//        	println(s"${line(2)}: ${depParent}!=${tok.parseParent.string} || ${depType} != ${tok.parseLabel.value}")
        col = BAD_COLOR
      }
      
        if (line(4) == "0")
          writer.write(s"\\deproot[edge style={${col},->}]{${line(1)}}{root}\n")
        else
          writer.write(s"\\depedge[color=${col}]{${line(1)}}{${processedSentence(line(4).toInt)(1)}}{${line(5)}}\n")
    }
    writer.write("\\end{dependency}\n")
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
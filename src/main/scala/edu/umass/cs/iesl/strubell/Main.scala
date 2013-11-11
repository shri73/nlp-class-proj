package edu.umass.cs.iesl.strubell

import cc.factorie._
import java.net.Socket
import java.io._
//import resource._


object Main {
  def main(args: Array[String]) {
    
    var dotOutput = true
    var tikzOutput = true
    var outputDir = "output"
    
    /* Load documents */
    print("Loading documents... ")
    var t0 = System.currentTimeMillis()
    val input = Seq(
      "But the new homes have cracked walls, leaking windows and elevators with rusted out floors.",
      "For farmers who were asked to surrender their ancestral lands for an apartment, the deterioration adds to a sense of having been cheated."
    )
    println(s"${System.currentTimeMillis()-t0}ms")     
    
    for((sentence,i) <- input.zipWithIndex){
	    val connection = new Socket("localhost", 3228)
	    val server = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream)), true)
	    //val result = io.Source.fromInputStream(connection.getInputStream)
	    
	    println("Writing to socket...")
	    server.println(sentence)
	    connection.shutdownOutput
	    println("Reading from socket...")
	    val result = io.Source.fromInputStream(connection.getInputStream).getLines().toArray.map(_.split("\\s+"))
	    val name = s"${outputDir}/s${i}"
	    if(dotOutput)
	      generateDotFile(result, name)
	    if(tikzOutput)
	      generateTikzFile(result, name)
	    //if(termOutput)
	    //  printResult(result)
	  
	    connection.close
    }

  }
  
  def generateDotFile(processedSentence: Array[Array[String]], name: String) = {
    val writer = new PrintWriter(new File(s"${name}.dot"))
    writer.write(s"digraph ${name}{")
    for(line <- processedSentence){
      if(line.size > 1){
    	  if(line(4) == "0")
    	    writer.write(s""""${line(2)}/${line(3)}";""")
    	  else
    		writer.write(s""""${line(2)}/${line(3)}" -> "${processedSentence(line(4).toInt)(2)}/${processedSentence(line(4).toInt)(3)}" [label=${line(5)}];""")
      }
    }
    writer.write("}")
    writer.close
  }
  
  def generateTikzFile(processedSentence: Array[Array[String]], name: String) = {
	  val writer = new PrintWriter(new File(s"${name}.tex"))
	  writer.write("\\begin{dependency}\n")
	  
	  val toks = processedSentence.filter(x => x.size > 1).map(y => y(2).replaceAll("\\$", "\\\\\\$"))
	  val tags = processedSentence.filter(x => x.size > 1).map(y => y(3).replaceAll("\\$", "\\\\\\$"))

      writer.write("\\begin{deptext}\n")
      for(i <- 0 to toks.size-2) writer.write(s"${toks(i)} \\& ")
      writer.write(s"${toks(toks.size-1)}\\\\\n")
      for(i <- 0 to tags.size-2) writer.write(s"${tags(i)} \\& ")
      writer.write(s"${tags(tags.size-1)}\\\\\n")
      writer.write("\\end{deptext}\n")
      
      // now dependencies
      for(line <- processedSentence){
	      if(line.size > 1){
	    	  if(line(4) == "0")
	    	    writer.write(s"\\deproot{${line(1)}}{${line(2)}}\n")
	    	  else
	    		writer.write(s"\\depedge{${line(1)}}{${processedSentence(line(4).toInt)(1)}}{${line(5)}}\n")
	      }
    }
    writer.write("\\end{dependency}\n")
    writer.close
  }
  
      /* Load serialized models */
    // TODO use NLP server:
    //  1. check to see whether one is already running
    //  2. if not start one with desired modules
    //  3. open socket connection to server & get output from there
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
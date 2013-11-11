package edu.umass.cs.iesl.strubell

import cc.factorie._
import java.net.Socket
import java.io._
//import resource._


object Main {
  def main(args: Array[String]) {
//    val doc = new app.nlp.Document("All work and no play makes Jack a dull boy. All work and no play makes Jack a dull boy.")
//    val pipeline = app.nlp.DocumentAnnotatorPipeline[app.nlp.ner.NerTag,app.nlp.parse.ParseTree]
//    pipeline.process(doc)
//    val printers = for (ann <- Seq(app.nlp.pos.OntonotesForwardPosTagger, app.nlp.parse.OntonotesTransitionBasedParser, app.nlp.ner.ConllStackedChainNer)) yield (t: app.nlp.Token) => ann.tokenAnnotationString(t)
//    println(doc.owplString(printers))
    
    var dotOutput = true
    var tikzOutput = true
    
    /* Load documents */
    print("Loading documents... ")
    var t0 = System.currentTimeMillis()
    //val doc = new app.nlp.Document("But the new homes have cracked walls, leaking windows and elevators with rusted out floors. For farmers who were asked to surrender their ancestral lands for an apartment, the deterioration adds to a sense of having been cheated.")
    val text = "But the new homes have cracked walls, leaking windows and elevators with rusted out floors."
    val text2 = "For farmers who were asked to surrender their ancestral lands for an apartment, the deterioration adds to a sense of having been cheated."
    println(s"${System.currentTimeMillis()-t0}ms")     
    
    val connection = new Socket("localhost", 3228)
    val server = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream)), true)
    //val result = io.Source.fromInputStream(connection.getInputStream)
    
    println("Writing to socket...")
    server.println(text)
    connection.shutdownOutput
    println("Reading from socket...")
    val result = io.Source.fromInputStream(connection.getInputStream).getLines().toArray.map(_.split("\\s+"))
    //println(result.deep.mkString)
    
    if(dotOutput)
      generateDotFile(result, "test")
    if(tikzOutput)
      generateTikzFile(result)
    if(termOutput)
      printResult(result)
  
    connection.close

  }
  
  def generateDotFile(processedSentence: Array[Array[String]], name: String) = {
    val writer = new PrintWriter(new File(s"${name}.dot"))
    writer.write(s"digraph ${name}{")
    for(line <- result){
      //println(s"size: ${line.size}; ${line(0)}")
      if(line.size > 1){
    	  if(line(4) == "0")
    	    writer.write(s""""${line(2)}/${line(3)}";""")
    	  else
    		writer.write(s""""${line(2)}/${line(3)}" -> "${result(line(4).toInt)(2)}/${result(line(4).toInt)(3)}" [label=${line(5)}];""")
      }
    }
    writer.write("}")
    writer.close
  }
  
  def generateTikzFile(processedSentence: Array[Array[String]], name: String) = {
	  val writer = new PrintWriter(new File(s"${name}.tex"))
	  writer.write("\\begin{dependency}\n")
	  
	  var toks = Seq()
	  var tags = Seq()
	  // first need to write tokens + pos tags
	  for(line <- result){
      if(line.size > 1){
        toks ++= line(2)
        tags ++= line(3)
      }
      writer.write("\\begin{deptext}[column sep=.5cm]\n")
      for(i <- Seq(toks.size-1)) writer.write(s"${toks(i)} \& ")
      writer.write(s"${toks(toks.size-1)}\\\\\n")
      for(i <- Seq(tags.size-1)) writer.write(s"${tags(i)} \& ")
      writer.write(s"${tags(tags.size-1)}\\\\\n")
      writer.write("\\end{deptext}\n")
      
      // now dependencies
      for(line <- result){
      if(line.size > 1){
    	  if(line(4) == "0")
    	    writer.write(s"\\deproot{${line(1)}}{${line(2)}}")
    	  else
    		writer.write(s"\\depedge{${line(1)}}{${line(2)}}")
    		    """"${line(2)}/${line(3)}" -> "${result(line(4).toInt)(2)}/${result(line(4).toInt)(3)}" [label=${line(5)}];""")
      }
    }
    writer.write("\\end{dependency}\n")
    writer.close
  }
  
  /*
  \begin{dependency}
\begin{deptext}[column sep=.5cm]
My \& dog \& also \& likes \& eating \& sausage \\
PRP\$ \& NN \& RB \&[.5cm] VBZ \& VBG \& NN \\
\end{deptext}
\deproot{4}{root}
\depedge{2}{1}{poss}
\depedge{4}{2}{nsubj}
\depedge{4}{3}{advmod}
\depedge{4}{5}{xcomp}
\depedge{5}{6}{dobj}
\end{dependency}
*/
  
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
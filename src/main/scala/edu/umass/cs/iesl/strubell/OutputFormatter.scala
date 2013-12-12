package edu.umass.cs.iesl.strubell

import cc.factorie._
import java.io._
import cc.factorie.util.FileUtils
import cc.factorie.app.nlp.load._
import cc.factorie.app.nlp._
import cc.factorie.app.nlp.parse._
import cc.factorie.app.nlp.pos._

object OutputFormatter {

  final val GOOD_COLOR = "black"
  final val BAD_COLOR = "red"
  final val NO_LABEL_COLOR = "blue"

  final val TEX_DIR = "latex"
  final val TEX_OUTPUT_DIR = TEX_DIR + "/output"
  final val DOT_OUTPUT_DIR = "dot"

  def generateTikzFiles(doc: Document) = {
    (doc.sentences, (1 until doc.sentences.size)).zipped.foreach((s, i) => generateTikzFile(s, s"parse$i"))
  }

  def generateTikzFile(sentence: Sentence, name: String) = {
    // make sure output directory exists
    val outFile = new File(s"${TEX_OUTPUT_DIR}/${name}.tex")
    val dir = new File(outFile.getParentFile().getAbsolutePath())
    dir.mkdirs()

    val writer = new PrintWriter(outFile)
    writer.println("\\begin{dependency}[edge style={<-}]")

    val words = sentence.tokens.map(_.string)
    val assignedPosTags = sentence.tokens.map(_.attr[LabeledPennPosTag].value.toString)
    val goldPosTags = sentence.tokens.map(_.attr[LabeledPennPosTag].target.value.toString)

    val tagColors = (assignedPosTags, goldPosTags).zipped.map((x, y) => { if (x == y) GOOD_COLOR else BAD_COLOR })

    // TODO this could make better use of regex?
    val cleanAssignedPosTags = assignedPosTags.map(_.replaceAll("\\$", "\\\\\\$").replaceAll("&", "\\\\&").replaceAll("%", "\\\\%").replaceAll("{", "\\\\{").replaceAll("}", "\\\\}"))
    val cleanWords = words.map(_.replaceAll("\\$", "\\\\\\$").replaceAll("&", "\\\\&").replaceAll("%", "\\\\%").replaceAll("{", "\\\\{").replaceAll("}", "\\\\}"))
    
    /* Print actual words */
    writer.println("\\begin{deptext}")
    cleanWords.foreach(word => { writer.print(s"$word \\& ") })
    writer.println("\\\\")

    /* Print assigned POS tags */
    (cleanAssignedPosTags, tagColors).zipped.foreach((tag, color) => { writer.print(s"\\color{$color}{$tag} \\& ") })
    writer.println("\\\\\n\\end{deptext}")

    /* Print dependencies */
    val parseTree = sentence.attr[ParseTree]
    val assignedParents = parseTree._parents // ints
    val goldParents = parseTree._targetParents // ints
    val assignedLabels = parseTree._labels.map(_.value.toString) // should be strings
    val goldLabels = parseTree._labels.map(_.target.value.toString) // should be strings
    val parentColors = (assignedParents, goldParents).zipped.map((x, y) => if (x == y) GOOD_COLOR else BAD_COLOR)
    val labelColors = (assignedLabels, goldLabels).zipped.map((x, y) => if (x == y) GOOD_COLOR else BAD_COLOR)

    println("words:")
    words.foreach(x => print(s"${x} "))
    println()

    println("assigned labels:")
    assignedLabels.foreach(x => if (x != "") print(s"${x} ") else print("_ "))
    println()

    println("gold labels:")
    goldLabels.foreach(x => print(s"${x} "))
    println()

    println("assigned parents:")
    assignedParents.foreach(x => print(s"${x} "))
    println()

    println("gold parents:")
    goldParents.foreach(x => print(s"${x} "))
    println()

    println("parent colors:")
    parentColors.foreach(x => print(s"${x} "))
    println()

    println("label colors:")
    labelColors.foreach(x => print(s"${x} "))
    println()

    for (i <- 0 until sentence.length) {
      // eval skips punctuation so we will too
      println(s"word: ${words(i)}, isPunctuation: ${parseTree.sentence.tokens(i).isPunctuation}")
      if (!parseTree.sentence.tokens(i).isPunctuation) {
        
        var col = GOOD_COLOR
        val word = words(i)
        val label = assignedLabels(i)
        
        println(s"word: $word; label: $label; parent: $i")
        
        val hasParent = assignedParents(i) != -1
        val parentIdx = if (label != "root" && hasParent) assignedParents(i) else i
        
        // deal with case where no label was assigned
        if(!hasParent)
          parentColors(i) = NO_LABEL_COLOR
          
        

        //val assignedParent = assignedParents(i)
        //val goldParent = goldParents(i)
        //val assignedLabel = assignedLabels(i)
        //val goldLabel = goldLabels(i)

        if (label == "root")
          writer.println(s"\\deproot[edge style=${labelColors(i)},->]{${i+1}}{root}")
        else
          writer.println(s"\\depedge[color=${parentColors(i)}]{${i+1}}{${parentIdx+1}}{$label}")
      }
    }
    writer.println("\\end{dependency}")
    writer.close
  }
}
package edu.umass.cs.iesl.strubell

import cc.factorie._
import java.io._
import cc.factorie.util.FileUtils
import cc.factorie.app.nlp.load._
import cc.factorie.app.nlp._
import cc.factorie.app.nlp.parse._
import cc.factorie.app.nlp.pos._
import cc.factorie.app.nlp.ner._

object OutputFormatter {

  final val GOOD_COLOR = "black"
  final val BAD_COLOR = "red"
  final val NO_LABEL_COLOR = "blue"

  final val TEX_DIR = "latex"
  final val TEX_OUTPUT_DIR = TEX_DIR + "/output"
  final val DOT_OUTPUT_DIR = "dot"

  def generateConfusionDataParse(doc: Document) = {
    val numDepLabels = ParseTreeLabelDomain.categories.size
    var parseMistakes = Array.fill(numDepLabels, numDepLabels) { 0 }
    val parseConfusionWriter = new PrintWriter(new File("parse-confusion.dat"))
    val parseLabelWriter = new PrintWriter(new File("parse-labels"))

    doc.sentences.foreach(sentence => {
      val parseTree = sentence.attr[ParseTree]
      val assignedParents = parseTree._parents // ints
      val goldParents = parseTree._targetParents // ints
      val assignedLabels = parseTree._labels.map(_.value.toString) // should be strings
      val goldLabels = parseTree._labels.map(_.target.value.toString) // should be strings
      for (i <- 0 until sentence.length) {
        // eval skips punctuation so we will too
        if (!parseTree.sentence.tokens(i).isPunctuation) {
          val goldLabel = goldLabels(i)
          val assignedLabel = assignedLabels(i)
          if (goldLabel != assignedLabel) {
            parseMistakes(ParseTreeLabelDomain.getIndex(goldLabel))(ParseTreeLabelDomain.getIndex(assignedLabel)) += 1
          }
        }
      }
    })
    parseMistakes.foreach(x => parseConfusionWriter.println(x.mkString(" ")))
    app.nlp.parse.ParseTreeLabelDomain.foreach(x => parseLabelWriter.println(x))
    parseLabelWriter.close
    parseConfusionWriter.close
  }
  
  def generateConfusionDataPos(docs: Seq[Document]) = {
    val numLabels = PennPosDomain.size
    var mistakes = Array.fill(numLabels, numLabels) { 0 }
    val confusionWriter = new PrintWriter(new File("pos-confusion.dat"))
    val labelWriter = new PrintWriter(new File("pos-labels"))

    docs.flatMap(_.sentences).flatMap(_.tokens).foreach(tok => {
      val assignedLabel = tok.attr[LabeledPennPosTag].value
      val goldLabel = tok.attr[LabeledPennPosTag].target.value
      //println(s"${assignedLabel.toString}/${goldLabel.toString}")
      println(s"${tok.attr[PennPosTag].value}/${tok.attr[LabeledPennPosTag].target.value}")
      //if (goldLabel != assignedLabel) {
      if(!tok.attr[LabeledPennPosTag].valueIsTarget){
        mistakes(PennPosDomain.getIndex(goldLabel.toString))(PennPosDomain.getIndex(assignedLabel.toString)) += 1
      }
    })
    mistakes.foreach(x => confusionWriter.println(x.mkString(" ")))
    PennPosDomain.foreach(x => labelWriter.println(x))
    labelWriter.close
    confusionWriter.close
  }
  
//  def generateConfusionDataPos(doc: Document) = {
//    val numLabels = PennPosDomain.size
//    var mistakes = Array.fill(numLabels, numLabels) { 0 }
//    val confusionWriter = new PrintWriter(new File("pos-confusion.dat"))
//    val labelWriter = new PrintWriter(new File("pos-labels"))
//
//    doc.sentences.flatMap(_.tokens).foreach(tok => {
//      val assignedLabel = tok.attr[LabeledPennPosTag].value
//      val goldLabel = tok.attr[LabeledPennPosTag].target.value
//      if (goldLabel != assignedLabel) {
//        mistakes(PennPosDomain.getIndex(goldLabel.toString))(PennPosDomain.getIndex(assignedLabel.toString)) += 1
//      }
//    })
//    mistakes.foreach(x => confusionWriter.println(x.mkString(" ")))
//    PennPosDomain.foreach(x => labelWriter.println(x))
//    labelWriter.close
//    confusionWriter.close
//  }
  
  def generateConfusionDataNer(doc: Document) = {
    val numLabels = BilouConllNerDomain.size
    var mistakes = Array.fill(numLabels, numLabels) { 0 }
    val confusionWriter = new PrintWriter(new File("ner-confusion.dat"))
    val labelWriter = new PrintWriter(new File("ner-labels"))

    doc.sentences.flatMap(_.tokens).foreach(tok => {
      val assignedLabel = tok.attr[LabeledBilouConllNerTag].value
      val goldLabel = tok.attr[LabeledBilouConllNerTag].target.value
      if (goldLabel != assignedLabel) {
        mistakes(BilouConllNerDomain.getIndex(goldLabel.toString))(BilouConllNerDomain.getIndex(assignedLabel.toString)) += 1
      }
    })
    mistakes.foreach(x => confusionWriter.println(x.mkString(" ")))
    BilouConllNerDomain.foreach(x => labelWriter.println(x))
    labelWriter.close
    confusionWriter.close
  }

  def generateTikzFiles(doc: Document, name: String) = {
    (doc.sentences, (1 until doc.sentences.size)).zipped.foreach((s, i) => generateTikzFile(s, s"$name-$i"))
  }

  def generateTikzFile(sentence: Sentence, name: String) {
    // make sure output directory exists

    val words = sentence.tokens.map(_.string)

    /* Print dependencies */
    val parseTree = sentence.attr[ParseTree]
    val assignedParents = parseTree._parents // ints
    val goldParents = parseTree._targetParents // ints
    val assignedLabels = parseTree._labels.map(_.value.toString) // should be strings
    val goldLabels = parseTree._labels.map(_.target.value.toString) // should be strings
    val parentColors = (assignedParents, goldParents).zipped.map((x, y) => if (x == y) GOOD_COLOR else BAD_COLOR)
    val labelColors = (assignedLabels, goldLabels).zipped.map((x, y) => if (x == y) GOOD_COLOR else BAD_COLOR)

    var wasErr = 0
    var doPrint = false
    for (i <- 0 until sentence.length) {
      if (assignedLabels(i) == "prep" && goldLabels(i) == "mark") {
        doPrint = true
        wasErr = 1
        println(s"${goldLabels(i)}/${assignedLabels(i)}: ${sentence.tokens.map(_.string).mkString(" ")}")
        println(goldLabels.mkString(" "))
        println(assignedLabels.mkString(" "))
        println()
      }
    }
    if (doPrint) {
      
      val outFile = new File(s"${TEX_OUTPUT_DIR}/${name}.tex")
      val dir = new File(outFile.getParentFile().getAbsolutePath())
      dir.mkdirs()

      val writer = new PrintWriter(outFile)
      writer.println("\\begin{dependency}[edge style={<-}]")
      val assignedPosTags = sentence.tokens.map(_.attr[LabeledPennPosTag].value.toString)
      val goldPosTags = sentence.tokens.map(_.attr[LabeledPennPosTag].target.value.toString)

      val tagColors = (assignedPosTags, goldPosTags).zipped.map((x, y) => { if (x == y) GOOD_COLOR else BAD_COLOR })

      /* escape LaTeX special characters */
      val cleanAssignedPosTags = assignedPosTags.map(_.replaceAll("([$&{}%#])", "\\\\$1"))
      val cleanWords = words.map(_.replaceAll("([$&{}%#])", "\\\\$1"))

      /* Print actual words */
      writer.println("\\begin{deptext}")
      cleanWords.foreach(word => { writer.print(s"$word \\& ") })
      writer.println("\\\\")

      /* Print assigned POS tags */
      (cleanAssignedPosTags, tagColors).zipped.foreach((tag, color) => { writer.print(s"\\color{$color}{$tag} \\& ") })
      writer.println("\\\\\n\\end{deptext}")

      for (i <- 0 until sentence.length) {
        // eval skips punctuation so we will too
        if (!parseTree.sentence.tokens(i).isPunctuation) {

          var col = GOOD_COLOR
          val word = words(i)
          val label = assignedLabels(i)
          val hasParent = assignedParents(i) != -1
          val parentIdx = if (label != "root" && hasParent) assignedParents(i) else i

          // deal with case where no label was assigned
          if (!hasParent)
            parentColors(i) = NO_LABEL_COLOR

          if (label == "root")
            writer.println(s"\\deproot[edge style=${labelColors(i)},->]{${i + 1}}{root}")
          else
            writer.println(s"\\depedge[color=${parentColors(i)}]{${i + 1}}{${parentIdx + 1}}{$label}")
        }
      }
      writer.println("\\end{dependency}")
      writer.close
    }
  }
}
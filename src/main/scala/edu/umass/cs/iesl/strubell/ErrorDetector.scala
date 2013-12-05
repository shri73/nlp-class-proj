package edu.umass.cs.iesl.strubell

import cc.factorie.app.nlp._
import cc.factorie.app.chain.ChainModel
import cc.factorie.app.chain.Observations._
import java.io._
import cc.factorie.util.{HyperparameterMain, ClasspathURL, BinarySerializer}
import cc.factorie.optimize.Trainer
import cc.factorie.variable.{CategoricalLabeling, HammingObjective, BinaryFeatureVectorVariable, CategoricalVectorDomain}
import cc.factorie.app.nlp.pos._


//class Mistake(val posData : Array[String], val parseData : Array[String], val posCorrect : Boolean, val parseCorrect: Boolean)

//class OntonotesErrorDetector(url:java.net.URL) extends ErrorDetector(url)
//object OntonotesChainPosTagger extends OntonotesErrorDetector(ClasspathURL[OntonotesChainPosTagger](".factorie"))

object ErrorDomain extends cc.factorie.variable.CategoricalDomain[scala.Predef.String] {
  this ++= Vector("TRUE", "FALSE")
  freeze()
}

class ErrorTag(val token : cc.factorie.app.nlp.Token, initialValue : scala.Predef.String) extends cc.factorie.variable.CategoricalVariable(initialValue) {
  def domain = ErrorDomain
}

class LabeledErrorTag(token:Token, targetValue:String) extends ErrorTag(token, targetValue) with CategoricalLabeling[scala.Predef.String]

class ErrorDetector extends DocumentAnnotator {
  def this(url:java.net.URL) = { this(); deserialize(url.openConnection().getInputStream) }

  def process(document: Document) = {
    document.sentences.foreach(s => {
      if (s.nonEmpty) {
//        s.tokens.foreach(t => if (!t.attr.contains[ErrorTag]) t.attr += new ErrorTag(t, "FALSE"))
//        initPOSFeatures(s)
//        model.maximize(s.tokens.map(_.posTag))(null)
        println("FOUND EMPTY SHIT")
      }
      else {
        println("FOUND NONEMPTY SHIT")
      }
    })
    document
  }

  def prereqAttrs = Seq(classOf[Token], classOf[Sentence], classOf[PennPosTag])

  def postAttrs = Seq(classOf[PennPosTag], classOf[ErrorTag])

  def tokenAnnotationString(token: Token) = {
    val label = token.attr[PennPosTag]; if (label ne null) label.categoryValue else "(null)"
  }

  def serialize(stream: OutputStream) {
    import cc.factorie.util.CubbieConversions._
    val dstream = new DataOutputStream(new BufferedOutputStream(stream))
    BinarySerializer.serialize(PosFeaturesDomain.dimensionDomain, dstream)
    BinarySerializer.serialize(model, dstream)
    dstream.close()
  }

  def deserialize(stream: InputStream) {
    import cc.factorie.util.CubbieConversions._
    val dstream = new DataInputStream(new BufferedInputStream(stream))
    BinarySerializer.deserialize(PosFeaturesDomain.dimensionDomain, dstream)
    BinarySerializer.deserialize(model, dstream)
    dstream.close()
  }

  def train(trainData: Seq[SentenceData], testData: Seq[SentenceData], lrate: Double = 0.1, decay: Double = 0.01, cutoff: Int = 2, doBootstrap: Boolean = true, useHingeLoss: Boolean = false, numIterations: Int = 5, l1Factor: Double = 0.000001, l2Factor: Double = 0.000001)(implicit random: scala.util.Random) {
    // TODO Accomplish this TokenNormalization instead by calling POS3.preProcess
    trainData.foreach( s => initPOSFeatures(s.sentence, s.predictedPosLabels, s.predictedParseLabels) )
    PosFeaturesDomain.freeze()
    testData.foreach( s => initPOSFeatures(s.sentence, s.predictedPosLabels, s.predictedParseLabels) )

    val trainSentences = trainData.map(_.sentence)
    val testSentences = testData.map(_.sentence)

    println(trainSentences.length)

    println("TRAIN SENT VALS")
    trainSentences.foreach( s => println(s.attr[LabeledErrorTag]))

    def evaluate() {
      (trainSentences ++ testSentences).foreach(s => model.maximize(s.tokens.map(_.attr[LabeledErrorTag]))(null))
      println("Train accuracy: " + HammingObjective.accuracy(trainSentences.flatMap(s => s.tokens.map(_.attr[LabeledErrorTag]))))
      println("Test accuracy: " + HammingObjective.accuracy(testSentences.flatMap(s => s.tokens.map(_.attr[LabeledErrorTag]))))
    }
    val examples = trainSentences.map(sentence => new model.ChainStructuredSVMExample(sentence.tokens.map(_.attr[LabeledErrorTag]))).toSeq
    //val optimizer = new cc.factorie.optimize.AdaGrad(rate=lrate)
    val optimizer = new cc.factorie.optimize.AdaGradRDA(rate = lrate, l1 = l1Factor / examples.length, l2 = l2Factor / examples.length)
    Trainer.onlineTrain(model.parameters, examples, maxIterations = numIterations, optimizer = optimizer, evaluate = evaluate, useParallelTrainer = false)
  }


  object PosFeaturesDomain extends CategoricalVectorDomain[String]

  class PosFeatures(val token: Token) extends BinaryFeatureVectorVariable[String] {
    def domain = PosFeaturesDomain;

    override def skipNonCategories = true
  }


  val model = new ChainModel[ErrorTag, PosFeatures, Token](ErrorDomain,
    PosFeaturesDomain,
    l => l.token.attr[PosFeatures],
    l => l.token,
    t => t.attr[ErrorTag]) {
    useObsMarkov = false
  }


  def initPOSFeatures(sentence: Sentence, posTags: Seq[Prediction], parseTags: Seq[Prediction]): Unit = {
    import cc.factorie.app.strings.simplifyDigits
    (sentence.tokens, posTags, parseTags).zipped foreach { (token, posTag, parseTag) =>
      if (token.attr[PosFeatures] ne null)
        token.attr.remove[PosFeatures]

      val features = token.attr += new PosFeatures(token)
      val rawWord = token.string
      val word = simplifyDigits(rawWord).toLowerCase
      features += "W=" + word
      features += "STEM=" + cc.factorie.app.strings.porterStem(word)
      features += "SHAPE2=" + cc.factorie.app.strings.stringShape(rawWord, 2)
      features += "SHAPE3=" + cc.factorie.app.strings.stringShape(rawWord, 3)
      // pre/suf of length 1..9
      //for (i <- 1 to 9) {
      val i = 3
      features += "SUFFIX" + i + "=" + word.takeRight(i)
      features += "PREFIX" + i + "=" + word.take(i)
      //}
      if (token.isCapitalized) features += "CAPITALIZED"
      if (token.string.matches("[A-Z]")) features += "CONTAINS_CAPITAL"
      if (token.string.matches("-")) features += "CONTAINS_DASH"
      if (token.containsDigit) features += "NUMERIC"
      if (token.isPunctuation) features += "PUNCTUATION"

      features += "PREDICTEDPOS=" + posTag.label
      features += "PREDICTEDPARSE=" + parseTag.label
      
      val correct = if(posTag.correct) "TRUE" else "FALSE"

      token.attr += new LabeledErrorTag(token, correct)

    }
//    addNeighboringFeatureConjunctions(sentence.tokens, (t: Token) => t.attr[PosFeatures], "W=[^@]*$", List(-2), List(-1), List(1), List(-2, -1), List(-1, 0))
  }

  def initialize(sentenceData : Seq[SentenceData]) {
    for(data <- sentenceData) {
      for(token <- data.sentence) {
        token
      }
    }
  }


}


//object ErrorDetectorTrainer {
//  def evaluateParameters(errors : Seq[Seq[Mistake]]): Double = {
//    implicit val random = new scala.util.Random(0)
//    val opts = new ForwardPosOptions
//    opts.parse(args)
//    assert(opts.trainFile.wasInvoked)
//
//    val pos = new ErrorDetector
//
//    val shuffledErrors = random.shuffle(errors)
//    val trainDocs = shuffledErrors.slice(0, shuffledErrors.length / 2)
//    val testDocs = shuffledErrors.slice(shuffledErrors.length / 2, shuffledErrors.length)
//
//    // Now take the output and put everything into the correct object
//    for(sentence <- trainDocs) {
//      // Assume for now we have a Seq of Seqs of Mistakes which represent our sentences
//      sentence = new Sentence()
//      for(token <- sentence) {
//
//      }
//    }
//
//    //for (d <- trainDocs) println("POS3.train 1 trainDoc.length="+d.length)
//    println("Read %d training tokens.".format(trainDocs.map(_.tokenCount).sum))
//    println("Read %d testing tokens.".format(testDocs.map(_.tokenCount).sum))
//
//    val trainPortionToTake = if(opts.trainPortion.wasInvoked) opts.trainPortion.value.toDouble  else 1.0
//    val testPortionToTake =  if(opts.testPortion.wasInvoked) opts.testPortion.value.toDouble  else 1.0
//    val trainSentencesFull = trainDocs.flatMap(_.sentences)
//    val trainSentences = trainSentencesFull.take((trainPortionToTake*trainSentencesFull.length).floor.toInt)
//    val testSentencesFull = testDocs.flatMap(_.sentences)
//    val testSentences = testSentencesFull.take((testPortionToTake*testSentencesFull.length).floor.toInt)
//
//
//    pos.train(trainSentences, testSentences,
//      opts.rate.value, opts.delta.value, opts.cutoff.value, opts.updateExamples.value, opts.useHingeLoss.value, l1Factor=opts.l1.value, l2Factor=opts.l2.value)
//
//    val acc = HammingObjective.accuracy(testDocs.flatMap(d => d.sentences.flatMap(s => s.tokens.map(_.attr[LabeledPennPosTag]))))
//    if(opts.targetAccuracy.wasInvoked) cc.factorie.assertMinimalAccuracy(acc,opts.targetAccuracy.value.toDouble)
//
//    acc
//  }
//}
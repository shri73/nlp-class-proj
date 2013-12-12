# Analyze the POS and NER results, look for correlations between them

import numpy as np
import matplotlib.pyplot as plt
import operator

class Token:
	def __init__(self, word, postag, gold_postag, nertag, gold_nertag):
		self.word = word
		self.postag = postag
		self.gold_postag = gold_postag
		self.nertag = nertag
		self.gold_nertag = gold_nertag

class Sentence:
	def __init__(self, tokens):
		self.tokens = tokens

class Document:
	def __init__(self, sentences):
		self.sentences = sentences
		self.ner_incorrect = []
		self.pos_incorrect = []
		self.ner_incorrect_pos_correct = []
		self.ner_correct_pos_incorrect = []
		self.both_incorrect = []

def read_output_file(filename):
	f = open(filename, 'r')
	sentences = []
	tokens = []
	for line in f:
		line = line.strip()
		if line == "":
			sentences.append(Sentence(tokens))
			tokens = []
		else:
			# line = line.replace("(", "")
			# line = line.replace(")", "")
			spline = line.split('~*~')
			# print(spline)
			tokens.append(Token(spline[0], spline[1], spline[2], spline[3], spline[4]))

	return Document(sentences)

def collate_errors(doc):
	# Go through each of the sentence and mark the type of error that occurs
	for s in doc.sentences:
		for t in s.tokens:
			pos_c = t.postag == t.gold_postag
			ner_c = t.nertag == t.gold_nertag
			if not ner_c:
				doc.ner_incorrect.append(t)
			if not pos_c:
				doc.pos_incorrect.append(t)
			if ner_c and not pos_c:
				doc.ner_correct_pos_incorrect.append(t)
			if not ner_c and pos_c:
				doc.ner_incorrect_pos_correct.append(t)
			if not ner_c and not pos_c:
				doc.both_incorrect.append(t)

def report_errors_4key(errors):
	report = {}
	for e in errors:
		key = "{0} {1} {2} {3}".format(e.postag, e.gold_postag, e.nertag, e.gold_nertag)
		# key = "{2} {3}".format(e.postag, e.gold_postag, e.nertag, e.gold_nertag)
		# key = "{0} {1}".format(e.postag, e.gold_postag, e.nertag, e.gold_nertag)


		if key not in report:
			report[key] = 0
		report[key] += 1

	sorted_report = sorted(report.iteritems(), key=operator.itemgetter(1))
	sorted_report.reverse()
	return sorted_report

def report_errors_nerkey(errors):
	report = {}
	for e in errors:
		# key = "{0} {1} {2} {3}".format(e.postag, e.gold_postag, e.nertag, e.gold_nertag)
		key = "{2} {3}".format(e.postag, e.gold_postag, e.nertag, e.gold_nertag)
		# key = "{0} {1}".format(e.postag, e.gold_postag, e.nertag, e.gold_nertag)


		if key not in report:
			report[key] = 0
		report[key] += 1

	sorted_report = sorted(report.iteritems(), key=operator.itemgetter(1))
	sorted_report.reverse()
	return sorted_report

def report_errors_poskey(errors):
	report = {}
	for e in errors:
		# key = "{0} {1} {2} {3}".format(e.postag, e.gold_postag, e.nertag, e.gold_nertag)
		# key = "{2} {3}".format(e.postag, e.gold_postag, e.nertag, e.gold_nertag)
		key = "{0} {1}".format(e.postag, e.gold_postag, e.nertag, e.gold_nertag)


		if key not in report:
			report[key] = 0
		report[key] += 1

	sorted_report = sorted(report.iteritems(), key=operator.itemgetter(1))
	sorted_report.reverse()
	return sorted_report

def write_report(filename, report):
	f = open(filename, 'w')
	for tok in report:
		f.write("\t".join(map(str, tok)) + "\n")

doc = read_output_file("ner-tag-output.txt")
collate_errors(doc)

write_report("both_incorrect.txt", report_errors_4key(doc.both_incorrect))
write_report("ner_incorrect.txt", report_errors_4key(doc.ner_incorrect))
write_report("pos_incorrect.txt", report_errors_4key(doc.pos_incorrect))
write_report("ner_correct_pos_incorrect.txt", report_errors_4key(doc.ner_correct_pos_incorrect))
write_report("ner_incorrect_pos_correct.txt", report_errors_4key(doc.ner_incorrect_pos_correct))

write_report("both_incorrect_nerkey.txt", report_errors_nerkey(doc.both_incorrect))
write_report("ner_incorrect_nerkey.txt", report_errors_nerkey(doc.ner_incorrect))
write_report("pos_incorrect_nerkey.txt", report_errors_nerkey(doc.pos_incorrect))
write_report("ner_correct_pos_incorrect_nerkey.txt", report_errors_nerkey(doc.ner_correct_pos_incorrect))
write_report("ner_incorrect_pos_correct_nerkey.txt", report_errors_nerkey(doc.ner_incorrect_pos_correct))

write_report("both_incorrect_poskey.txt", report_errors_poskey(doc.both_incorrect))
write_report("ner_incorrect_poskey.txt", report_errors_poskey(doc.ner_incorrect))
write_report("pos_incorrect_poskey.txt", report_errors_poskey(doc.pos_incorrect))
write_report("ner_correct_pos_incorrect_poskey.txt", report_errors_poskey(doc.ner_correct_pos_incorrect))
write_report("ner_incorrect_pos_correct_poskey.txt", report_errors_poskey(doc.ner_incorrect_pos_correct))

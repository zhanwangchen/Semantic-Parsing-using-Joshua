#coding:utf-8
import codecs
import sys


if len(sys.argv)>1:
	evlFile = sys.argv[1]
else:
	print "useage: "+sys.argv[0]+"evlFileName"
	exit(-1)


testSentences = 0
wrongTranslate = 0

#one testSentences may have many TranslateParse
TranslateParse = 0
for line in open(evlFile):
	if line.startswith("correct translation:"):
		testSentences += 1
	if line.startswith("*"):
		wrongTranslate += 1
	if line.startswith("parse"):
		TranslateParse += 1

precision = float(testSentences - wrongTranslate)/testSentences
recall = float(testSentences - wrongTranslate)/TranslateParse
Fmeasure = (2.0 * precision * recall)/(precision + recall)

print "precision: " + str(precision)
print "Recall: " + str(recall)
print "Fmeasure: " + str(Fmeasure)


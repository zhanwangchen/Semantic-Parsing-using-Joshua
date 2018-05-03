#coding:utf-8  
import codecs, random
lineIndexList = range(0,880)
random.shuffle (lineIndexList)
print lineIndexList
NL=[]
MR=[]

for NLline in open("nl.txt"):
	NL.append(NLline)	
	
for MRline in open("MRL.txt"):
	MR.append(MRline)



codecs.open("training.nl-mr.mr","w","utf-8").write(''.join(MR[lineIndex] for lineIndex in  lineIndexList[:550]))

codecs.open("training.nl-mr.nl","w","utf-8").write(''.join(NL[lineIndex] for lineIndex in  lineIndexList[:550]))

codecs.open("dev.nl-mr.nl","w","utf-8").write(''.join(NL[lineIndex] for lineIndex in  lineIndexList[550:600]))
codecs.open("dev.nl-mr.mr","w","utf-8").write(''.join(MR[lineIndex] for lineIndex in  lineIndexList[550:600]))

codecs.open("devtest.nl-mr.mr","w","utf-8").write(''.join(MR[lineIndex] for lineIndex in  lineIndexList[600:]))
codecs.open("devtest.nl-mr.nl","w","utf-8").write(''.join(NL[lineIndex] for lineIndex in  lineIndexList[600:]))

codecs.open("testMask.txt","w","utf-8").write(" ".join(str(x) for x in lineIndexList[600:]))
#

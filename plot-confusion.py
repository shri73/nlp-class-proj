import numpy as np
import matplotlib.pyplot as plt
import codecs

parse_labels = ["acomp", "advcl", "advmod", "agent", "amod", "appos", "attr", \
                "aux", "auxpass", "cc", "ccomp", "complm", "conj", "csubj", "csubjpass", \
                "dep", "det", "dobj", "expl", "hmod", "hyph", "infmod", "intj", "iobj", \
                "mark", "meta", "neg", "nmod", "nn", "npadvmod", "nsubj", "nsubjpass", \
                "num", "number", "oprd", "parataxis", "partmod", "pcomp", "pobj", "poss", \
                "possessive", "preconj", "predet", "prep", "prt", "punct", "quantmod", \
                "rcmod", "root", "xcomp" ]

pos_labels = ["#", "$", "''", ",", "-LRB-", "-RRB-", ".", ":", "CC", "CD", "DT", "EX", \
              "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS", \
              "PDT", "POS", "PRP", "PRP$", "PUNC", "RB", "RBR", "RBS", "RP", "SYM", \
              "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", \
              "WRB", "``", "ADD", "AFX", "HYPH", "NFP", "XX" ]

parse_fname = "parse-confusion.dat"
pos_fname = "pos-confusion.dat"

parse_labels_fname = "parse-labels"
pos_labels_fname = "pos-labels"

parse_data = np.loadtxt(parse_fname)
pos_data = np.loadtxt(pos_fname)

#f = codecs.open(parse_data, encoding='utf-8')
#parse_labels = f.readlines()
    
#f = codecs.open(pos_data, encoding='utf-8')
#pos_labels = f.readlines()

def ln0(x, minval=10e-9):
    return np.log(x.clip(min=minval))

fig1 = plt.figure()
ax1 = fig1.add_subplot(111)
ax1.imshow(pos_data, interpolation='nearest')
ax1.set_title("POS confusion")
ax1.set_ylabel("Gold")
ax1.set_xlabel("Tagger")
ax1.set_yticks(np.arange(len(pos_labels)))
ax1.set_yticklabels(pos_labels,fontsize=8)
ax1.set_xticks(np.arange(len(pos_labels)))
ax1.set_xticklabels(pos_labels,rotation=90,fontsize=8)

fig2 = plt.figure()
ax2 = fig2.add_subplot(111)
ax2.imshow(parse_data, interpolation='nearest')
ax2.set_title("Parsing confusion")
ax2.set_ylabel("Gold")
ax2.set_xlabel("Parser")
ax2.set_yticks(np.arange(len(parse_labels)))
ax2.set_yticklabels(parse_labels,fontsize=8)
ax2.set_xticks(np.arange(len(parse_labels)))
ax2.set_xticklabels(parse_labels,rotation=90,fontsize=8)

pos_worst = np.argsort(pos_data)
print pos_worst

plt.show()

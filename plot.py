import numpy as np
import matplotlib.pyplot as plt
import codecs
from string import strip

fname = "tagdata"
data = np.loadtxt(fname, delimiter=":")

print data

fig1 = plt.figure()
ax1 = fig1.add_subplot(111)
ax1.plot(data[:,0],data[:,1])
#ax1.set_title("POS confusion")
ax1.set_xlabel("min number of observations")
ax1.set_ylabel("percent of words with one POS tag")
#plt.savefig("pos_output.pdf", bbox_inches='tight')

plt.show()
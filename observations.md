#Observations

##POS observations
- The tag IN when the gold label is TO is the most common error. This seems easily correctable, since "to" should always be TO tag, correct?

##NER Observations
- NNP JJ O U-MISC -> common error, weird, but are things like "Ontario-based" or "ex-England". One of the errors is a gold standard error. Yay.
- U-LOC U-PER -> NER context getting the names of people wrong because they share a name with a place.

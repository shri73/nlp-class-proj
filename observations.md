#Observations

##POS observations
- The tag IN when the gold label is TO is the most common error. This seems easily correctable, since "to" should always be TO tag, correct?

##NER Observations
- NNP JJ O U-MISC -> common error, weird, but are things like "Ontario-based" or "ex-England". One of the errors is a gold standard error. Yay.
- U-LOC U-PER -> NER context getting the names of people wrong because they share a name with a place.

##Parse Observations

##Joint observations between the sets
- ('NNP VB B-PER B-ORG', 6) and ('NNP JJ O U-MISC', 11) are the two most common errors where both POS and NER were wrong, but they seem to largely be independent of each other. There isn't much correlation between what each algorithm gets right/wrong.
- Perhaps we can exploit this fact, since when one is wrong the other is usually correct by linking the labels. So if POS guesses NNP, then NER is forced to guess an NER label and not default to O.
- And vice versa, though less common, is when NER guesses that it is a named entity but the POS tagger thinks the word is an adjective. Link them together so taht the two have to be the same.

##Suggestions for improvement
### POS
- Use inferred NER label?

### NER
- Use POS label?

### Parser

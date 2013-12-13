#Observations

##POS observations
- The tag IN when the gold label is TO is the most common error. This seems easily correctable, since "to" should always be TO tag, correct?
- __Forcing the POS parser to be a "TO" tag when the word is "TO" and the predicted label is "IN" results in a boost of 0.06 accuracy__, this is a very common kind of error with the POS tagger
- Forcing the POS parser to be "NNP" when the NER label is not "O" does not work, there also appears to be many legitimate cases of this as well

##NER Observations
- NNP JJ O U-MISC -> common error, weird, but are things like "Ontario-based" or "ex-England". One of the errors is a gold standard error. Yay.
- U-LOC U-PER -> NER context getting the names of people wrong because they share a name with a place.
- Using the second most likely path doesn't work since most of the errors have to do with setting it to "O", and "O" seems to represent some path through viterbi that isn't true, resulting in a label of -1, which isn't what we want. Maybe this needs to be extended?
- Another really common error is when the POS tagger predicts NNP, and NER sets it to "O". However, this doesn't work, as there are too many legit cases where this is correct (why?)
- Since the accuracy is already at 98%, there may not be much to be gained anywhere. Though it does seem like it is getting days of the week wrong quite often, especially when they are all in CAPS

##Parse Observations

##Joint observations between the sets
- ('NNP VB B-PER B-ORG', 6) and ('NNP JJ O U-MISC', 11) are the two most common errors where both POS and NER were wrong, but they seem to largely be independent of each other. There isn't much correlation between what each algorithm gets right/wrong.
- Perhaps we can exploit this fact, since when one is wrong the other is usually correct by linking the labels. So if POS guesses NNP, then NER is forced to guess an NER label and not default to O.
- And vice versa, though less common, is when NER guesses that it is a named entity but the POS tagger thinks the word is an adjective. Link them together so taht the two have to be the same.
- There are lots of potential things in here for retraining the model. IDEA: Have 2 models for NLP and POS, one that uses information from the other tagger and one that doesn't. Train both on model that doesn't, then retrain both on model that does. Maybe the extra information will help them to cross-fix each other.

##Suggestions for improvement
### POS
- Use inferred NER label?

### NER
- Use POS label?

### Parser

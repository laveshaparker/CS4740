import nltk
import question_formatter

sentence = "Edmonton Oiler owner Peter Pocklington told reporters that day a year ago that Gretzky had asked to be traded to the Kings because he wanted to spend more time with his bride, Janet Jones, and that he had let Gretzky go with a heavy heart."

tokens = nltk.word_tokenize(sentence)
tags = nltk.pos_tag(sentence)


print(tokens)
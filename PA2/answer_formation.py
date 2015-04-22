import sys
import nltk
import re

from question_formatter import *
from passage_retrieval import *
from nltk.corpus import wordnet
from nltk.sem import relextract

i = int(sys.argv[1])

ENTITYMAPPINGS = {
    "PERSON": ["PERSON", "ORGANIZATION", "GPE"],
    "TIME" : ["DATE", "TIME"],
    "PLACE" : ["LOCATION", "GPE"],
    "NUMBER" : ["PERCENTAGE"],
    "NOUN" : ["ORGANIZATION", "GPE", "FACILITY"]
}

def subfinder(mylist, pattern):
    matches = []
    for i in range(len(mylist)):
        if mylist[i] == pattern[0] and mylist[i:i+len(pattern)] == pattern:
            matches.append(i)
    return matches

def extractEntities(tree, targetMatches):
	entityNames = []

	if (hasattr(tree, 'label')):
		if (tree.label() in targetMatches):
			entityNames.append([child[0] for child in tree])
		else:
			for child in tree:
				entityNames.extend(extractEntities(child, targetMatches))
	return entityNames

def return10Answers(questionNumber):
	retrieval = PassageRetrieval(questions[questionNumber], PassageRetrieval.DEV)
	TFIDF(retrieval)
	for passage in retrieval.passages_top_10_docs:
		# PERSON
		if (questions[questionNumber].descriptor.entityType == "PERSON"):
			taggedPassage = nltk.pos_tag(passage) 
			tree = nltk.ne_chunk(taggedPassage)
			potentialEntities = extractEntities(tree, ENTITYMAPPINGS[questions[i].descriptor.entityType])

			if (len(potentialEntities) == 0):
				continue;

			for entity in potentialEntities:
				indices = subfinder(passage, entity)
				print(entity)
				print(indices)
				print("")

		# NUMBER
		if (questions[questionNumber].descriptor.entityType == "NUMBER"):

			taggedPassage = nltk.pos_tag(passage) 
			tree = nltk.ne_chunk(taggedPassage)
			potentialEntities = []
			potentialEntities.extend([re.findall('\d+', token)[0] for token in passage if len(re.findall('\d+', token)) > 0])

			if (len(potentialEntities) == 0):
				continue;

			for entity in potentialEntities:
				indices = subfinder(passage, entity)
				print(entity)
				print(indices)
				print("")

		# TIME
		if (questions[questionNumber].descriptor.entityType == "TIME"):
			
			taggedPassage = nltk.pos_tag(passage) 
			tree = nltk.ne_chunk(taggedPassage)
			potentialEntities = extractEntities(tree, ENTITYMAPPINGS[questions[i].descriptor.entityType])
			potentialEntities.extend([re.findall('\d+', token)[0] for token in passage if len(re.findall('\d+', token)) > 0])

			if (len(potentialEntities) == 0):
				continue;

			for entity in potentialEntities:
				indices = subfinder(passage, entity)
				print(entity)
				print(indices)
				print("")

		# PLACE
		if (questions[questionNumber].descriptor.entityType == "PLACE"):
			taggedPassage = nltk.pos_tag(passage) 
			tree = nltk.ne_chunk(taggedPassage)
			potentialEntities = extractEntities(tree, ENTITYMAPPINGS[questions[i].descriptor.entityType])

			if (len(potentialEntities) == 0):
				continue;

			for entity in potentialEntities:
				indices = subfinder(passage, entity)
				print(entity)
				print(indices)
				print("")

		# NOUN
		if (questions[questionNumber].descriptor.entityType == "NOUN"):
			taggedPassage = nltk.pos_tag(passage) 
			tree = nltk.ne_chunk(taggedPassage)
			potentialEntities = extractEntities(tree, ENTITYMAPPINGS[questions[i].descriptor.entityType])

			if (len(potentialEntities) == 0):
				continue;

			for entity in potentialEntities:
				indices = subfinder(passage, entity)
				print(entity)
				print(indices)
				print("")

questions = loadQuestions(PassageRetrieval.DEV)
return10Answers(i)

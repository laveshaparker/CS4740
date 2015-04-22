import sys
import nltk
import re

from question_formatter import *
from passage_retrieval import *
from nltk.corpus import wordnet
from nltk.sem import relextract

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

def return10Answers(question):
	retrieval = PassageRetrieval(question, PassageRetrieval.DEV)
	TFIDF(retrieval)

	answers = []

	for passage in retrieval.passages_top_10_docs:
		# PERSON
		if (question.descriptor.entityType == "PERSON"):
			taggedPassage = nltk.pos_tag(passage) 
			tree = nltk.ne_chunk(taggedPassage)
			potentialEntities = extractEntities(tree, ENTITYMAPPINGS[question.descriptor.entityType])

			if (len(potentialEntities) == 0):
				continue;

			relevantTokenIndices = []
			potentialAnswers = []

			for token in question.descriptor.relevantTokens:
				relevantTokenIndices.extend(subfinder(passage, [token]))

			for entity in potentialEntities:
				indices = subfinder(passage, entity)

				sum = 0

				for eI in indices:
					for tI in relevantTokenIndices:
						sum += abs(eI - tI)

				if (sum <= 0):
					potentialAnswers.append((' '.join(entity), 1))
				else:
					weight = float(sum) / float(len(passage) * len(indices) * len(relevantTokenIndices))
					potentialAnswers.append((' '.join(entity), weight))

			potentialAnswers = sorted(potentialAnswers,key=lambda x: x[1])
			answers.extend([token[0] for token in potentialAnswers])

		# NUMBER
		if (question.descriptor.entityType == "NUMBER"):

			taggedPassage = nltk.pos_tag(passage) 
			tree = nltk.ne_chunk(taggedPassage)
			potentialEntities = []
			potentialEntities.extend([[re.findall('\d+', token)[0]] for token in passage if len(re.findall('\d+', token)) > 0])

			if (len(potentialEntities) == 0):
				continue;

			relevantTokenIndices = []
			potentialAnswers = []

			for token in question.descriptor.relevantTokens:
				relevantTokenIndices.extend(subfinder(passage, [token]))

			for entity in potentialEntities:
				indices = subfinder(passage, entity)

				sum = 0

				for eI in indices:
					for tI in relevantTokenIndices:
						sum += abs(eI - tI)

				if (sum <= 0):
					potentialAnswers.append((' '.join(entity), 1))
				else:
					weight = float(sum) / float(len(passage) * len(indices) * len(relevantTokenIndices))
					potentialAnswers.append((' '.join(entity), weight))

			potentialAnswers = sorted(potentialAnswers,key=lambda x: x[1])
			answers.extend([token[0] for token in potentialAnswers])

		# TIME
		if (question.descriptor.entityType == "TIME"):
			
			taggedPassage = nltk.pos_tag(passage) 
			tree = nltk.ne_chunk(taggedPassage)
			potentialEntities = extractEntities(tree, ENTITYMAPPINGS[question.descriptor.entityType])
			potentialEntities.extend([[re.findall('\d+', token)[0]] for token in passage if len(re.findall('\d+', token)) > 0])

			if (len(potentialEntities) == 0):
				continue;

			relevantTokenIndices = []
			potentialAnswers = []

			for token in question.descriptor.relevantTokens:
				relevantTokenIndices.extend(subfinder(passage, [token]))

			for entity in potentialEntities:
				indices = subfinder(passage, entity)

				sum = 0

				for eI in indices:
					for tI in relevantTokenIndices:
						sum += abs(eI - tI)

				if (sum <= 0):
					potentialAnswers.append((' '.join(entity), 1))
				else:
					weight = float(sum) / float(len(passage) * len(indices) * len(relevantTokenIndices))
					potentialAnswers.append((' '.join(entity), weight))

			potentialAnswers = sorted(potentialAnswers,key=lambda x: x[1])
			answers.extend([token[0] for token in potentialAnswers])

		# PLACE
		if (question.descriptor.entityType == "PLACE"):
			taggedPassage = nltk.pos_tag(passage) 
			tree = nltk.ne_chunk(taggedPassage)
			potentialEntities = extractEntities(tree, ENTITYMAPPINGS[question.descriptor.entityType])

			if (len(potentialEntities) == 0):
				continue;

			relevantTokenIndices = []
			potentialAnswers = []

			for token in question.descriptor.relevantTokens:
				relevantTokenIndices.extend(subfinder(passage, [token]))

			for entity in potentialEntities:
				indices = subfinder(passage, entity)

				sum = 0

				for eI in indices:
					for tI in relevantTokenIndices:
						sum += abs(eI - tI)

				if (sum <= 0):
					potentialAnswers.append((' '.join(entity), 1))
				else:
					weight = float(sum) / float(len(passage) * len(indices) * len(relevantTokenIndices))
					potentialAnswers.append((' '.join(entity), weight))

			potentialAnswers = sorted(potentialAnswers,key=lambda x: x[1])
			answers.extend([token[0] for token in potentialAnswers])

		# NOUN
		if (question.descriptor.entityType == "NOUN"):
			taggedPassage = nltk.pos_tag(passage) 

			tree = nltk.ne_chunk(taggedPassage)
			potentialEntities = extractEntities(tree, ENTITYMAPPINGS[question.descriptor.entityType])
			potentialNouns = [[token[0]] for token in taggedPassage if token[1].startswith("N")]

			if (len(potentialEntities) == 0):
				print("Empty")
				continue;

			relevantTokenIndices = []
			potentialEntityAnswers = []
			potentialNounAnswers = []

			for token in question.descriptor.relevantTokens:
				relevantTokenIndices.extend(subfinder(passage, [token]))

			for entity in potentialEntities:
				indices = subfinder(passage, entity)

				sum = 0

				for eI in indices:
					for tI in relevantTokenIndices:
						sum += abs(eI - tI)

				if (sum <= 0):
					potentialEntityAnswers.append((' '.join(entity), 1))
				else:
					weight = float(sum) / float(len(passage) * len(indices) * len(relevantTokenIndices))
					potentialEntityAnswers.append((' '.join(entity), weight))

			for entity in potentialNouns:
				indices = subfinder(passage, entity)

				sum = 0

				for eI in indices:
					for tI in relevantTokenIndices:
						sum += abs(eI - tI)

				if (sum <= 0):
					potentialNounAnswers.append((' '.join(entity), 1))
				else:
					weight = float(sum) / float(len(passage) * len(indices) * len(relevantTokenIndices))
					potentialNounAnswers.append((' '.join(entity), weight))


			potentialEntityAnswers = sorted(potentialEntityAnswers,key=lambda x: x[1])
			potentialNounAnswers = sorted(potentialNounAnswers,key=lambda x: x[1])
			answers.extend([token[0] for token in potentialEntityAnswers])
			answers.extend([token[0] for token in potentialNounAnswers[0:len(potentialEntityAnswers)]])

	return answers[0:10]

questions = loadQuestions(PassageRetrieval.DEV)

with open('output.txt', 'w') as outputFile:
	for key in questions:
		outputFile.write("qid " + str(key) + "\n")
		answers = return10Answers(questions[key])
		for i in range(len(answers)):
			outputFile.write(str(i + 1) + " " + answers[i] + "\n")
		print("Finished " + str(key))

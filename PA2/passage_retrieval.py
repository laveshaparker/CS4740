from nltk import *
from question_formatter import *
from sklearn.feature_extraction.text import *

class PassageRetrieval:
    '''
    Manages the passage retrieval step of our QA system. Given a qid,
    We create Document objects for each of the 50 relevant documents
    (found in topdocs/<dev_or_test>/top_docs.<qid>), retrieve relevant information
    on those documents and their candidate passages, and select the most-similar
    passage of those candidates.

    instance.question     : Question, the question .
    instance.documents    : Document[], all documents relevant to this question.
    instance.most_similar : Token[][], The most similar passages from the top 10 documents.
    instance.top_passage  : Token[], the most similar passage (according to tf-idf similarity measure).
    '''

    DEV = 'dev'
    TEST = 'test'

    def __init__(self, question, dev_or_test):
        self.dev_or_test = dev_or_test
        self.question = question
        self.documents = []
        self.getDocs()
        self.getMostSimilarPassages()


    # Retrieves all relevant documents for this question.
    def getDocs(self):
        filepath = 'topdocs/' + self.dev_or_test + '/top_docs.' + str(self.question.number)

        with open(filepath, 'r') as all_documents:
            docs = all_documents.read()

        qid_str = 'Qid: ' + str(self.question.number)
        split_docs = docs.split(qid_str)

        count = 1
        for document_text in split_docs[2:]:
            if count == 1:
                self.documents.append(Document(self.question, document_text.lower().strip()))
                count = 0

    # Searches all of the documents and finds the 10 passages with the highest
    # tf-idf scores.
    def getMostSimilarPassages(self):
        pass

class Document:
    '''
    Representation of a single document, including 10-grams and tf-idf
    similarity score.

    instance.question    : Question, the question.
    instance.docno       : String, the document number (in between the <DOCNO></DOCNO> tags).
    instance.docid       : Int, the unique document identifier.
    instance.rank        : Int, the rank of this document for the question denoted by qid.
    instance.score       : Float, the score of this document for the question denoted by qid.
    instance.text        : String, the text of the document, with 'p' tags removed.
    instance.description : Token[], descriptive terms for this document.
    instance.max_tfidf   : (Int, Token[]), a tuple of the maximum tf-idf score given to a passage in this document, along with the passage itself.
    instance.sentences   : Token[][], the entire text of the document, separated by sentences (sentences are represented as Token arrays).
    '''
    def __init__(self, question, document_text):
        self.question = question
        self.process_basic_info(document_text)
        self.getDescription()
        self.getSentences()
        self.calculateTfIdfSimilarities()

    # May or may not ever be implemented
    def getDescription(self):
        pass

    # Reads the document text and sets the following properties:
    # self.docno
    # self.docid
    # self.rank
    # self.score
    # self.text (well, this is set in self.removeTags())
    def process_basic_info(self, document_text):
        self.docno = document_text.split('<docno>')[1].split('</docno>')[0].strip()
        self.docid = document_text.split('<docid>')[1].split('</docid>')[0].strip()
        self.rank = int(document_text.split('rank:')[1].split('score:')[0])
        self.score = float(document_text.split('score:')[1].split('\n')[0])

        text = document_text.split('<text>')[1].split('</text>')[0].strip()
        self.text = text.replace('<p>', '').replace('</p>', '')

    def getSentences(self):
        # From http://www.nltk.org/book/ch07.html
        sentences = nltk.sent_tokenize(self.text)

        # From top answer at http://stackoverflow.com/questions/23317458/how-to-remove-punctuation
        tokenizer_sans_punctuation = RegexpTokenizer(r'((?<=[^\w\s])\w(?=[^\w\s])|(\W))+', gaps=True)
        self.sentences = [tokenizer_sans_punctuation.tokenize(sentence) for sentence in sentences]

    # Calculates all tf-idf similarities between the question and each sentence
    # in this document.
    def calculateTfIdfSimilarities(self):
        pass


def main():
    questions = loadQuestions(PassageRetrieval.DEV)
    question1 = PassageRetrieval(questions[0], PassageRetrieval.DEV)


if __name__ == "__main__": main()

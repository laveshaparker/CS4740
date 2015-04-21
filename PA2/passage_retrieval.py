class PassageRetrieval:
    '''
    Manages the passage retrieval step of our QA system. Given a qid,
    We create Document objects for each of the 50 relevant documents
    (found in topdocs/<dev_or_test>/top_docs.<qid>), retrieve relevant information
    on those documents and their candidate passages, and select the most-similar
    passage of those candidates.


    self.question     : Question, the question .
    self.documents    : Document[], all documents relevant to this question.
    self.most_similar : String[], The most similar passages from the top 10 documents.
    self.top_passage  : String, the most similar passage (according to tf-idf similarity measure).
    '''

    DEV = 'dev'
    TEST = 'test'

    def __init__(self, qid, dev_or_test):
        self.dev_or_test = dev_or_test
        self.question = Question(qid, self.dev_or_test)
        self.getDocs()
        self.getMostSimilarPassages()


    # Retrieves all relevant documents for this question.
    def getDocs(self):
        pass

    # Searches all of the documents and finds the 10 passages with the highest
    # tf-idf scores.
    def getMostSimilarPassages(self):
        pass

class Question:
    '''
    Represents a single question in one of the questions.txt files.

    self.qid      : Int, the question's unique identifier.
    self.question : String, the text containing the question.
    self.description  : String[], descriptive terms for this question, if any can be found.
    '''
    def __init__(self, qid, dev_or_test):
        self.qid = qid
        self.dev_or_test = dev_or_test
        self.getQuestion()
        self.getQuestionDescription()

    # Retrieves the question text given a question id (qid) from
    # <qadata/<self.dev_or_test>/questions.txt.
    def getQuestion(self):
        filepath = 'qadata/' + self.dev_or_test + '/questions.txt'

        with open(filepath, 'r') as questions:
            content = questions.read()

        qid_string = 'Number: ' + str(self.qid) + '\n'
        end = '\n'

        self.question = content.split(qid_string)[1].split(end)[0]

    # Attempts to infer descriptive terms for this question.
    # May or may not be completed.
    def getQuestionDescription(self):
        pass

class Document:
    '''
    Representation of a single document, including 10-grams and tf-idf
    similarity score.

    self.qid         : Int, the unique id of this document.
    self.docno       : String, the unique document identifier
    self.rank        : Int, the rank of this document for the question denoted by qid.
    self.score       : Float, the score of this document for the question denoted by qid.
    self.text        : String, the text of the document.
    self.description : String[], descriptive terms for this document.
    self.max_tfidf   : Int, the maximum tf-idf score given to a passage in this document.
    self.ten_grams   : Token[][], the total
    '''
    def __init__(self, qid, document):
        self.process_document_text()
        self.getDescription()
        self.getTenGrams()
        self.calculateTfIdf()
        self.getMaxTfIdfScore()


    # Reads the document text and sets the following properties:
    # self.docno
    # self.rank
    # self.score
    # self.text
    def process_document_text(self):
        pass

    def getDescription(self):
        pass

    def getTenGrams(self):
        pass

    def calculateTfIdf(self):
        pass

    def getMaxTfIdfScore(self):
        pass


def main():
    question1 = PassageRetrieval(0, PassageRetrieval.DEV)


if __name__ == "__main__": main()

from nltk import *
from question_formatter import *
from collections import Counter
from math import log, sqrt

class PassageRetrieval:
    '''
    Manages the passage retrieval step of our QA system. Given a qid,
    We create Document objects for each of the 50 relevant documents
    (found in topdocs/<dev_or_test>/top_docs.<qid>), retrieve relevant information
    on those documents and their candidate passages, and select the most-similar
    passage of those candidates.

    instance.question     : Question, the question.
    instance.documents    : Document[], all documents relevant to this question.
    instance.passages_top_10_docs : Token[][], The most similar passages from the top 10 documents.
    instance.top_passage  : Token[], the most similar passage (according to tf-idf similarity measure).
    '''

    DEV = 'dev'
    TEST = 'test'

    def __init__(self, question, dev_or_test):
        self.dev_or_test = dev_or_test
        self.question = question
        self.documents = []
        self.getDocs()


    # Retrieves all relevant documents for this question.
    def getDocs(self):
        filepath = 'topdocs/' + self.dev_or_test + '/top_docs.' + str(self.question.number)

        with open(filepath, 'r') as all_documents:
            docs = all_documents.read()

        qid_str = 'Qid: ' + str(self.question.number)
        split_docs = docs.split(qid_str)

        for document_text in split_docs[1:]:
            try:
                doc = Document(self.question, document_text.strip())
                self.documents.append(doc)
            except Exception as e:
                print(e)

    # Searches the top 10 documents and stores the passages with the highest
    # tf-idf scores.
    def getMostSimilarPassages(self):
        self.passages_top_10_docs = [] # may or may not update/use
        top_tfidf = 0
        self.top_passage = []
        for document in self.documents:
            if document.rank < 10:
                self.passages_top_10_docs.append(document.max_tfidf[1])
            if document.max_tfidf[0] > top_tfidf:
                top_tfidf = document.max_tfidf[0]
                self.top_passage = document.max_tfidf[1]


class Document:
    '''
    Representation of a single document, including 10-grams and tf-idf
    similarity score.

    instance.question         : Question, the question.
    instance.docno            : String, the document number (in between the <DOCNO></DOCNO> tags).
    instance.rank             : Int, the rank of this document for the question denoted by qid.
    instance.score            : Float, the score of this document for the question denoted by qid.
    instance.text             : String, the text of the document, with original case and with 'p'
                                tags removed.
    instance.max_tfidf        : (Float, Token[]), a tuple of the maximum tf-idf score given to a
                                passage in this document, along with the passage itself.
    instance.sentences        : Token[][] An array of sentences (lower-case) (represented as an
                                array of tokens hence an array of arrays)
    instance.sentences_w_case : Token[][] An array of sentences (represented as an array of tokens
                                hence an array of arrays)
    '''
    def __init__(self, question, document_text):
        self.question = question
        self.process_basic_info(document_text)
        self.getSentences()
        self.max_tfidf = (0.0, []) # Will be updated by TFIDF class

    # Reads the document text and sets the following properties:
    # self.docno
    # self.docid
    # self.rank
    # self.score
    # self.text (well, this is set in self.removeTags())
    def process_basic_info(self, document_text):
        self.docno = self.extract_info(document_text, '<DOCNO>', '</DOCNO>')
        self.rank = int(self.extract_info(document_text, 'Rank:', 'Score:'))
        self.score = float(self.extract_info(document_text, 'Score:', '\n'))
        text = self.extract_info(document_text, '<TEXT>', '</TEXT>')
        self.text = text.replace('<P>', '').replace('</P>', '')

    def extract_info(self, document_text, start_tag, end_tag):
        first_step = document_text.split(start_tag)
        if (len(first_step) < 2):
            raise Exception("Important data is missing. We won't add this document.")

        second_step = first_step[1].split(end_tag)
        if (len(second_step) < 1):
            raise Exception("Important data is missing. We won't add this document.")

        return second_step[0].strip()

    def getSentences(self):
        # From http://www.nltk.org/book/ch07.html
        sentences = nltk.sent_tokenize(self.text)

        # From top answer at http://stackoverflow.com/questions/23317458/how-to-remove-punctuation
        tokenizer_sans_punctuation = RegexpTokenizer(r'((?<=[^\w\s])\w(?=[^\w\s])|(\W))+', gaps=True)
        self.sentences_w_case = [tokenizer_sans_punctuation.tokenize(sentence) for sentence in sentences]

        # From http://www.nltk.org/book/ch07.html
        sentences = nltk.sent_tokenize(self.text.lower())
        self.sentences = [tokenizer_sans_punctuation.tokenize(sentence) for sentence in sentences]

        self.text = self.text.lower()


class TFIDF:
    '''
    Given a PassageRetrieval instance, computes the term frequency - inverse
    document frequency of its question and document passages. It is left to the
    PassageRetrieval instance to then use this information to select a most-similar passage.

    It is worth noting that in this implementation of tf-idf calculation, we do not
    use vectors the length of the number of unique terms in the document. Instead,
    for each text fragment (i.e. question text or document passage), we only
    include the terms involved in that particular text fragment to avoid having
    massive arrays with a ton of 0 counts. So in calculating the cosine similarity
    of two tf-idf vectors, we just infer count 0 for any terms that don't appear in
    the tf-idf vector.

    instance.passage_retrieval  : PassageRetrieval, an instance
    instance.unique_term_counts : {Token => Int}, a map of all unique terms (excluding
                                  stop words) and their frequency in the corpus,
                                  the corpus being the relevant documents and question text.
    '''

    def __init__(self, passage_retrieval):
        self.passage_retrieval = passage_retrieval
        self.getTermCounts()
        self.question_vector = self.tfidf(self.passage_retrieval.question.tokensCaseInsensitive)
        self.getDocTfidfs()

        # Selects the most similar passages given these tf-idf similarity
        # scores in the PassageRetrieval instance (ugly, I know)
        self.passage_retrieval.getMostSimilarPassages()

    # Iterates through every document and finds the total count of every
    # term in the corpus relevant to this question.
    def getTermCounts(self):
        self.unique_term_counts = Counter()

        # Add question terms
        for token in self.passage_retrieval.question.tokensCaseInsensitive:
            self.unique_term_counts[token] += 1

        # Add document terms
        for document in self.passage_retrieval.documents:
            tokenizer_sans_punctuation = RegexpTokenizer(r'((?<=[^\w\s])\w(?=[^\w\s])|(\W))+', gaps=True)
            tokenized_text = tokenizer_sans_punctuation.tokenize(document.text)

            # Get all of the counts for unique terms in the document
            for token in tokenized_text:
                self.unique_term_counts[token] += 1

    # Computes the tfidf score for each passage in each document.
    # Additionally sets the property max_tfidf on each document instance with
    # the closest passage information.
    def getDocTfidfs(self):
        for i in range(0, len(self.passage_retrieval.documents)):
            document = self.passage_retrieval.documents[i]
            for j in range(0, len(document.sentences)):
                passage = document.sentences[j]
                passage_w_case = document.sentences_w_case[j]
                passage_tfidf_vector = self.tfidf(passage)
                cosine_similarity = self.cosineSimilarity(passage_tfidf_vector)

                if cosine_similarity > document.max_tfidf[0]:
                    # if this passage has a higher cosine similarity to the
                    # question than the previous "max"
                    document.max_tfidf = (cosine_similarity, passage_w_case)


    # @param passage : Token[]
    def tfidf(self, passage):
        passage_vector = Counter()

        # Store the term frequencies
        for token in passage:
            passage_vector[token] += 1

        # Calculate tf-idf and replace the counts in passage_vector with those scores
        for token in passage_vector:
            # Actual tf-idf calculation
            tf = passage_vector[token]
            N = len(self.unique_term_counts)
            idf = log(N / self.unique_term_counts[token])
            passage_vector[token] = tf * idf

        return passage_vector

    # Computes the cosine similarity between the tf-idf
    # vector of a passage with that of the question.
    def cosineSimilarity(self, passage_tfidf_vector):

        # The sums of the product of each element in the question and passage
        # vectors, square of question vector, and square of passage vector, respectively.
        sumqp, sumqq, sumpp = 0, 0, 0

        for term in self.question_vector:
            sumqp += self.question_vector[term] * passage_tfidf_vector[term]
            sumqq += self.question_vector[term] * self.question_vector[term]
            sumpp += passage_tfidf_vector[term] * passage_tfidf_vector[term]

        for term in passage_tfidf_vector:
            sumqp += self.question_vector[term] * passage_tfidf_vector[term]
            sumqq += self.question_vector[term] * self.question_vector[term]
            sumpp += passage_tfidf_vector[term] * passage_tfidf_vector[term]


        if sumqq == 0 or sumpp == 0 or sumqp == 0:
            return 0

        return sumqp/(sqrt(sumqq) * sqrt(sumpp))


def main():
    questions = loadQuestions(PassageRetrieval.DEV)
    question1 = PassageRetrieval(questions[57], PassageRetrieval.DEV)
    tfidf = TFIDF(question1)

if __name__ == "__main__": main()

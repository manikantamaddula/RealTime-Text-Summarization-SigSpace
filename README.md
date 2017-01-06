# RealTime-Text-Summarization

https://youtu.be/PCWL1x4TniQ

Text Mining is one of the research areas that has been gaining lot of interest these days. The increasing availability of online information has necessitated intensive research in the area of automatic text summarization within the Natural Language Processing (NLP) community. Summarization technique preserves important information while reducing the original document. Text summarization can be implemented using Text classification. Any text mining application needs a corpus and the data (or features) that is fed to classification machine learning algorithm is huge. This study focuses on optimizing computing of machine learning by using SigSpace model. SigSpace model uses signatures instead of raw features reducing data drastically and it implements other features like independent learning, distributed learning, optimized accuracy, fuzzy matching etc. Finally, evaluation is done comparing different methodologies. And Real-time implementation in a web-client using Twitter heron, Apache Kafka, Mongo DB.

As the name says SigSpace uses signatures of a class as features to classification algorithm. In SigSpace, class based model was built by an evaluation and extension of existing machine learning models i.e., K-Means and Self Organizing Maps (SOM). The machine learning with SigSpace was modeled as a feature set with standard machine learning algorithms (e.g., Naive Bayes, Random Forests, Decision Tree) as well as a class model using L1 (Manhattan distance) and L2 (Euclidean distance) norms. SigSpace author Pradyumna, D. evaluated the model for Image Clustering and Audio classification. This project is one of the first implementations of SigSpace to text mining and classification.

Training Workflow:
Workflow:
Preprocessing is the first stage to clean text data and remove unnecessary data etc. Next, raw features are generated using stop word removal, lemmatization, Tokenizer, TFIDF, Word2vec. Using SOM data dimensions are reduced and then using KMeans, signatures of data are produced. These signatures are given to Naive Bayes Classification algorithm.

Dataset:
wikipedia, 20 News groups, BBC Sports

The size of the word vector representation sent as input to SOM is of 60.4 MB (60400 KB). And the size of signatures of all the classes combined together is 538 KB which is just 0.89 % of raw feature data and 1 % of raw text data. Here data reduction is done drastically. As the number of classes increased, the size of the signatures increased but it is minimal.

The presented approach can be said as Global SigSpace since the workflow is not implemented for each class individually. And models at different stages had be saved, so, it is code book dependent. The data reduction has been done drastically that it adds value in computing power and storage. Original data is not lost during process and distributed learning is also implemented. This model can be extended to implement incremental learning.

Realtime implementation:
From a web-client data or in this case the text to be summarized is sent to Apache Heron using a Kafka Producer. Inside heron there is a Kafka Consumer to receive text from the client. In heron, spark is used to extract features, generate signatures, to classify the sentences to sub-topics. And then summarization is done using these results. Summarized sentences are saved in mongo db which is used to display results in web-client.

Results:
For the test condition an article based on Cricket is selected. source: https://cricket.yahoo.com/news/india-pegengland-back-jennings-ton-debut-133017554.html. This article contained 28 sentences and after summarization, it contained 21 lines. This summarization efficiency can be improved by considering individual words instead of sentences while classifying to sub-topics. There was more latency than expected in real-time implementation because of two reasons: 1. Because of single machine, spout and bolt processing was serial. 2. Spark is implemented inside heron which is sub-second latency system but the latency in spark itself is in seconds. To implement the whole process in a more fast processing system needs training phase to be also implemented in the new system because text feature extraction process is code book dependent 
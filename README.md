Coarsening WordNet
=============
Abstract:

Currently used general purpose dictionaries are often too fine-grained, with narrow sense divisions that are not relevant for many Natural Language applications. WordNet, which is a widely used sense inventory for Word Sense Disambiguation, has the same problem. With different applications requiring different levels of sense granularities, producing sense clustered inventories of arbitrary sense granularity has evolved as a crucial task. We try to exploit the resources available like human-labelled sense clusterings and semi-automatically generated domain labels of synsets, to estimate the similarity between synsets. Using supervision, we learn a model which predicts the probability of any two senses of a word to be merged. To learn a more generic model, we propose a graph based approach, which allows us to use the information learnt from supervision as well. Using this complete similarity measure, we propose a simple method for clustering synsets. 

=============
Notes:

1) A demo of the system is available in Demo.java in applet package.

2) For more details refer: Merging Word Senses, M.Tech. Thesis - Sumit Bhagwani, Computer Science and Engineering, IIT Kanpur.

=============

Installation Instructions:

1. The system requires all following additional datasets in /home/USERNAME/Data folder:
  * BabelNet precompiled index version 1.1: http://lcl.uniroma1.it/babelnet/
  * SentiWordNet: http://sentiwordnet.isti.cnr.it/
  * eXtended WordNet Domains: http://adimen.si.ehu.es/web/XWND
  * navigli_sense_inventory: http://lcl.uniroma1.it/coarse-grained-aw/index.html
  * WordNet 3.0: http://wordnet.princeton.edu/

2. The USERNAME needs to be set in the code (StaticValues.java) and in properties files of 
EXTJWNL and BabelNet.

----------------------------------------------------------------------------------------------
Code Details:

1. Data Processing: The data preprocessing codes are available in krsystem.ontology.senseClustering package
2. Supervised Learning Framework: 
	* The SVM framework used for learning similarity metric is available in krsystem.ontology.senseClustering.svm package. 
	* The FeatureGenerator.java class collects the features and passes it to learning module.
	* Evaluation.java serves as the main class for the package in which we learn and evaluate our models.
3. Semi-supervised Learning Framework:
  * The SimRank framework is available in simRank package
  * The Sigmoid Transformation of SVM scores is available in svmPredictionNormalization package
  * connectedComponentAnalysis package uses the similarity metric learnt and performs clustering using Connected Components in the graph. 
  * The connectedComponentAnalysis package also includes the code to evaluate the performance of clustering obtained on standard datasets

############# FUNCTIONS #############
FakequestionFeatures = function(questionsFile, questionFeaturesFile){
    
    # Reading the Questions database
    questions = read.csv(questionsFile)
    questionIds = questions$Id
    rm(questions)
    
    # Creating the collumn names
    print(noquote("Creating the collumn names..."))
    defaultFeatures = c("Score", "ViewCount", "AnswerCount", "CommentCount", "FavoriteCount")
    derivedFeatures = c("HasAcceptedAnswer", "QuestionAgeInDays", "DaysRetired", "BodyWordsNumber", 
                        "HyperlinksNumber", "BlockquoteLinesNumber", "CodeLinesNumber", "ImagesNumber", 
                        "TitleSize", "TagsNumber", "IsCommunityOwned", "CommunityOverCreationDaysRatio", 
                        "WeigthedScore", "FirstAnswerTimeDelay", "AcceptedAnswerTimeDelay", "LastAnswerTimeDelay", 
                        "FirstAnswerScore", "AcceptedAnswerScore", "LastAnswerScore", "MaxScore", 
                        "AcceptedAnswerBodyWordsNumber", "MaxBodyWordsNumber", "AnswersWithCode", "AnswersWithImages",
                        "AnswersWithLinks", "AcceptedAnswerCommentsNumber", "CommentsNumberMedian",
                        "CommunityAndUserAnswersRatio", "QuestionCommentsScoreAverage", "MaxQuestionCommentsScore",
                        "QuestionComnentWordsNumber", "FirstCommentAge", "LastCommentAge", "AskerQuestionComments", 
                        "HasCommentAfterAnswer", "TotalAnswerComments", "AnswersWithComments", "MaxCommentsScore", 
                        "AskerAnswerComments")
    clusteringApproaches = c("Cluster")
    
    colNum = sum(1, length(defaultFeatures), length(derivedFeatures), length(clusteringApproaches))
    
    ## Generating random Features for all questions
    print(noquote("Generating random feature values for all questions..."))
    newFeatures = foreach(i = questionIds, .combine = rbind) %dopar%
{
    c(i, 
      round(rnorm(colNum - 2, mean=200, sd = 150) + i, digits = 1), 
      (round(rpois(1, 100), 0) %% 8)+1)
}
    
    questionFeatures = as.data.frame(newFeatures)
    colnames(questionFeatures) = c("Id", defaultFeatures, derivedFeatures, clusteringApproaches)
    
    # Persisting the data
    print(noquote("Persisting the questionFeatures data..."))
    write.csv(questionFeatures, file = questionFeaturesFile, row.names = F)
}

# Calculate the Cluster Centroids (DEPRECATED, it is being calculated with JAVA!)
# ClusterCentroids = function(questionFeaturesFile, clusterCentroidsFile){
#     # Reading the QuestionFeatures
#     print(noquote("Reading the QuestionFeatures data..."))
#     questionFeatures = read.csv(questionFeaturesFile)
#     
#     print(noquote("Calculating the mean of the instances per group..."))
#     clusterCentroids = ddply(questionFeatures, .(Cluster), colMeans)
#     clusterCentroids = clusterCentroids[,-1]
#     
#     # Persisting the data
#     print(noquote("Persisting the ClusterCentroids data..."))
#     write.csv(clusterCentroids, file = clusterCentroidsFile, row.names = F)
# }

# Remove useless attributes from the Questions table
QuestionsClean = function(){
    questions = read.csv("../TutorQeA/data/Questions.csv")
    
    questions = questions[,c("Id", "AcceptedAnswerId", "CreationDate", "Score", "ViewCount", 
                             "Body", "OwnerUserId", "OwnerDisplayName", "LastActivityDate", 
                             "Title", "Tags", "AnswerCount", "CommentCount", "FavoriteCount", 
                             "CommunityOwnedDate")]
    
    write.csv(questions, file = "../TutorQeA/data/Questions.csv", row.names = F)    
    
}


############# MAIN #############
library(foreach)
library(plyr)
library(doMC)

# Register the Cores of the Machine (runs only in Unix)
registerDoMC()

FakequestionFeatures("../TutorQeA/data/Questions.csv", "../TutorQeA/data/QuestionFeatures.csv")
# QuestionsClean()
ClusterCentroids("../TutorQeA/data/QuestionFeatures.csv", "../TutorQeA/data/ClusterCentroids.csv")
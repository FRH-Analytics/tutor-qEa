CreateTagLinks = function(inputDir, outputDir){
    
    print(noquote("Creating TagLinks Table..."))
    
    postTags = read.csv(paste(inputDir, "/PostTags.csv", sep =""))
    
    # Aggregate by Tag
    tagQuestionsList = dlply(postTags, .(TagId), function(x) list(Tag = x$TagId[1], 
                                                                  Questions = x$PostId))
    
    # Loop over each Tag and QuestionList 
    tagLinks = foreach(tagQuest = tagQuestionsList, .combine = rbind) %dopar%
{
    # Get the PostTags that are not from this Tag and that contains Questions in common
    relatedPostTags = postTags[postTags$TagId != tagQuest[[1]] & 
        postTags$PostId %in% tagQuest[[2]],]
    if (nrow(relatedPostTags) > 0){
        # Count the Weight of the Tag Links
        linkedTags = count(relatedPostTags, .(TagId))
        # Organize the output data.frame
        linkedTags$TagIdOne = tagQuest[[1]]
        colnames(linkedTags) = c("TagIdTwo", "Weight", "TagIdOne")
        linkedTags = linkedTags[,c("TagIdOne", "TagIdTwo", "Weight")]
        linkedTags
    }else{
        NULL
    }
}
    
    write.csv(tagLinks, file = paste(outputDir, "/TagLinks.csv", sep = ""), row.names = F)
}

CreateQuestionData = function(inputDir, outputDir){
    print(noquote("Creating the QuestionData table (keeping only the needed collumns from Questions)..."))
    questions = read.csv(paste(inputDir,"/Questions.csv", sep = ""))
    
    print(noquote("Selecting the question collumns..."))
    questions.data = questions[,c("Id", "Title", "Score", "AnswerCount", "CommentCount")]
    
    print(noquote("Adding fake Clusters..."))
    questions.data$cluster = sample(1:8, nrow(questions.data), replace=T)
    
    write.csv(questions.data, file = paste(outputDir,"/QuestionData.csv", sep = ""), row.names = F)
}

CreateQuestionAnswers = function(inputDir, outputDir){
    print(noquote("Creating the QuestionAnswers table (ordered by: QuestionId and Answer$CreationDate)..."))
    answers = read.csv(paste(inputDir,"/Answers.csv", sep = ""))
    accepted.answers = read.csv(paste(inputDir,"/Questions.csv", sep = ""))$AcceptedAnswerId
    
    print(noquote("Selecting the answer collumns..."))
    question.answers = answers[,c("ParentId", "Id", "Score", "CreationDate", "CommentCount")]
    
    print(noquote("Setting the accepted answers..."))
    question.answers$isAcceptedAnswer = question.answers$Id %in% accepted.answers
    colnames(question.answers) = c("QuestionId", "AnswerId", "Score", "CreationDate","AnswerCommentCount","IsAcceptedAnswer")
    
    print(noquote("Sorting by the QuestionId then by the date/time..."))
    question.answers = question.answers[order(question.answers$QuestionId, 
                                              strptime(question.answers$CreationDate,"%Y-%m-%d %H:%M:%S"), decreasing=F),]
    
    write.csv(question.answers, file = paste(outputDir,"/QuestionAnswers.csv", sep = ""), row.names = F)
}

CreateTagNames = function(inputDir, outputDir){
    tags = read.csv(paste(inputDir,"/Tags.csv", sep = ""))
    
    print(noquote("Selecting the tags collumns..."))
    tagNames = tags[,c("Id", "TagName")]
    
    write.csv(tagNames, file = paste(outputDir,"/TagsDictionary.csv", sep = ""),  row.names = F)
}

CopyPostTags = function(inputDir, outputDir){
    write.csv(read.csv(paste(inputDir, "/PostTags.csv", sep = "")), 
              file = paste(outputDir, "/PostTags.csv", sep = ""), row.names = F)
}

############# MAIN #############
library(foreach)
library(plyr)

# Register the Cores of the Machine (runs only in Linux)
if (Sys.info()["sysname"] == "Linux"){
    library(doMC)
    registerDoMC()
}

# Data Collect - Treatment
source("DataCollect-Treatment.R")

# Visual Analytics Project - Data Generation
print(noquote(""))
print(noquote(">> Visual Analytics Project - Data Generation"))

inputDir = "../AllData/preprocessed/"
outputDir = "../TutorQeA/data/"

CreateTagLinks(inputDir, outputDir)
CreateQuestionData(inputDir, outputDir)
CreateQuestionAnswers(inputDir, outputDir)
CreateTagNames(inputDir, outputDir)
CopyPostTags(inputDir, outputDir)
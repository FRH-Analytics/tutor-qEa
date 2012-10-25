CopyAllTables = function(inputDir, outputDir){
    print(noquote("Copying from inputDir to outputDir..."))
    questions = read.csv(paste(inputDir,"/Questions.csv", sep = ""))
    answers = read.csv(paste(inputDir,"/Answers.csv", sep = ""))
    write.csv(questions, file = paste(outputDir,"/Questions.csv", sep = ""), row.names = F)      
    write.csv(answers, file = paste(outputDir,"/Answers.csv", sep = ""), row.names = F)
    rm(questions, answers)
    
    tags = read.csv(paste(inputDir,"/Tags.csv", sep = ""))
    postTags = read.csv(paste(inputDir,"/PostTags.csv", sep = ""))
    questionComments = read.csv(paste(inputDir,"/Comments-Questions.csv", sep = ""))
    answerComments = read.csv(paste(inputDir,"/Comments-Answers.csv", sep = ""))
    
    write.csv(tags, file = paste(outputDir,"/Tags.csv", sep = ""), row.names = F)      
    write.csv(postTags, file = paste(outputDir,"/PostTags.csv", sep = ""), row.names = F)
    write.csv(questionComments, file = paste(outputDir,"/Comments-Questions.csv", sep = ""), row.names = F)
    write.csv(answerComments, file = paste(outputDir,"/Comments-Answers.csv", sep = ""), row.names = F)
}

# Remove useless attributes from the Questions and Answers table
RemoveUnusedCollumns = function(dir){
    questions = read.csv(paste(dir,"/Questions.csv", sep = ""))
    answers = read.csv(paste(dir,"/Answers.csv", sep = ""))
    
    print(noquote("Removing unused collumns from Questions and Answers..."))
    questions = questions[,c("Id", "AcceptedAnswerId", "CreationDate", "Score", "ViewCount", 
                             "Body", "OwnerUserId", "OwnerDisplayName", "LastActivityDate", 
                             "Title", "Tags", "AnswerCount", "CommentCount", "FavoriteCount", 
                             "CommunityOwnedDate")]
    answers = answers[,c("Id", "ParentId", "CreationDate", "Score", "Body", "OwnerUserId", 
                         "OwnerDisplayName", "CommentCount", "CommunityOwnedDate")]
    
    write.csv(questions, file = paste(dir,"/Questions.csv", sep = ""), row.names = F)      
    write.csv(answers, file = paste(dir,"/Answers.csv", sep = ""), row.names = F)
    rm(questions, answers)
    
    tags = read.csv(paste(dir, "/Tags.csv", sep = ""))
    tags = tags[,c("Id", "TagName", "Count")]
    write.csv(tags, paste(dir, "/Tags.csv", sep = ""), row.names = F)
}

CheckForeignKeysBetweenTables = function(dir){
    qIds = read.csv(paste(dir, "/Questions.csv", sep = ""))$Id
    
    # Answers.ParentId %in% Questions.Id
    print(noquote("Checking: Answers.ParentId %in% Questions.Id"))
    answers = read.csv(paste(dir, "/Answers.csv", sep = ""))
    answers = answers[answers$ParentId %in% qIds,] # 0 rows deleted...
    write.csv(answers, file = paste(dir, "/Answers.csv", sep = ""), row.names = F)
    rm(answers)
    
    # PostTags.PostId %in% Questions.Id
    print(noquote("Checking: PostTags.PostId %in% Questions.Id"))
    postTags = read.csv(paste(dir,"/PostTags.csv", sep = ""))
    postTags = postTags[postTags$PostId %in% qIds,] # 19 rows deleted!
    write.csv(postTags, file = paste(dir,"/PostTags.csv", sep = ""), row.names = F)
    rm(postTags)
    
    # Comment-Questions.PostId %in% Questions.Id
    print(noquote("Checking: Comment-Questions.PostId %in% Questions.Id"))
    commentQ = read.csv(paste(dir,"/Comments-Questions.csv", sep = ""))
    commentQ = commentQ[commentQ$PostId %in% qIds,] # 0 rows deleted...
    write.csv(commentQ, file = paste(dir,"/Comments-Questions.csv", sep = ""), row.names = F)
    rm(commentQ)
    
    # Comment-Answers.PostId %in% Answers.Id
    print(noquote("Checking: Comment-Answers.PostId %in% Answers.Id"))
    commentA = read.csv(paste(dir,"/Comments-Answers.csv", sep = ""))
    aIds = read.csv(paste(dir,"/Answers.csv", sep = ""))$Id
    commentA = commentA[commentA$PostId %in% aIds,] # 0 rows deleted...
    write.csv(commentA, file = paste(dir,"/Comments-Answers.csv", sep = ""), row.names = F)
    rm(commentA, aIds)
}

ReplaceNAValues = function(dir){
    print(noquote("Replacing: Questions NA's..."))
    questions = read.csv(paste(dir, "/Questions.csv", sep = ""))
    # Set -1 in AcceptedAnswerId with NA values (that means, there is no accepted answer)
    questions[is.na(questions$AcceptedAnswerId),"AcceptedAnswerId"] = -1
    # Set -1 in OwnerUserId with NA values (that means, there is no owner)
    questions[is.na(questions$OwnerUserId),"OwnerUserId"] = -1
    # Set 0 in AnswerCount, CommentCount or FavoriteCount (that means, there is no answer, 
    # comment or favorite vote)
    questions[is.na(questions$AnswerCount),"AnswerCount"] = 0
    questions[is.na(questions$CommentCount),"CommentCount"] = 0
    questions[is.na(questions$FavoriteCount),"FavoriteCount"] = 0
    write.csv(questions, file = paste(dir, "/Questions.csv", sep = ""), row.names = F)
    rm(questions)
    
    print(noquote("Replacing: Answer NA's..."))
    answers = read.csv(paste(dir, "/Answers.csv", sep = ""))
    # Set -1 in OwnerUserId with NA values (that means, there is no owner)
    answers[is.na(answers$OwnerUserId),"OwnerUserId"] = -1    
    # Set 0 in CommentCount (that means, there is no comment)
    answers[is.na(answers$CommentCount),"CommentCount"] = 0
    write.csv(answers, file = paste(dir, "/Answers.csv", sep = ""), row.names = F)
    rm(answers)
    
    print(noquote("Replacing: Comments-Questions NA's..."))
    commentsQ = read.csv(paste(dir, "/Comments-Questions.csv", sep = ""))
    # Set 0 in Score (that means, there is no vote, as the majority)
    commentsQ[is.na(commentsQ$Score),"Score"] = 0
    # Set -1 in UserId with NA values (that means, there is no owner)
    commentsQ[is.na(commentsQ$UserId),"UserId"] = -1    
    write.csv(commentsQ, file = paste(dir, "/Comments-Questions.csv", sep = ""), row.names = F)
    rm(commentsQ)
    
    print(noquote("Replacing: Comments-Answers NA's..."))
    commentsA = read.csv(paste(dir, "/Comments-Answers.csv", sep = ""))
    # Set 0 in Score (that means, there is no vote, as the majority)
    commentsA[is.na(commentsA$Score),"Score"] = 0
    # Set -1 in UserId with NA values (that means, there is no owner)
    commentsA[is.na(commentsA$UserId),"UserId"] = -1  
    write.csv(commentsA, file = paste(dir, "/Comments-Answers.csv", sep = ""), row.names = F)
    rm(commentsA)
}

################ MAIN ################
print(noquote(""))
print(noquote(">> Data Collect - Treatment"))

# Machine dependent file paths
raw.dir = "../AllData/raw/"
preProcessed.dir = "../AllData/preprocessed/"

print(noquote("Removing old pre-processed directory..."))
unlink(preProcessed.dir, recursive=T)
dir.create(preProcessed.dir, showWarnings=F)

CopyAllTables(inputDir=raw.dir, outputDir=preProcessed.dir)
RemoveUnusedCollumns(preProcessed.dir)
CheckForeignKeysBetweenTables(preProcessed.dir)
ReplaceNAValues(preProcessed.dir)
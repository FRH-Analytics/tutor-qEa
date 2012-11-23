debatePoints.comment = function(comments, questionerId, value, commentType, 
                                questionersEvent, othersEvent){
    
    if (nrow(comments) <= 0){
        return (NA)
    }
    
    ownerIds = comments[comments$UserId != -1,]$UserId
    if (length(ownerIds) <= 0){
        return (NA)
    }

    commValue = 1
    lastUserWasQuestioner = T
    
    for(i in 1:length(ownerIds)){
        ownerId = ownerIds[i]
        newValue = if (lastUserWasQuestioner){
            if (ownerId == questionerId){
                commValue + i
            }else{
                commValue + (2 * i) 
            }
        }else{
            if (ownerId == questionerId){
                commValue + (2 * i)
            }else{
                commValue + i
            }
        }

        if(ownerId == questionerId){
            lastUserWasQuestioner = T
            value = rbind(value, data.frame(Event = questionersEvent, User = "Questioner", 
                                            Post = commentType, PostId = comments$Id[i],
                                            DebatePoints = (newValue - commValue)))
        }else{
            lastUserWasQuestioner = F
            value = rbind(value, data.frame(Event = othersEvent, User = "Other", 
                                            Post = commentType, PostId = comments$Id[i],
                                            DebatePoints = (newValue - commValue)))
        }
        commValue = newValue
    }
    return(value)
}

debate.function = function(question, comments, ans){
    qId = question$Id
    userId = question$OwnerUserId
    if (userId == -1){
        return(data.frame(Event = "USER_UNIDENTIFIED", User = "Questioner", 
                          Post = "Question", PostId = qId, DebatePoints = 0))
    }

    # Value from the Question
    value = data.frame(Event = "QUESTION_CREATED", User = "Questioner", 
                       Post = "Question", PostId = qId, DebatePoints = 1)

    # Selecting the Question Comments...
    qComments = comments[comments$PostId == qId,]
    
    # Value from the Question Comments...
    qCommentValue = debatePoints.comment(comments=qComments, questionerId=userId, value, commentType="Question_Comment", 
                                         questionersEvent="QUESTIONERS_Q_COMMENT", othersEvent="OTHERS_Q_COMMENT")    
    if(class(qCommentValue) == "data.frame") value = qCommentValue
    
    # Selecting the Answers and Ordering by CreationDate...
    ans = ans[order(ans$CreationDate),]

    # Value from the Answers
    if (nrow(ans) > 0){
        for(i in 1:nrow(ans)){
            a = ans[i,]
            
            # Value from the Answer Creation (not such a big debate)
            value = rbind(value, data.frame(Event = "ANSWER_CREATED", User = "Answerer", 
                                            Post = "Answer", PostId = a$Id, DebatePoints = 1))
            
            if (a$Id == question$AcceptedAnswerId){
                # Value from the Answer Acceptance (considers that a big debate, 
                # the Questioner accepted the arguments, the value is as high as the Score)
                value = rbind(value, data.frame(Event = "ANSWER_ACCEPTED", User = "Questioner", 
                                                Post = "Answer", PostId = a$Id, 
                                                DebatePoints = a$Score))    
            }
            
            # Selecting the Answer Comment Users...
            aComments = qComments[qComments$PostId == qId,]
            
            # Value from the Question Comments...
            answerValue = debatePoints.comment(aComments, userId, value, commentType="Answer_Comment", 
                                               questionersEvent="QUESTIONERS_A_COMMENT", othersEvent="OTHERS_A_COMMENT")    
            
            if(class(answerValue) == "data.frame") value = answerValue
        }
    }
    return(value)    
}

hotness.function = function(question, ans){
    if (nrow(ans) > 0){
        ans = ans[order(ans$CreationDate, decreasing=F),][1,]
        val = as.numeric(difftime(ans$CreationDate, question$CreationDate, units="hours"))
        
        # PROBLEM 1: Some answers have been created before the question!!! 
        # (Cause: Question Merging...)
        val = if (val <= 0){1}else{val}
        
        floor(question$Score/val)
    } else {
        # PROBLEM 2: No answer!
        -1
    }
}

MainDerivedFeatures = function(){
    library(foreach)
    
    # Register the Cores of the Machine (runs only in Linux)
    if (Sys.info()["sysname"] == "Linux"){
        library(doMC)
        registerDoMC()
    }
    
    preprocessedDir = "../AllData/preprocessed/"
    dir.create("output/", showWarnings=F)
    
    print(noquote("Reading Data..."))
    questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
    answers = read.csv(paste(preprocessedDir, "Answers.csv", sep = ""), header = T)
    qComments = read.csv(paste(preprocessedDir, "Comments-Questions.csv", sep = ""), header = T)
    
    print(noquote("Calculating: New Features...")) 
    DerivedFeatures = foreach(id = questions$Id, .combine = rbind) %dopar%{
        question = questions[questions$Id == id,]
        ans = answers[answers$ParentId == id,]
        
        debate = debate.function(question, qComments, ans)
        hotness = hotness.function(question, ans)
        data.frame(Id = id, 
                   Debate = sum(debate$DebatePoints),
                   Hotness = hotness)
    }
    
    print(noquote("Persisting: DerivedFeatures..."))
    write.csv(DerivedFeatures, file = paste(preprocessedDir,
                                             "DerivedFeatures.csv", sep = ""), row.names = F)   
}
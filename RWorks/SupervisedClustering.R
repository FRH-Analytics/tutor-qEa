countWords = function(body){
    return(length(strsplit(body, " ")[[1]]))
}

content.function = function(qId, questions, answers, qComments, aComments){

    ans = answers[answers$ParentId == qId, ]
    ans = ans[order(ans$CreationDate),]
    qComm = qComments[qComments$PostId == qId,]
    
    accAnsId = questions[questions$Id == qId,]$AcceptedAnswerId
    
    content = NULL
    
    # Sum the question features
    # VALUE: Question with positive score comments
    qValue = sum(qComm$Score)
    
    content = data.frame(qValue = qValue)
    allAnsData = NULL
    
    ansAfterAccepted = 0
    for(i in 1:nrow(ans)){
        a = ans[i,]
        aComm = aComments[aComments$PostId == a$Id,]

        diffScoreQuantil = max(max(a$Score - quantile(ans$Score, .25), a$Score - quantile(ans$Score, .75)), 0)
        
        # VALUE: Answer Score, Sum(AnswerComment$Score)
        ansValue = 2 * a$Score + sum(aComm$Score) 
        # VALUE: BodySize 
        ansValue = ansValue + sqrt(countWords(as.character(a$Body))) * diffScoreQuantil
        
        ansData = data.frame(Id = a$Id, CreationDate = a$CreationDate, 
                             Score = a$Score, SumCommentScore = sum(aComm$Score), 
                             BodyWordsNumber = countWords(as.character(a$Body)), 
                             AcceptedAnswer = F, AfterAccepted = ansAfterAccepted)
        
        if (ansAfterAccepted > 0){
            # TODO: This should be changed to check if the answer ocurred after the VoteEvent, AcceptedByOriginator
            # VALUE: Answers after the AcceptedEvent
            ansValue = ansValue + (ansValue * ansAfterAccepted/2)
            ansAfterAccepted = ansAfterAccepted + 1
            
        }else{
            if (accAnsId == a$Id){
                # VALUE: Accepted Answer gain a minimum value
                ansValue = ansValue + 10
                ansData$AcceptedAnswer = T
                ansAfterAccepted = ansAfterAccepted + 1
            }
        }
        content = cbind(content, ansValue)
        allAnsData = rbind(allAnsData, ansData)
    }
    
    colnames(content) = c(paste("Q_", qId, sep = ""), paste("A_", ans$Id, sep = ""))
    return(list(qId = qId, content = sum(content), content.sep = content, ansData = allAnsData))
}

plotQuestion = function(index, qContent){
    x = colnames(qContent[[index]]$content.sep)
    y = as.numeric(qContent[[index]]$content.sep)
    accAnsIndex = sum((qContent[[index]]$ansData$AcceptedAnswer == T) * (1: nrow(qContent[[index]]$ansData)))
    
    plot(y, type = "h", main = paste("Question", qContent[[index]]$qId, " - Content =", qContent[[index]]$content),
         xlab = "Event", ylab = "Content", xaxt = "n")
    axis(1, at=1:length(y), labels=x)
    pointSize = 2
    points(1, y[1], type = "b", pch = 20, cex = pointSize, col = "blue")
    points(2:length(y), y[-1], type = "b", pch = 20, cex = pointSize, col = "red")
    if (accAnsIndex != 0){
        points(accAnsIndex + 1, y[accAnsIndex + 1], type = "b", pch = 17, cex = pointSize, col = "green")
    }
}

# MAIN
library(foreach)
library(doMC)
registerDoMC()

preprocessedDir = "../AllData/preprocessed/"
# print(noquote("Reading Data..."))
# questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
# answers = read.csv(paste(preprocessedDir, "Answers.csv", sep = ""), header = T)
# qComments = read.csv(paste(preprocessedDir, "Comments-Questions.csv", sep = ""), header = T)
# aComments = read.csv(paste(preprocessedDir, "Comments-Answers.csv", sep = ""), header = T)

qContent = foreach(id = questions$Id[11:20]) %dopar%{
    content.function(id, questions, answers, qComments, aComments)
}

# TESTING - PLOTTING
require(reshape)
require(ggplot2)
dir.create("output2", showWarnings=F)

png("output2/SuperCluster-Content.png", width = 1000, height = 1200)
par(mfrow = c(5, 2))
for (i in 1:length(qContent)){
    plotQuestion(i, qContent)
}
dev.off()

for (i in 1:10){
    a = qContent[[i]]$ansData
    b = melt(a[,-ncol(a)], id.vars=c("Id", "CreationDate"))
    png(paste("output2/SuperCluster: Q_", qContent[[i]]$qId,"-A_", paste(a$Id, collapse = "-"), ".png", sep = ""), width = 600, height = 1000)
    print(ggplot(b, aes(x = CreationDate, y = value)) + xlab("CreationDate") + ylab("") + 
        geom_bar(aes(fill = variable), width = .5) + facet_wrap(~variable, ncol=1,scales="free") + 
        theme(legend.position="none"))
    dev.off()
}
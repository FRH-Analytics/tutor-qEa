########## FUNCTIONS ##########
require(ggplot2)
require(gridExtra)

plot.pdf.group = function(df, var, groupVar = "", color = "gray"){
    if (groupVar != ""){
        ggplot(df, aes_string(x = var, col = groupVar, fill = groupVar)) + 
            geom_density() + facet_grid(paste("~", groupVar, sep = "")) + 
            theme(legend.position="none")
    }else{
        ggplot(df, aes_string(x = var)) + geom_histogram(binwidth = 1, fill = color, colour = color)
    }
}

plot.pdf = function(df, var){
    plot(prop.table(table(df[,var])), ylab = "Probabilidade", main = var)
}

plot.boxplot = function(df, var, groupVar = "", horizontal = F){
    if (groupVar != ""){
        boxplot(formula = as.formula(paste(var, "~", groupVar)), data = df, outline=T, 
                horizontal=horizontal, main = var)
    }else{
        boxplot(df[,var], outline=T, horizontal=horizontal,
                main = var)
    }
}

plot.sampĺeSize = function(df, var){
    plot(c(1, 11), type="n", axes=F, xlab="", ylab="")
    text(1.5, 9, labels = "Sample Size", cex = 2.5)
    text(1.5, 4, labels = paste(length(df[,var])), cex = 4)    
}

plot.questions = function(questions, outputDir){
    png(paste(outputDir, "Questions-PDFs.png", sep =""), width = 800, height = 1000)    
    par(mfrow = c(3,2))
    plot.pdf(questions, var = "Score")
    plot.pdf(questions, var = "ViewCount")
    plot.pdf(questions, var = "AnswerCount")
    plot.pdf(questions, var = "CommentCount")
    plot.pdf(questions, var = "FavoriteCount")
    dev.off()
    
    # PLot boxplots (grouped)
    png(paste(outputDir, "Questions-Boxplots.png", sep = ""), width = 600, height = 800)
    par(mfrow = c(5, 1))
    plot.boxplot(questions, var = "Score", horizontal = T)
    plot.boxplot(questions, var = "ViewCount", horizontal = T)
    plot.boxplot(questions, var = "AnswerCount", horizontal = T)
    plot.boxplot(questions, var = "CommentCount", horizontal = T)
    plot.boxplot(questions, var = "FavoriteCount", horizontal = T)
    dev.off()
}

plot.questions2 = function(questions, outputDir){
    questions$IsCommunity = NULL
    questions[questions$CommunityOwnedDate != "", "IsCommunity"] = "IsCommunity"
    questions[questions$CommunityOwnedDate == "", "IsCommunity"] = "IsNotCommunity"
    
    png(paste(outputDir, "Questions_IsCommunity-Boxplots.png", sep = ""), width = 600, height = 1200)
    par(mfrow = c(5, 1))
    plot.boxplot(questions, var = "Score", groupVar = "IsCommunity", horizontal = T)
    plot.boxplot(questions, var = "ViewCount", groupVar = "IsCommunity", horizontal = T)
    plot.boxplot(questions, var = "AnswerCount", groupVar = "IsCommunity", horizontal = T)
    plot.boxplot(questions, var = "CommentCount", groupVar = "IsCommunity", horizontal = T)
    plot.boxplot(questions, var = "FavoriteCount", groupVar = "IsCommunity", horizontal = T)
    dev.off()
    
    # NOT USED: we cant see the differences...
    #     png(paste(outputDir, "Questions_IsCommunity-PDFs.png", sep = ""), width = 800, height = 1600)
    #     p1 = plot.pdf.group(questions, var = "Score", groupVar = "IsCommunity")
    #     p2 = plot.pdf.group(questions, var = "ViewCount", groupVar = "IsCommunity")
    #     p3 = plot.pdf.group(questions, var = "AnswerCount", groupVar = "IsCommunity")
    #     p4 = plot.pdf.group(questions, var = "CommentCount", groupVar = "IsCommunity")
    #     p5 = plot.pdf.group(questions, var = "FavoriteCount", groupVar = "IsCommunity")
    #     grid.arrange(p1, p2, p3, p4, p5, ncol = 1)
    #     dev.off()
    
}

########## MAIN ########## 
dir = "../AllData/preprocessed/"
theme_set(theme_bw())

questions = read.csv(paste(dir, "Questions.csv", sep = ""), header = T)
plot.questions(questions, outputDir = "output/")
plot.questions2(questions, outputDir = "output/")

# # Data Analysis
# RemoveSomeAttributes = function(dir){
#     print(noquote("Removing Att: ..."))
#     print(noquote("Removing Att: DONE!"))
#     print(noquote(""))
#     
#     # The Foreign keys should be checked again!
#     # source("DataPreProcessment.R")
#     # CheckForeignKeysBetweenTables(dir)
# }

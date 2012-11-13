Plot.PMF = function(df, var){
    plot(prop.table(table(df[,var])), ylab = "Probabilidade", main = var)
}

run.Kmedians = function(features){
    # Normalize data
    norm0_1 = function(data){
        (data - min(data))/(max(data) - min(data))
    }
    
    features2 = as.data.frame(sapply(features, function(x) norm0_1(x), USE.NAMES=T))
    
    kcca(x = features2, k = 4, family=kccaFamily("kmedians"))
    
    # image(cl)
    # barplot(cl)
}

run.Kmeans = function(features){
    kmeansruns(data = features, criterion="ch",
               krange = 3:5,                     # krange: range of the number of clusters
               iter.max = 100,                   # iter.max: number of iterations per execution
               runs = 10,                        # runs: Number of restarts each execution
               scaledata = T)                    # scaledata: center the data
}

persist.clustering = function(clusters, tree, dimensionName){
    print(noquote(paste("Persisting: ", dimensionName, "...", sep = "")))
    
    # Output to file...
    sink(file = paste("output/Analysis-", tolower(dimensionName), ".txt", sep = ""))
    write(paste("================ Análise da Dimensão:", dimensionName, "================"), file = "")
    write("", file = "")
    
    write(">> Tamanho dos Clusters:", file = "")
    count = count(clusters)
    colnames(count) = c("Cluster", "Count")
    print(count)
    write("", file = "")
    
    write(">> Resultado da Árvore de Decisão:", file = "")
    print(tree)
    sink()
    # Output to console again...
    
    print(noquote("Persisting: DONE!"))
    print(noquote(""))
}

clustering = function(preprocessedDir, clusteringDir, dimensionName, createFeaturesFunc){
    
    print(noquote(paste("Clustering: ", dimensionName, "...", sep = "")))
    
    featuresFile = paste(clusteringDir, "/", tolower(dimensionName), ".csv", sep = "")
    
    if (file.exists(featuresFile)){
        print(noquote("Loading Features..."))
        features = read.csv(featuresFile)
    }else{
        features = createFeaturesFunc(preprocessedDir)
        
        print(noquote("Saving Features..."))
        write.csv(features, file = featuresFile, row.names = F)
    }   
    
    if (! "Cluster.KMeans" %in% colnames(features)){
        # Excluding the ID and Cluster.KMeans collumns
        # features = features[,-ncol(features)]
        print(noquote("Clustering: Running KMeans..."))
        model = run.Kmeans(features[,-1])
        
        print(noquote("Clustering: Persisting..."))
        features$Cluster.KMeans = model$cluster
        write.csv(features, file = featuresFile, row.names = F)        
        
    }
    
    print(noquote("Clustering: DONE!"))
    print(noquote(""))
}

plot.clustering = function(clusteringDir, dimensionName){
    
    print(noquote(paste("Plotting: ", dimensionName, "...", sep = "")))
    
    features = read.csv(paste(clusteringDir, tolower(dimensionName), ".csv", sep = ""), header = T)
    clusters = features$Cluster.KMeans
    
    # Exclude the Id and Cluster attributes
    features = features[,-c(1, ncol(features))]
    
    # Plot Results
    print(noquote("Plotting ScatterPlot..."))
    png(paste("output/Cluster-Kmeans-", dimensionName, ".png", sep = ""), width = 850, height = 900)
    plot(features, col = (clusters + 2), 
         main = paste(toupper(dimensionName), " clustering - KMeans with ", length(unique(clusters)), " center(s)", sep = ""))
    dev.off()
    
    print(noquote("Plotting: DONE!"))
    print(noquote(""))
}

analyse.clustering = function(clusteringDir, dimensionName){
    
    print(noquote(paste("Analysing: ", dimensionName, "...", sep = "")))
    
    features = read.csv(paste(clusteringDir, tolower(dimensionName), ".csv", sep = ""), header = T)
    id = features$Id
    features = features[,-1]
    
    print(noquote("Analysing: Creating Decision Tree..."))
    tree = rpart(Cluster.KMeans ~ ., features, method = "class", parms = list(split = "gini"))
    
    png(paste("output/Cluster-KMeans-DecisionTree-", dimensionName, ".png",sep = ""), width = 700, height = 600)
    plot(tree, uniform = T, branch = .5, margin = .1, 
         main = paste(toupper(dimensionName), "dimension - Decision Tree Analysis"))
    text(tree, cex = 1, all = T) 
    dev.off()

    print(noquote("Analysing: Persisting Clustering and Tree..."))
    persist.clustering(features$Cluster.KMeans, tree, dimensionName)
    
    print(noquote("Analysing: Generating Feature PMFs..."))
    features = features[,-ncol(features)]
    
    nRows = ceiling(ncol(features)/2)
    png(paste("output/PMF-Features-", dimensionName, ".png", sep =""), width = 800, height = (nRows * 300))
    par(mfrow = c(nRows,2))
    for(i in 1:ncol(features)){
        Plot.PMF(features, var = colnames(features)[i])
    }
    dev.off()
    
    print(noquote("Analysing: DONE!"))
    print(noquote(""))
}

createFeatures.popularity = function(preprocessedDir){
    print(noquote("Reading Data..."))
    questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
    
    print(noquote("Creating Attributes..."))
    features = questions[,c("Id", "Score", "ViewCount", "CommentCount", 
                            "AnswerCount", "FavoriteCount")]
    
    return(features)
}
    
createFeatures.difficulty = function(preprocessedDir){
    print(noquote("Reading Data..."))
    questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
    answers = read.csv(paste(preprocessedDir, "Answers.csv", sep = ""), header = T)
    
    print(noquote("Creating Attributes..."))
    features = data.frame(Id = questions$Id,
                          AnswerCount = questions$AnswerCount,
                          CommentCount = questions$CommentCount,
                          HasAcceptedAnswer = (questions$AcceptedAnswerId != -1))
    
    features$WeeksToMaxScoreAnswer = foreach(i = 1:nrow(features), .combine = rbind) %dopar%{
        ans = answers[answers$ParentId == features$Id[i],]
        
        if (nrow(ans) > 0){
            ans = ans[order(ans$Score, ans$CreationDate, decreasing=T),][1,]
            val = floor(difftime(ans$CreationDate, questions$CreationDate[i], units="weeks"))
            
            # PROBLEM 1: Some answers have been created before the question!!! 
            # (Cause: Question Merging...)
            val = if (val < 0){0}else{val}
        } else {
            # PROBLEM 2: No answer! (Probably very difficult! We set -1...)
            -1
        }
    }
    
    return(features)
}

createFeatures.longevity = function(preprocessedDir){
    # TODO!
    # List Attributes...
}

createFeatures.coverage = function(preprocessedDir){
  print(noquote("Reading Data..."))
  questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
  answers = read.csv(paste(preprocessedDir, "Answers.csv", sep = ""), header = T)
  
  print(noquote("Creating Attributes..."))
  questions$BodySize = foreach(i = 1:nrow(questions), .combine = rbind) %dopar%{    
    size = length(unlist(strsplit(as.character(questions[i,"Body"])," ")))
  }
  
  questions$TagQuantity = foreach(i = 1:nrow(questions), .combine = rbind) %dopar%{
    post = read.csv(paste(preprocessedDir,"PostTags.csv",sep=""),header=T)$PostId
    post.id = questions[i,"Id"]
    quantity = length(which(post == post.id))
  }
  
  questions$AnswersBodySizeMedian = foreach(i = 1:nrow(questions), .combine = rbind) %dopar%{
    post.id = questions[i,"Id"]
    post.answers = answers[answers$ParentId == post.id,]
    
    body.sizes = foreach(i = 1:nrow(post.answers), .combine = rbind) %dopar%{
      size = length(unlist(strsplit(as.character(post.answers[i,"Body"])," ")))
    }
    
    median = median(body.sizes)
  }
  
  features = questions[,c("Id","AnswerCount","BodySize","TagQuantity","AnswersBodySizeMedian")]
  
  return(features)
  
}

createFeatures.supervised = function(preprocessedDir){
  print(noquote("Reading Data..."))
  questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
  answers = read.csv(paste(preprocessedDir, "Answers.csv", sep = ""), header = T)
  question.features = read.csv(paste(preprocessedDir, "QuestionFeatures.csv", sep = ""), header = T)

  print(noquote("Merging Features by Id..."))
  features = data.frame(Id = questions$Id,
                        Coverage = questions$AnswerCount)
  features = merge(features, question.features, by = "Id")
  
  print(noquote("Calculating the Empathy..."))
  features$Empathy = foreach(qId = features$Id, .combine = rbind) %dopar%{
      question = questions[questions$Id == qId,]
      ans = answers[answers$ParentId == qId,]
      
      qScore = questions[questions$Id == qId,"Score"]
      
      if (nrow(ans) > 0){
          ans = ans[order(ans$CreationDate, decreasing=F),][1,]
          val = as.numeric(difftime(ans$CreationDate, question$CreationDate, units="hours"))
          
          # PROBLEM 1: Some answers have been created before the question!!! 
          # (Cause: Question Merging...)
          val = if (val <= 0){-1}else{val}
          result = floor(qScore/val)
      } else {
          # PROBLEM 2: No answer! (Probably very difficult! We set -1...)
          result = -1
      }
      result
  }
  
  # Normalize the Feature between 0 and 1
  features$Coverage = (features$Coverage - min(features$Coverage))/(max(features$Coverage) - min(features$Coverage))
  features$Dialogue = (features$Dialogue - min(features$Dialogue))/(max(features$Dialogue) - min(features$Dialogue))
  features$Empathy = (features$Empathy - min(features$Empathy))/(max(features$Empathy) - min(features$Empathy))
  
  return(features)
}

####################################################
####################### MAIN #######################
####################################################
library(fpc, quietly = T)
library(flexclust, quietly = T) 
library(rpart, quietly=T)
require(foreach, quietly = T)
require(doMC, quietly = T)
library(scatterplot3d, quietly=T)
library(plyr, quietly=T)

if (Sys.info()["sysname"] == "Linux"){
  library(doMC)
  registerDoMC()
}

dir.create("../AllData/clustering", showWarnings=F)
preprocessedDir = "../AllData/preprocessed/"
clusteringDir = "../AllData/clustering/"

# clustering(preprocessedDir, clusteringDir, "Difficulty", createFeatures.difficulty)
# clustering(preprocessedDir, clusteringDir, "Popularity", createFeatures.popularity)
# clustering(preprocessedDir, clusteringDir, "Coverage", createFeatures.coverage)
clustering(preprocessedDir, clusteringDir, "Supervised", createFeatures.supervised)

# plot.clustering(clusteringDir, "Difficulty")
# plot.clustering(clusteringDir, "Popularity")
# plot.clustering(clusteringDir, "Coverage")
plot.clustering(clusteringDir, "Supervised")

# analyse.clustering(clusteringDir, "Difficulty")
# analyse.clustering(clusteringDir, "Popularity")
# analyse.clustering(clusteringDir, "Coverage")
analyse.clustering(clusteringDir, "Supervised")
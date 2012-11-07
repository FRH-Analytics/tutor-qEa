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

cluster.popularity = function(preprocessedDir, clusteringDir){
    
    featuresFile = paste(clusteringDir, "/difficulty.csv", sep = "")
    
    if (file.exists(featuresFile)){
        print(noquote("Loading Attributes..."))
        features = read.csv(featuresFile)
    }else{
        print(noquote("Reading Data..."))
        questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
        
        print(noquote("Creating Attributes..."))
        features = questions[,c("Id", "Score", "ViewCount", "CommentCount", "AnswerCount", "FavoriteCount")]
        
        print(noquote("Saving Attributes..."))
        write.csv(features, file = paste(clusteringDir, "/popularity.csv", sep = ""), row.names = F)        
    }
    
    # Exclude the ID from the cluster...
    features = features[,-c(1)]
    
    # Run Clustering
    print(noquote("Clustering..."))
    model = run.Kmeans(features)
    
    # Plot Results
    print(noquote("Plotting ScatterPlot..."))
    png("output/Cluster-Kmeans-Popularity.png", width = 850, height = 900)
    plot(data, col = (model$cluster + 2), 
         main = paste("KMeans Clusters com", nrow(model$centers), "centro(s)"))
    dev.off()
}

cluster.difficulty = function(preprocessedDir, clusteringDir){
    
    featuresFile = paste(clusteringDir, "/difficulty.csv", sep = "")
    
    if (file.exists(featuresFile)){
        print(noquote("Loading Attributes..."))
        features = read.csv(featuresFile)
    }else{
        print(noquote("Reading Data..."))
        questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
        answers = read.csv(paste(preprocessedDir, "Answers.csv", sep = ""), header = T)
        
        print(noquote("Creating Attributes..."))
        features = data.frame(Id = questions$Id,
                              AnswerCount = questions$AnswerCount,
                              CommentCount = questions$CommentCount,
                              HasAcceptedAnswer = (questions$AcceptedAnswerId != -1))
        
        features$TimeToMaxScoreAnswer = foreach(i = 1:nrow(features), .combine = rbind) %do%{
            ans = answers[answers$ParentId == features$Id[i],]
            
            if (nrow(ans) > 0){
                ans = ans[order(ans$Score, ans$CreationDate, decreasing=T),][1,]
                val = as.numeric(difftime(ans$CreationDate , questions$CreationDate[i], units="hours"))
                
                # PROBLEM 1: Some answers have been created before the question!!! 
                # (Cause: Question Merging...)
                val = if (val < 0){0}else{val}
            } else {
                #print(i)
                # PROBLEM 2: No answer! (Probably very difficult! We set -1...)
                -1
            }
        }
        
        print(noquote("Saving Attributes..."))
        write.csv(features, file = paste(clusteringDir, "/difficulty.csv", sep = ""), row.names = F)
    }   
    
    # Exclude the ID from the cluster...
    features = features[,-c(1)]
    
    print(noquote("Clustering..."))
    model = run.Kmeans(features[,-c(1)])
    
    # Plot Results
    print(noquote("Plotting ScatterPlot..."))
    png("output/Cluster-Kmeans-Difficulty.png", width = 850, height = 900)
    plot(features, col = (model$cluster + 2), 
         main = paste("KMeans Clusters com", nrow(model$centers), "centro(s)"))
    dev.off()
    
    print(noquote("Plotting ScatterPlot 3D... (not so useful)"))
    png("output/Cluster-Kmeans-Difficulty-3D.png", width = 850, height = 900)
    scatterplot3d(x = features$AnswerCount, 
                  y = features$CommentCount,
                  z = features$HasAcceptedAnswer, 
                  xlab = "x: Answer Count",
                  ylab = "y: Comment Count",
                  zlab = "z: Has Accepted Answer",
                  color = (model$cluster + 2),
                  main = paste("KMeans Clusters com", nrow(model$centers), "centro(s)"))
    legend("bottomright",  pch = rep(1, 6), col=as.integer(sort(unique(model$cluster) + 2)), 
           legend = paste("Cluster", sort(unique(model$cluster))))
    dev.off()
}

####################################################
####################### MAIN #######################
####################################################
library(fpc, quietly = T)
library(flexclust, quietly = T) 
require(foreach, quietly = T)
require(doMC, quietly = T)
library(scatterplot3d, quietly=T)

registerDoMC()

dir.create("../AllData/clustering", showWarnings=F)
preprocessedDir = "../AllData/preprocessed/"
clusteringDir = "../AllData/clustering/"

# cluster.difficulty(preprocessedDir, clusteringDir)
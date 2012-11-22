clustering.runKCenters = function(data, kccaFamilyName, outputDir){
    norm.0_1 = function(data){
        (data - min(data))/(max(data) - min(data))
    }
    norm.standardScore = function(data){
        (data - mean(data))/sd(data)
    }
    
    data = as.data.frame(sapply(data, norm.standardScore))
    
    krange = 2:8
    repetitions = 10
    
    stepFlexclust(data, k = krange, nrep = repetitions, save.data = F,
                  FUN = kcca, family=kccaFamily(kccaFamilyName), multicore = F)
}

clustering.kCentroidsSelection = function(allClusteringObj, data, kccaFamilyName, 
                                          outputDir, dimensionName){
    
    getUserInput.intRange = function(msg, startRange, endRange){
        n = NA
        while(! n %in% startRange:endRange){
            n = readline(paste(msg,  " (", startRange," to ", endRange, "): ", sep = ""))
            n = ifelse(grepl(paste("[^", startRange, "-", endRange, "]",sep = ""),n), NA, as.integer(n))
        }
        return(n)
    }
    
    print(noquote(paste(kccaFamilyName, "Centroids Selection: Plotting Sum of Within distances...")))
    png(paste(outputDir, dimensionName, "-", kccaFamilyName, "-K_SumWithins.png", sep = ""), width = 600, height = 500)
    plot(allClusteringObj, main = kccaFamilyName)
    dev.off()

    print(noquote(paste(kccaFamilyName, "Centroids Selection: Plotting the Neighborhood graph...")))
    xCol = getUserInput.intRange("Select the X axis collumn", 1, ncol(data))
    yCol = getUserInput.intRange("Select the Y axis collumn", 1, ncol(data))
    
    png(paste(outputDir, dimensionName, "-", kccaFamilyName, "-K_VoronoiCells.png", sep = ""), width = 900, height = 1200)
    nRows = ceiling(length(allClusteringObj@k)/2)
    par(mfrow = c(nRows,2))
    for(i in 1:length(allClusteringObj@k)){
        image(getModel(allClusteringObj, i), data = data, which = c(xCol, yCol), 
              xlab = colnames(data)[xCol], ylab = colnames(data)[yCol])
        title(paste(kccaFamilyName, "with", allClusteringObj@k[i], "Centroids"))
    }
    dev.off()
    print(noquote(paste(kccaFamilyName, "Centroid Selection: Analyse the Neighborhood and Voronoi cells and...")))
    centroidsNum = getUserInput.intRange("Select the Centroids Number", min(allClusteringObj@k), 
                                         max(allClusteringObj@k))

    print(noquote(paste(kccaFamilyName, "Centroids Selection: Plotting Barchart of selected Cluster...")))
    png(paste(outputDir, dimensionName, "-", kccaFamilyName, "-Barchart.png", sep = ""), width = 800, height = 850)
    plot(barchart(getModel(allClusteringObj, centroidsNum-1), data = data, 
             main = paste("Barchart\n", kccaFamilyName, "with", centroidsNum, " Centroids")))
    dev.off()
    
    print(noquote(paste(kccaFamilyName, "Centroids Selection: DONE!")))
    print(noquote(""))
    
    return(centroidsNum)
}

clustering = function(preprocessedDir, clusteringDir, dimensionName, 
                      createFeaturesFunc, outputDir){
    
    print(noquote(paste("Clustering: ", dimensionName, "...", sep = "")))
    
    featuresFile = paste(clusteringDir, tolower(dimensionName), ".csv", sep = "")
    
    if (file.exists(featuresFile)){
        print(noquote("Loading Features..."))
        features = read.csv(featuresFile)
    }else{
        features = createFeaturesFunc(preprocessedDir)
        
        print(noquote("Saving Features..."))
        write.csv(features, file = featuresFile, row.names = F)
    }   

    # Create a Data only Data.Frame, removing ID and possible previous clustering
    onlyData = features
    onlyData[,"Id"] = NULL
    onlyData[,"Cluster.KMeans"] = NULL
    onlyData[,"Cluster.KMedians"] = NULL

    ## KMEANS
    kmeansFile = paste(clusteringDir, tolower(dimensionName), "-Kmeans.dat", sep = "")
    if (file.exists(kmeansFile)){
        print(noquote("Clustering: Loading KMeans..."))
        load(kmeansFile)
    }else{
        print(noquote("Clustering: Running KMeans..."))
        allKMeans = clustering.runKCenters(onlyData, kccaFamilyName="kmeans", outputDir)
        save(allKMeans, file = kmeansFile)
    }
    
    centroidNum.KMeans = clustering.kCentroidsSelection(allKMeans, onlyData, kccaFamilyName="Kmeans", 
                                                        outputDir, dimensionName)
    features$Cluster.KMeans = getModel(allKMeans, centroidNum.KMeans - 1)@cluster
    
    ## KMEDIANS
    kmediansFile = paste(clusteringDir, tolower(dimensionName), "-Kmedians.dat", sep = "")
    if (file.exists(kmediansFile)){
        print(noquote("Clustering: Loading KMedians..."))
        load(kmediansFile)
    }else{
        print(noquote("Clustering: Running KMedians..."))
        allKMedians = clustering.runKCenters(onlyData, kccaFamilyName="kmedians", outputDir)
        save(allKMedians, file = kmediansFile)
    }
    
    centroidNum.KMedians = clustering.kCentroidsSelection(allKMedians, onlyData, kccaFamilyName="Kmedians", 
                                                          outputDir, dimensionName)    
    features$Cluster.KMedians = getModel(allKMedians, centroidNum.KMedians - 1)@cluster
    
    ## PERSISTING Clusters in featureFile
    print(noquote("Clustering: Persisting..."))
    write.csv(features, file = featuresFile, row.names = F)
    
    print(noquote("Clustering: DONE!"))
    print(noquote(""))
}

plot.clustering = function(clusteringDir, outputDir, dimensionName, 
                           kccaFamilyName, clusterCol, nonFeatureCols){
    
    print(noquote(paste("Plotting: ", dimensionName, "...", sep = "")))
    
    features = read.csv(paste(clusteringDir, tolower(dimensionName), ".csv", sep = ""), header = T)
    clusters = features[, clusterCol]
    
    # Exclude the Id and Cluster attributes
    features = features[,-nonFeatureCols]
    
    # Plot Results
    print(noquote("Plotting: ScatterPlot..."))
    png(paste(outputDir, dimensionName, "-", kccaFamilyName, "-Scatteplot.png", sep = ""), width = 850, height = 900)
    plot(features, col = (clusters + 2), 
         main = paste(dimensionName, " clustering - ", kccaFamilyName, 
                      " with ", length(unique(clusters)), " center(s)", sep = ""))
    dev.off()
    
    print(noquote("Plotting: DONE!"))
    print(noquote(""))
}

analyse.clustering = function(clusteringDir, outputDir, dimensionName, kccaFamilyName, 
                              clusterColName, nonFeatureCols){
    
    print(noquote(paste("Analysing: ", dimensionName, "...", sep = "")))
    
    features = read.csv(paste(clusteringDir, tolower(dimensionName), ".csv", sep = ""), header = T)

    onlyData = features[,-nonFeatureCols]
    onlyCluster = as.data.frame(features[,clusterColName])
    features = cbind(onlyData, onlyCluster)
    colnames(features)[ncol(features)] = clusterColName
    
    print(noquote("Analysing: Creating Decision Tree..."))
    tree = rpart(as.formula(paste(clusterColName, "~ .")), features, method = "class", parms = list(split = "gini"))
    
    png(paste(outputDir, dimensionName, "-", kccaFamilyName,"-DecisionTree.png",sep = ""), width = 700, height = 600)
    plot(tree, uniform = T, branch = .5, margin = .1, 
         main = paste(dimensionName, "clustering - ", kccaFamilyName, 
                      " with ", length(unique(onlyCluster[,1])), " center(s)\nDecision Tree Analysis"))
    text(tree, cex = 1, all = T) 
    dev.off()
    
    print(noquote("Analysing: Detailing Clustering Sizes and Tree..."))
    details.clustering(features[,clusterColName], tree, dimensionName, outputDir, kccaFamilyName)
    
    print(noquote("Analysing: Generating Feature PMFs..."))
    nRows = ceiling(ncol(onlyData)/2)
    png(paste(outputDir, dimensionName, "-PMF.png", sep =""), width = 800, height = (nRows * 300))
    par(mfrow = c(nRows,2))
    for(i in 1:ncol(onlyData)){
        plot(prop.table(table(onlyData[,i])), ylab = "Probabilidade", main = colnames(onlyData)[i])
    }
    dev.off()
    
    print(noquote("Analysing: DONE!"))
    print(noquote(""))
}

details.clustering = function(clusters, tree, dimensionName, outputDir, kccaFamilyName){
    print(noquote(paste("Persisting: ", dimensionName, "...", sep = "")))
    
    # Output to file...
    sink(file = paste(outputDir, dimensionName, "-", kccaFamilyName, "-Analysis.txt", sep = ""))
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


createFeatures.tutorQeA = function(preprocessedDir){
    
    derivedFeaturesFile = paste(preprocessedDir, "/DerivedFeatures.csv", sep = "")
    if (!file.exists(derivedFeaturesFile)){
        print(noquote("Running the DerivedFeatures script to generate the Debate and Hotness..."))
        source("DerivedFeatures.R")
        MainDerivedFeatures()
    }

    print(noquote("Loading the DerivedFeatures with the Debate and Hotness..."))
    question.features = read.csv(derivedFeaturesFile, header = T)
    
    print(noquote("Reading Data..."))
    questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
    
    print(noquote("Merging Default Features and Derived Features by Id..."))
    features = data.frame(Id = questions$Id,
                          Score = questions$Score,
                          AnswerCount = questions$AnswerCount)
    features = merge(features, question.features, by = "Id")
    
    # Normalize the new Features between 0 and 1
    features$Debate = (features$Debate - min(features$Debate))/(max(features$Debate) - min(features$Debate))
    features$Hotness = (features$Hotness - min(features$Hotness))/(max(features$Hotness) - min(features$Hotness))
    
    return(features)
}

####################################################
####################### MAIN #######################
####################################################
MainClustering = function(){
    library(flexclust, quietly = T) 
    library(rpart, quietly=T)
    library(doMC, quietly = T)
    library(plyr, quietly = T)
    
    if (Sys.info()["sysname"] == "Linux"){
        library(doMC)
        registerDoMC()
    }
    
    dir.create("../AllData/clustering/", showWarnings=F)
    dir.create("output/clustering/", showWarnings=F)
    
    preprocessedDir = "../AllData/preprocessed/"
    clusteringDir = "../AllData/clustering/"
    outputDir = "output/clustering/"
    
    clustering(preprocessedDir, clusteringDir, dimensionName = "TutorQeA",
                             createFeaturesFunc = createFeatures.tutorQeA, outputDir)

    # KMEANS
    plot.clustering(clusteringDir, outputDir, dimensionName = "TutorQeA", kccaFamilyName="KMeans",
                    clusterCol="Cluster.KMeans", nonFeatureCols = c(1, 6, 7))
    analyse.clustering(clusteringDir, outputDir, dimensionName = "TutorQeA", kccaFamilyName="KMeans",
                       clusterColName="Cluster.KMeans", nonFeatureCols = c(1, 6, 7))
    
    # KMEDIANS
    plot.clustering(clusteringDir, outputDir, dimensionName = "TutorQeA", kccaFamilyName="KMedians",
                    clusterCol="Cluster.KMedians", nonFeatureCols = c(1, 6, 7))
    analyse.clustering(clusteringDir, outputDir, dimensionName = "TutorQeA", kccaFamilyName="KMedians",
                       clusterColName="Cluster.KMedians", nonFeatureCols = c(1, 6, 7))
}
MainClustering()
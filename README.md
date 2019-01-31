# weatherCEP
data from: https://openweathermap.org/ 
and https://rp5.ru/Weather_archive_in_Alexandria_(airport)


Before start
  - Need to install and run kafka 
  - Need to install and run zookeeper
  - Need to install and run elasticsearch 6.5.4
  - Kibana 6.5.4 //optional to visualize data

## Run
### Step 1: run kafka ,zookeeper and elasticsarch
    //zookeeper
    $ bin\zkserver
    
    //kafka
    $ bin\windows\kafka-server-start.bat config\server.properties
    
    //elastic
    $ bin\elasticsearch
### Step 2: create kafka topic
    $ kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic weathersourcecsv
    
    $ kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic hotcep
    
    $ kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic coldcep
    
    $ kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic stormcep
    
    $ kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic raincep
### Step 3: run project
    //TODO change mail's targer, your mail's username and password
    1. run KafkaProducer.java
    //function producerFromCSV load data from csv
    // u can remove it for realtime data
    2. run  HotPatternCEP.java
            ColdPatternCEP.java
            RainPatternCEP.java
            StormPatternCEP.java
    3. run  consumerHotCEP.java
            consumerColdCEP.java
            consumerRainCEP.java
            consumerStormCEP.java
            
## Note:
Recommend using Intellij IDE and maven to import project
    
  
  
  


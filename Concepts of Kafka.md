
###  features of Kafka
1. can publish and subscribe to streams of records. it is stored in an mechanism similar to a message queue.

2. fault - tolerant and real time processing

3. can scale almost linearly, have very limited bottleneck problem

### concepts
1. topic: records are stored by categories. And a topic can have zero, one, or many consumers that subscribe to the data written to it.
2. producer: publish a stream of records to one or more topics(send message to cluster) And it will also determine which partition the record should be assigned to. And it has functions to achive that to balance load
3. consumer subscribe to one or more topics and process the stream of records

<img width="450" alt="screenshot 2017-04-27 21 55 27" src="https://cloud.githubusercontent.com/assets/27907550/25512554/4c0d1480-2b94-11e7-813e-d455b668ed54.png">


for each topic, cluster maintains a partitioned log as above. a partition is the basic unit in kafka.

And each partition is like a queue.Each partition is an ordered immutable sequence. Each record is assigned a sequential id number called offset. the partition can store as many as record until it meets the uper limit, then it will delete the least recent used part. Like if you are only want to keep information in 7 days. the record before 7 days will deleted.

And offset is controlled by the consumer, it will increase the offset linear. And also it can determin where to start the offset, can start at head or skip the head and start at the most recent record.

which is to say, when the system fails. It can continue to read date at where it was interrupted when system is recovered.

<img width="421" alt="screenshot 2017-04-27 21 55 37" src="https://cloud.githubusercontent.com/assets/27907550/25513348/1b5a9668-2b9a-11e7-9500-d84ee6b03428.png">

### Guarantees
1. message sent by a producer to a particular topic partition will be in the order of being send(have lower offset)
2. consumer also read the record in the order they are stored.(order access is efficient than random access)
3. For a topic with replication factor N, system can tolerate up to N-1 server failures without losing any records committed to the log.

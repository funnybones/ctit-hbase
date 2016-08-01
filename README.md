# CTIT-HBASE

Minimal hbase project for the ctit cluster. Adapted from similar project at cloudera. 

# Demo

To run a demo, first create a table ``test`` in namespace ``n`` with a column family ``myLittleFamily``:
  
  echo "create 'n:test', 'myLittleFamily'" | hbase shell
  
Then compile the code:
  
  mvn package
  
Now run the code:

  java -Djava.security.auth.login.config=/etc/hbase/conf/zk-jaas.conf -Djava.security.krb5.conf=/etc/krb5.service.conf  -cp target/ctit-hbase-1.0.0-SNAPSHOT.jar:$(hbase classpath) com.cloudera.hbase.HBaseClient  "n:test"

To run a word count example, writing the results to hdfs in file ``test123``:

  hadoop jar target/ctit-hbase-1.0.0-SNAPSHOT.jar com.cloudera.hbase.WordCount n:test test123

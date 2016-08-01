/**
 * Copyright (c) 2011, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.cloudera.hbase;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


/**
 * See {@link org.apache.hadoop.examples.WordCount}.
 * 
 * Version for org.apache.hadoop.mapreduce.* API.
 */
public class WordCount extends Configured implements Tool {
  public static class Map extends TableMapper<ImmutableBytesWritable, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private final Text word = new Text();

    /**
    * @param row  The current table row key.
    * @param value  The columns.
    * @param context  The current context.
    * @throws IOException When something is broken with the data.
    * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN,
    *   org.apache.hadoop.mapreduce.Mapper.Context)
    */
    @Override
    public void map(ImmutableBytesWritable row, Result value, Context context) throws IOException {
      try {
        context.write(row, one);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public static class Reduce extends
      Reducer<ImmutableBytesWritable, IntWritable, ImmutableBytesWritable, IntWritable> {
    private final IntWritable result = new IntWritable();

    @Override
    public void reduce(ImmutableBytesWritable key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public int run(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: wordcount <in-table> <out-file>");
      return 2;
    }

    Configuration conf = HBaseConfiguration.create();

    Job job = new Job(conf, "Word Count");
    job.setJarByClass(WordCount.class);
    //job.setMapperClass(Map.class);
    
    Scan scan = new Scan();
    
    TableMapReduceUtil.initTableMapperJob(
      args[0],        // input HBase table name
      scan,             // Scan instance to control CF and attribute selection
      Map.class,   // mapper
      null,             // mapper output key 
      null,             // mapper output value
      job);
    
    job.setCombinerClass(Reduce.class);
    job.setReducerClass(Reduce.class);
    job.setOutputKeyClass(ImmutableBytesWritable.class);
    job.setOutputValueClass(IntWritable.class);

    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    return job.waitForCompletion(true) ? 0 : 1;
  }

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new WordCount(), args);
    System.exit(res);
  }
}
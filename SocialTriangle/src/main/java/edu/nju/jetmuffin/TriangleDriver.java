package edu.nju.jetmuffin;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;

/**
 * Created by jeff on 16/11/19.
 */
public class TriangleDriver {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 4) {
            System.err.println("Usage: hadoop jar SocialTriangle.jar edu.nju.jetmuffin.TriangleDriver <in> <tmp1> <tmp2> <out>");
            System.exit(2);
        }

        // build graph
        Job job1 = Job.getInstance(conf, "social triangle(build graph)");
        job1.setJarByClass(TriangleDriver.class);
        job1.setMapperClass(GraphBuilder.GraphMapper.class);
        job1.setReducerClass(GraphBuilder.GraphReducer.class);
        job1.setPartitionerClass(GraphBuilder.GraphPartitioner.class);
        job1.setMapOutputKeyClass(Text.class);
        job1.setMapOutputValueClass(NullWritable.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job1, new Path(args[0]));
        FileOutputFormat.setOutputPath(job1, new Path(args[1]));
        job1.waitForCompletion(true);

        // build triads
        Job job2 = Job.getInstance(conf, "social triangle(build triads)");
        job2.setJarByClass(TriangleDriver.class);
        job2.setMapperClass(TriadBuilder.TriadsMapper.class);
        job2.setReducerClass(TriadBuilder.TraidsReducer.class);
        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(NullWritable.class);
        FileInputFormat.addInputPath(job2, new Path(args[1]));
        FileOutputFormat.setOutputPath(job2, new Path(args[2]));
        job2.waitForCompletion(true);

        // count triangles
        Job job3 = Job.getInstance(conf, "social triangle(count triangles)");
        job3.setJarByClass(TriangleDriver.class);
        job3.setMapperClass(TriangleCounter.CounterMapper.class);
        job3.setReducerClass(TriangleCounter.CounterReducer.class);
        job3.setCombinerClass(TriangleCounter.CounterReducer.class);
        job3.setMapOutputKeyClass(Text.class);
        job3.setMapOutputValueClass(IntWritable.class);
        job3.setOutputKeyClass(Text.class);
        job3.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job3, new Path(args[2]));
        FileOutputFormat.setOutputPath(job3, new Path(args[3]));

        System.exit(job3.waitForCompletion(true) ? 0 : 1);
    }
}

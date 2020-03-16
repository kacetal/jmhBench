package fr.kacetal.bench;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Fork(1)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
public class PerformanceCompare {

    public static final Map<String, Class> CLASSES = Stream.of(ArrayList.class, LinkedList.class)
            .collect(Collectors.toMap(Class::getSimpleName, c -> c));

    public static final int ITERATIONS = 1000;

    @Timeout(time = 1, timeUnit = TimeUnit.MILLISECONDS)
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PerformanceCompare.class.getSimpleName())
                .forks(1)
                .jvmArgs("-Xms4096m", "-Xmx4096m", "-XX:MaxDirectMemorySize=2048M")
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public void indexOfKnown(Plan plan, Blackhole blackhole) {
        List<Integer> list = plan.list;
        Random random = plan.random;
        int value = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            value = list.indexOf(random.nextInt(plan.size));
        }
        blackhole.consume(value);
    }

    @Benchmark
    public void indexOfUnknown(Plan plan, Blackhole blackhole) {
        List<Integer> list = plan.list;
        Random random = plan.random;
        int value = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            value += list.indexOf(random.nextInt());
        }
        blackhole.consume(value);
    }

    @Benchmark
    public void addRemoveRandom(Plan plan, Blackhole blackhole) {
        List<Integer> list = plan.list;
        Random random = plan.random;
        int value = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            list.add(random.nextInt(list.size() + 1), random.nextInt());
            value += list.remove(random.nextInt(list.size()));
        }
        blackhole.consume(value);
    }


    @Benchmark
    public void addRemoveFirst(Plan plan, Blackhole blackhole) {
        List<Integer> list = plan.list;
        Random random = plan.random;
        int value = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            list.add(0, random.nextInt());
            value += list.remove(0);
        }
        blackhole.consume(value);
    }


    @Benchmark
    public void addRemoveLast(Plan plan, Blackhole blackhole) {
        List<Integer> list = plan.list;
        Random random = plan.random;
        int value = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            list.add(random.nextInt());
            value += list.remove(list.size() - 1);
        }
        blackhole.consume(value);
    }

    @Benchmark
    public void get(Plan plan, Blackhole blackhole) {
        List<Integer> list = plan.list;
        Random random = plan.random;
        int value = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            value += list.get(random.nextInt(list.size()));
        }
        blackhole.consume(value);
    }

    @State(Scope.Benchmark)
    public static class Plan {

        @Param({"10", "100", "1000", "10000", "100000", "1000000", "10000000"})
        public int size;

        @Param({"ArrayList", "LinkedList"})
        public String className;

        private Random random;

        private List<Integer> list;

        @Setup
        public void init() throws IllegalAccessException, InstantiationException {
            random = new Random();
            list = (List<Integer>) CLASSES.get(className).newInstance();

            for (int i = 0; i < size; i++) {
                list.add(i);
            }
        }
    }
}

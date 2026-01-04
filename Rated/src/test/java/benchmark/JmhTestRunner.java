package benchmark;

import org.junit.jupiter.api.Test; // Oppure org.junit.Test se usi JUnit 4
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JmhTestRunner {

    @Test
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
            // Includi qui le classi di benchmark che vuoi eseguire
            .include(FieldValidatorBenchmark.class.getSimpleName()) 
            // .include(AltroServiceBenchmark.class.getSimpleName())
            .forks(1) // 1 fork per il debug, mettine di più per i risultati finali
            .warmupIterations(2) // Riduciamo per fare prima
            .measurementIterations(3)
            .build();

        new Runner(opt).run();
    }
}
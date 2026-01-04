package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import utilities.PasswordUtility;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime) // Misuriamo il tempo medio di esecuzione
@OutputTimeUnit(TimeUnit.MICROSECONDS) // Output in microsecondi
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class PasswordUtilityBenchmark {

    // @Param permette di eseguire lo stesso benchmark con valori diversi.
    // JMH eseguirà tutti i test prima con "123456" e poi con la frase lunga.
    @Param({
        "123456", 
        "UnaPasswordMoltoLungaPerVedereSeCambiaQualcosaNelTempoDiHashing"
    })
    private String passwordToTest;

    @Benchmark
    public void testHashPassword(Blackhole bh) {
        // Chiamiamo il metodo statico
        String result = PasswordUtility.hashPassword(passwordToTest);
        
        // Consumiamo il risultato per evitare ottimizzazioni del compilatore
        bh.consume(result);
    }
}
package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import utilities.FieldValidator;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS) // Usiamo i Nanosecondi perché le operazioni su stringhe sono veloci
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class FieldValidatorBenchmark {

    // --- DATI VALIDI ---
    private String usernameValid = "SuperMario_99";
    private String passwordValid = "PasswordSegreta1!";
    private String emailValid    = "mario.rossi@example.com";

    // --- DATI INVALIDI (per vedere quanto costa rifiutare) ---
    private String usernameInvalid = "A"; // Troppo corto
    private String passwordInvalid = "sololettere"; // Manca numero e simbolo
    private String emailInvalid    = "mariorossi.com"; // Manca la @

    // ---------------- USERNAME ----------------
    @Benchmark
    public void testUsernameValid(Blackhole bh) {
        boolean res = FieldValidator.validateUsername(usernameValid);
        bh.consume(res);
    }

    @Benchmark
    public void testUsernameInvalid(Blackhole bh) {
        boolean res = FieldValidator.validateUsername(usernameInvalid);
        bh.consume(res);
    }

    // ---------------- PASSWORD ----------------
    // Questo sarà il più lento perché la regex è complessa
    @Benchmark
    public void testPasswordValid(Blackhole bh) {
        boolean res = FieldValidator.validatePassword(passwordValid);
        bh.consume(res);
    }

    @Benchmark
    public void testPasswordInvalid(Blackhole bh) {
        boolean res = FieldValidator.validatePassword(passwordInvalid);
        bh.consume(res);
    }

    // ---------------- EMAIL ----------------
    @Benchmark
    public void testEmailValid(Blackhole bh) {
        boolean res = FieldValidator.validateEmail(emailValid);
        bh.consume(res);
    }

    @Benchmark
    public void testEmailInvalid(Blackhole bh) {
        boolean res = FieldValidator.validateEmail(emailInvalid);
        bh.consume(res);
    }
}
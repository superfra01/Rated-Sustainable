package benchmark;

import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ModerationService;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS) // Usiamo Nanosecondi perché il metodo è molto veloce
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class ModerationServiceBenchmark {

    private ModerationService service;

    // Definiamo due costanti per pilotare il test
    private final String EMAIL_ESISTENTE = "baduser@test.com";
    private final String EMAIL_INESISTENTE = "ghost@test.com";

    @Setup(Level.Trial)
    public void setup() {
        // CORREZIONE 1: Aggiungi 'true' qui per usare il costruttore sicuro
        UtenteDAO mockDao = new UtenteDAO(true) { 
            @Override
            public UtenteBean findByEmail(String email) {
                if ("baduser@test.com".equals(email)) { // Usa le tue costanti qui
                    UtenteBean u = new UtenteBean();
                    u.setEmail(email);
                    u.setNWarning(0);
                    return u;
                }
                return null;
            }

            @Override
            public void update(UtenteBean u) {
                // Non fa nulla
            }
        };

        // CORREZIONE 2: Usa il costruttore che accetta il DAO, non quello vuoto!
        // Se usi new ModerationService(), lui creerà internamente un UtenteDAO "cattivo".
        this.service = new ModerationService(mockDao);
    }

    // --- BENCHMARK 1: Utente Trovato (Logica Completa) ---
    // Questo test misura:
    // 1. Chiamata al DAO (mock)
    // 2. Controllo if (user != null) -> VERO
    // 3. Incremento variabile (getNWarning + 1)
    // 4. Chiamata a update()
    @Benchmark
    public void testWarnUserExists() {
        service.warn(EMAIL_ESISTENTE);
    }

    // --- BENCHMARK 2: Utente Non Trovato (Short Circuit) ---
    // Questo test misura:
    // 1. Chiamata al DAO (mock)
    // 2. Controllo if (user != null) -> FALSO
    // 3. Fine metodo.
    // Dovrebbe essere leggermente più veloce del test precedente.
    @Benchmark
    public void testWarnUserNotFound() {
        service.warn(EMAIL_INESISTENTE);
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ModerationServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
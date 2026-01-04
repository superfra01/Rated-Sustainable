package benchmark;

import model.DAO.UtenteDAO;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import utilities.PasswordUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS) // Microsecondi va bene per l'hashing
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class ProfileServiceBenchmark {

    private ProfileService service;
    
    // Dati per i test
    private List<RecensioneBean> listaRecensioni;
    private final String EMAIL_TEST = "mario.rossi@test.com";
    private final String USERNAME_TEST = "SuperMario";
    private final String PASSWORD_TEST = "PasswordSicura123";

    @Setup(Level.Trial)
    public void setup() {
        // 1. Creiamo il Mock DAO usando il costruttore "sicuro" (true)
        UtenteDAO mockDao = new UtenteDAO(true) { // <--- IMPORTANTE: true
            @Override
            public UtenteBean findByEmail(String email) {
                UtenteBean u = new UtenteBean();
                u.setEmail(email);
                u.setUsername("User_" + email);
                // Mettiamo una password finta già hashata per evitare null pointer
                u.setPassword("oldHash"); 
                return u;
            }

            @Override
            public UtenteBean findByUsername(String username) {
                return null; // Username libero
            }

            @Override
            public void update(UtenteBean user) {
                // Non fa nulla
            }
        };

        // 2. INIEZIONE TRAMITE COSTRUTTORE
        // Invece di `new ProfileService()`, usiamo quello nuovo:
        this.service = new ProfileService(mockDao);

        // 3. Prepariamo la lista per testare il ciclo getUsers
        this.listaRecensioni = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            RecensioneBean r = new RecensioneBean();
            r.setEmail("user" + i + "@test.com"); // Email diverse
            listaRecensioni.add(r);
        }
    }

    // --- BENCHMARK 1: Aggiornamento Profilo Completo ---
    // Questo è il test "pesante". Include:
    // - Check username (findByUsername)
    // - Recupero utente (findByEmail)
    // - HASHING DELLA PASSWORD (costoso)
    // - Update
    @Benchmark
    public void testProfileUpdate(Blackhole bh) {
        UtenteBean u = service.ProfileUpdate(USERNAME_TEST, EMAIL_TEST, PASSWORD_TEST, "Nuova Bio", null);
        bh.consume(u);
    }

    // --- BENCHMARK 2: Solo Aggiornamento Password ---
    // Simile al precedente ma con meno logica di controllo.
    // Utile per vedere quanto incide l'hashing puro rispetto al resto.
    @Benchmark
    public void testPasswordUpdate(Blackhole bh) {
        UtenteBean u = service.PasswordUpdate(EMAIL_TEST, PASSWORD_TEST);
        bh.consume(u);
    }

    // --- BENCHMARK 3: Mappatura Utenti da Recensioni ---
    // Questo testa il ciclo for e la creazione della HashMap.
    // Attenzione: nel codice reale questo metodo fa 100 query al DB (molto lento).
    // Qui misuriamo solo quanto tempo impiega Java a gestire gli oggetti e la Map.
    @Benchmark
    public void testGetUsersMap(Blackhole bh) {
        HashMap<String, String> map = service.getUsers(listaRecensioni);
        bh.consume(map);
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ProfileServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
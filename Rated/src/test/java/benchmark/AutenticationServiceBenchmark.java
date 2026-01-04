package benchmark;

import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.AutenticationService;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import utilities.PasswordUtility; // Assicurati che questa classe sia visibile

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS) // Usiamo microsecondi, ma per l'hashing potremmo vedere millisecondi
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class AutenticationServiceBenchmark {

    private AutenticationService service;
    
    // Dati di test
    private final String VALID_EMAIL = "user@test.com";
    private final String VALID_PASSWORD = "PasswordSegreta123!";
    private final String WRONG_PASSWORD = "Sbagliata";
    private final String NEW_USER_EMAIL = "new@test.com";
    private final String NEW_USER_NAME = "NewUser";

    private HttpSession mockSession;

    @Setup(Level.Trial)
    public void setup() {
        // --- 1. MOCK UTENTE DAO ---
        // ERRORE ERA QUI: mancava (true)
        UtenteDAO mockDao = new UtenteDAO(true) { 
            @Override
            public UtenteBean findByEmail(String email) {
                if ("user@test.com".equals(email)) { // Usa le tue costanti
                    UtenteBean u = new UtenteBean();
                    u.setEmail(email);
                    // Importante: la password deve matchare l'hash nel test
                    u.setPassword(PasswordUtility.hashPassword("PasswordSegreta123!")); 
                    return u;
                }
                return null; 
            }

            @Override
            public UtenteBean findByUsername(String username) {
                if ("ExistingUser".equals(username)) {
                    return new UtenteBean();
                }
                return null; 
            }

            @Override
            public void save(UtenteBean u) {
                // Simulazione salvataggio veloce
            }
        };

        // --- 2. MOCK SESSION ---
        this.mockSession = new javax.servlet.http.HttpSession() {
            @Override
            public void invalidate() {
                // Non fa nulla, serve solo per il test di logout
            }

            // --- Metodi obbligatori dell'interfaccia HttpSession ---
            @Override public long getCreationTime() { return 0; }
            @Override public String getId() { return "mockId"; }
            @Override public long getLastAccessedTime() { return 0; }
            @Override public javax.servlet.ServletContext getServletContext() { return null; }
            @Override public void setMaxInactiveInterval(int interval) {}
            @Override public int getMaxInactiveInterval() { return 0; }
            @Override public javax.servlet.http.HttpSessionContext getSessionContext() { return null; }
            @Override public Object getAttribute(String name) { return null; }
            @Override public Object getValue(String name) { return null; }
            @Override public java.util.Enumeration<String> getAttributeNames() { return null; }
            @Override public String[] getValueNames() { return new String[0]; }
            @Override public void setAttribute(String name, Object value) {}
            @Override public void putValue(String name, Object value) {}
            @Override public void removeAttribute(String name) {}
            @Override public void removeValue(String name) {}
            
            // ECCO IL METODO CHE MANCAVA E DAVA ERRORE:
            @Override 
            public boolean isNew() { 
                return false; 
            }
        };

        // --- 3. INIEZIONE ---
        this.service = new AutenticationService(mockDao);
    }

    // --- BENCHMARK 1: Login Corretto ---
    // Questo è il test più pesante. Misura quanto ci mette PasswordUtility a fare l'hash
    // e confrontarlo.
    @Benchmark
    public void testLoginSuccess(Blackhole bh) {
        UtenteBean u = service.login(VALID_EMAIL, VALID_PASSWORD);
        bh.consume(u);
    }

    // --- BENCHMARK 2: Login Fallito (Password errata) ---
    // Anche qui viene calcolato l'hash della password (sbagliata) per il confronto.
    // Dovrebbe impiegare lo stesso tempo del login corretto.
    @Benchmark
    public void testLoginWrongPassword(Blackhole bh) {
        UtenteBean u = service.login(VALID_EMAIL, WRONG_PASSWORD);
        bh.consume(u);
    }
    
    // --- BENCHMARK 3: Login Fallito (Utente non esiste) ---
    // Qui NON viene fatto l'hash (c'è un return null subito dopo findByEmail).
    // Questo benchmark sarà MOLTO più veloce degli altri due.
    @Benchmark
    public void testLoginUserNotFound(Blackhole bh) {
        UtenteBean u = service.login("non@esiste.com", "qualcosa");
        bh.consume(u);
    }

    // --- BENCHMARK 4: Registrazione ---
    // Include: 2 controlli (mock) + 1 hashing password + creazione oggetto
    @Benchmark
    public void testRegister(Blackhole bh) {
        UtenteBean u = service.register(NEW_USER_NAME, NEW_USER_EMAIL, VALID_PASSWORD, "Bio", null);
        bh.consume(u);
    }

    // --- BENCHMARK 5: Logout ---
    // Verifica l'overhead dell'invalidazione sessione (qui è mock, quindi quasi zero)
    @Benchmark
    public void testLogout() {
        service.logout(mockSession);
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(AutenticationServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
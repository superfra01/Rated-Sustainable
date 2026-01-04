package benchmark;

import model.DAO.FilmDAO;
import model.DAO.RecensioneDAO;
import model.DAO.ReportDAO;
import model.DAO.ValutazioneDAO;
import model.Entity.FilmBean;
import model.Entity.RecensioneBean;
import model.Entity.ValutazioneBean;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class RecensioniServiceBenchmark {

    private RecensioniService service;

    // Variabili per i test
    private String emailTest = "user@test.com";
    private int idFilmTest = 1;
    private List<RecensioneBean> listaRecensioniMock;

    @Setup(Level.Trial)
    public void setup() {
    	
        // --- 1. PREPARIAMO I DATI FINTI ---
        // Simuliamo una lista di 1000 recensioni per testare i cicli
        listaRecensioniMock = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            RecensioneBean r = new RecensioneBean();
            r.setIdFilm(idFilmTest);
            r.setValutazione((i % 5) + 1); // Voti da 1 a 5 a rotazione
            // Simuliamo che 1 recensione su 10 sia segnalata
            r.setNReports(i % 10 == 0 ? 5 : 0);
            listaRecensioniMock.add(r);
        }

        // --- 2. MOCK DI RECENSIONE DAO ---
     // Nel metodo setup() del Benchmark:

        RecensioneDAO mockRecensioneDAO = new RecensioneDAO(true) { // <--- Passa true qui!
            @Override
            public RecensioneBean findById(String email, int idFilm) {
                return null;
            }

            @Override
            public List<RecensioneBean> findByIdFilm(int idFilm) {
                return listaRecensioniMock;
            }

            @Override
            public List<RecensioneBean> findAll() {
                return listaRecensioniMock;
            }

            @Override public void save(RecensioneBean r) {}
            @Override public void update(RecensioneBean r) {}
        };

        // --- 3. MOCK DI FILM DAO ---
        FilmDAO mockFilmDAO = new FilmDAO(true) {
            @Override
            public FilmBean findById(int idFilm) {
                FilmBean f = new FilmBean();
                f.setIdFilm(idFilm);
                return f;
            }
            @Override public void update(FilmBean f) {} // Non fa nulla
        };

        // --- 4. MOCK DI ALTRI DAO (Meno rilevanti per questi test specifici ma necessari) ---
        ValutazioneDAO mockValutazioneDAO = new ValutazioneDAO(true) {
            @Override public ValutazioneBean findById(String email, String emailRecensore, int idFilm) { return null; }
            @Override public void save(ValutazioneBean v) {}
        };

        ReportDAO mockReportDAO = new ReportDAO(true) {
             // Metodi vuoti se necessario
        };

        // --- 5. INIEZIONE DELLE DIPENDENZE ---
        this.service = new RecensioniService(mockRecensioneDAO, mockValutazioneDAO, mockReportDAO, mockFilmDAO);
    }

    // --- BENCHMARK 1: Logica di Filtraggio ---
    // Misura quanto tempo ci mette a scorrere 1000 recensioni e trovare quelle segnalate.
    // Testa il metodo: GetAllRecensioniSegnalate()
    @Benchmark
    public void testFiltroSegnalazioni(Blackhole bh) {
        List<RecensioneBean> result = service.GetAllRecensioniSegnalate();
        bh.consume(result);
    }

    // --- BENCHMARK 2: Logica Matematica (Media) ---
    // Misura quanto tempo ci mette a ricalcolare la media voti su 1000 recensioni.
    // Viene chiamato indirettamente da addRecensione.
    @Benchmark
    public void testCalcoloMediaVoti(Blackhole bh) {
        // Chiamiamo addRecensione. Grazie ai Mock:
        // 1. Il save() non scrive su DB (veloce)
        // 2. findByIdFilm() ritorna 1000 elementi
        // 3. Il codice esegue il ciclo for per calcolare la media (questo è ciò che misuriamo)
        service.addRecensione(emailTest, idFilmTest, "Bella trama", "Titolo", 5);
        
        // Nota: non c'è return, quindi non serve bh.consume() esplicito, 
        // ma JMH misura l'esecuzione del metodo void.
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(RecensioniServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
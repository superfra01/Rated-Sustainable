package benchmark;

import model.DAO.FilmDAO;
import model.Entity.FilmBean;
import model.Entity.RecensioneBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

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
public class CatalogoServiceBenchmark {

    private CatalogoService service;
    private List<RecensioneBean> listaRecensioniTest;
    private String nomeDaCercare;

    // Eseguiamo questo setup una sola volta prima del benchmark
    @Setup(Level.Trial)
    public void setup() {
        // 1. CREIAMO IL MOCK DI FILM DAO
        // Passiamo 'true' per usare il costruttore vuoto ed evitare la connessione al DB
        FilmDAO mockDao = new FilmDAO(true) {
            
            @Override
            public List<FilmBean> findAll() {
                // Simuliamo una lista di 100 film
                List<FilmBean> list = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    FilmBean f = new FilmBean();
                    f.setIdFilm(i);
                    f.setNome("Film " + i);
                    list.add(f);
                }
                return list;
            }

            @Override
            public FilmBean findById(int id) {
                // Ritorniamo un film finto immediato
                FilmBean f = new FilmBean();
                f.setIdFilm(id);
                f.setNome("Film Trovato " + id);
                return f;
            }

            @Override
            public List<FilmBean> findByName(String name) {
                // Simuliamo il risultato di una ricerca
                List<FilmBean> list = new ArrayList<>();
                FilmBean f = new FilmBean();
                f.setNome(name);
                list.add(f);
                return list;
            }

            // Override dei metodi di scrittura per non fare nulla
            @Override public void save(FilmBean f) {}
            @Override public void update(FilmBean f) {}
            @Override public void delete(int id) {}
        };

        // 2. INIEZIONE DEL MOCK NEL SERVICE
        // Usiamo il costruttore di CatalogoService che accetta il DAO
        this.service = new CatalogoService(mockDao);

        // 3. PREPARAZIONE DATI PER I TEST
        // Prepariamo la lista di recensioni per testare il metodo getFilms(List<RecensioneBean>)
        this.listaRecensioniTest = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            RecensioneBean r = new RecensioneBean();
            r.setIdFilm(i); // Colleghiamo agli ID generati nel mock
            listaRecensioniTest.add(r);
        }

        this.nomeDaCercare = "Matrix";
    }

    // --- BENCHMARK 1: Testiamo il recupero di tutti i film ---
    @Benchmark
    public void testGetFilms(Blackhole bh) {
        List<FilmBean> result = service.getFilms();
        bh.consume(result);
    }

    // --- BENCHMARK 2: Testiamo la logica di mapping delle recensioni ---
    // Questo è il test più interessante perché contiene logica Java (il ciclo for e l'HashMap)
    @Benchmark
    public void testGetFilmsByRecensioni(Blackhole bh) {
        // Chiama il metodo che trasforma List<Recensione> in HashMap<Integer, Film>
        var result = service.getFilms(listaRecensioniTest);
        bh.consume(result);
    }

    // --- BENCHMARK 3: Testiamo la ricerca ---
    @Benchmark
    public void testRicercaFilm(Blackhole bh) {
        List<FilmBean> result = service.ricercaFilm(nomeDaCercare);
        bh.consume(result);
    }

    // Main per avviare il test
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(CatalogoServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
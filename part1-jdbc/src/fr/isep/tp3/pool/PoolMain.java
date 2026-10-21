package fr.isep.tp3.pool;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * Bonus partie IV : comparaison sans pool / pool maison / BoneCP.
 * On simule NB_REQUESTS "clients" servis par 10 threads, chacun fait un SELECT.
 */
public class PoolMain {

    static final String URL = "jdbc:postgresql://localhost/isep";
    static final String USER = "postgres";
    static final String PASS = "postgres";
    static final int NB_REQUESTS = 500;
    static final int NB_THREADS = 10;

    interface ConnectionProvider {
        Connection get() throws Exception;

        void giveBack(Connection c) throws Exception;
    }

    public static void main(String[] args) throws Exception {
        // 1) Sans pool : une nouvelle connexion par requête (DriverManager)
        long t = run(new ConnectionProvider() {
            public Connection get() throws SQLException {
                return DriverManager.getConnection(URL, USER, PASS);
            }

            public void giveBack(Connection c) throws SQLException {
                c.close();
            }
        });
        System.out.println("Sans pool (DriverManager)      : " + NB_REQUESTS + " requêtes en " + t + " ms");

        // 2) Exercice IV.1 : pool maison
        ConnectionPool pool = new ConnectionPool(URL, USER, PASS, NB_THREADS);
        System.out.println("Pool maison créé, connexions disponibles : " + pool.available());
        t = run(new ConnectionProvider() {
            public Connection get() throws InterruptedException {
                return pool.getConnection();
            }

            public void giveBack(Connection c) {
                pool.release(c);
            }
        });
        System.out.println("Pool maison (IV.1)             : " + NB_REQUESTS + " requêtes en " + t + " ms");
        System.out.println("Connexions disponibles après le test : " + pool.available());
        pool.shutdown();

        // 3) Exercice IV.2 : BoneCP
        BoneCPConfig config = new BoneCPConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASS);
        config.setMinConnectionsPerPartition(5);
        config.setMaxConnectionsPerPartition(10);
        config.setPartitionCount(1);
        BoneCP boneCP = new BoneCP(config);
        t = run(new ConnectionProvider() {
            public Connection get() throws SQLException {
                return boneCP.getConnection();
            }

            public void giveBack(Connection c) throws SQLException {
                c.close(); // avec BoneCP, close() rend la connexion au pool
            }
        });
        System.out.println("BoneCP (IV.2)                  : " + NB_REQUESTS + " requêtes en " + t + " ms");
        System.out.println("BoneCP : connexions créées = " + boneCP.getTotalCreatedConnections());
        boneCP.shutdown();
    }

    static long run(ConnectionProvider provider) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NB_THREADS);
        long start = System.currentTimeMillis();
        for (int i = 0; i < NB_REQUESTS; i++) {
            final int empno = (i % 2 == 0) ? 7369 : 7839;
            executor.submit(() -> {
                Connection c = null;
                try {
                    c = provider.get();
                    try (PreparedStatement ps = c.prepareStatement("SELECT ename FROM emp WHERE empno = ?")) {
                        ps.setInt(1, empno);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        provider.giveBack(c);
                    } catch (Exception ignore) {
                    }
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        return System.currentTimeMillis() - start;
    }
}

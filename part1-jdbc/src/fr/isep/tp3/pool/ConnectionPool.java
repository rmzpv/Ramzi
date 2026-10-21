package fr.isep.tp3.pool;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/*
 * Exercice IV.1 (bonus) : pool de connexions "fait main".
 * Au démarrage on ouvre N connexions via une DataSource et on les range dans une file.
 * Un client prend une connexion (getConnection), fait sa requête et la rend (release).
 * La BlockingQueue gère le multi-thread : si le pool est vide, le client attend.
 */
public class ConnectionPool {

    private final BlockingQueue<Connection> pool;

    public ConnectionPool(String url, String user, String password, int size) throws SQLException {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(url);
        ds.setUser(user);
        ds.setPassword(password);
        this.pool = new ArrayBlockingQueue<>(size);
        for (int i = 0; i < size; i++) {
            pool.add(createConnection(ds));
        }
    }

    private static Connection createConnection(DataSource ds) throws SQLException {
        return ds.getConnection();
    }

    public Connection getConnection() throws InterruptedException {
        return pool.take();
    }

    public void release(Connection connection) {
        if (connection != null) {
            pool.offer(connection);
        }
    }

    public int available() {
        return pool.size();
    }

    public void shutdown() throws SQLException {
        for (Connection c : pool) {
            c.close();
        }
        pool.clear();
    }
}

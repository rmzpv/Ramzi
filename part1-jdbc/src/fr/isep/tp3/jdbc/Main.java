package fr.isep.tp3.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * TP 3 - Partie I : communication avec la base via JDBC.
 * Usage : java fr.isep.tp3.jdbc.Main <ex1|ex2|ex3|ex4|ex5|ex6>
 */
public class Main {

    static final String URL = "jdbc:postgresql://localhost/isep";
    static final String USER = "postgres";
    static final String PASS = "postgres";

    public static void main(String[] args) {
        /* Load JDBC Driver. */
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String exercise = args.length > 0 ? args[0] : "ex1";
        Connection connexion = null;
        try {
            connexion = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Bdd Connected");

            switch (exercise) {
                case "ex1":
                    displayDepartment(connexion);
                    break;
                case "ex2": {
                    Scanner sc = new Scanner(System.in);
                    System.out.print("Employee number : ");
                    int empno = sc.nextInt();
                    System.out.print("New department number : ");
                    int newDeptno = sc.nextInt();
                    displayEmployee(connexion, empno);
                    moveDepartment(connexion, empno, newDeptno);
                    displayEmployee(connexion, empno);
                    break;
                }
                case "ex3":
                    displayTable(connexion, "emp");
                    System.out.println();
                    displayTable(connexion, "dept");
                    break;
                case "ex4":
                    sqlInjectionDemo(connexion);
                    break;
                case "ex5": {
                    Scanner sc = new Scanner(System.in);
                    System.out.print("Employee number : ");
                    String empno = sc.nextLine();
                    System.out.print("New department number : ");
                    String newDeptno = sc.nextLine();
                    moveDepartmentPrepared(connexion, empno, newDeptno);
                    displayTable(connexion, "emp");
                    break;
                }
                case "ex6":
                    displayTablePrepared(connexion, "emp");
                    System.out.println();
                    displayTableSafe(connexion, "dept");
                    System.out.println();
                    displayTableSafe(connexion, "emp; DROP TABLE emp");
                    break;
                default:
                    System.out.println("Unknown exercise " + exercise);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connexion != null)
                try {
                    connexion.close();
                } catch (SQLException ignore) {
                    ignore.printStackTrace();
                }
        }
    }

    /* Exercice 1 : affichage des départements avec leur localisation. */
    public static void displayDepartment(Connection connexion) throws SQLException {
        Statement statement = connexion.createStatement();
        ResultSet resultat = statement.executeQuery("SELECT deptno, dname, loc FROM dept");

        while (resultat.next()) {
            int deptno = resultat.getInt("deptno");
            String dname = resultat.getString("dname");
            String loc = resultat.getString("loc");

            System.out.println("Department " + deptno + " is for "
                    + dname + " and located in " + loc);
        }
        resultat.close();
        statement.close();
    }

    /* Exercice 2 : déplacement d'un employé (version Statement, vulnérable). */
    public static void moveDepartment(Connection connexion, int empno, int newDeptno) throws SQLException {
        Statement statement = connexion.createStatement();
        int nb = statement.executeUpdate(
                "UPDATE emp SET deptno = " + newDeptno + " WHERE empno = " + empno);
        System.out.println(nb + " employee(s) moved to department " + newDeptno);
        statement.close();
    }

    /* Petite méthode utilitaire pour vérifier le résultat de l'exercice 2. */
    public static void displayEmployee(Connection connexion, int empno) throws SQLException {
        PreparedStatement ps = connexion.prepareStatement(
                "SELECT e.empno, e.ename, e.deptno, d.dname FROM emp e JOIN dept d ON e.deptno = d.deptno WHERE e.empno = ?");
        ps.setInt(1, empno);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println("  -> " + rs.getInt("empno") + " " + rs.getString("ename")
                    + " works in department " + rs.getInt("deptno") + " (" + rs.getString("dname") + ")");
        }
        rs.close();
        ps.close();
    }

    /* Exercice 3 : affichage générique d'une table grâce à ResultSetMetaData. */
    public static void displayTable(Connection connexion, String tableName) throws SQLException {
        Statement statement = connexion.createStatement();
        ResultSet results = statement.executeQuery("SELECT * FROM " + tableName);
        printResultSet(results);
        results.close();
        statement.close();
    }

    static void printResultSet(ResultSet results) throws SQLException {
        ResultSetMetaData rsmd = results.getMetaData();
        int columnsNumber = rsmd.getColumnCount();

        StringBuilder header = new StringBuilder();
        for (int i = 1; i <= columnsNumber; i++) {
            header.append(rsmd.getColumnName(i).toUpperCase()).append(" | ");
        }
        System.out.println(header);

        while (results.next()) {
            StringBuilder line = new StringBuilder();
            for (int i = 1; i <= columnsNumber; i++) {
                line.append(results.getString(i)).append(" | ");
            }
            System.out.println(line);
        }
    }

    /*
     * Exercice 4 : démonstration de l'injection SQL.
     * On simule une saisie utilisateur malveillante passée à une requête concaténée.
     * Tout est fait dans une transaction annulée pour ne pas abîmer la base.
     */
    public static void sqlInjectionDemo(Connection connexion) throws SQLException {
        connexion.setAutoCommit(false);
        try {
            String empnoSaisi = "7369 OR 1=1";   // saisie de l'"utilisateur"
            String deptSaisi = "40";
            String sql = "UPDATE emp SET deptno = " + deptSaisi + " WHERE empno = " + empnoSaisi;
            System.out.println("Requête réellement envoyée : " + sql);
            Statement statement = connexion.createStatement();
            int nb = statement.executeUpdate(sql);
            System.out.println(nb + " employee(s) moved (au lieu de 1 !)");
            statement.close();

            String nomSaisi = "x' OR '1'='1";
            sql = "SELECT empno, ename, sal FROM emp WHERE ename = '" + nomSaisi + "'";
            System.out.println("\nRequête réellement envoyée : " + sql);
            statement = connexion.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            printResultSet(rs);
            rs.close();
            statement.close();
        } finally {
            connexion.rollback();
            connexion.setAutoCommit(true);
            System.out.println("\n(rollback effectué, la base est intacte)");
        }
    }

    /* Exercice 5 : moveDepartment avec PreparedStatement. */
    public static void moveDepartmentPrepared(Connection connexion, String empno, String newDeptno) {
        String sql = "UPDATE emp SET deptno = ? WHERE empno = ?";
        try (PreparedStatement preparedStatement = connexion.prepareStatement(sql)) {
            preparedStatement.setInt(1, Integer.parseInt(newDeptno.trim()));
            preparedStatement.setInt(2, Integer.parseInt(empno.trim()));
            int nb = preparedStatement.executeUpdate();
            System.out.println(nb + " employee(s) moved to department " + newDeptno);
        } catch (NumberFormatException e) {
            System.out.println("Saisie refusée (pas un entier) : " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        }
    }

    /* Exercice 6 : displayTable avec PreparedStatement -> ne fonctionne pas. */
    public static void displayTablePrepared(Connection connexion, String tableName) {
        try (PreparedStatement ps = connexion.prepareStatement("SELECT * FROM ?")) {
            ps.setString(1, tableName);
            ResultSet rs = ps.executeQuery();
            printResultSet(rs);
        } catch (SQLException e) {
            System.out.println("SQLException : " + e.getMessage());
            System.out.println("SQLState : " + e.getSQLState());
        }
    }

    /*
     * Solution pour l'exercice 6 : un nom de table ne peut pas être un paramètre,
     * on le vérifie donc contre la liste des tables réelles (DatabaseMetaData) avant de l'utiliser.
     */
    public static void displayTableSafe(Connection connexion, String tableName) throws SQLException {
        boolean exists;
        try (ResultSet tables = connexion.getMetaData().getTables(null, "public", tableName, new String[]{"TABLE"})) {
            exists = tables.next();
        }
        if (!exists) {
            System.out.println("Table refusée : \"" + tableName + "\" n'existe pas");
            return;
        }
        displayTable(connexion, tableName);
    }
}

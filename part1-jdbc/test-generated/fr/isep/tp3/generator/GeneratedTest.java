package fr.isep.tp3.generator;

import fr.isep.tp3.generated.DeptDAO;
import fr.isep.tp3.generated.EmpDAO;

import java.sql.Connection;
import java.sql.DriverManager;

/* Vérifie que les classes générées compilent et fonctionnent. */
public class GeneratedTest {
    public static void main(String[] args) throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:postgresql://localhost/isep", "postgres", "postgres")) {
            System.out.println(new DeptDAO(c).find(10));
            System.out.println(new EmpDAO(c).find(7839));
        }
    }
}

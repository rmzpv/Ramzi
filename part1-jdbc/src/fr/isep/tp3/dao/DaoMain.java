package fr.isep.tp3.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/* Tests des exercices 10, 11 et 12. Usage : java fr.isep.tp3.dao.DaoMain <ex10|ex11|ex12> */
public class DaoMain {

    public static void main(String[] args) throws SQLException {
        String exercise = args.length > 0 ? args[0] : "ex10";
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost/isep", "postgres", "postgres")) {

            if (exercise.equals("ex10")) {
                DAO<Dept> departmentDao = new DeptDAO(connection);
                Dept dept20 = departmentDao.find(20);
                System.out.println(dept20);
                System.out.println(departmentDao.find(99)); // département inexistant -> null
            } else if (exercise.equals("ex11")) {
                DAO<Emp> employeeDao = new EmpDAO(connection);
                Emp smith = employeeDao.find(7369);
                System.out.println(smith);
                // on remonte la chaîne hiérarchique construite récursivement
                Emp e = smith;
                StringBuilder chain = new StringBuilder();
                while (e != null) {
                    chain.append(e.getEname()).append(" (").append(e.getJob()).append(")");
                    e = e.getMgr();
                    if (e != null) chain.append(" -> ");
                }
                System.out.println("Hiérarchie : " + chain);
            } else if (exercise.equals("ex12")) {
                DAOFactory factory = new DAOFactory(connection);
                DAO<Dept> deptDao = factory.getDeptDAO();
                DAO<Emp> empDao = factory.getEmpDAO();

                System.out.println(deptDao.find(30));
                System.out.println(empDao.find(7499));

                // petit test CRUD complet sur un département
                Dept d = new Dept(50, "IT", "PARIS");
                System.out.println("create : " + deptDao.create(d) + " -> " + deptDao.find(50));
                d.setLoc("LYON");
                System.out.println("update : " + deptDao.update(d) + " -> " + deptDao.find(50));
                System.out.println("delete : " + deptDao.delete(d) + " -> " + deptDao.find(50));
            }
        }
    }
}

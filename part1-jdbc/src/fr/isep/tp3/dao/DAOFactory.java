package fr.isep.tp3.dao;

import java.sql.Connection;

/* Exercice 12 : toutes les instanciations de DAO sont centralisées ici. */
public class DAOFactory {

    protected Connection connect;

    public DAOFactory(Connection connect) {
        this.connect = connect;
    }

    public DAO<Dept> getDeptDAO() {
        return new DeptDAO(connect);
    }

    public DAO<Emp> getEmpDAO() {
        return new EmpDAO(connect);
    }
}

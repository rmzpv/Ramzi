package fr.isep.tp3.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/* Exercice 10 : DAO du département. */
public class DeptDAO extends DAO<Dept> {

    public DeptDAO(Connection connect) {
        super(connect);
    }

    @Override
    public Dept find(int id) {
        Dept dept = null;
        String sql = "SELECT deptno, dname, loc FROM dept WHERE deptno = ?";
        try (PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dept = new Dept(rs.getInt("deptno"), rs.getString("dname"), rs.getString("loc"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dept;
    }

    @Override
    public boolean create(Dept dept) {
        String sql = "INSERT INTO dept (deptno, dname, loc) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setInt(1, dept.getDeptNo());
            ps.setString(2, dept.getDname());
            ps.setString(3, dept.getLoc());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Dept dept) {
        String sql = "UPDATE dept SET dname = ?, loc = ? WHERE deptno = ?";
        try (PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setString(1, dept.getDname());
            ps.setString(2, dept.getLoc());
            ps.setInt(3, dept.getDeptNo());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Dept dept) {
        String sql = "DELETE FROM dept WHERE deptno = ?";
        try (PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setInt(1, dept.getDeptNo());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

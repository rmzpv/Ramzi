package fr.isep.tp3.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/* Exercice 11 : DAO de l'employé, le manager est reconstruit récursivement. */
public class EmpDAO extends DAO<Emp> {

    private final DAO<Dept> deptDao;

    public EmpDAO(Connection connect) {
        super(connect);
        this.deptDao = new DeptDAO(connect);
    }

    @Override
    public Emp find(int id) {
        Emp emp = null;
        String sql = "SELECT * FROM emp WHERE empno = ?";
        try (PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    emp = new Emp();
                    emp.setEmpNo(rs.getLong("empno"));
                    emp.setEname(rs.getString("ename"));
                    emp.setEfirst(rs.getString("efirst"));
                    emp.setJob(rs.getString("job"));
                    emp.setHireDate(rs.getDate("hiredate"));
                    emp.setSal(rs.getInt("sal"));
                    emp.setComm(rs.getInt("comm"));
                    emp.setTel(rs.getString("tel"));

                    // le département est une clé étrangère -> on passe par le DAO Dept
                    emp.setDepartment(deptDao.find(rs.getInt("deptno")));

                    // le manager est un entier en base mais un Emp en Java :
                    // appel récursif de find() jusqu'au sommet de la hiérarchie (mgr NULL)
                    int mgrId = rs.getInt("mgr");
                    if (!rs.wasNull()) {
                        emp.setMgr(find(mgrId));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emp;
    }

    @Override
    public boolean create(Emp emp) {
        String sql = "INSERT INTO emp (empno, ename, efirst, job, mgr, hiredate, sal, comm, tel, deptno) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setLong(1, emp.getEmpNo());
            fill(ps, emp, 2);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Emp emp) {
        String sql = "UPDATE emp SET ename = ?, efirst = ?, job = ?, mgr = ?, hiredate = ?, sal = ?, "
                + "comm = ?, tel = ?, deptno = ? WHERE empno = ?";
        try (PreparedStatement ps = connect.prepareStatement(sql)) {
            fill(ps, emp, 1);
            ps.setLong(10, emp.getEmpNo());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Emp emp) {
        String sql = "DELETE FROM emp WHERE empno = ?";
        try (PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setLong(1, emp.getEmpNo());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* Remplit les 9 colonnes (hors empno) à partir de l'indice start. */
    private void fill(PreparedStatement ps, Emp emp, int start) throws SQLException {
        ps.setString(start, emp.getEname());
        ps.setString(start + 1, emp.getEfirst());
        ps.setString(start + 2, emp.getJob());
        if (emp.getMgr() != null) ps.setLong(start + 3, emp.getMgr().getEmpNo());
        else ps.setNull(start + 3, Types.INTEGER);
        if (emp.getHireDate() != null) ps.setDate(start + 4, new java.sql.Date(emp.getHireDate().getTime()));
        else ps.setNull(start + 4, Types.DATE);
        ps.setInt(start + 5, emp.getSal());
        ps.setInt(start + 6, emp.getComm());
        ps.setString(start + 7, emp.getTel());
        if (emp.getDepartment() != null) ps.setInt(start + 8, emp.getDepartment().getDeptNo());
        else ps.setNull(start + 8, Types.INTEGER);
    }
}

package fr.isep.tp3.dao;

import java.util.Date;

/* Bean (POJO) correspondant à la table EMP (fourni dans l'énoncé, complété). */
public class Emp {

    private Long empNo;

    private String ename;

    private String efirst;

    private String job;

    private Emp mgr;

    private Date hireDate;

    private int sal;

    private int comm;

    private String tel;

    private Dept department;

    public Long getEmpNo() {
        return empNo;
    }

    public void setEmpNo(Long empNo) {
        this.empNo = empNo;
    }

    public String getEname() {
        return ename;
    }

    public void setEname(String ename) {
        this.ename = ename;
    }

    public String getEfirst() {
        return efirst;
    }

    public void setEfirst(String efirst) {
        this.efirst = efirst;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public Emp getMgr() {
        return mgr;
    }

    public void setMgr(Emp mgr) {
        this.mgr = mgr;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public int getSal() {
        return sal;
    }

    public void setSal(int sal) {
        this.sal = sal;
    }

    public int getComm() {
        return comm;
    }

    public void setComm(int comm) {
        this.comm = comm;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Dept getDepartment() {
        return department;
    }

    public void setDepartment(Dept department) {
        this.department = department;
    }

    @Override
    public String toString() {
        // on n'affiche que le nom du manager pour éviter un affichage récursif énorme
        return "Emp{empNo=" + empNo + ", ename='" + ename + "', efirst='" + efirst + "', job='" + job
                + "', mgr=" + (mgr == null ? "null" : mgr.getEmpNo() + " " + mgr.getEname())
                + ", hireDate=" + hireDate + ", sal=" + sal + ", comm=" + comm + ", tel='" + tel
                + "', department=" + department + "}";
    }
}

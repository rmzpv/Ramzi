package com.isep.testjpa.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Data
@Entity
public class Emp {
    // pas de @GeneratedValue : empno n'est pas auto-incrémenté dans la table,
    // avec la stratégie AUTO Hibernate cherche une séquence "hibernate_sequence" qui n'existe pas
    @Id
    private Long empno;
    @Column(name = "ename")
    private String ename;
    @Column(name = "efirst")
    private String efirst;
    @Column(name = "job")
    private String job;
    @Column(name = "mgr")
    private Long mgr;
    @Column(name = "hiredate")
    private LocalDate hiredate;
    @Column(name = "sal")
    private Long sal;
    @Column(name = "comm")
    private Long comm;
    @Column(name = "tel")
    private String tel;
    @Column(name = "deptno")
    private Long deptno;
}

package com.isep.testjpa.controller;

import com.isep.testjpa.repository.EmpRepository;
import com.isep.testjpa.model.Emp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
public class SimpleController {

    @Autowired
    private EmpRepository empRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello(@RequestParam(value = "name", required = false) String name) {
        return "Hello " + name;
    }

    /* GET /employees (option : ?job=CLERK pour filtrer) */
    @GetMapping("/employees")
    public List<Emp> getEmployees(@RequestParam(value = "job", required = false) String job) {
        if (job != null) {
            return empRepository.findByJob(job.toUpperCase());
        }
        return empRepository.findAll();
    }

    /* GET /employees/{id} : 200 + employé, ou 404 */
    @GetMapping("/employees/{id}")
    public ResponseEntity<Emp> getEmployee(@PathVariable Long id) {
        return empRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* POST /employees : 201 + header Location, 400 si pas d'empno, 409 si déjà existant */
    @PostMapping("/employees")
    public ResponseEntity<Emp> addEmployee(@RequestBody Emp emp) {
        if (emp.getEmpno() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (empRepository.existsById(emp.getEmpno())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Emp saved = empRepository.save(emp);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getEmpno()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    /* PUT /employees/{id} : remplace l'employé, 200 ou 404 */
    @PutMapping("/employees/{id}")
    public ResponseEntity<Emp> updateEmployee(@PathVariable Long id, @RequestBody Emp emp) {
        if (!empRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        emp.setEmpno(id); // l'id de l'URL fait foi
        return ResponseEntity.ok(empRepository.save(emp));
    }

    /* DELETE /employees/{id} : 204 ou 404 */
    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        if (!empRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        empRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}

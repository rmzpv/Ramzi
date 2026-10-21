#!/bin/bash
# Tests de l'API REST /employees (exercices III.4 et III.5)
API=http://localhost:9090/employees
H='Content-Type: application/json'
c() { curl -s -w "   -> HTTP %{http_code}\n" "$@"; }

echo "### GET $API/7788  (get by id)"
c $API/7788
echo "### GET $API/1234  (id inexistant)"
c $API/1234
echo "### POST $API  (création)"
c -X POST $API -H "$H" -d '{"empno":8000,"ename":"DOE","efirst":"JANE","job":"CLERK","mgr":7782,"hiredate":"2026-10-21","sal":1200,"deptno":10}'
echo "### POST $API  (même empno -> conflit)"
c -X POST $API -H "$H" -d '{"empno":8000,"ename":"DOE"}'
echo "### PUT $API/8000  (mise à jour : augmentation + changement de département)"
c -X PUT $API/8000 -H "$H" -d '{"ename":"DOE","efirst":"JANE","job":"ANALYST","mgr":7566,"hiredate":"2026-10-21","sal":3100,"deptno":20}'
echo "### GET $API?job=analyst  (filtre, on n'affiche que empno/ename)"
curl -s "$API?job=analyst" | grep -o '"empno":[0-9]*,"ename":"[A-Z]*"'
echo "### DELETE $API/8000"
c -X DELETE $API/8000
echo "### GET $API/8000  (après suppression)"
c $API/8000

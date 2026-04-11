package zamestnanci;

import java.util.*;

public class CompanyDatabase {

    private Map<Integer, Employee> zamestnanci = new HashMap<>();
    private int nextId = 1;

    public Employee pridatZamestnance(String typ, String jmeno, String prijmeni, int rok) {
        Employee e;

        if (typ.equalsIgnoreCase("analyst")) {
            e = new Analyst(nextId++, jmeno, prijmeni, rok);
        } else {
            e = new SecuritySpecialist(nextId++, jmeno, prijmeni, rok);
        }

        zamestnanci.put(e.getId(), e);
        return e;
    }

    public void odebratZamestnance(int id) {
        zamestnanci.remove(id);
        for (Employee e : zamestnanci.values()) {
            e.odeberSpolupraci(id);
        }
    }

    public void pridatSpolupraci(int id1, int id2, CollaborationLevel level) {
        Employee e1 = zamestnanci.get(id1);
        Employee e2 = zamestnanci.get(id2);

        if (e1 != null && e2 != null) {
            e1.pridejSpolupraci(id2, level);
            e2.pridejSpolupraci(id1, level);
        }
    }

    public void vypisZamestnance() {
        zamestnanci.values().stream()
                .sorted(Comparator.comparing(Employee::getPrijmeni))
                .forEach(System.out::println);
    }

    public void statistiky() {
        int max = 0;
        Employee top = null;

        for (Employee e : zamestnanci.values()) {
            int size = e.getSpoluprace().size();
            if (size > max) {
                max = size;
                top = e;
            }
        }

        if (top != null) {
            System.out.println("Nejvíce vazeb: " + top);
        }
    }

    public void spustVypocet() {
        for (Employee e : zamestnanci.values()) {
            e.vypocet();
        }
    }
}

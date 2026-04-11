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

    public void pridatSpolupraci(int id1, int id2, CollaborationLevel level, Scanner sc) {
        Employee e1 = zamestnanci.get(id1);
        Employee e2 = zamestnanci.get(id2);

        if (e1 == null || e2 == null) {
            System.out.println("Neplatné ID!");
            return;
        }

        
        if (e1.getSpoluprace().containsKey(id2)) {
            System.out.println("Tato vazba už existuje!");
            System.out.println("Chceš ji přepsat? (ano[1]/ne[0])");

            String vstup = sc.nextLine();

            if (!vstup.equals("1") && !vstup.equalsIgnoreCase("ano")) {
                System.out.println("Změna zrušena.");
                return;
            }
        }

        
        e1.pridejSpolupraci(id2, level);
        e2.pridejSpolupraci(id1, level);

        System.out.println("Spolupráce uložena.");
    }

    public void vypisZamestnance() {

        System.out.println("=== ANALYSTI ===");
        zamestnanci.values().stream()
                .filter(e -> e instanceof Analyst)
                .sorted(Comparator.comparing(Employee::getPrijmeni))
                .forEach(System.out::println);

        System.out.println("\n=== SECURITY ===");
        zamestnanci.values().stream()
                .filter(e -> e instanceof SecuritySpecialist)
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
    
    public void najdiZamestnance(int id) {
        Employee e = zamestnanci.get(id);

        if (e != null) {
            System.out.println("Nalezen:");
            System.out.println(e);

            if (e.getSpoluprace().isEmpty()) {
                System.out.println("Žádné spolupráce.");
            } else {
                System.out.println("Spolupráce:");
                for (var entry : e.getSpoluprace().entrySet()) {
                    System.out.println("ID: " + entry.getKey() + " -> " + entry.getValue());
                }
            }

        } else {
            System.out.println("Pod tímto ID nikdo není.");
        }
    }
    
    public void vypisSpoluprace() {
        System.out.println("=== VŠECHNY SPOLUPRÁCE ===");

        for (Employee e : zamestnanci.values()) {
            for (var entry : e.getSpoluprace().entrySet()) {

                int id1 = e.getId();
                int id2 = entry.getKey();

                // 🔥 zajistí, že se nevypíše 2x (1-2 a 2-1)
                if (id1 < id2) {
                    Employee e2 = zamestnanci.get(id2);

                    System.out.println(
                            e + " <-> " + e2 + " : " + entry.getValue()
                    );
                }
            }
        }
    }
}

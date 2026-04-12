package zamestnanci;

import java.util.*;
import java.io.*;

public class CompanyDatabase {

    private Map<Integer, Employee> zamestnanci = new HashMap<>();
 // Toto vlož do třídy CompanyDatabase (obvykle pod deklaraci mapy)
    public Map<Integer, Employee> getZamestnanci() {
        return this.zamestnanci;
    }
    private int nextId = 1;

    public Employee pridatZamestnance(String typ, String jmeno, String prijmeni, int rok) {
        Employee e;

        if (typ.equalsIgnoreCase("analyst")) {
            // Vytvoříme analytika a předáme mu mapu pro jeho budoucí výpočty
            e = new Analyst(nextId++, jmeno, prijmeni, rok, this.zamestnanci);
        } else {
            // SecuritySpecialist mapu nepotřebuje
            e = new SecuritySpecialist(nextId++, jmeno, prijmeni, rok);
        }
        
        // Uložíme do mapy
        zamestnanci.put(e.getId(), e);

        // --- TADY JE TEN VÝPIS NOVÉHO ID ---
        System.out.println("--------------------------------------------------");
        System.out.println("Zaměstnanec úspěšně přidán do systému.");
        System.out.println("Přiřazené ID: " + e.getId());
        System.out.println("--------------------------------------------------");

        return e;
    }

    public void odebratZamestnance(int id) {
        if (zamestnanci.containsKey(id)) {
            zamestnanci.remove(id);
            
            // Odstranění vazeb u ostatních kolegů
            for (Employee e : zamestnanci.values()) {
                e.odeberSpolupraci(id);
            }

            // Tady zadáš název souboru přímo (např. "data.txt")
            // Použij ten stejný název, který používáš v Mainu pro načítání
            ulozDoSouboru("zamestnanci.txt"); 
            
            System.out.println("Zaměstnanec s ID " + id + " byl smazán a změna uložena.");
        } else {
            System.out.println("Zaměstnanec s ID " + id + " nebyl nalezen.");
        }
    }
    
    public void pridatSpolupraci(int id1, int id2, CollaborationLevel level, Scanner sc) {
        // 1. KONTROLA: Zda nejsou ID stejná
        if (id1 == id2) {
            System.out.println("Chyba: Nelze vytvořit spolupráci se stejným ID!");
            return;
        }

        Employee e1 = zamestnanci.get(id1);
        Employee e2 = zamestnanci.get(id2);

        // 2. KONTROLA: Zda obě ID existují v databázi
        if (e1 == null || e2 == null) {
            System.out.println("Neplatné ID!");
            return;
        }

        // 3. KONTROLA: Zda už vazba neexistuje
        if (e1.getSpoluprace().containsKey(id2)) {
            System.out.println("Tato vazba už existuje!");
            System.out.print("Chceš ji přepsat? (ano[1]/ne[0]): ");

            String vstup = sc.nextLine();
            if (!vstup.equals("1") && !vstup.equalsIgnoreCase("ano")) {
                System.out.println("Změna zrušena.");
                return;
            }
        }

        // Uložení oboustranné spolupráce
        e1.pridejSpolupraci(id2, level);
        e2.pridejSpolupraci(id1, level);

        System.out.println("Spolupráce byla úspěšně uložena mezi ID: " + id1 + " a ID: " + id2);
    }

    public void vypisZamestnance() {
        // 1. ANALYSTI
        System.out.println("=== ANALYSTI ===");
        List<Employee> analysti = zamestnanci.values().stream()
                .filter(e -> e instanceof Analyst)
                .sorted(Comparator.comparing(Employee::getPrijmeni))
                .toList();

        if (analysti.isEmpty()) {
            System.out.println("V této skupině není žádný zaměstnanec.");
        } else {
            // Tady je změna formátu
            analysti.forEach(e -> System.out.println("ID:" + e.getId() + " " + e.getPrijmeni() + " " + e.getJmeno() + " (" + e.getRokNarozeni() + ")"));
        }

        // 2. SECURITY
        System.out.println("\n=== SECURITY ===");
        List<Employee> security = zamestnanci.values().stream()
                .filter(e -> e instanceof SecuritySpecialist)
                .sorted(Comparator.comparing(Employee::getPrijmeni))
                .toList();

        if (security.isEmpty()) {
            System.out.println("V této skupině není žádný zaměstnanec.");
        } else {
            // Tady je stejná změna formátu
            security.forEach(e -> System.out.println("ID:" + e.getId() + " " + e.getPrijmeni() + " " + e.getJmeno() + " (" + e.getRokNarozeni() + ")"));
        }
    }

    public void statistiky() {
        if (zamestnanci.isEmpty()) {
            System.out.println("Žádná data pro statistiku.");
            return;
        }

        int maxVazeb = -1;
        double maxPrumer = -1.0;

        // 1. KROK: Zjistíme maximální hodnoty
        for (Employee e : zamestnanci.values()) {
            int pocet = e.getSpoluprace().size();
            if (pocet > maxVazeb) maxVazeb = pocet;

            if (pocet > 0) {
                double suma = 0;
                for (CollaborationLevel lvl : e.getSpoluprace().values()) {
                    suma += (lvl == CollaborationLevel.DOBRA) ? 3 : (lvl == CollaborationLevel.PRUMERNA ? 2 : 1);
                }
                double prumer = suma / pocet;
                if (prumer > maxPrumer) maxPrumer = prumer;
            }
        }

        // 2. KROK: Najdeme všechny, kteří těmto maximům odpovídají
        List<Employee> nejvicVazebList = new ArrayList<>();
        List<Employee> nejlepsiPrumerList = new ArrayList<>();

        for (Employee e : zamestnanci.values()) {
            // Kontrola vazeb
            if (maxVazeb > 0 && e.getSpoluprace().size() == maxVazeb) {
                nejvicVazebList.add(e);
            }

            // Kontrola průměru
            int pocet = e.getSpoluprace().size();
            if (pocet > 0) {
                double suma = 0;
                for (CollaborationLevel lvl : e.getSpoluprace().values()) {
                    suma += (lvl == CollaborationLevel.DOBRA) ? 3 : (lvl == CollaborationLevel.PRUMERNA ? 2 : 1);
                }
                double prumer = suma / pocet;
                // Použijeme malou odchylku pro porovnání double (kvůli přesnosti)
                if (Math.abs(prumer - maxPrumer) < 0.001) {
                    nejlepsiPrumerList.add(e);
                }
            }
        }

        // 3. KROK: Výpis
        System.out.println("=== STATISTIKY ===");
        
        System.out.println("Zaměstnanci s nejvíce vazbami (" + maxVazeb + "):");
        if (nejvicVazebList.isEmpty() || maxVazeb <= 0) {
            System.out.println("- žádné spolupráce");
        } else {
            nejvicVazebList.forEach(e -> System.out.println("- " + e.getJmeno() + " " + e.getPrijmeni()));
        }

        System.out.printf("\nZaměstnanci s nejlepším průměrem (%.2f):%n", maxPrumer);
        if (nejlepsiPrumerList.isEmpty()) {
            System.out.println("- žádné spolupráce");
        } else {
            nejlepsiPrumerList.forEach(e -> System.out.println("- " + e.getJmeno() + " " + e.getPrijmeni()));
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
            
            // Ruční formátování výpisu: ID:x Prijmeni Jmeno (Rok)
            System.out.println("ID:" + e.getId() + " " + e.getPrijmeni() + " " + e.getJmeno() + " (" + e.getRokNarozeni() + ")");

            if (e.getSpoluprace().isEmpty()) {
                System.out.println("Žádné spolupráce.");
            } else {
                System.out.println("Spolupráce:");
                for (var entry : e.getSpoluprace().entrySet()) {
                    // Tady vypisujeme ID kolegy a úroveň (např. DOBRA, SPATNA)
                    System.out.println("- ID kolegy: " + entry.getKey() + " (Kvalita: " + entry.getValue() + ")");
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

                //  zajistí, že se nevypíše 2x (1-2 a 2-1)
                if (id1 < id2) {
                    Employee e2 = zamestnanci.get(id2);

                    System.out.println(
                            e + " <-> " + e2 + " : " + entry.getValue()
                    );
                }
            }
        }
    }
    
    public void ulozDoSouboru(String soubor) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(soubor))) {

            for (Employee e : zamestnanci.values()) {
                // Základní 4 parametry
                String radek = e.getId() + ";" + e.getJmeno() + ";" + e.getPrijmeni() + ";" + e.getRokNarozeni();
                
                // Zjištění a přidání pátého parametru (Typu)
                if (e instanceof Analyst) {
                    radek += ";Analyst";
                } else if (e instanceof SecuritySpecialist) {
                    radek += ";SecuritySpecialist";
                }
                
                pw.println(radek);
            }

            // Zde bys měl případně uložit i ty spolupráce (řádky začínající "S"), 
            // pokud je chceš v souboru zachovat!

            System.out.println("Uloženo!");
        } catch (Exception e) {
            System.out.println("Chyba při ukládání!");
            e.printStackTrace();
        }
    }
    
    public void nactiZeSouboru(String soubor) {
        try (BufferedReader br = new BufferedReader(new FileReader(soubor))) {
            // Vyčistíme stávající data a resetujeme ID
            zamestnanci.clear();
            this.nextId = 1; 

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; 
                
                String[] parts = line.split(";");

                // --- A) ZPRACOVÁNÍ SPOLUPRÁCE ---
                if (parts[0].equals("S")) {
                    int id1 = Integer.parseInt(parts[1]);
                    int id2 = Integer.parseInt(parts[2]);
                    CollaborationLevel level = CollaborationLevel.valueOf(parts[3]);
                    
                    if (zamestnanci.containsKey(id1) && zamestnanci.containsKey(id2)) {
                        zamestnanci.get(id1).pridejSpolupraci(id2, level);
                        zamestnanci.get(id2).pridejSpolupraci(id1, level);
                    }
                } 
                // --- B) ZPRACOVÁNÍ ZAMĚSTNANCE ---
                else {
                    int id = Integer.parseInt(parts[0]);
                    String jmeno = parts[1];
                    String prijmeni = parts[2];
                    int rok = Integer.parseInt(parts[3]);
                    String typ = parts[4];

                    if (id >= nextId) {
                        nextId = id + 1;
                    }

                    Employee e;
                    if (typ.equalsIgnoreCase("Analyst")) {
                        // Předáváme mapu pro budoucí analýzy
                        e = new Analyst(id, jmeno, prijmeni, rok, this.zamestnanci);
                    } else {
                        e = new SecuritySpecialist(id, jmeno, prijmeni, rok);
                    }

                    zamestnanci.put(id, e);
                }
            }

            // VÝPIS PO ÚSPĚŠNÉM ČTENÍ
            System.out.println("Data byla úspěšně načtena ze souboru.");
            System.out.println("Celkový počet načtených zaměstnanců: " + zamestnanci.size());

        } catch (FileNotFoundException e) {
            System.out.println("Soubor nenalezen, začínám s prázdnou databází.");
        } catch (Exception e) {
            System.out.println("Chyba při načítání souboru: " + e.getMessage());
        }
    }
}

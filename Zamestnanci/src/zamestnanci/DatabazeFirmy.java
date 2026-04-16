package zamestnanci;

import java.io.*;
import java.util.*;

public class DatabazeFirmy {

    private static final String SOUBOR = "data.txt";

    private Map<Integer, Zamestnanec> zamestnanci = new HashMap<>();
    private int dalsiId = 1;

    public Map<Integer, Zamestnanec> getZamestnanci() {
        return this.zamestnanci;
    }

    public Zamestnanec pridatZamestnance(String typ, String jmeno, String prijmeni, int rok) {
        Zamestnanec z;

        if (typ.equalsIgnoreCase("analytik")) {
            z = new DatovyAnalytik(dalsiId++, jmeno, prijmeni, rok, this.zamestnanci);
        } else {
            z = new BezpecnostniSpecialista(dalsiId++, jmeno, prijmeni, rok);
        }

        zamestnanci.put(z.getId(), z);

        System.out.println("--------------------------------------------------");
        System.out.println("Zaměstnanec úspěšně přidán do systému.");
        System.out.println("Přiřazené ID: " + z.getId());
        System.out.println("--------------------------------------------------");

        return z;
    }

    public void odebratZamestnance(int id) {
        if (zamestnanci.containsKey(id)) {
            zamestnanci.remove(id);

            for (Zamestnanec z : zamestnanci.values()) {
                z.odeberSpolupraci(id);
            }

            ulozDoSouboru();
            System.out.println("Zaměstnanec s ID " + id + " byl smazán a změna uložena.");
        } else {
            System.out.println("Zaměstnanec s ID " + id + " nebyl nalezen.");
        }
    }

    public void pridatSpolupraci(int id1, int id2, UrovenSpoluprace uroven, Scanner sc) {
        if (id1 == id2) {
            System.out.println("Chyba: Nelze vytvořit spolupráci se stejným ID!");
            return;
        }

        Zamestnanec z1 = zamestnanci.get(id1);
        Zamestnanec z2 = zamestnanci.get(id2);

        if (z1 == null || z2 == null) {
            System.out.println("Neplatné ID!");
            return;
        }

        if (z1.getSpoluprace().containsKey(id2)) {
            System.out.println("Tato vazba už existuje!");
            System.out.print("Chceš ji přepsat? (ano[1]/ne[0]): ");
            String vstup = sc.nextLine();
            if (!vstup.equals("1") && !vstup.equalsIgnoreCase("ano")) {
                System.out.println("Změna zrušena.");
                return;
            }
        }

        z1.pridejSpolupraci(id2, uroven);
        z2.pridejSpolupraci(id1, uroven);

        System.out.println("Spolupráce byla úspěšně uložena mezi ID: " + id1 + " a ID: " + id2);
    }

    public void vypisZamestnance() {
        System.out.println("=== ANALYTICI ===");
        List<Zamestnanec> analytici = zamestnanci.values().stream()
                .filter(z -> z instanceof DatovyAnalytik)
                .sorted(Comparator.comparing(Zamestnanec::getPrijmeni, String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (analytici.isEmpty()) {
            System.out.println("V této skupině není žádný zaměstnanec.");
        } else {
            analytici.forEach(z -> System.out.println(
                    "ID:" + z.getId() + " " + z.getPrijmeni() + " " + z.getJmeno() + " (" + z.getRokNarozeni() + ")"));
        }

        System.out.println("\n=== BEZPECACI ===");
        List<Zamestnanec> bezpecaci = zamestnanci.values().stream()
                .filter(z -> z instanceof BezpecnostniSpecialista)
                .sorted(Comparator.comparing(Zamestnanec::getPrijmeni, String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (bezpecaci.isEmpty()) {
            System.out.println("V této skupině není žádný zaměstnanec.");
        } else {
            bezpecaci.forEach(z -> System.out.println(
                    "ID:" + z.getId() + " " + z.getPrijmeni() + " " + z.getJmeno() + " (" + z.getRokNarozeni() + ")"));
        }
    }

    public void vypisPoctyVeSkupinach() {
        long pocetAnalytiku = zamestnanci.values().stream().filter(z -> z instanceof DatovyAnalytik).count();
        long pocetSecurity = zamestnanci.values().stream().filter(z -> z instanceof BezpecnostniSpecialista).count();
        System.out.println("=== POČTY VE SKUPINÁCH ===");
        System.out.println("Datoví analytici: " + pocetAnalytiku);
        System.out.println("Bezpečnostní specialisté: " + pocetSecurity);
    }

    public void statistiky() {
        if (zamestnanci.isEmpty()) {
            System.out.println("Žádná data pro statistiku.");
            return;
        }

        int maxVazeb = -1;
        double maxPrumer = -1.0;

        for (Zamestnanec z : zamestnanci.values()) {
            int pocet = z.getSpoluprace().size();
            if (pocet > maxVazeb) maxVazeb = pocet;

            if (pocet > 0) {
                double suma = spocitejSumu(z);
                double prumer = suma / pocet;
                if (prumer > maxPrumer) maxPrumer = prumer;
            }
        }

        List<Zamestnanec> nejvicVazebList = new ArrayList<>();
        List<Zamestnanec> nejlepsiPrumerList = new ArrayList<>();

        for (Zamestnanec z : zamestnanci.values()) {
            if (maxVazeb > 0 && z.getSpoluprace().size() == maxVazeb) {
                nejvicVazebList.add(z);
            }

            int pocet = z.getSpoluprace().size();
            if (pocet > 0) {
                double prumer = spocitejSumu(z) / pocet;
                if (Math.abs(prumer - maxPrumer) < 0.001) {
                    nejlepsiPrumerList.add(z);
                }
            }
        }

        System.out.println("=== STATISTIKY ===");
        System.out.println("Zaměstnanci s nejvíce vazbami (" + maxVazeb + "):");
        if (nejvicVazebList.isEmpty() || maxVazeb <= 0) {
            System.out.println("- žádné spolupráce");
        } else {
            nejvicVazebList.forEach(z -> System.out.println("- " + z.getJmeno() + " " + z.getPrijmeni()));
        }

        System.out.printf("%nZaměstnanci s nejlepším průměrem (%.2f):%n", maxPrumer);
        if (nejlepsiPrumerList.isEmpty()) {
            System.out.println("- žádné spolupráce");
        } else {
            nejlepsiPrumerList.forEach(z -> System.out.println("- " + z.getJmeno() + " " + z.getPrijmeni()));
        }
    }

    private double spocitejSumu(Zamestnanec z) {
        double suma = 0;
        for (UrovenSpoluprace uroven : z.getSpoluprace().values()) {
            suma += switch (uroven) {
                case DOBRA -> 3;
                case PRUMERNA -> 2;
                case SPATNA -> 1;
            };
        }
        return suma;
    }

    public void spustVypocet() {
        for (Zamestnanec z : zamestnanci.values()) {
            z.vypocet();
        }
    }

    public void najdiZamestnance(int id) {
        Zamestnanec z = zamestnanci.get(id);

        if (z != null) {
            System.out.println("Nalezen:");
            System.out.println("ID:" + z.getId() + " " + z.getPrijmeni() + " " + z.getJmeno() + " (" + z.getRokNarozeni() + ")");

            if (z.getSpoluprace().isEmpty()) {
                System.out.println("Žádné spolupráce.");
            } else {
                System.out.println("Spolupráce:");
                for (var entry : z.getSpoluprace().entrySet()) {
                    System.out.println("- ID kolegy: " + entry.getKey() + " (Kvalita: " + entry.getValue() + ")");
                }
            }
        } else {
            System.out.println("Pod tímto ID nikdo není.");
        }
    }

    public void vypisSpoluprace() {
        System.out.println("=== VŠECHNY SPOLUPRÁCE ===");

        for (Zamestnanec z : zamestnanci.values()) {
            for (var entry : z.getSpoluprace().entrySet()) {
                int id1 = z.getId();
                int id2 = entry.getKey();

                if (id1 < id2) {
                    Zamestnanec z2 = zamestnanci.get(id2);
                    System.out.println(z + " <-> " + z2 + " : " + entry.getValue());
                }
            }
        }
    }

    public void ulozDoSouboru() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SOUBOR))) {

            // Uložení zaměstnanců
            for (Zamestnanec z : zamestnanci.values()) {
                String typ = (z instanceof DatovyAnalytik) ? "Analytik" : "BezpecnostniSpecialista";
                pw.println(z.getId() + ";" + z.getJmeno() + ";" + z.getPrijmeni() + ";" + z.getRokNarozeni() + ";" + typ);
            }

            // Uložení spolupráce (každá vazba jen jednou)
            for (Zamestnanec z : zamestnanci.values()) {
                for (var entry : z.getSpoluprace().entrySet()) {
                    if (z.getId() < entry.getKey()) {
                        pw.println("S;" + z.getId() + ";" + entry.getKey() + ";" + entry.getValue());
                    }
                }
            }

            System.out.println("Uloženo do souboru: " + SOUBOR);
        } catch (Exception e) {
            System.out.println("Chyba při ukládání!");
            e.printStackTrace();
        }
    }

    public void nactiZeSouboru() {
        try (BufferedReader br = new BufferedReader(new FileReader(SOUBOR))) {
            zamestnanci.clear();
            this.dalsiId = 1;

            String radek;
            while ((radek = br.readLine()) != null) {
                if (radek.trim().isEmpty()) continue;

                String[] casti = radek.split(";");

                if (casti[0].equals("S")) {
                    int id1 = Integer.parseInt(casti[1]);
                    int id2 = Integer.parseInt(casti[2]);
                    UrovenSpoluprace uroven = UrovenSpoluprace.valueOf(casti[3]);

                    if (zamestnanci.containsKey(id1) && zamestnanci.containsKey(id2)) {
                        zamestnanci.get(id1).pridejSpolupraci(id2, uroven);
                        zamestnanci.get(id2).pridejSpolupraci(id1, uroven);
                    }
                } else {
                    int id = Integer.parseInt(casti[0]);
                    String jmeno = casti[1];
                    String prijmeni = casti[2];
                    int rok = Integer.parseInt(casti[3]);
                    String typ = casti[4];

                    if (id >= dalsiId) dalsiId = id + 1;

                    Zamestnanec z;
                    if (typ.equalsIgnoreCase("Analytik")) {
                        z = new DatovyAnalytik(id, jmeno, prijmeni, rok, this.zamestnanci);
                    } else {
                        z = new BezpecnostniSpecialista(id, jmeno, prijmeni, rok);
                    }

                    zamestnanci.put(id, z);
                }
            }

            System.out.println("Data byla úspěšně načtena ze souboru.");
            System.out.println("Celkový počet načtených zaměstnanců: " + zamestnanci.size());

        } catch (FileNotFoundException e) {
            System.out.println("Soubor nenalezen, začínám s prázdnou databází.");
        } catch (Exception e) {
            System.out.println("Chyba při načítání souboru: " + e.getMessage());
        }
    }
}
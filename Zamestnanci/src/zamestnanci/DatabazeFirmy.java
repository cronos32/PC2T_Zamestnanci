package zamestnanci;

import java.io.*;
import java.sql.*;
import java.util.*;

public class DatabazeFirmy {

    private static final String SOUBOR = "data.txt";

    private Connection conn;
    private Map<Integer, Zamestnanec> zamestnanci = new HashMap<>();
    private int dalsiId = 1;

    public boolean connect(String dbName) {
        conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public void disconnect() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

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

            for (Zamestnanec z : zamestnanci.values()) {
                String typ = (z instanceof DatovyAnalytik) ? "Analytik" : "BezpecnostniSpecialista";
                pw.println(z.getId() + ";" + z.getJmeno() + ";" + z.getPrijmeni() + ";" + z.getRokNarozeni() + ";" + typ);
            }

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
            List<String[]> spoluprace = new ArrayList<>();

            String radek;
            while ((radek = br.readLine()) != null) {
                if (radek.trim().isEmpty()) continue;

                String[] casti = radek.split(";");

                if (casti[0].equals("S")) {
                    spoluprace.add(casti);
                } else {
                    int id = Integer.parseInt(casti[0]);
                    if (zamestnanci.containsKey(id)) continue;

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

            for (String[] casti : spoluprace) {
                int id1 = Integer.parseInt(casti[1]);
                int id2 = Integer.parseInt(casti[2]);
                UrovenSpoluprace uroven = UrovenSpoluprace.valueOf(casti[3]);

                if (zamestnanci.containsKey(id1) && zamestnanci.containsKey(id2)) {
                    if (!zamestnanci.get(id1).getSpoluprace().containsKey(id2)) {
                        zamestnanci.get(id1).pridejSpolupraci(id2, uroven);
                        zamestnanci.get(id2).pridejSpolupraci(id1, uroven);
                    }
                }
            }

            System.out.println("Data úspěšně načtena a sloučena ze souboru.");
            System.out.println("Celkový počet zaměstnanců v databázi: " + zamestnanci.size());

        } catch (FileNotFoundException e) {
            System.out.println("Soubor nenalezen, začínám s prázdnou databází.");
        } catch (Exception e) {
            System.out.println("Chyba při načítání souboru: " + e.getMessage());
        }
    }

    public void nactiZeSQL() {
        try {
            vytvorTabulkyPokudNeExistuji();

            zamestnanci.clear();
            dalsiId = 1;

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM zamestnanci")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String jmeno = rs.getString("jmeno");
                    String prijmeni = rs.getString("prijmeni");
                    int rok = rs.getInt("rok_narozeni");
                    String typ = rs.getString("typ");

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

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM spoluprace")) {
                while (rs.next()) {
                    int id1 = rs.getInt("id1");
                    int id2 = rs.getInt("id2");
                    UrovenSpoluprace uroven = UrovenSpoluprace.valueOf(rs.getString("uroven"));

                    if (zamestnanci.containsKey(id1) && zamestnanci.containsKey(id2)) {
                        zamestnanci.get(id1).pridejSpolupraci(id2, uroven);
                        zamestnanci.get(id2).pridejSpolupraci(id1, uroven);
                    }
                }
            }

            System.out.println("Úspěšně načteno z SQL databáze. Počet zaměstnanců: " + zamestnanci.size());

        } catch (Exception e) {
            System.out.println("SQL databáze nenalezena nebo prázdná, začínám s prázdnou databází.");
        }
    }

    public void ulozDoSQL() {
        try {
            vytvorTabulkyPokudNeExistuji();

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM spoluprace");
                st.executeUpdate("DELETE FROM zamestnanci");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO zamestnanci (id, jmeno, prijmeni, rok_narozeni, typ) VALUES (?, ?, ?, ?, ?)")) {
                for (Zamestnanec z : zamestnanci.values()) {
                    String typ = (z instanceof DatovyAnalytik) ? "Analytik" : "BezpecnostniSpecialista";
                    ps.setInt(1, z.getId());
                    ps.setString(2, z.getJmeno());
                    ps.setString(3, z.getPrijmeni());
                    ps.setInt(4, z.getRokNarozeni());
                    ps.setString(5, typ);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO spoluprace (id1, id2, uroven) VALUES (?, ?, ?)")) {
                for (Zamestnanec z : zamestnanci.values()) {
                    for (var entry : z.getSpoluprace().entrySet()) {
                        if (z.getId() < entry.getKey()) {
                            ps.setInt(1, z.getId());
                            ps.setInt(2, entry.getKey());
                            ps.setString(3, entry.getValue().name());
                            ps.executeUpdate();
                        }
                    }
                }
            }

            System.out.println("Úspěšně uloženo do SQL databáze. Počet zaměstnanců: " + zamestnanci.size());

        } catch (Exception e) {
            System.out.println("Chyba při ukládání do SQL: " + e.getMessage());
        }
    }

    private void vytvorTabulkyPokudNeExistuji() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS zamestnanci (" +
                "id INT PRIMARY KEY, " +
                "jmeno VARCHAR(100) NOT NULL, " +
                "prijmeni VARCHAR(100) NOT NULL, " +
                "rok_narozeni INT NOT NULL, " +
                "typ VARCHAR(50) NOT NULL)");

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS spoluprace (" +
                "id1 INT NOT NULL, " +
                "id2 INT NOT NULL, " +
                "uroven VARCHAR(20) NOT NULL, " +
                "PRIMARY KEY (id1, id2))");
        }
    }
}
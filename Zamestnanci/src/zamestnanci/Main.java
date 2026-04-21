package zamestnanci;

import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        DatabazeFirmy db = new DatabazeFirmy();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n1 - Přidat zaměstnance");
            System.out.println("2 - Přidat spolupráci");
            System.out.println("3 - Odebrat zaměstnance");
            System.out.println("4 - Výpis zaměstnanců");
            System.out.println("5 - Statistiky");
            System.out.println("6 - Výpočty");
            System.out.println("7 - Najít zaměstnance podle ID");
            System.out.println("8 - Vypsat spolupráce");
            System.out.println("9 - Počty ve skupinách");
            System.out.println("10 - Načtení ze souboru");
            System.out.println("11 - Uložení do souboru");
            System.out.println("0 - Konec");

            int volba;
            try {
                volba = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Zadej číslo!");
                continue;
            }

            switch (volba) {
                case 1 -> {
                    System.out.println("Typ analytik[1] / security[2]:");
                    String vstup = sc.nextLine().trim().toLowerCase();

                    String typ;
                    if (vstup.equals("1") || vstup.equals("analytik")) {
                        typ = "analytik";
                    } else if (vstup.equals("2") || vstup.equals("security")) {
                        typ = "security";
                    } else {
                        System.out.println("Neplatný typ!");
                        continue;
                    }

                    System.out.println("Jméno:");
                    String jmeno = sc.nextLine().trim();
                    System.out.println("Příjmení:");
                    String prijmeni = sc.nextLine().trim();
                    System.out.println("Rok narození:");
                    int rok;
                    try {
                        rok = Integer.parseInt(sc.nextLine().trim());
                    } catch (Exception e) {
                        System.out.println("Neplatný rok!");
                        continue;
                    }

                    db.pridatZamestnance(typ, jmeno, prijmeni, rok);
                }

                case 2 -> {
                    System.out.println("ID1:");
                    int id1;
                    int id2;
                    try {
                        id1 = Integer.parseInt(sc.nextLine().trim());
                        System.out.println("ID2:");
                        id2 = Integer.parseInt(sc.nextLine().trim());
                    } catch (Exception e) {
                        System.out.println("Neplatné ID!");
                        continue;
                    }

                    System.out.println("Úroveň (spatna[1], prumerna[2], dobra[3]):");
                    String vstup = sc.nextLine().trim().toLowerCase();

                    UrovenSpoluprace uroven;
                    if (vstup.equals("1") || vstup.startsWith("spat")) {
                        uroven = UrovenSpoluprace.SPATNA;
                    } else if (vstup.equals("2") || vstup.startsWith("prum")) {
                        uroven = UrovenSpoluprace.PRUMERNA;
                    } else if (vstup.equals("3") || vstup.startsWith("dobr")) {
                        uroven = UrovenSpoluprace.DOBRA;
                    } else {
                        System.out.println("Neplatná hodnota!");
                        continue;
                    }

                    db.pridatSpolupraci(id1, id2, uroven, sc);
                }

                case 3 -> {
                    System.out.println("Zadejte ID k odstranění:");
                    try {
                        int id = Integer.parseInt(sc.nextLine().trim());
                        db.odebratZamestnance(id);
                    } catch (Exception e) {
                        System.out.println("Neplatné ID!");
                    }
                }

                case 4 -> db.vypisZamestnance();

                case 5 -> db.statistiky();

                case 6 -> {
                    System.out.println("\n=== VÝSLEDKY ANALÝZ A VÝPOČTŮ (Seřazeno podle skupin) ===");

                    Map<Integer, Zamestnanec> mapa = db.getZamestnanci();

                    if (mapa == null || mapa.isEmpty()) {
                        System.out.println("V databázi nejsou žádní zaměstnanci.");
                    } else {
                        System.out.println("\n>>> DATOVÍ ANALYTICI");
                        System.out.println("===================================");
                        for (Zamestnanec z : mapa.values()) {
                            if (z instanceof DatovyAnalytik) {
                                z.vypocet();
                                System.out.println("-----------------------------------");
                            }
                        }

                        System.out.println("\n>>> SECURITY SPECIALISTÉ");
                        System.out.println("====== (DOBRÁ = 3, PRŮMĚRNÁ = 2, ŠPATNÁ = 1) ======");
                        for (Zamestnanec z : mapa.values()) {
                            if (z instanceof BezpecnostniSpecialista) {
                                z.vypocet();
                                System.out.println("-----------------------------------");
                            }
                        }
                    }
                }

                case 7 -> {
                    System.out.println("Zadej ID:");
                    try {
                        int id = Integer.parseInt(sc.nextLine().trim());
                        db.najdiZamestnance(id);
                    } catch (Exception e) {
                        System.out.println("Špatný vstup!");
                    }
                }

                case 8 -> db.vypisSpoluprace();

                case 9 -> db.vypisPoctyVeSkupinach();

                case 10 -> db.nactiZeSouboru();

                case 11 -> db.ulozDoSouboru();

                case 0 -> {
                    System.out.println("Nashledanou!");
                    System.exit(0);
                }
            }
        }
    }
}
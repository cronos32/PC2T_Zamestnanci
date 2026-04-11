package zamestnanci;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        CompanyDatabase db = new CompanyDatabase();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n1 - Přidat zaměstnance");
            System.out.println("2 - Přidat spolupráci");
            System.out.println("3 - Odebrat zaměstnance");
            System.out.println("4 - Výpis");
            System.out.println("5 - Statistiky");
            System.out.println("6 - Výpočty");
            System.out.println("7 - Najít zaměstnance podle ID");
            System.out.println("8 - Vypsat spolupráce");
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
                	System.out.println("Typ analyst[1] / security[2]:");
                	String vstup = sc.next().toLowerCase();

                	String typ;

                	if (vstup.equals("1") || vstup.equals("analyst")) {
                	    typ = "analyst";
                	} else if (vstup.equals("2") || vstup.equals("security")) {
                	    typ = "security";
                	} else {
                	    System.out.println("Neplatný typ!");
                	    continue; 
                	}
                    System.out.println("Jméno:");
                    String jmeno = sc.next();
                    System.out.println("Příjmení:");
                    String prijmeni = sc.next();
                    System.out.println("Rok narození:");
                    int rok = sc.nextInt();
                    
                    sc.nextLine();

                    db.pridatZamestnance(typ, jmeno, prijmeni, rok);
                }

                case 2 -> {
                    System.out.println("ID1:");
                    int id1 = sc.nextInt();
                    
                    System.out.println("ID2:");
                    int id2 = sc.nextInt();
                    
                    sc.nextLine();
                    
                    System.out.println("Level (spatna[1], prumerna[2], dobra[3]):");
                    String vstup = sc.nextLine().toLowerCase();

                    CollaborationLevel level;

                    if (vstup.equals("1") || vstup.startsWith("spat")) {
                        level = CollaborationLevel.SPATNA;
                    } else if (vstup.equals("2") || vstup.startsWith("prum")) {
                        level = CollaborationLevel.PRUMERNA;
                    } else if (vstup.equals("3") || vstup.startsWith("dobr")) {
                        level = CollaborationLevel.DOBRA;
                    } else {
                        System.out.println("Neplatná hodnota!");
                        continue;
                    }

                    db.pridatSpolupraci(id1, id2, level, sc);
                }

                case 3 -> {
                    System.out.println("ID:");
                    int id = sc.nextInt();
                    db.odebratZamestnance(id);
                }

                case 4 -> db.vypisZamestnance();
                
                case 5 -> db.statistiky();
                
                case 6 -> db.spustVypocet();
                
                case 7 -> {
                    System.out.println("Zadej ID:");

                    try {
                        int id = Integer.parseInt(sc.nextLine());
                        db.najdiZamestnance(id);
                    } catch (Exception e) {
                        System.out.println("Špatný vstup!");
                    }
                }
                
                case 8 -> db.vypisSpoluprace();
                
                case 0 -> System.exit(0);
            }
        }
    }
}

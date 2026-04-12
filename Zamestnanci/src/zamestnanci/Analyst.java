package zamestnanci;

import java.util.Map;

public class Analyst extends Employee {
    private Map<Integer, Employee> vsichni; // Tady si uložíme odkaz na celou databázi

    public Analyst(int id, String jmeno, String prijmeni, int rok, Map<Integer, Employee> vsichni) {
        super(id, jmeno, prijmeni, rok);
        this.vsichni = vsichni;
    }

    @Override
    public void vypocet() {
        System.out.println("Analytik: " + jmeno + " " + prijmeni);
        Employee nejKolega = null;
        int maxSpolecnych = -1;

        // Procházím lidi, se kterými já přímo spolupracuji
        for (Integer idKolegy : spoluprace.keySet()) {
            Employee kolega = vsichni.get(idKolegy);
            if (kolega == null) continue;

            int spolecni = 0;
            // Koukám, kolik mých známých zná i tento kolega
            for (Integer mojeId : spoluprace.keySet()) {
                if (kolega.getSpoluprace().containsKey(mojeId)) {
                    spolecni++;
                }
            }

            if (spolecni > maxSpolecnych) {
                maxSpolecnych = spolecni;
                nejKolega = kolega;
            }
        }

        if (nejKolega != null) {
            System.out.println("-> Nejvíce společných kolegů má s: " + nejKolega.getPrijmeni() + " (počet: " + maxSpolecnych + ")");
        } else {
            System.out.println("-> Zatím nemá žádné společné vazby s kolegy.");
        }
    }
}
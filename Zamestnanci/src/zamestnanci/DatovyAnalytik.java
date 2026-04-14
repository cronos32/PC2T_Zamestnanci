package zamestnanci;

import java.util.Map;

public class DatovyAnalytik extends Zamestnanec {
    private Map<Integer, Zamestnanec> vsichniZamestnanci;

    public DatovyAnalytik(int id, String jmeno, String prijmeni, int rok, Map<Integer, Zamestnanec> vsichniZamestnanci) {
        super(id, jmeno, prijmeni, rok);
        this.vsichniZamestnanci = vsichniZamestnanci;
    }

    @Override
    public void vypocet() {
        System.out.println("Analytik: " + jmeno + " " + prijmeni);
        Zamestnanec nejlepsiKolega = null;
        int maxSpolecnych = -1;

        for (Integer idKolegy : spoluprace.keySet()) {
            Zamestnanec kolega = vsichniZamestnanci.get(idKolegy);
            if (kolega == null) continue;

            int pocetSpolecnych = 0;
            for (Integer mojeId : spoluprace.keySet()) {
                if (!mojeId.equals(idKolegy) && kolega.getSpoluprace().containsKey(mojeId)) {
                    pocetSpolecnych++;
                }
            }

            if (pocetSpolecnych > maxSpolecnych) {
                maxSpolecnych = pocetSpolecnych;
                nejlepsiKolega = kolega;
            }
        }

        if (nejlepsiKolega != null) {
            System.out.println("-> Nejvíce společných kolegů má s: "
                    + nejlepsiKolega.getPrijmeni() + " (počet: " + maxSpolecnych + ")");
        } else {
            System.out.println("-> Zatím nemá žádné společné vazby s kolegy.");
        }
    }
}
package zamestnanci;

public class SecuritySpecialist extends Employee {

    public SecuritySpecialist(int id, String jmeno, String prijmeni, int rok) {
        super(id, jmeno, prijmeni, rok);
    }

    @Override
    public void vypocet() {
        System.out.println("Security: " + jmeno + " " + prijmeni);
        int pocet = spoluprace.size();
        
        if (pocet == 0) {
            System.out.println("-> Rizikové skóre: 0.00 (žádné spolupráce)");
            return;
        }

        double sumaRizika = 0;
        for (CollaborationLevel level : spoluprace.values()) {
            sumaRizika += switch (level) {
                case SPATNA -> 1.0;
                case PRUMERNA -> 2.0;
                case DOBRA -> 3.0;
            };
        }
        
        double skore = sumaRizika / pocet;
        System.out.printf("-> Rizikové skóre: %.2f (průměrná rizikovost vazeb)%n", skore);
    }
}
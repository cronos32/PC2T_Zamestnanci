package zamestnanci;

public class SecuritySpecialist extends Employee {

    public SecuritySpecialist(int id, String jmeno, String prijmeni, int rokNarozeni) {
        super(id, jmeno, prijmeni, rokNarozeni);
    }

    @Override
    public void vypocet() {
        int score = 0;

        for (CollaborationLevel c : spoluprace.values()) {
            switch (c) {
                case SPATNA -> score += 3;
                case PRUMERNA -> score += 2;
                case DOBRA -> score += 1;
            }
        }

        System.out.println("Rizikové skóre: " + score);
    }
}

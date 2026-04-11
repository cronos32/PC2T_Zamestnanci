package zamestnanci;

public class Analyst extends Employee {

    public Analyst(int id, String jmeno, String prijmeni, int rokNarozeni) {
        super(id, jmeno, prijmeni, rokNarozeni);
    }

    @Override
    public void vypocet() {
        System.out.println("Analýza spoluprací pro: " + jmeno);
        System.out.println("Počet spolupracovníků: " + spoluprace.size());
    }
}

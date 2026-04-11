package zamestnanci;

import java.util.*;

public abstract class Employee {
    protected int id;
    protected String jmeno;
    protected String prijmeni;
    protected int rokNarozeni;

    // vztahy: ID kolegy -> kvalita spolupráce
    protected Map<Integer, CollaborationLevel> spoluprace = new HashMap<>();

    public Employee(int id, String jmeno, String prijmeni, int rokNarozeni) {
        this.id = id;
        this.jmeno = jmeno;
        this.prijmeni = prijmeni;
        this.rokNarozeni = rokNarozeni;
    }

    public void pridejSpolupraci(int kolegaId, CollaborationLevel level) {
        spoluprace.put(kolegaId, level);
    }

    public void odeberSpolupraci(int kolegaId) {
        spoluprace.remove(kolegaId);
    }

    public int getId() {
        return id;
    }

    public String getPrijmeni() {
        return prijmeni;
    }

    public Map<Integer, CollaborationLevel> getSpoluprace() {
        return spoluprace;
    }

    public abstract void vypocet(); // specifická funkce skupiny

    @Override
    public String toString() {
        return id + ": " + jmeno + " " + prijmeni + " (" + rokNarozeni + ")";
    }
}

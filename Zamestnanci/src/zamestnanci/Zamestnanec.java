package zamestnanci;

import java.util.*;

public abstract class Zamestnanec {
    protected int id;
    protected String jmeno;
    protected String prijmeni;
    protected int rokNarozeni;

    // vztahy: ID kolegy -> kvalita spolupráce
    protected Map<Integer, UrovenSpoluprace> spoluprace = new HashMap<>();

    public Zamestnanec(int id, String jmeno, String prijmeni, int rokNarozeni) {
        this.id = id;
        this.jmeno = jmeno;
        this.prijmeni = prijmeni;
        this.rokNarozeni = rokNarozeni;
    }

    public void pridejSpolupraci(int idKolegy, UrovenSpoluprace uroven) {
        spoluprace.put(idKolegy, uroven);
    }

    public void odeberSpolupraci(int idKolegy) {
        spoluprace.remove(idKolegy);
    }

    public int getId() {
        return id;
    }

    public String getPrijmeni() {
        return prijmeni;
    }

    public String getJmeno() {
        return jmeno;
    }

    public int getRokNarozeni() {
        return rokNarozeni;
    }

    public Map<Integer, UrovenSpoluprace> getSpoluprace() {
        return spoluprace;
    }

    public abstract void vypocet();

    @Override
    public String toString() {
        return id + ": " + jmeno + " " + prijmeni + " (" + rokNarozeni + ")";
    }
}
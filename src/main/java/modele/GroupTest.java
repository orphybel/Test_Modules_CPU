package modele;

import com.jcraft.jsch.JSchException;
import services.CommunicationSSH;

import java.beans.EventHandler;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;

public class GroupTest implements Testable , List<Testable> {
    private Consumer<CommunicationSSH> beforeTest;
    private Consumer<CommunicationSSH> beforeEachTest;
    private Consumer<CommunicationSSH> afterTest;
    private Consumer<CommunicationSSH> afterEachTest;
    private List<Testable> tests;
    private String nom;

    public GroupTest removeConnectionManager() {
        return this.setBeforeTest(null).setAfterTest(null);

    }

    public GroupTest addConnectionManager() {
        return  this
                .setBeforeTest((connexion) -> {try {connexion.etablirConnexion();} catch (JSchException | IOException e) {e.printStackTrace();}
                }).setAfterTest((connexion -> connexion.fermerConnexion()));
    }

    public GroupTest(List<? extends Testable > tests) {
        this.tests = (List<Testable>) tests;
        nom = getDetails();
        addConnectionManager();
    }
    public GroupTest(Testable... tests) {
        this.tests = new ArrayList<>();
        this.tests.addAll(Arrays.asList(tests));
        nom = getDetails();
        addConnectionManager();
    }
    public GroupTest(String nom,Testable... tests) {
        this.tests = new ArrayList<>();
        this.tests.addAll(Arrays.asList(tests));
        this.nom = nom;
        addConnectionManager();
    }
    public GroupTest(String nom, List<? extends Testable > tests) {
        this.tests = (List<Testable>) tests;
        this.nom = nom;
        addConnectionManager();
    }
    @Override
    public void run(CommunicationSSH connexion) {
        if (beforeTest != null) beforeTest.accept(connexion);
        for (Testable t : tests) {
            if (beforeEachTest != null) beforeEachTest.accept(connexion);
            t.run(connexion);
            if(afterEachTest != null ) afterEachTest.accept(connexion);
        }
        if(afterTest != null ) afterTest.accept(connexion);

    }

    @Override
    public boolean isTestOk() {
        boolean res = true;
        for (Testable t : tests) {
            res = res && t.isTestOk();
        }
        return res;
    }

    @Override
    public String getDetails() {
        String res = "";
        for (Testable t : tests) {
            res += t.getDetails() + "\n";
        }
        return res;
    }

    @Override
    public List<Balise> toBalise() {
        // Concatener les balises de chaque test
        List<Testable> Alltests = getTests();
        List<Balise> res = new ArrayList<>();

        for (Testable t : Alltests) {
            res.addAll(t.toBalise());
        }

        return res;
    }

    private List<Testable> getTests() {
        List<Testable> tests = new ArrayList<>();
        for (Testable t : this.tests) {
            if (t instanceof GroupTest) {
                tests.addAll(((GroupTest) t).getTests());
            } else {
                tests.add(t);
            }
        }
        return tests;
    }

    private int getNbTest(int nbTest) {
        for (Testable t : tests) {
            if (t instanceof GroupTest) {
                nbTest += ((GroupTest) t).getNbTest(nbTest);
            } else {
                nbTest++;
            }
        }
        return nbTest;
    }

    @Override
    public int getTempAlancer() {
        int res = 0;
        for (Testable t : tests) {
            res += t.getTempAlancer();
        }
        return res;
    }

    @Override
    public String getNomTest() {
        if (nom == null) {
            nom = "";
            for (Testable t : tests) nom += t.getNomTest() + " ";
        }
        return nom;
    }

    @Override
    public int size() {
        return tests.size();
    }

    @Override
    public boolean isEmpty() {
        return tests.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return tests.contains(o);
    }

    @Override
    public Iterator<Testable> iterator() {
        return (Iterator<Testable>) tests.iterator();
    }

    @Override
    public Object[] toArray() {
        return tests.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return tests.toArray(a);
    }

    @Override
    public boolean add(Testable testable) {
        return tests.add(testable);
    }

    @Override
    public boolean remove(Object o) {
        return tests.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return tests.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Testable> c) {
        return tests.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Testable> c) {
        return tests.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return tests.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return tests.retainAll(c);
    }

    @Override
    public void clear() {
        tests.clear();
    }

    @Override
    public Testable get(int index) {
        return tests.get(index);
    }

    @Override
    public Testable set(int index, Testable element) {
        return tests.set(index, element);
    }

    @Override
    public void add(int index, Testable element) {
        tests.add(index, element);
    }

    @Override
    public Testable remove(int index) {
        return tests.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return tests.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return tests.lastIndexOf(o);
    }

    @Override
    public ListIterator<Testable> listIterator() {
        return (ListIterator<Testable>) tests.listIterator();
    }

    @Override
    public ListIterator<Testable> listIterator(int index) {
        return (ListIterator<Testable>) tests.listIterator(index);
    }

    @Override
    public List<Testable> subList(int fromIndex, int toIndex) {
        return tests.subList(fromIndex, toIndex);
    }


    public GroupTest setBeforeTest(Consumer<CommunicationSSH> beforeTest) {
        this.beforeTest = beforeTest;
        return this;
    }
    public GroupTest setAfterTest(Consumer<CommunicationSSH> afterTest) {
        this.afterTest = afterTest;
        return this;
    }
    public GroupTest setBeforeEachTest(Consumer<CommunicationSSH> beforeEachTest) {
        this.beforeEachTest = beforeEachTest;
        return this;
    }
    public GroupTest setAfterEachTest(Consumer<CommunicationSSH> afterEachTest) {
        this.afterEachTest = afterEachTest;
        return this;
    }
}

package modele;

import jakarta.xml.bind.annotation.*;
import modele.composants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
@XmlRootElement(name = "Module")
@XmlAccessorType(XmlAccessType.FIELD)
public class Module {

    @XmlAttribute(name = "numero")
    private String numero;
    @XmlAttribute(name = "designation")
    private String designation;
    @XmlAttribute(name = "pathModelePv")
    private String pathModelePv;
    @XmlAttribute(name = "type")
    private String type;

    @XmlElementWrapper(name = "DisqueSatas")
    @XmlElement(name = "DisqueSata")
    protected List<DisqueSATA> disquesSata;
    @XmlElementWrapper(name = "PortEthernets")
    @XmlElement(name = "PortEthernet")
    private List<PortEthernet> portsEthernet;
    @XmlElementWrapper(name = "Switchs")
    @XmlElement(name = "Switch")
    private List<Switch> switchs;

    @XmlElementWrapper(name = "EnssembleLeds")
    @XmlElement(name = "EnssembleLed")
    private List<EnssembleLedsTestManuel> enssembleLeds;

    @XmlElementWrapper(name = "EnssembleLedsAuto")
    @XmlElement(name = "EnssembleLedAuto")
    private List<EnsembleLedsTestAuto> enssembleLedsAuto;
    @XmlElement(name = "Dataflash")
    private Dataflash dataflash;
    @XmlElement(name = "Fram")
    private Fram fram;
    @XmlElement(name = "Rs")
    private Rs rs;
    @XmlElement(name = "RTC")
    private RTC rtc;
    @XmlElement(name = "Watchdog")
    private Watchdog watchdog;
    @XmlElement(name = "Temperature")
    private Temperature temperature;


    public Module() {
    }


    public ArrayList<Testable> getTestables() {

        ArrayList<Testable> testables = new ArrayList<>();
        testables.add(new GroupTest("Test leds \nface avant",enssembleLeds)
                .setBeforeEachTest(connexion -> getAllLeds().forEach(led -> led.turnState(connexion,false))));
        testables.addAll(enssembleLedsAuto);
        testables.add(new GroupTest("Test switchs \nGPIO",switchs));
        testables.add(new GroupTest("Test des \naccès SATA",disquesSata));
        testables.add(new GroupTest("Test des \naccès Ethernet",portsEthernet));

        testables.addAll(getSingleElements());

        return testables;
    }

    public void forEachTestableCompenent(Consumer<Testable> consumer) {
    	ArrayList<Testable> testables = getTestables();
        testables.forEach((testable) -> {
            if (testable != null) {
                consumer.accept(testable);
            }
        });
    }

    public ArrayList<Leds> getAllLeds() {
    	ArrayList<Leds> leds = new ArrayList<>();
    	enssembleLeds.forEach((enssemble) -> {
    		leds.addAll(enssemble.getLeds());
    	});
    	return leds;
    }

    public String getTemplate() {
        return pathModelePv;
    }
    public String toString() {
        return designation + " (" + numero + ")";
    }
    public String getNumero() {
        return numero;
    }
    public String getType() {
        return type;
    }
    public String getDesignation() {
        return designation;
    }

    public List<DisqueSATA> getDisqueSatas() {
        return disquesSata;
    }

    public List<PortEthernet> getPortEthernets() {
        return portsEthernet;
    }

    public List<Switch> getSwitchs() {
        return switchs;
    }
    public List<EnsembleLedsTestAuto> getEnsembleLedsTestAuto() {
        return enssembleLedsAuto;
    }
    public List<EnssembleLedsTestManuel> getEnsembleLedsTestManuel() {
        return enssembleLeds;
    }
    public List<Testable> getSingleElements() {
        return Arrays.asList(dataflash,rtc,fram,temperature,watchdog,rs);
    }
}

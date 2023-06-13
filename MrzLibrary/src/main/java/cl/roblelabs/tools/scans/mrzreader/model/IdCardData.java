package cl.roblelabs.tools.scans.mrzreader.model;

import java.io.Serializable;

public class IdCardData implements Serializable {
    String pais, paisEmisor;
    String apellidos, nombres, sexo, personalIdNumber;
    String documentNumber;
    String vencimiento, nacimiento;
}

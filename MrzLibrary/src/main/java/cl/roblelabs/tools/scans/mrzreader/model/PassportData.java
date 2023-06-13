package cl.roblelabs.tools.scans.mrzreader.model;

import java.io.Serializable;

public class PassportData implements Serializable {
    String paisEmisor, nacionalidad;
    String apellidos, nombres;
    String passportNumber, nacimiento, vencimiento, sexo;
    String identifier;
}
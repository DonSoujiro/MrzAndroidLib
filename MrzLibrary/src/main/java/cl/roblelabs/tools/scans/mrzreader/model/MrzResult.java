package cl.roblelabs.tools.scans.mrzreader.model;


import java.io.Serializable;

import cl.roblelabs.tools.scans.mrzreader.ocr.OcrResult;

public class MrzResult implements Serializable {

    public MRZ_TYPE type;
    public String[] lines;
    public int confianza;
    public DOC_TYPE docType;

    public PassportData passportData;
    public IdCardData idCardData;

    public enum COUNTRY  {
        CHILE, PERU, COLOMBIA
    }

    public enum DOC_TYPE  {
        PASSPORT, ID_CARD
    }

    public enum MRZ_TYPE  {
            TYPE1, TYPE2, TYPE3
    }

    public MrzResult(OcrResult ocrResult){
        try {
            this.lines = ocrResult.getText().split("\\n");
            if (this.lines.length == 3) {
                this.type = MRZ_TYPE.TYPE1;
            } else if (this.lines.length == 2) {
                if (this.lines[0].length() == 44) {
                    this.type = MRZ_TYPE.TYPE3;
                } else if (this.lines[0].length() == 36) {
                    this.type = MRZ_TYPE.TYPE2;
                }
            }
            if (this.type == MRZ_TYPE.TYPE3) {
                if (this.lines[0].charAt(0) == 'P') {
                    this.docType = DOC_TYPE.PASSPORT;
                    setPassportData();
                }
            } else if (this.type == MRZ_TYPE.TYPE1) {
                if (this.lines[0].length() == 30 && this.lines[1].length() == 30 && this.lines[2].length() == 30) {
                    char initChar = this.lines[0].charAt(0);
                    if ( initChar == 'I' || initChar == 'A' || initChar == 'C') {
                        setIdCardData();
                    }
                }
            }
            this.confianza = ocrResult.getMeanConfidence();

        } catch(Exception e){
            this.formatoValido = false;
        }
    }

    private void setIdCardData(){
        this.docType = DOC_TYPE.ID_CARD;
        this.idCardData = new IdCardData();
        idCardData.paisEmisor = this.lines[0].substring(2,5);
        idCardData.documentNumber = this.lines[0].substring(5,14);

        idCardData.nacimiento = this.lines[1].substring(0, 6);
        idCardData.sexo = String.valueOf(this.lines[1].charAt(7));
        idCardData.vencimiento = this.lines[1].substring(8,14);
        idCardData.pais = this.lines[1].substring(15,18);
        idCardData.personalIdNumber = this.lines[1].substring(18, 29).replaceAll("<", " ").trim();

        String[] nombresApellidos = this.lines[2].split("<<");
        idCardData.apellidos = nombresApellidos[0].replaceAll("<", " ");
        idCardData.nombres = nombresApellidos[1].replaceAll("<", " ");

        this.validaLetras(idCardData.nombres);
        this.validaLetras(idCardData.apellidos);
        this.validaLetras(idCardData.pais);
        this.validaLetras(idCardData.paisEmisor);
        this.validaNumeros(idCardData.nacimiento);
        this.validaNumeros(idCardData.vencimiento);
    }

    private void setPassportData(){
        this.passportData = new PassportData();
        passportData.paisEmisor = this.lines[0].substring(2,5);
        String linea1SoloNombres = this.lines[0].substring(5);
        String[] nombresApellidos = linea1SoloNombres.split("<<");
        passportData.apellidos = nombresApellidos[0].replaceAll("<", " ");
        passportData.nombres = nombresApellidos[1].replaceAll("<", " ");

        passportData.passportNumber = this.lines[1].substring(0,9);
        passportData.nacionalidad = this.lines[1].substring(10,13);
        passportData.nacimiento = this.lines[1].substring(13,19);
        passportData.sexo = String.valueOf(this.lines[1].charAt(20));
        passportData.vencimiento = this.lines[1].substring(21, 27);
        passportData.identifier = this.lines[1].substring(28,42).replaceAll("<", " ").trim();

        this.validaLetras(passportData.nombres);
        this.validaLetras(passportData.apellidos);
        this.validaLetras(passportData.nacionalidad);
        this.validaLetras(passportData.paisEmisor);
        this.validaNumeros(passportData.nacimiento);
        this.validaNumeros(passportData.vencimiento);
    }

    private void validaLetras(String input){
        char[] chars = input.replaceAll(" ", "").toCharArray();
        for (char c : chars) {
            if(!Character.isLetter(c)) {
                this.formatoValido = false;
            }
        }
    }
    private void validaNumeros(String input){
        char[] chars = input.toCharArray();
        for (char c : chars) {
            if(!Character.isDigit(c)) {
                this.formatoValido = false;
            }
        }
    }

    // se calento peru !
    //
    private boolean formatoValido = true;
    public boolean validaFormato(){
        if(this.type == null || this.docType == null)
            return false;
        if(this.passportData == null && this.idCardData == null)
            return false;

        return this.formatoValido;
    }

}

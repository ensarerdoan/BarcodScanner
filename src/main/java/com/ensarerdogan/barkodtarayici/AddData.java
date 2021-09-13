package com.ensarerdogan.barkodtarayici;

public class AddData {
    private String depo;
    private String marka;
    private String barcode;
    private String stok;
    private String category;

    public AddData(String depo,String marka, String barcode,String stok,String category){
        this.depo=depo;
        this.marka=marka;
        this.barcode=barcode;
        this.stok=stok;
        this.category=category;
    }

    public String getDepo() { return depo; }

    public void setDepo(String depo) { this.depo = depo; }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMarka() {
        return marka;
    }

    public void setMarka(String marka) {
        this.marka = marka;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getStok() {
        return stok;
    }

    public void setStok(String stok) {
        this.stok = stok;
    }
}

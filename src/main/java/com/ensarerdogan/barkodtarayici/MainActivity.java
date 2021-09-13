package com.ensarerdogan.barkodtarayici;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase database;

    AlertDialog.Builder mAlertDialog,mAlertDialog2;

    private static final String TAG = "MainActivity";
    private FirebaseFirestore firestore;
    private FirebaseDatabase db;
    private DatabaseReference dbRef;
    Button btnApplyFilter, btnSave,btnFilter;
    EditText eTxtCode, eTxtStok;
    TextView txtCode,txtStok;
    int sayac=0;
    int stock;
    String updateKey,updateBarcode;
    int controlForNewOrOld; // 1 = old - 0 = new

    String datafromEt;
    ArrayList<String> markaFromDB;
    ArrayList<String> marka;
    ArrayList<String> categoryList;
    ArrayList<String> categoryFromDB;
    ArrayList<String> wareHouseList;
    ArrayList<String> warehouseFromDB;
    ArrayList<String> listCodes,listKeys,listStock,listWarehouse;

    private Spinner markalar, category,warehouse;
    private ArrayAdapter<String> adapter, adapterCategory, adapterWarehouse;
    public String markaforDB,categoryforDB,warehouseforDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getInfo();

        database=this.openOrCreateDatabase("Scanner",MODE_PRIVATE,null);
        marka=new ArrayList<>();
        markaFromDB=new ArrayList<>();
        categoryList=new ArrayList<>();
        categoryFromDB=new ArrayList<>();
        wareHouseList=new ArrayList<>();
        warehouseFromDB=new ArrayList<>();
        listCodes=new ArrayList<>();
        listKeys=new ArrayList<>();
        listStock=new ArrayList<>();
        listWarehouse=new ArrayList<>();
        //btnFilter=findViewById(R.id.btnFilter);
        btnSave=findViewById(R.id.btnKaydet);
        eTxtCode=findViewById(R.id.txtCode);
        eTxtStok=findViewById(R.id.txtAdet);
        txtCode=findViewById(R.id.tViewAddBarcode);
        txtStok=findViewById(R.id.tViewAddStok);

        InputFilter[] filters=new InputFilter[1];
        filters[0]=new InputFilter.LengthFilter(13);
        eTxtCode.setFilters(filters);

        //firestore=FirebaseFirestore.getInstance();
        db=FirebaseDatabase.getInstance();

        getDataBrand();
        getDataCategory();
        getDataWarehouse();
        markalar=(Spinner) findViewById(R.id.spinnerMarka);
        category=(Spinner) findViewById(R.id.spinnerCategory);
        warehouse=(Spinner) findViewById(R.id.spinnerWarehouse);

        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,markaFromDB);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        markalar.setAdapter(adapter);

        adapterCategory=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,categoryFromDB);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapterCategory);

        adapterWarehouse=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, warehouseFromDB);
        adapterWarehouse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        warehouse.setAdapter(adapterWarehouse);

        markalar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                markaforDB=(String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryforDB=(String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        warehouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                warehouseforDB=(String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        getData();
    }


   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.info:
                sayac++;
                getInfo();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void btnScanCode(View v){
        scanCode();
    }

    public void btnSave(View v){
        datafromEt=eTxtCode.getText().toString();

        int i=0;
        while(i<listKeys.size()){
            if (listCodes.get(i).equals(datafromEt)&&listWarehouse.get(i).equals(warehouseforDB)){
                controlForNewOrOld=1;

                //System.out.println(listCodes.get(i));
                //System.out.println("Eşit");
                updateKey=listKeys.get(i);
                stock=Integer.parseInt(listStock.get(i));
                if (eTxtStok.getText().toString().equals("")) {
                    stock = stock+1;
                } else {
                    stock = stock+ Integer.parseInt(eTxtStok.getText().toString());
                }
                //

                break;
            }else{
                controlForNewOrOld=0;
            }

            //System.out.println(listKeys.get(i));
            //System.out.println(listCodes.get(i));
            //System.out.println(listStock.get(i));
            i++;
        }

        compareCode(controlForNewOrOld);
        updateKey="";
        stock=0;
        getData();
    }
    // search butonu
   /* public void btnFilter(View v){
        final Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.filter_dialog);

        btnApplyFilter= dialog.findViewById(R.id.applyFilter);

        getDataBrand();
        getDataCategory();
        getDataWarehouse();

        markalar= dialog.findViewById(R.id.spinnerMarka);
        category= dialog.findViewById(R.id.spinnerCategory);
        warehouse= dialog.findViewById(R.id.spinnerWarehouse);

        markalar.setAdapter(adapter);

       adapterCategory=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,categoryFromDB);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapterCategory);

        adapterWarehouse=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, warehouseFromDB);
        adapterWarehouse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        warehouse.setAdapter(adapterWarehouse);

        markalar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                markaforDB=(String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryforDB=(String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        warehouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                warehouseforDB=(String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }*/

    private void scanCode(){
        IntentIntegrator integrator=new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result !=null){
            if (result.getContents()!=null) {
                eTxtCode.setText(result.getContents());
            } else {
                Toast.makeText(getApplicationContext(),"Sonuç Yok",Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void addBarcode(String depo, String marka, String dbCode, String dbStok, String dbCategory) {
        //Firestore
        /*
        Map<String, Object> barcode = new HashMap<>();
        barcode.put("barcode", eTxtCode.getText().toString());
        barcode.put("stok", eTxtStok.getText().toString());

        // Add a new document with a generated ID
        firestore.collection("barcodes")
                .add(barcode)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
        // [END add_ada_lovelace]*/
        //Realtime Database

        DatabaseReference dbRef = db.getReference().child("barkodlar");
        dbRef.push().setValue(new AddData(depo, marka, dbCode, dbStok, dbCategory));
    }

    public void addBrand(String brandName){
        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS brand (id INTEGER PRIMARY KEY AUTOINCREMENT, brand VARCHAR)");

            database.execSQL("INSERT INTO brand (brand) VALUES ('"+brandName+"')");
            //database.execSQL("DELETE FROM brand");


            //String sqlString="INSERT INTO brand (brand) VALUES (?)";
            //SQLiteStatement statement=database.compileStatement(sqlString);
            //statement.bindString(1,brandName);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addCat(String catName){
        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS category (id INTEGER PRIMARY KEY AUTOINCREMENT, category VARCHAR)");

            database.execSQL("INSERT INTO category (category) VALUES ('"+catName+"')");
            //database.execSQL("DELETE FROM category");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addWh(String whName){
        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS warehouse (id INTEGER PRIMARY KEY AUTOINCREMENT, WarehouseDesc VARCHAR)");

            database.execSQL("INSERT INTO warehouse (WarehouseDesc) VALUES ('"+whName+"')");
            //database.execSQL("DELETE FROM category");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getDataBrand(){
        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS brand (id INTEGER PRIMARY KEY AUTOINCREMENT, brand VARCHAR)");
            Cursor cursor=database.rawQuery("SELECT * FROM brand",null);
            int brandIx=cursor.getColumnIndex("brand");

            while (cursor.moveToNext()){
                markaFromDB.add(cursor.getString(brandIx));
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getDataCategory(){
        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS category (id INTEGER PRIMARY KEY AUTOINCREMENT, category VARCHAR)");
            Cursor cursor=database.rawQuery("SELECT * FROM category",null);
            int categoryIx=cursor.getColumnIndex("category");

            while (cursor.moveToNext()){
                categoryFromDB.add(cursor.getString(categoryIx));
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getDataWarehouse(){
        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS warehouse (id INTEGER PRIMARY KEY AUTOINCREMENT, WarehouseDesc VARCHAR)");
            Cursor cursor=database.rawQuery("SELECT * FROM warehouse",null);
            int warehouseIx=cursor.getColumnIndex("WarehouseDesc");

            while (cursor.moveToNext()){
                warehouseFromDB.add(cursor.getString(warehouseIx));
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getInfo(){
        if (sayac==1) {
            wareHouseList.add("Merkez Depo");
            wareHouseList.add("Sarıyer 1 Mağaza");
            wareHouseList.add("Sarıyer 2 Mağaza");
            wareHouseList.add("Okmeydanı Mağaza");
            wareHouseList.add("Şirinevler Mağaza");
            wareHouseList.add("Çağlayan Mağaza");
            wareHouseList.add("Samsun Mağaza");
            wareHouseList.add("Çorum Mağaza");
            wareHouseList.add("Tokat Depo");
            wareHouseList.add("Turhal Mağaza");
            wareHouseList.add("Kastamonu Mağaza");
            wareHouseList.add("Amasya Mağaza");
            wareHouseList.add("Güneşli Mağaza");
            wareHouseList.add("Bafra 1 Mağaza");
            wareHouseList.add("Bafra 2 Mağaza");
            wareHouseList.add("Erbaa Mağaza");
            wareHouseList.add("Çekmeköy Mağaza");
            wareHouseList.add("Kurtköy Mağaza");
            wareHouseList.add("Fatsa Mağaza");
            wareHouseList.add("Beşyüzevler Mağaza");
            wareHouseList.add("Sivas Mağaza");
            wareHouseList.add("Merzifon Mağaza");
            wareHouseList.add("Ordu Mağaza");
            wareHouseList.add("Ünye Mağaza");
            wareHouseList.add("Antalya Mağaza");
            wareHouseList.add("Antalya Serik Mağaza");
            wareHouseList.add("Antalya Doğu Garajı Mağaza");
            wareHouseList.add("Edirne Mağaza");
            wareHouseList.add("Kırklareli Mağaza");
            wareHouseList.add("Mondi/Antalya Mağaza");
            wareHouseList.add("Sorgun Mağaza");
            wareHouseList.add("Antalya Merkez Depo");
            wareHouseList.add("Antalya Borusan Depo");
            wareHouseList.add("Edirne Merkez Depo");
            wareHouseList.add("Horoz Merkez Depo");
            wareHouseList.add("Merzifon Merkez Depo");
            wareHouseList.add("Samsun Loj.Mob. Depo");
            wareHouseList.add("Tokat Loj.Mob. Depo");
            wareHouseList.add("İstanbul Tezel Depo");

            for (int i = 0; i < wareHouseList.size(); i++) {
                addWh(wareHouseList.get(i));
            }
            categoryList.add("BEYAZ EŞYA");
            categoryList.add("ELEKTRONİK");
            categoryList.add("BİLGİSAYAR-TABLET-FOTOĞRAF GRUBU");
            categoryList.add("CEP TELEFONU");
            categoryList.add("KÜÇÜK EV ALETLERİ");
            categoryList.add("ALTIN VE KOL SAATİ");
            categoryList.add("EV TEKSTİLİ");
            categoryList.add("ZÜCCACİYE VE MUTFAK GRUBU");
            categoryList.add("ISITICI-SOĞUTUCU GRUPLARI");
            categoryList.add("MOBİLYA AHŞAP GRUBU");
            categoryList.add("MOBİLYA KOLTUK GRUBU");
            categoryList.add("BAZA VE YATAK");
            categoryList.add("HALI");
            categoryList.add("ÇOCUK/SPOR");

            for (int i = 0; i < categoryList.size(); i++) {
                addCat(categoryList.get(i));
            }
            marka.add("4 K");
            marka.add("AIRKING");
            marka.add("ALPA BAVUL");
            marka.add("ANKARA MOBİLYA");
            marka.add("ARMAĞAN TİCARAT");
            marka.add("ARNICA");
            marka.add("ARZUM");
            marka.add("ASIR MOBİLYA");
            marka.add("ASSOS MOBİLYA");
            marka.add("BADER BEŞİK");
            marka.add("BAYMAK");
            marka.add("BEKO");
            marka.add("BRAUN");
            marka.add("CANBA CAM");
            marka.add("CASPER");
            marka.add("CEV SU ARITMA");
            marka.add("COOKER");
            marka.add("EMSAN");
            marka.add("ENTI BABY");
            marka.add("EVMODUL MOBİLYA/AKSESUAR");
            marka.add("FAKİR");
            marka.add("GENCO");
            marka.add("GENERAL MOBILE");
            marka.add("GİZEM GRUP");
            marka.add("GOLD MASTER");
            marka.add("HALL HALI");
            marka.add("HİSAR MOBİLYA");
            marka.add("İPEK ÇEYİZ");
            marka.add("İSTANBUL HOME");
            marka.add("KARACA HOME");
            marka.add("KORKMAZ");
            marka.add("KOZA MECRA HALI");
            marka.add("MA ELEKTRONİK AS ONVO");
            marka.add("MARCA BELLA");
            marka.add("MENZİL AKSESUAR");
            marka.add("MİRAS SEHPA EV GEREÇLERİ");
            marka.add("MOBİLİ");
            marka.add("MODASEL");
            marka.add("MONDİ");
            marka.add("NECO");
            marka.add("NESA MOBİLYA");
            marka.add("PHILIPS");
            marka.add("PROFILO");
            marka.add("REGAL");
            marka.add("ROYAL SU ARITMA");
            marka.add("SABIR MOBİLYA");
            marka.add("SAMSUNG");
            marka.add("SCHAFER");
            marka.add("SELVİ MOBİLYA");
            marka.add("SENGÜLLER");
            marka.add("SEVGİ YATAKLARI");
            marka.add("SİMA MOBİLYA");
            marka.add("SIMFER");
            marka.add("SINGER");
            marka.add("SUNNY");
            marka.add("TAÇ");
            marka.add("TECNOPC");
            marka.add("TEFAL");
            marka.add("TEKNORYA");
            marka.add("ULUSAL TEKSTİL/VİNALDİ");
            marka.add("ÜMİT BİSİKLET");
            marka.add("VESTEL");
            marka.add("XIAOMI CEP");
            marka.add("XIAOMI DRK ELEKTRONİK");
            marka.add("ZÜCCACİYE/MUTFAK");


            for (int i = 0; i < marka.size(); i++) {
                addBrand(marka.get(i));
            }
        }
    }

    private void getData(){

        listCodes.clear();
        listKeys.clear();
        listStock.clear();
        listWarehouse.clear();

        dbRef=db.getReference().child("barkodlar");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    listCodes.add(ds.child("barcode").getValue().toString());
                    listKeys.add(ds.getKey());
                    listStock.add(ds.child("stok").getValue().toString());
                    listWarehouse.add(ds.child("depo").getValue().toString());
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updateStok(String key, int stock){

        //stock++;
        String stk=String.valueOf(stock);
        DatabaseReference dbRef=db.getReference().child("barkodlar");
        dbRef.child(key).child("stok").setValue(stk);

        System.out.println("Key : "+key);
        System.out.println("stok arttı : "+stk);

    }

    public void compareCode(int control){

        switch (control){
            case 1:
                //eski kayıt stok arttır güncelleme
                updateStok(updateKey,stock);
                break;
            case 0:
                //yeni kayıt stok ekle
                if (eTxtCode.getText().toString().matches("") && eTxtStok.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), "Alanlar boş bırakılamaz.", Toast.LENGTH_LONG).show();
                } else {
                    String stok;
                    if (eTxtStok.getText().toString().equals("")) {
                        stok = "1";
                    } else {
                        stok = eTxtStok.getText().toString();
                    }
                    addBarcode(warehouseforDB, markaforDB, eTxtCode.getText().toString(), stok, categoryforDB);
                    txtCode.setText(eTxtCode.getText().toString());
                    //txtStok.setText(stok);
                    txtStok.setText(eTxtStok.getText().toString());
                    //datafromEt = eTxtCode.getText().toString();
                    eTxtStok.setText("");
                    eTxtCode.setText("");
                }

                break;

        }
        System.out.println("control"+controlForNewOrOld);

    }
}

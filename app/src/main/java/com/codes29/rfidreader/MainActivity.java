package com.codes29.rfidreader;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_NFC = 200;
    private static final String TAG = "RFID reader";
    private final String[][] mTechLists;
    PendingIntent mPendingIntent;
    private NfcAdapter nfcAdapter;
    private TextView textViewInfo, tvRawData;
    private LinearLayout llLogs, llDetails;
    private TextView tvSchool,
            tvFirstName,
            tvLastName,
            tvDOB,
            tvSex,
            tvExamNo,
            tvDistrict,
            tvSubject1,
            tvSubject2,
            tvSubject3,
            tvSubject4,
            tvSubject5,
            tvSubject6,
            tvSubject7,
            tvSubject8,
            tvSubject9,
            tvRegion;
    private Button btnAttendance, btnMalpractice;
    private ImageView imgAvator;
    private IntentFilter mNdef;
    private IntentFilter[] mFilters;
    private Tag tag;
    private boolean authenticated = false;
    private Utils utils;
    private NfcManager mManager;

    public MainActivity() {

        // Tech list for all desired tags
        mTechLists = new String[][]{new String[]{NfcA.class.getName()}};
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        askPermissions();

        checkNFCDeviceSupport();

        wrapPendingIntent();

        saveMeta();


        btnAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llLogs.setVisibility(View.VISIBLE);
                llDetails.setVisibility(View.GONE);
            }
        });

        btnMalpractice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llLogs.setVisibility(View.GONE);
                llDetails.setVisibility(View.VISIBLE );
            }
        });
    }

    private void saveMeta() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());

        SQLiteHandler mydb = new SQLiteHandler(getApplicationContext());
        mydb.addUser("JB000001", "Femi", "Agoro", -33.86785, 151.20732, "Ikeja", 0, currentDateandTime);
        mydb.addUser("JB000002", "Juwon", "Ladega", -33.86785, 151.20732, "Ikorodu", 0, currentDateandTime);
        mydb.addUser("JB000003", "Seyi", "Adio", -33.86785, 151.20732, "Agege", 0, currentDateandTime);
        mydb.addUser("JB000004", "Dupe", "Williams", -33.86785, 151.20732, "Abeokuta", 0, currentDateandTime);
        mydb.addUser("JB000005", "Chioma", "Arinze", -33.86785, 151.20732, "Ibadan", 0, currentDateandTime);

        mydb.addUser("JB000006", "Yemi", "Agoro", -33.86785, 151.20732, "Federal Government College Ikorodu", 1, currentDateandTime);
        mydb.addUser("JB000007", "Bomi", "Kadiri", -33.86785, 151.20732, "Federal Government College Ilorin", 1, currentDateandTime);
        mydb.addUser("JB000008", "Feyi", "Sule", -33.86785, 151.20732, "Federal Government College Akure", 1, currentDateandTime);
        mydb.addUser("JB000009", "Sope", "Shomeru", -33.86785, 151.20732, "Federal Government College Benin", 1, currentDateandTime);
        mydb.addUser("JB000010", "Florence", "Ake", -33.86785, 151.20732, "Federal Government College Lagos", 1, currentDateandTime);

//        Toast.makeText(getApplicationContext(), "Registered Successful!!", Toast.LENGTH_LONG).show();
        mydb.addUserImage(100001, "9d df 3b b7", 61110010, "Acilo", "http://1.bp.blogspot.com/_sY37ini3P4Q/Sl8Rh9oQ2wI/AAAAAAAAAAw/OTRCUgl60f4/S240/ako.jpg");
        mydb.addUserImage(100002, "8d ff 57 b7", 6111002, "Jane", "http://1.bp.blogspot.com/-nYAqdM5wJac/VmPcpZpfo0I/AAAAAAAAXco/AG_i-TnTRxo/s1600/_DSC3175-Modifica.jpg");


    }

    private void wrapPendingIntent() {

        // PendingIntent object containing details of the scanned tag
        mPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        try {
            // Accept all MIME types
            mNdef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            mNdef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Toast.makeText(this, String.format("MalformedMimeTypeException: %s", e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
            e.printStackTrace(); // to logcat
        }

        mFilters = new IntentFilter[]{mNdef, new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};

    }

    private void checkNFCDeviceSupport() {
        if (nfcAdapter == null) {
            Toast.makeText(this,
                    "NFC NOT supported on this devices!",
                    Toast.LENGTH_LONG).show();
            finish();
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this,
                    "NFC NOT Enabled!",
                    Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this,
                    "Place Tag on Phone!",
                    Toast.LENGTH_LONG).show();
        }
        if (hasMifareClassic()) {
            Toast.makeText(this, "NXP enabled for mifare classic", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Device is NXP enabled for mifare classic");
        } else {
            Toast.makeText(this, "NXP is not available on this device", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "NXP is not available on this device");
        }

    }

    private void askPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.NFC)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.NFC, Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_NFC);
        }
    }

    private void findViews() {

        textViewInfo = (TextView) findViewById(R.id.tv_tag_info);
        tvRawData = (TextView) findViewById(R.id.tv_raw_data);
        utils = new Utils();

        mManager = (NfcManager) getSystemService(NFC_SERVICE);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        tvSchool = (TextView) findViewById(R.id.school);
        tvFirstName = (TextView) findViewById(R.id.first_name);
        tvLastName = (TextView) findViewById(R.id.last_name);
//        tvDOB= (TextView) findViewById(R.id.d_o_b);
        tvSex = (TextView) findViewById(R.id.sex);
        tvExamNo = (TextView) findViewById(R.id.exam_no);
        tvDistrict = (TextView) findViewById(R.id.district);
        tvSubject1 = (TextView) findViewById(R.id.subject1);
        tvSubject2 = (TextView) findViewById(R.id.subject2);
        tvSubject3 = (TextView) findViewById(R.id.subject3);
        tvSubject4 = (TextView) findViewById(R.id.subject4);
        tvSubject5 = (TextView) findViewById(R.id.subject5);
        tvSubject6 = (TextView) findViewById(R.id.subject6);
        tvSubject7 = (TextView) findViewById(R.id.subject7);
        tvSubject8 = (TextView) findViewById(R.id.subject8);
        tvSubject9 = (TextView) findViewById(R.id.subject9);
        tvRegion = (TextView) findViewById(R.id.region);

        imgAvator = (ImageView) findViewById(R.id.img_avator);

        btnAttendance=findViewById(R.id.btn_attendance);
        btnMalpractice=findViewById(R.id.btn_malpractice);

        llLogs=findViewById(R.id.ll_logs);
        llDetails=findViewById(R.id.ll_details);
    }

    //device has NXP or not
    private boolean hasMifareClassic() {
        return getPackageManager().hasSystemFeature("com.nxp.mifare");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_NFC: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    NfcManager mManager = (NfcManager) getSystemService(NFC_SERVICE);
                    nfcAdapter = NfcAdapter.getDefaultAdapter(this);

                    if (nfcAdapter == null) {
                        Toast.makeText(this,
                                "NFC NOT supported on this devices!",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else if (!nfcAdapter.isEnabled()) {
                        Toast.makeText(this,
                                "NFC NOT Enabled!",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Place Tag on Phone!",
                                Toast.LENGTH_LONG).show();
                    }

                    // PendingIntent object containing details of the scanned tag
                    mPendingIntent = PendingIntent.getActivity(
                            this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

                    try {
                        // Accept all MIME types
                        mNdef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
                        mNdef.addDataType("*/*");
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        Toast.makeText(this, String.format("MalformedMimeTypeException: %s", e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
                        e.printStackTrace(); // to logcat
                    }

                    mFilters = new IntentFilter[]{mNdef, new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};
                } else
                    Toast.makeText(this,
                            "Permission not granted :(",
                            Toast.LENGTH_SHORT).show();
            }

        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String action = intent.getAction();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Make sure we have an adapter, otherwise this fails
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        tvRawData.setText("");
        // Make sure intent is TECH DISCOVERED; ignore the rest
        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))
            return;

        // Retrieve extended data from the intent
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        Toast.makeText(this, "New Tag Found", Toast.LENGTH_SHORT).show();

//        Utils.getHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));

        new TagInfo().execute();
        new TagData().execute();

//        Thread aThread = new Thread(this);
//        aThread.start();

    }

    public String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }
        System.out.println("Decimal : " + temp.toString());

        return sb.toString().trim();
    }

    private String ByteArrayToHexString(byte[] inarray) { // converts byte
        // arrays to string
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    /**
     * Display a "Toast" message
     *
     * @param message The message of course
     */
    private void eatToast(final String message) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private class TagInfo extends AsyncTask<Void, Void, String> {

        ProgressDialog dialog;
        MifareClassic mMfc = MifareClassic.get(tag);
        Boolean connected;


        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.show();
            try {
                mMfc.connect();
                connected= true;

            } catch (IOException e) {
                e.printStackTrace();
                dialog.dismiss();
            }

        }

        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder tagInfo = new StringBuilder();

//            tagInfo += "\nTag Id: ";
            byte[] tagId = tag.getId();
//                tagInfo += "length = " + tagId.length +"\n";
            for (byte aTagId : tagId) {
                tagInfo.append(Integer.toHexString(aTagId & 0xFF)).append(" ");
            }
            tagInfo.append("\n");

            String[] techList = tag.getTechList();
//            tagInfo += "\nTech List\n";
//                tagInfo += "length = " + techList.length +"\n";
//            for (int i = 0; i < techList.length; i++) {
//                tagInfo += techList[i] + "\n ";
//            }
            return tagInfo.toString().trim();
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
//            ArrayList<UserImage> userImages = new ArrayList<>();
//            SQLiteHandler mydb = new SQLiteHandler(getApplicationContext());
//            userImages = mydb.getImages(s);
//            Glide.with(MainActivity.this)
//                    .load(userImages.get(0).imageURL)
//                    .into(imgAvator);
            textViewInfo.setText(s);
        }
    }

    private class TagData extends AsyncTask<Void, Void, String> {
        ProgressDialog progressDialog;
        MifareClassic mMfc = MifareClassic.get(tag);
        String rawData = "";
        String School,
                FirstName,
                LastName,
                ExamNo,
                District,
                Subject1,
                Subject2,
                Subject3,
                Subject4,
                Subject5,
                Subject6,
                Subject7,
                Subject8,
                Subject9,
                Region,
                sex;
        Bitmap avator;

        @Override
        protected void onPreExecute() {
            if (!mMfc.isConnected()){
                try {
                    mMfc.connect();
                } catch (IOException e) {
                    Log.e(TAG, "MFC is not connected");
                }
            }
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            // Loop through all sectors
//            for (int sector = 0; sector < (mMfc.getSectorCount()); ++sector) {
                for (int sector = 0; sector < 16; ++sector) {
                rawData += ("\n Sector " + sector + " : ");

                if (mMfc.isConnected()){
                    authenticated = autheticateSector(sector);
                }else{
                    progressDialog.dismiss();
                    cancel(true);

                }


                // Authentication to sector failed, invalid key(s)
                if (!authenticated) {
                    Log.w(TAG, "Authentication to sector failed, invalid key(s)");
                    rawData += ("Authentication Failed XXX \n");
//                    continue;
                } else {

                    readSectorData(sector);
                }
            }
            return rawData;
        }

        private void readSectorData(int sector) {
            switch (sector) {
                case 0:
                    School = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 1:
                    FirstName = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 2:
                    LastName = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 3:
                    sex = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 4:
                    ExamNo = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 5:
                    District = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 6:
                    Subject1 = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 7:
                    Subject2 = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 8:
                    Subject3 = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 9:
                    Subject4 = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 10:
                    Subject5 = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 11:
                    Subject6 = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 12:
                    Subject7 = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 13:
                    Subject8 = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 14:
                    Subject9 = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 15:
                    Region = convertHexToString(readBlockData(sector)).trim();
                    break;
                case 32:
//                    avator = readImage(sector);

            }
        }

//        private Bitmap readImage(int sector) {
//
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//            for (int block = 0; (block < mMfc.getBlockCountInSector(sector)); ++block) {
//                // Get block number for sector + block
//                int blockIndex = (mMfc.sectorToBlock(sector) + block);
//
//
//                try {
//                    if (sector == 32 && block < 15) {
//                        byte[] data = mMfc.readBlock(blockIndex);
//                        outputStream.write(data);
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//
//            return convertByteToImage(outputStream.toByteArray());
//        }

        private Bitmap convertByteToImage(byte[] data) {

            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }

        private String readBlockData(int sector) {
            String blockvalues = "";


            // Read all blocks in sector
            for (int block = 0; (block < mMfc.getBlockCountInSector(sector)); ++block) {
                // Get block number for sector + block
                int blockIndex = (mMfc.sectorToBlock(sector) + block);

                try {

                    // Create a string of bits from block data and fix endianness
                    // http://en.wikipedia.org/wiki/Endianness


                    if (sector <= 15 && block < 3) {
                        // Read block data from block index
                        byte[] data = mMfc.readBlock(blockIndex);
                        if (!(sector == 0 && block == 0)) {
                            String temp = ByteArrayToHexString(data);
                            blockvalues += temp;
                            Log.i(TAG, "Block " + blockIndex + " : " + temp);
                            rawData += ("Block " + blockIndex + " : " + temp + "\n");
                        }

                    }

//                    temp=convertHexToString(temp);


//                        for (int x = 0; x < temp.length(); x += 8)
//                            bits.append(new StringBuilder(temp.substring(x, x + 8)).reverse().toString());

//                        Log.w(TAG, "New byte string is: " + bits.toString());


                } catch (IOException e) {
                    eatToast(String.format("Exception: %s", e.getLocalizedMessage()));
                    Log.e(TAG, "Exception occurred  " + e.getLocalizedMessage());
                }
            }
            return blockvalues.trim();
        }

        private boolean autheticateSector(int sector) {
            authenticated = false;
            Log.i(TAG, "Authenticating Sector: " + sector + " It contains Blocks: " + mMfc.getBlockCountInSector(sector));


            try {
                if (mMfc.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                    authenticated = true;
                    Log.w(TAG, "Authenticated!!! ");
                    rawData += ("Authenticated!!! \n");
                    //                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!authenticated) {

                autheticateSector(sector);
            }
//                if (mMfc.authenticateSectorWithKeyB(sector,MifareClassic.KEY_DEFAULT)){
//                    authenticated = true;
//                    Log.w(TAG, "Authenticated for read and write!!!");
//                }
            return authenticated;
        }

        @Override
        protected void onPostExecute(String s) {
            s += "\n Card read successfully \n";
            tvRawData.setText(s);
            progressDialog.dismiss();
            setDataInTV();

            try {
                mMfc.close();
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) v.vibrate(100);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(MainActivity.this, "Tag was lost", Toast.LENGTH_SHORT).show();
        }

        private void setDataInTV() {
//            ExamNo="010101012";
            setImage(ExamNo);
            tvFirstName.setText(FirstName);
            tvLastName.setText(LastName);
            tvSex.setText(sex);
            tvExamNo.setText(ExamNo);

            tvSchool.setText(School);
            tvDistrict.setText(District);
            tvRegion.setText(Region);

            tvSubject1.setText(Subject1);
            tvSubject2.setText(Subject2);
            tvSubject3.setText(Subject3);
            tvSubject4.setText(Subject4);
            tvSubject5.setText(Subject5);
            tvSubject6.setText(Subject6);
            tvSubject7.setText(Subject7);
            tvSubject8.setText(Subject8);
            tvSubject9.setText(Subject9);


        }

        public void setImage(String examNo) {
            switch (examNo) {
                case "010101001":
                    imgAvator.setImageResource(R.drawable.one1);
                    break;
                case "010101002":
                    imgAvator.setImageResource(R.drawable.two2);
                    break;
                case "010101003":
                    imgAvator.setImageResource(R.drawable.three3);
                    break;
                case "010101004":
                    imgAvator.setImageResource(R.drawable.four4);
                    break;
                case "010101005":
                    imgAvator.setImageResource(R.drawable.five5);
                    break;
                case "010101012":
                    imgAvator.setImageResource(R.drawable.six6);
                    break;
                case "010101013":
                    imgAvator.setImageResource(R.drawable.seven7);
                    break;
                case "010101014":
                    imgAvator.setImageResource(R.drawable.eight8);
                    break;
                case "010101015":
                    imgAvator.setImageResource(R.drawable.nine9);
                    break;
                case "010101016":
                    imgAvator.setImageResource(R.drawable.ten10);
                    break;
                default:
                    imgAvator.setImageResource(R.drawable.dummyavatar);
                    break;
            }
        }
    }

}

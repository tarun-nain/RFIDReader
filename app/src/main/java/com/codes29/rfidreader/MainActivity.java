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
    private Button btnShowLogs, btnDetails;
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

        initialize();

        askPermissions();

        checkNFCDeviceSupport();

        wrapPendingIntent();

    }

    /**
     * initialize the NFC manager and adapter
     * find views of the widgets
     */
    private void initialize() {

        utils = new Utils();

        //instantiate manager and adapter
        mManager = (NfcManager) getSystemService(NFC_SERVICE);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        textViewInfo = (TextView) findViewById(R.id.tv_tag_info);
        tvRawData = (TextView) findViewById(R.id.tv_raw_data);

        tvSchool = (TextView) findViewById(R.id.school);
        tvFirstName = (TextView) findViewById(R.id.first_name);
        tvLastName = (TextView) findViewById(R.id.last_name);
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

        llLogs = findViewById(R.id.ll_logs);
        llDetails = findViewById(R.id.ll_details);

        btnShowLogs = findViewById(R.id.btn_showlogs);
        btnDetails = findViewById(R.id.btn_details);

        btnShowLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llLogs.setVisibility(View.VISIBLE);
                llDetails.setVisibility(View.GONE);
            }
        });

        btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llLogs.setVisibility(View.GONE);
                llDetails.setVisibility(View.VISIBLE);
            }
        });
    }


    /**
     * Ask permission to access NFC
     */
    private void askPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.NFC)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.NFC, Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_NFC);
        }
    }


    /**
     * Check whether the device supports nfc or not!
     */
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


    /**
     * Check if the device has feature to read Mifare type cards.
     *
     * @return Boolean true/false accordingly
     */
    private boolean hasMifareClassic() {
        return getPackageManager().hasSystemFeature("com.nxp.mifare");
    }


    /**
     * Create a PendingIntent object so the Android system can populate it with the details of the tag when it is scanned.
     * //Reference: https://developer.android.com/guide/topics/connectivity/nfc/advanced-nfc.html#java
     */
    private void wrapPendingIntent() {

        // PendingIntent object containing details of the scanned tag
        mPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Accept all MIME types when card is detected
        // or you may specify only the ones you need
        try {
            mNdef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            mNdef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Toast.makeText(this, String.format("MalformedMimeTypeException: %s", e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
            e.printStackTrace(); // to logcat
        }

        // specify the intent filter
        mFilters = new IntentFilter[]{mNdef, new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};
    }

    @Override
    protected void onResume() {
        super.onResume();

       /*The foreground dispatch system allows an activity to intercept an intent
       and claim priority over other activities that handle the same intent.*/
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_NFC: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

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

                } else
                    Toast.makeText(this,
                            "Permission not granted :(",
                            Toast.LENGTH_SHORT).show();
            }

        }
    }


    /**
     * card is detected by the system
     *
     * @param intent containing the card information
     */
    @Override
    protected void onNewIntent(Intent intent) {

        tvRawData.setText("");

        // Make sure intent is TECH DISCOVERED; ignore the rest
        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))
            return;

        // Retrieve extended data from the intent
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        Log.e(TAG, "onNewIntent: " + tag);

        Toast.makeText(this, "New Tag Found", Toast.LENGTH_SHORT).show();

        //get information of card
        new TagInfo().execute();

        //get the data stored in the card
        new TagData().execute();

    }


    /**
     * get the tag information
     */
    private class TagInfo extends AsyncTask<Void, Void, String> {

        ProgressDialog dialog;
        MifareClassic mMfc = MifareClassic.get(tag);
        Boolean connected;


        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.show();
            try {
                Log.e(TAG, "onPreExecute: creating connection");
                
                if (mMfc != null) {
                    mMfc.connect();  //create connection with the card
                    connected = true;
                    Log.e(TAG, "onPreExecute: connection successful");
                } else {
                    Log.e(TAG, "onPreExecute: connection failed");
                }


            } catch (Exception e) {
                e.printStackTrace();
                dialog.dismiss();
            }

        }

        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder tagInfo = new StringBuilder();

            byte[] tagId = tag.getId();

            for (byte aTagId : tagId) {
                tagInfo.append(Integer.toHexString(aTagId & 0xFF)).append(" ");
            }
            tagInfo.append("\n");

            String[] techList = tag.getTechList();

            tagInfo.append("\nTech List\n");
            tagInfo.append("length = " + techList.length + "\n");
            for (int i = 0; i < techList.length; i++) {
                tagInfo.append(techList[i] + "\n ");
            }

            return tagInfo.toString().trim();
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
            Log.i("Tag Details", s);
            textViewInfo.setText(s);
        }
    }


    /**
     * get the data stored in the card.
     * need to identify the sectors,
     * authenticate each sector with default key,
     * read the data in block of the corresponding sector
     */
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
            if (!mMfc.isConnected()) { // Check if card is still connected
                try {
                    mMfc.connect();
                } catch (Exception e) {
                    if (e instanceof IOException)
                        Log.e(TAG, "MFC is not connected");

                    if (e instanceof IllegalStateException) {
                        try {
                            mMfc.close();
                            mMfc.connect();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {

            // Loop through all sectors
            for (int sector = 0; sector < (mMfc.getSectorCount()); ++sector) {

                rawData += ("\n Sector " + sector + " : ");

                //Authenticate the sector first!
                // then only you can read the data in the block of the current sector
                if (mMfc.isConnected()) {
                    authenticated = autheticateSector(sector);
                } else {
                    progressDialog.dismiss();
                    cancel(true);
                }


                // Authentication to sector failed, invalid key(s)
                if (!authenticated) {
                    Log.w(TAG, "Authentication to sector failed, invalid key(s)");
                    rawData += ("Authentication Failed XXX \n");
                } else {
                    //Authentication successful!
                    //you can now read the data in this sector
                    readSectorData(sector);
                }
            }
            return rawData;
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

        /**
         * Authenticate the sector with default key
         * OR the key that you used to write the card
         * (if you didn't then use the default one!)
         *
         * @param sector current sector
         * @return Boolean authenticated? true:false
         */
        private boolean autheticateSector(int sector) {

            authenticated = false;
            Log.i(TAG, "Authenticating Sector: " + sector + " It contains Blocks: " + mMfc.getBlockCountInSector(sector));

            //https://developer.android.com/reference/android/nfc/tech/MifareClassic.html#authenticateSectorWithKeyA(int,%20byte[])
            try {
                if (mMfc.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                    authenticated = true;
                    Log.w(TAG, "Authenticated!!! ");
                    rawData += ("Authenticated!!! \n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!authenticated) {

                Log.e(TAG, "autheticateSector: Authenticating with key B");

                try {
                    if (mMfc.authenticateSectorWithKeyB(sector, MifareClassic.KEY_DEFAULT)) {
                        authenticated = true;
                        Log.w(TAG, "Authenticated!!! ");
                        rawData += ("Authenticated!!! \n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return authenticated;
        }

        /**
         * read the data in blocks in binary,
         * convert it to string
         *
         * @param sector current sector that is authenticated for I/O
         */
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

        /**
         * reads the binary data in blocks
         *
         * @param sector current sector
         * @return hex string of the read data
         */
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
                } catch (IOException e) {
                    Log.e(TAG, "Exception occurred  " + e.getLocalizedMessage());
                }
            }
            return blockvalues.trim();
        }

        /**
         * set the data read into the respective textviews
         */
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
    }

    /**
     * @param hex hex data of the blocks
     * @return string (readable) data of the blocks
     */
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

    /**
     * @param inarray byte stream read from block
     * @return hex string of byte array
     */
    private String ByteArrayToHexString(byte[] inarray) {
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


}

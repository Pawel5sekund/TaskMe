package com.GenialQuirk.TaskMe;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final int TX_APPEND = 1;
    static final int TX_SET = 0;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    GoogleAccountCredential mCredential;
    private final ArrayList<ArrayList<String>> listOfTasks_general = new ArrayList<ArrayList<String>>();
    private final ArrayList<ArrayList<String>> listOfTasks_wyslane = new ArrayList<ArrayList<String>>();
    private final ArrayList<ArrayList<String>> listOfTasks_odebrane = new ArrayList<ArrayList<String>>();
    private final ArrayList<ArrayList<String>> listOfSpreadsheets = new ArrayList<ArrayList<String>>();
    private final List<Switch> switchList_general = new ArrayList<Switch>();
    private final List<Switch> switchList_wyslane = new ArrayList<Switch>();
    private final List<Switch> switchList_odebrane = new ArrayList<Switch>();
    private LinearLayout listTasks;
    private LinearLayout listOdebrane;
    private LinearLayout listWyslane;
    private ScrollView scrollView_general;
    private ScrollView scrollView_odebrane;
    private ScrollView scrollView_wyslane;
    private ConstraintLayout mainLayout;
    private String accountName = "null";

     private  String spreadsheetId = "1F8YtMCIOkfJrXSg1-oJBCSqGsIj_wQbyefbA0qMFrYA";
    private String range = "myTasks!A:G";
    public ValueRange response;
    private static final int read = 1;
    private int loginGoogleFunctionReturn = 0;
    private TabLayout tabLists;
    private Button btn_addTask;

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        //String linkSpreadsheet = sharedPreferences.getString("me_spreadsheetLink", "");
        //String extractor[] = linkSpreadsheet.split("spreadsheets/d/");
        //if(extractor.length > 0) spreadsheetId = extractor[1].split("/")[0];
        listTasks = (LinearLayout) findViewById(R.id.taskList_general);
        listOdebrane = (LinearLayout) findViewById(R.id.taskList_odebrane);
        listWyslane = (LinearLayout) findViewById(R.id.taskList_wyslane);
        scrollView_general = findViewById(R.id.scrollView_general);
        scrollView_odebrane = findViewById(R.id.scrollView_odebrane);
        scrollView_wyslane = findViewById(R.id.scrollView_wyslane);
        scrollView_general.setVisibility(View.VISIBLE);
        scrollView_wyslane.setVisibility(View.INVISIBLE);
        scrollView_odebrane.setVisibility(View.INVISIBLE);
        listOdebrane.setVisibility(View.VISIBLE);
        listTasks.setVisibility(View.VISIBLE);
        listWyslane.setVisibility(View.VISIBLE);
        tabLists = findViewById(R.id.main_tabs);
        mainLayout = findViewById(R.id.mainLayout);
        tabLists.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                                                   @Override
                                                   public void onTabSelected(TabLayout.Tab tab) {
                                                       switch (tab.getPosition()){
                                                           case 0:
                                                               scrollView_general.setVisibility(View.VISIBLE);
                                                               scrollView_wyslane.setVisibility(View.INVISIBLE);
                                                               scrollView_odebrane.setVisibility(View.INVISIBLE);
                                                               Log.e(this.toString(), "Selectd: 0");
                                                               break;
                                                           case 1:
                                                               scrollView_general.setVisibility(View.INVISIBLE);
                                                               scrollView_wyslane.setVisibility(View.INVISIBLE);
                                                               scrollView_odebrane.setVisibility(View.VISIBLE);
                                                               Log.e(this.toString(), "Selectd: 1");
                                                               break;
                                                           case 2:
                                                               scrollView_general.setVisibility(View.INVISIBLE);
                                                               scrollView_wyslane.setVisibility(View.VISIBLE);
                                                               scrollView_odebrane.setVisibility(View.INVISIBLE);
                                                               Log.e(this.toString(), "Selectd: 2");
                                                               break;
                                                       }
                                                   }
                                                   @Override
                                                   public void onTabUnselected(TabLayout.Tab tab) {
                                                   }
                                                   @Override
                                                   public void onTabReselected(TabLayout.Tab tab) {
                                                   }
                                               }
        );

        btn_addTask = findViewById(R.id.buttonAdd);

        btn_addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tabLists.getSelectedTabPosition() == 0 | tabLists.getSelectedTabPosition() == 2) showDialogAddTask();
            }
        });


        mCredential = GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccountName(accountName);
        getData(range, listOfTasks_general, spreadsheetId, this::updateTaskList);
        getData("sheetsList!A:B", listOfSpreadsheets, spreadsheetId, this::getCrossData);
        Log.e(this.toString(), mCredential.getSelectedAccountName());
    }
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    public void showDialogAddTask () {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_addtask);
        TextView tytul = dialog.findViewById(R.id.editText_Tytul);
        TextView data = dialog.findViewById(R.id.editText_data);
        TextView punkty = dialog.findViewById(R.id.editText_punkty);
        TextView opis = dialog.findViewById(R.id.editText_opis);
        Time id = new Time();
        id.setToNow();
        ArrayList<ArrayList<String>> toSend = new ArrayList<ArrayList<String>>();
        toSend.add(new ArrayList<String>());
        tytul.getText();
        Button button_close;
        Button button_confirm;
        button_confirm = dialog.findViewById(R.id.button_zatwierdz);
        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long dateInMilis;
                if(isNumeric(punkty.getText().toString()) == false) {
                    dialog.dismiss();
                    Snackbar.make(mainLayout, "Punkty muszą być liczbą!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat formatterDaty = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);

                try {
                    dateInMilis = formatterDaty.parse(data.getText().toString()).getTime();
                } catch (ParseException e) {
                    dialog.dismiss();
                    Snackbar.make(mainLayout, "Zły format daty!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                toSend.get(0).add(Long.toString(id.toMillis(true)));
                toSend.get(0).add(tytul.getText().toString());
                toSend.get(0).add(opis.getText().toString());
                toSend.get(0).add(data.getText().toString());
                toSend.get(0).add(punkty.getText().toString());
                switch(tabLists.getSelectedTabPosition()) {
                    case 0:
                        toSend.get(0).add("me");
                        toSend.get(0).add("not_done");
                        setData("myTasks!A:G", toSend, spreadsheetId, 1);
                        dialog.dismiss();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                getData(range, listOfTasks_general, spreadsheetId, MainActivity.this::updateTaskList);
                            }
                        }, 5000);

                        break;
                    case 1:
                        break;
                    case 2:
                        toSend.get(0).add(mCredential.getSelectedAccountName());
                        toSend.get(0).add("not_done");
                        setData("tasksTo_"+listOfSpreadsheets.get(0).get(1), toSend, spreadsheetId, 1);

                        dialog.dismiss();

                        Handler handler2 = new Handler();
                        handler2.postDelayed(new Runnable() {
                            public void run() {
                                getData("tasksTo_"+listOfSpreadsheets.get(0).get(1)+"!A:G", listOfTasks_wyslane, spreadsheetId, MainActivity.this::updateTaskList);
                            }
                        }, 5000);
                        break;
                }
            }
        });
        button_close = dialog.findViewById(R.id.button_closeDialogAddTask);
        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        //dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        dialog.show();
        //dialog.dismiss();
    }
    public void getCrossData () {
        getData("tasksTo_"+mCredential.getSelectedAccountName()+"!A:G", listOfTasks_odebrane, listOfSpreadsheets.get(0).get(0), this::updateTaskList);
        getData("tasksTo_"+listOfSpreadsheets.get(0).get(1)+"!A:G", listOfTasks_wyslane, spreadsheetId, this::updateTaskList);
    }
    public void emptyFunction() {

    }
    public void updateTaskList() {
        createSwitches (listTasks, listOfTasks_general, switchList_general,true, spreadsheetId, "myTasks");
        if (listOfSpreadsheets.size() != 0) createSwitches(listOdebrane, listOfTasks_odebrane, switchList_odebrane, false, listOfSpreadsheets.get(0).get(0), "tasksTo_"+mCredential.getSelectedAccountName());
        if (listOfSpreadsheets.size() != 0) createSwitches(listWyslane, listOfTasks_wyslane, switchList_wyslane, true, spreadsheetId, "tasksTo_"+listOfSpreadsheets.get(0).get(1));
        }

    public void createSwitches (LinearLayout listTasks, ArrayList<ArrayList<String>> listOfTasks_general, List<Switch> switchList, boolean active, String spreadsheetId, String sheetName) {
        listTasks.removeAllViews(); //kasuje stare zadania z listy
        switchList.clear();
        for (int i = 0; i < listOfTasks_general.size(); i+=1) { //tworzenie nowej listy
            if(listOfTasks_general.get(i).size() < 7) {
                continue;
            }
            Log.e(this.toString(), "UPDATE");
            Log.e(this.toString(), listOfTasks_general.get(i).toString());
            Switch switchElement = new Switch(getApplicationContext());
            switchElement.setId(i);
            switchElement.setText(listOfTasks_general.get(i).get(1)+"@"+listOfTasks_general.get(i).get(5));
            switchElement.setChecked(listOfTasks_general.get(i).get(6).equals("done"));
            switchElement.setClickable(active);
            switchElement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        //if(listOfTasks_general.get(buttonView.getId()).get(5).equals("me")) {
                            listOfTasks_general.get(buttonView.getId()).set(6, "done");
                        //}
                    } else {
                        //if(listOfTasks_general.get(buttonView.getId()).get(5).equals("me")) {
                            listOfTasks_general.get(buttonView.getId()).set(6, "not_done");
                        //}
                    }
                    int taskPlacement = (int)buttonView.getId()+2;
                    ArrayList<ArrayList<String>> toSend = new ArrayList<>();
                    toSend.add(listOfTasks_general.get(buttonView.getId()));
                    setData(sheetName+"!A"+taskPlacement+":G"+taskPlacement, toSend, spreadsheetId, TX_SET);
                }
            });
            switchList.add(switchElement);
            listTasks.addView(switchList.get(i));

        }
    }

    public int getData(String range, ArrayList<ArrayList<String>> target, String spreadsheetId, subFunctionCaller afterRX) {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    getData(range, target, spreadsheetId, afterRX);
                }
            }, 3000);

        } else if (!isDeviceOnline()) {
            Log.e(this.toString(), "No network connection available.");

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    getData(range, target, spreadsheetId, afterRX);
                }
            }, 3000);

        } else {
            Log.e(this.toString(), "toRX");
            new MainActivity.RXData(mCredential, range, target, spreadsheetId, this, afterRX).execute();
            return 1;
        }
        return 0;
    }

    public int setData(String range, ArrayList<ArrayList<String>> target, String spreadsheetId, int operationSelector) {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    setData(range, target, spreadsheetId, operationSelector);
                }
            }, 1000);

        } else if (!isDeviceOnline()) {
            Log.e(this.toString(), "No network connection available.");

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    setData(range, target, spreadsheetId, operationSelector);
                }
            }, 1000);


        } else {
            new MainActivity.TXData(mCredential, range, target, spreadsheetId, this, operationSelector).execute();
            return 1;
        }
        return 0;
    }


    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, android.Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                //getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    android.Manifest.permission.GET_ACCOUNTS);
        }
    }


    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Log.e(this.toString(), "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.");

                } else {
                    //getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        //getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    //getResultsFromApi();
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }


    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }


    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public interface subFunctionCaller {
        void doSomething();
    }

    public interface serialGetDataCaller {
        int getData(String range, ArrayList<ArrayList<String>> target, String spreadsheetId);
    }

    private class RXData extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;

        private Exception mLastError = null;
        private Context mContext;

        private String range = "";
        ArrayList<ArrayList<String>> target;
        private String spreadsheetId = "";
        int postFunction = 0;
        subFunctionCaller afterRX;

        int error_counter = 0;

        RXData(GoogleAccountCredential credential, String range, ArrayList<ArrayList<String>> target, String spreadsheetId, Context context, subFunctionCaller afterRX) {

            mContext = context;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("GSheetJavaApiSample")
                    .build();
            this.spreadsheetId = spreadsheetId;
            this.target = target;
            this.range = range;
            this.afterRX = afterRX;
            Log.e(this.toString(), "executeRX");
            for(int i = 0; i < this.target.size(); i+=1) {
                this.target.get(i).clear();
            }
            this.target.clear();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return ReadFromSheetUsingApi();
            } catch (Exception e) {
                mLastError = e;

                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {

                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            getData.REQUEST_AUTHORIZATION);
                } else {
                    Log.e(this.toString(), "The following error occurred:\n" + mLastError.getMessage());
                }
                Log.e(this.toString(), e + "");
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
            Log.e(this.toString(), "onPost");
            this.afterRX.doSomething();
        }

        private List<String> ReadFromSheetUsingApi() throws IOException, GeneralSecurityException {
            Log.e(this.toString(), "read");
            Sheets.Spreadsheets.Values.Get request =
                    mService.spreadsheets().values().get(this.spreadsheetId, this.range);

            try {
                response = request.execute();
                Log.e(this.toString(), "From: "+this.range+" "+this.spreadsheetId);
                ArrayList lists_temp = new ArrayList<String>();
                for(int  i = 1; i < response.getValues().size(); i+=1) {
                    lists_temp.clear();
                    for(int ii = 0; ii < response.getValues().get(i).size(); ii += 1) {
                        lists_temp.add(""+response.getValues().get(i).get(ii));
                    }
                    this.target.add((ArrayList<String>)lists_temp.clone());
                    //Log.e(this.toString(), "size_adding:"+listOfTasks_general.size());

                }
            } catch (GoogleJsonResponseException e) {
                if (error_counter == 5) {
                    return null;
                }
                Log.e(this.toString(), "RX_error_counter: " + error_counter);
                error_counter++;
                ReadFromSheetUsingApi();
            }


            return null;

        }
    }

    private class TXData extends AsyncTask<Void, Void, Void> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        private Context mContext;
        private String range = "";
        ArrayList<ArrayList<String>> target;
        private String spreadsheetId = "";

        private int error_counter = 0;
        UpdateValuesResponse response = new UpdateValuesResponse();
        AppendValuesResponse append_response = new AppendValuesResponse();
        BatchUpdateSpreadsheetResponse response_sheetCreeation = new BatchUpdateSpreadsheetResponse();
        private int operationSelector = 0;

        TXData(GoogleAccountCredential credential, String range, ArrayList<ArrayList<String>> target, String spreadsheetId, Context context, int operationSelector) {

            mContext = context;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("GSheetJavaApiSample")
                    .build();
            this.spreadsheetId = spreadsheetId;
            this.target = target;
            this.range = range;
            this.operationSelector = operationSelector;
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {


            try {
                if(this.operationSelector == TX_SET) writeToSheetUsingApi();
                if(this.operationSelector == TX_APPEND) appendToSheetUsingApi();
            }
            catch (Exception e) {
                mLastError = e;

                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {

                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            appendData.REQUEST_AUTHORIZATION);
                } else {
                    Log.e(this.toString(),"The following error occurred:\n"+ mLastError.getMessage());
                }
                Log.e(this.toString(),e+"");
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }


        @Override
        protected void onCancelled() {

        }


        public void writeToSheetUsingApi() throws IOException, GeneralSecurityException {

            String valueInputOption = "USER_ENTERED";

            ValueRange requestBody = new ValueRange();

            List<Object> data1 = new ArrayList<>();
            List<List<Object>> requestBodyData = new ArrayList<>();

            for(int i = 0; i < this.target.size(); i+=1 ){
                data1.clear();
                requestBodyData.add(new ArrayList<Object>());
                for(int ii = 0; ii < this.target.get(i).size(); ii+=1){
                    requestBodyData.get(requestBodyData.size()-1).add(this.target.get(i).get(ii));
                }
            }

            requestBody.setValues(requestBodyData);

            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setAddSheet(new AddSheetRequest().setProperties(new SheetProperties()
                    .setTitle(range.split("!", 1)[0]))));

            Sheets.Spreadsheets.Values.Update request =
                    mService.spreadsheets().values().update(this.spreadsheetId, this.range, requestBody);
            request.setValueInputOption(valueInputOption);


            try {
                response = request.execute();
            } catch (GoogleJsonResponseException e) {
                if (error_counter == 5) {
                    return;
                }
                error_counter++;
                GoogleJsonError error = e.getDetails();
                if (error.getCode() == 400) {
                    BatchUpdateSpreadsheetRequest createSpreadsheet = new BatchUpdateSpreadsheetRequest().setRequests(requests);
                    Sheets.Spreadsheets.BatchUpdate request_sheet =
                            mService.spreadsheets().batchUpdate(this.spreadsheetId, createSpreadsheet);
                    response_sheetCreeation = request_sheet.execute();
                }
            }
            error_counter = 0;

            Log.e(this.toString(), target.toString());
            //Log.e(this.toString(), "il celli: "+response.getUpdatedCells());
            Log.e(this.toString(),response.toString());
            Log.e(this.toString(), "To: " + spreadsheetId + " " + range);

        }

        public void appendToSheetUsingApi() throws IOException, GeneralSecurityException {

            String valueInputOption = "USER_ENTERED";

            ValueRange requestBody = new ValueRange();

            List<Object> data1 = new ArrayList<>();
            List<List<Object>> requestBodyData = new ArrayList<>();

            for(int i = 0; i < this.target.size(); i+=1 ){
                data1.clear();
                requestBodyData.add(new ArrayList<Object>());
                for(int ii = 0; ii < this.target.get(i).size(); ii+=1){
                    requestBodyData.get(requestBodyData.size()-1).add(this.target.get(i).get(ii));
                }
            }

            requestBody.setValues(requestBodyData);

            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setAddSheet(new AddSheetRequest().setProperties(new SheetProperties()
                    .setTitle(range.split("!", 1)[0]))));

            Sheets.Spreadsheets.Values.Append request =
                    mService.spreadsheets().values().append(this.spreadsheetId, this.range, requestBody);
            request.setValueInputOption(valueInputOption);


            try {
                append_response = request.execute();
            } catch (GoogleJsonResponseException e) {
                if (error_counter == 5) {
                    return;
                }
                error_counter++;
                GoogleJsonError error = e.getDetails();
                if (error.getCode() == 400) {
                    BatchUpdateSpreadsheetRequest createSpreadsheet = new BatchUpdateSpreadsheetRequest().setRequests(requests);
                    Sheets.Spreadsheets.BatchUpdate request_sheet =
                            mService.spreadsheets().batchUpdate(this.spreadsheetId, createSpreadsheet);
                    response_sheetCreeation = request_sheet.execute();
                }
            }
            error_counter = 0;
            Log.e(this.toString(), "To: " + spreadsheetId + " " + range);


        }
    }




}

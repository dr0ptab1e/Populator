package firstproject.populator;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends Activity {
    ProgressDialog pd;
    int message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View.OnClickListener oclBtnCreate=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AsyncTask task=new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        for(int i=0;i<Integer.parseInt(((EditText) findViewById(R.id.textAmount)).getText().toString());i++){
                            if(Thread.interrupted()){
                                return -1;
                            }
                            Random r=new Random();
                            StringBuilder newNumber=new StringBuilder("+7");
                            for(int l=0;l<10;l++) {
                                newNumber.append(r.nextInt(10));
                            }
                            String newName="";
                            int fileSize=0;
                            try{
                                InputStream is=getResources().openRawResource(R.raw.names);
                                BufferedReader reader=new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.names)));
                                try {
                                    byte[] c = new byte[1024];
                                    int readChars = 0;
                                    while ((readChars = is.read(c)) != -1) {
                                        for (int p = 0; p < readChars; ++p) {
                                            if (c[p] == (byte)'\n') {
                                                ++fileSize;
                                            }
                                        }
                                    }
                                    int qwe=r.nextInt(fileSize);
                                    for(int line=0;line<qwe;line++){
                                        reader.readLine();
                                    }
                                    newName=reader.readLine();
                                } finally {
                                    is.close();
                                    reader.close();
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            addContact(newName + "_" + ((EditText)findViewById(R.id.suffix)).getText().toString(),newNumber.toString());
                            publishProgress(i+1);
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    protected void onProgressUpdate(Object[] values) {
                        super.onProgressUpdate(values);
                        pd.incrementProgressBy(1);
                        if(pd.getProgress()==pd.getMax()){
                            pd.dismiss();
                        }
                    }
                };
                task.execute();
                int amount=Integer.parseInt(((EditText) findViewById(R.id.textAmount)).getText().toString());
                if(amount>10){
                    pd=new ProgressDialog(MainActivity.this);
                    message=R.string.pdCreating;
                    pd.setTitle(message);
                    pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    pd.setMax(amount);
                    pd.setOwnerActivity(MainActivity.this);
                    pd.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            task.cancel(true);
                        }
                    });
                    pd.show();
                }
            }
        };
        findViewById(R.id.btnAdd).setOnClickListener(oclBtnCreate);

        View.OnClickListener oclBtnRemove=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AsyncTask task=new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        Uri contactUri = ContactsContract.Contacts.CONTENT_URI;
                        Cursor cur = getApplicationContext().getContentResolver().query(contactUri, null, null, null, null);
                        try {
                            if (cur.moveToFirst()) {
                                do {
                                    if(Thread.interrupted()){
                                        return -1;
                                    }
                                    if (cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)).endsWith("_"+((EditText) findViewById(R.id.suffix)).getText().toString())) {
                                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                                        pd.incrementProgressBy(getApplicationContext().getContentResolver().delete(uri, null, null));
                                        if(pd.getProgress()==pd.getMax()){
                                            pd.dismiss();
                                        }
                                    }
                                } while (cur.moveToNext());
                            }
                        } catch (Exception e) {
                            System.out.println(e.getStackTrace());
                        } finally {
                            cur.close();
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_LONG).show();
                    }
                };
                task.execute();
                int amount=countContacts(getApplicationContext(), ((EditText) findViewById(R.id.suffix)).getText().toString());
                if(amount>10){
                    pd=new ProgressDialog(MainActivity.this);
                    message=R.string.pdRemoving;
                    pd.setTitle(message);
                    pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    pd.setMax(amount);
                    pd.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            task.cancel(true);
                        }
                    });
                    pd.setOwnerActivity(MainActivity.this);
                    pd.show();
                }
            }
        };
        findViewById(R.id.btnRemove).setOnClickListener(oclBtnRemove);
        ((EditText)findViewById(R.id.suffix)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                findViewById(R.id.btnRemove).setEnabled(((EditText) findViewById(R.id.suffix)).getText().toString().length() > 0);
            }
        });
        findViewById(R.id.btnRemove).setEnabled(false);
        if(pd!=null){
            pd.show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Log.e("GOVNO", new Integer(pd.getProgress()).toString());
        if(pd!=null && pd.isShowing()){
            ProgressReceiver receiver=new ProgressReceiver(new Handler());
            MyIntentService.startActionRedrawPD(getApplicationContext(),pd.getProgress(),message,receiver);
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static int countContacts(Context ctx, String suffix) {
        Uri contactUri = ContactsContract.Contacts.CONTENT_URI;
        Cursor cur = ctx.getContentResolver().query(contactUri, null, null, null, null);
        int count=0;
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)).endsWith("_"+suffix)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        count++;
                    }
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        } finally {
            cur.close();
        }
        return count;
    }

    private void addContact(String name, String mobileNumber){
        ArrayList<ContentProviderOperation> ops = new ArrayList <ContentProviderOperation> ();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());
        //------------------------------------------------------ Mobile Number

        ops.add(ContentProviderOperation.
                newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());
        // Asking the Contact provider to create a new contact
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private class ProgressReceiver extends ResultReceiver{

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            int progress=resultData.getInt("progress");
            int message=resultData.getInt("message");
            pd.setProgress(progress);
            pd.setMessage(getString(message));
            pd.show();
        }

        public ProgressReceiver(Handler handler) {
            super(handler);
        }
    }
}

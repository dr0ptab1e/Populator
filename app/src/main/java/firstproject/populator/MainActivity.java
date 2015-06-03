package firstproject.populator;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View.OnClickListener oclBtnCreate=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<Integer.parseInt(((EditText) findViewById(R.id.textAmount)).getText().toString());i++){
                    Random r=new Random();
                    StringBuilder newNumber=new StringBuilder("+7");
                    for(int l=0;l<10;l++) {
                        newNumber.append(r.nextInt(10));
                    }
                    String newName="";
                    int fileSize=0;
                    try{
                        InputStream is=new FileInputStream(getResources().openRawResource(R.id.names));
                        BufferedReader reader=new BufferedReader(new InputStreamReader(is));
                        try {
                            byte[] c = new byte[1024];
                            int readChars = 0;
                            while ((readChars = is.read(c)) != -1) {
                                for (int p = 0; p < readChars; ++p) {
                                    if (c[i] == '\n') {
                                        ++fileSize;
                                    }
                                }
                            }
                            for(int line=0;line<r.nextInt(fileSize);line++){
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
                }
            }
        };
        findViewById(R.id.btnAdd).setOnClickListener(oclBtnCreate);

        View.OnClickListener oclBtnRemove=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContacts(getApplicationContext(), ((EditText) findViewById(R.id.suffix)).getText().toString());
            }
        };
        findViewById(R.id.btnRemove).setOnClickListener(oclBtnRemove);
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

    public static boolean deleteContacts(Context ctx, String suffix) {
        Uri contactUri = ContactsContract.Contacts.CONTENT_URI;
        Cursor cur = ctx.getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)).contains(suffix)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        ctx.getContentResolver().delete(uri, null, null);
                    }

                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        } finally {
            cur.close();
        }
        return false;
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
}

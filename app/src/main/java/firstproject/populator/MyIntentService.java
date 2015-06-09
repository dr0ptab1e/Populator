package firstproject.populator;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.ContactsContract;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class MyIntentService extends IntentService {
    private static final String ACTION_REDRAW_PD = "firstproject.populator.action.REDRAW_PD";
    private static final String ACTION_CREATE_CONTACTS = "firstproject.populator.action.CREATE_CONTACTS";
    private static final String ACTION_REMOVE_CONTACTS = "firstproject.populator.action.REMOVE_CONTACTS";

    public MyIntentService() {
        super("MyIntentService");
    }

    boolean interruptNow=false;

    public static void startActionRedrawPD(Context context, int progress, int message, ResultReceiver receiver) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_REDRAW_PD);
        intent.putExtra("receiver", receiver).putExtra("progress",progress).putExtra("message",message);
        context.startService(intent);
    }

    public static void startActionCreateContacts(Context context, int amount, String suffix, ResultReceiver receiver){
        Intent intent=new Intent(context,MyIntentService.class);
        intent.setAction(ACTION_CREATE_CONTACTS);
        intent.putExtra("receiver",receiver).putExtra("amount", amount).putExtra("suffix", suffix);
        context.startService(intent);
    }

    public static void startActionRemoveContacts(Context context, String suffix, ResultReceiver receiver){
        Intent intent=new Intent(context,MyIntentService.class);
        intent.setAction(ACTION_REMOVE_CONTACTS);
        intent.putExtra("receiver",receiver).putExtra("suffix",suffix);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REDRAW_PD.equals(action)) {
                final int progress = intent.getIntExtra("progress", 0);
                final int message = intent.getIntExtra("message", R.string.pdCreating);
                final ResultReceiver receiver=intent.getParcelableExtra("receiver");
                Bundle data=new Bundle();
                data.putInt("progress",progress);
                data.putInt("message",message);
                receiver.send(0,data);
            }
            if (ACTION_CREATE_CONTACTS.equals(action)){
                final int amount = intent.getIntExtra("amount",0);
                final String suffix = intent.getStringExtra("suffix");
                final ResultReceiver receiver=intent.getParcelableExtra("receiver");
                try{
                    InputStream is = getResources().openRawResource(R.raw.names);
                    try {
                        int fileSize = 0;
                        byte[] c = new byte[1024];
                        int readChars = 0;
                        while ((readChars = is.read(c)) != -1) {
                            for (int p = 0; p < readChars; ++p) {
                                if (c[p] == (byte) '\n') {
                                    ++fileSize;
                                }
                            }
                        }
                        for (int i = 0; i < amount; i++) {
                            if (interruptNow) {
                                return;
                            }
                            Random r = new Random();
                            StringBuilder newNumber = new StringBuilder("+7");
                            for (int l = 0; l < 10; l++) {
                                newNumber.append(r.nextInt(10));
                            }
                            String newName = "";
                            BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.names)));
                            try {
                                int qwe = r.nextInt(fileSize);
                                for (int line = 0; line < qwe; line++) {
                                    reader.readLine();
                                }
                                newName = reader.readLine();
                            } finally {
                                reader.close();
                            }
                            addContact(newName + "_" + suffix, newNumber.toString());
                            Bundle data=new Bundle();
                            data.putInt("progress", i + 1);
                            receiver.send(1,data);
                        }
                    }
                    finally{
                        is.close();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(ACTION_REMOVE_CONTACTS.equals(action)){
                final String suffix=intent.getStringExtra("suffix");
                final ResultReceiver receiver=intent.getParcelableExtra("receiver");
                int progress=0;
                Uri contactUri = ContactsContract.Contacts.CONTENT_URI;
                Cursor cur = getApplicationContext().getContentResolver().query(contactUri, null, null, null, null);
                try {
                    if (cur.moveToFirst()) {
                        do {
                            if(interruptNow){
                                return;
                            }
                            if (cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)).endsWith("_"+suffix)) {
                                String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                                progress+=getApplicationContext().getContentResolver().delete(uri, null, null);
                                Bundle data=new Bundle();
                                data.putInt("progress",progress);
                                receiver.send(1,data);
                            }
                        } while (cur.moveToNext());
                    }
                } catch (Exception e) {
                    System.out.println(e.getStackTrace());
                } finally {
                    cur.close();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        interruptNow=true;
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

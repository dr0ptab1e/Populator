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
        pd=new ProgressDialog(MainActivity.this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        View.OnClickListener oclBtnCreate=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int amount=Integer.parseInt(((EditText) findViewById(R.id.textAmount)).getText().toString());
                String suffix=((EditText)findViewById(R.id.suffix)).getText().toString();
                MyIntentService.startActionCreateContacts(getApplicationContext(), amount, suffix, new ProgressReceiver(new Handler()));
                if(amount>10){
                    message=R.string.pdCreating;
                    pd.setTitle(message);
                    pd.setProgress(0);
                    pd.setMax(amount);
                    pd.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stopService(new Intent(MainActivity.this, MyIntentService.class));
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
                String suffix=((EditText) findViewById(R.id.suffix)).getText().toString();
                MyIntentService.startActionRemoveContacts(getApplicationContext(), suffix, new ProgressReceiver(new Handler()));
                int amount=countContacts(getApplicationContext(), suffix);
                if(amount>10){
                    message=R.string.pdRemoving;
                    pd.setTitle(message);
                    pd.setProgress(0);
                    pd.setMax(amount);
                    pd.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stopService(new Intent(MainActivity.this, MyIntentService.class));
                        }
                    });
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
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(pd!=null && pd.isShowing()){
            ProgressReceiver receiver=new ProgressReceiver(new Handler());
            MyIntentService.startActionRedrawPD(getApplicationContext(), pd.getProgress(), message, receiver);
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

    private static int countContacts(Context ctx, String suffix) {
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

    private class ProgressReceiver extends ResultReceiver{

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            int progress=resultData.getInt("progress");
            pd.setProgress(progress);
            if(pd.getMax()==progress){
                pd.dismiss();
                Toast.makeText(getApplicationContext(),R.string.done,Toast.LENGTH_SHORT).show();
                return;
            }
        }

        public ProgressReceiver(Handler handler) {
            super(handler);
        }
    }
}

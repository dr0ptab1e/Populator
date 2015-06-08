package firstproject.populator;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;

public class MyIntentService extends IntentService {
    private static final String ACTION_REDRAW_PD = "firstproject.populator.action.REDRAW_PD";

    public static void startActionRedrawPD(Context context, int progress, int message, ResultReceiver receiver) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_REDRAW_PD);
        intent.putExtra("receiver", receiver).putExtra("progress",progress).putExtra("message",message);
        context.startService(intent);
    }

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REDRAW_PD.equals(action)) {
                final int progress = intent.getIntExtra("progress",0);
                final int message = intent.getIntExtra("message",R.string.pdCreating);
                final ResultReceiver receiver=intent.getParcelableExtra("receiver");
                Bundle data=new Bundle();
                data.putInt("progress",progress);
                data.putInt("message",message);
                receiver.send(0,data);
            }
        }
    }
}

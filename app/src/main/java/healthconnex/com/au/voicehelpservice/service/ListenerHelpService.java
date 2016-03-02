package healthconnex.com.au.voicehelpservice.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

import healthconnex.com.au.voicehelpservice.R;

/**
 * Created by FRincon on 2/03/16.
 */
public class ListenerHelpService extends Service {

    private static final String TAG = "MainActivity";

    private SpeechRecognizer sr;
    TTSManager ttsManager = null;

    private final  static int DELAY_CLOSE = 5000;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ttsManager = new TTSManager();
        ttsManager.init(this);

        initProcess();

        return Service.START_STICKY; //Start every time
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //Function to start the process
    public void initProcess() {
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());
        sr.startListening(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));
    }

    //Function to display the notification
    protected void displayNotification(String notificationMessage, String toSpeechText) {

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Help alert")
                        .setContentText(notificationMessage);
        int NOTIFICATION_ID = 12345;

        Intent targetIntent = new Intent(this, ListenerHelpService.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());

        //Push the text
        ttsManager.initQueue(toSpeechText);

    }


    class listener implements RecognitionListener
    {
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndofSpeech");
        }
        public void onError(int error)
        {
            Log.d(TAG, "error " + error);
            stopSelf();
        }
        public void onResults(Bundle results)
        {
            String str = new String();
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++)
            {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i);
            }
            processInput(str);
            //mText.setText("results: "+String.valueOf(data.size()));
        }
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

    //Function to process Input
    public void processInput(String textReceived) {

        if (textReceived.equals("help help help")) {
            displayNotification("Please help me", "Please help me");
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        }, DELAY_CLOSE);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        ttsManager.shutDown();
        sr.stopListening();
        sr.destroy();

        stopService(new Intent(this, ListenerHelpService.class));
        startService(new Intent(this, ListenerHelpService.class));
    }
}

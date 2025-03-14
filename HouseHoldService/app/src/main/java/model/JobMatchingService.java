package model;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class JobMatchingService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("JobMatchingService", "Service Started");

        new WorkerListener();
        new JobListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Keep service running
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


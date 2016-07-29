package sonorita;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class SonoritaService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("Sonorita", "Sonorita started");
        Toast.makeText(this, "Sonorita started", Toast.LENGTH_LONG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

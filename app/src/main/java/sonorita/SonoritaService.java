package sonorita;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;
import org.tomahawk.libtomahawk.resolver.Query;
import org.tomahawk.tomahawk_android.services.PlaybackService;
import org.tomahawk.tomahawk_android.utils.PlaybackManager;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class SonoritaService extends Service {
    private final String TAG = "SONORITA";
    private final String UUIDString = "be7b9a08-a0a0-4131-a60a-445af04e7f7e";

    private BluetoothSocket socket;

    private EventSender eventListener;

    private MediaBrowserCompat mediaBrowser;

    private final MediaBrowserCompat.ConnectionCallback connectionInit =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "MediaBrowser connected");
                    try {
                        final MediaControllerCompat mediaController = new MediaControllerCompat(
                                SonoritaService.this, mediaBrowser.getSessionToken());

                        Dictionary<Query, String> trackIDs = new Hashtable<>();

                        eventListener = new EventSender(trackIDs);
                        eventListener.onPlaybackStateChanged(mediaController.getPlaybackState());

                        /*ContentHeaderFragment.MediaControllerConnectedEvent event
                                = new ContentHeaderFragment.MediaControllerConnectedEvent();
                        EventBus.getDefault().post(event);*/

                        final String playbackManagerId = mediaController.getExtras()
                                .getString(PlaybackService.EXTRAS_KEY_PLAYBACKMANAGER);
                        final PlaybackManager playbackManager = PlaybackManager.getByKey(playbackManagerId);
                        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                        mediaController.registerCallback(eventListener);
                        eventListener.talk(socket);
                        new RequestReceiver(playbackManager, mediaController, audioManager, trackIDs).listen(socket);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Could not connect media controller: ", e);
                    }
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Sonorita started");

       /*try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            socket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                    "Sonorita", UUID.fromString(UUIDString)).accept();

            Log.d(TAG, "Bluetooth connection established");
        }
        catch (IOException ex) { throw new RuntimeException(ex); }*/


        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, PlaybackService.class), connectionInit, null);
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

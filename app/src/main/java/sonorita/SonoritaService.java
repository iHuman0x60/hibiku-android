package sonorita;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import org.tomahawk.libtomahawk.collection.StationPlaylist;
import org.tomahawk.libtomahawk.resolver.Query;
import org.tomahawk.tomahawk_android.activities.TomahawkMainActivity;
import org.tomahawk.tomahawk_android.fragments.ContentHeaderFragment;
import org.tomahawk.tomahawk_android.services.PlaybackService;
import org.tomahawk.tomahawk_android.utils.PlaybackManager;

import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class SonoritaService extends Service {

    private final String TAG = "SONORITA";

    public static Query transform(Blprotocol.Request.Track track) {
        return Query.get(track.title, track.album, track.artist, false);
    }

    public static Blprotocol.Event.Metadata transform() {

    }

    private void handleRequest(Blprotocol.Request request) {
        if (request.type == Blprotocol.Request.VOLUME_UP) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
        }

        if (request.type == Blprotocol.Request.VOLUME_DOWN) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
        }

        if (request.type == Blprotocol.Request.PAUSE) {
            mediaController.getTransportControls().pause();
        }

        if (request.type == Blprotocol.Request.PLAY) {
            mediaController.getTransportControls().play();
        }

        if (request.type == Blprotocol.Request.CHANGE_PLAYLIST) {
            while (playbackManager.hasNextEntry()) {
                playbackManager.deleteFromQueue(playbackManager.getNextEntry());
            }

            for (Blprotocol.Request.Track track : request.playlist) {
                playbackManager.addToQueue(transform(track));
            }
        }
    }

    private void doBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private PlaybackManager playbackManager;
    private MediaControllerCompat mediaController;
    private AudioManager audioManager;

    private final MediaControllerCompat.Callback eventListener = new EventListener();

    private MediaBrowserCompat mediaBrowser;

    private final MediaBrowserCompat.ConnectionCallback connectionInit =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "MediaBrowser connected");
                    try {
                        mediaController = new MediaControllerCompat(
                                SonoritaService.this, mediaBrowser.getSessionToken());
                        mediaController.registerCallback(eventListener);
                        eventListener.onPlaybackStateChanged(mediaController.getPlaybackState());

                        /*ContentHeaderFragment.MediaControllerConnectedEvent event
                                = new ContentHeaderFragment.MediaControllerConnectedEvent();
                        EventBus.getDefault().post(event);*/

                        String playbackManagerId = mediaController.getExtras()
                                .getString(PlaybackService.EXTRAS_KEY_PLAYBACKMANAGER);
                        playbackManager = PlaybackManager.getByKey(playbackManagerId);
                        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Could not connect media controller: ", e);
                    }
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Sonorita started");

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

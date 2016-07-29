package sonorita;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import org.tomahawk.libtomahawk.collection.StationPlaylist;
import org.tomahawk.libtomahawk.resolver.Query;
import org.tomahawk.libtomahawk.utils.StringUtils;
import org.tomahawk.tomahawk_android.services.PlaybackService;
import org.tomahawk.tomahawk_android.utils.PlaybackManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.List;

/**
 * Created by Vadim Liventsev on 29.07.2016.
 * <p>
 * All rights reserved
 */
public class EventSender extends MediaControllerCompat.Callback {

    private final Dictionary<Query, String> trackIDs;

    public EventSender(final Dictionary<Query, String> trackIDs) {
        this.trackIDs = trackIDs;
    }

    private static final String TAG = "Sonorita-EventSender";

    private OutputStream output;

    public void talk(BluetoothSocket socket) {
        try {
            this.output = socket.getOutputStream();
        }
        catch (IOException ex) { throw new RuntimeException(ex); }
    }

    private static Blprotocol.Event.Metadata transform() {
        throw new UnsupportedOperationException();
    }

    private void send(Blprotocol.Event event) {
        try {
            output.write(Blprotocol.Event.toByteArray(event));
        }
        catch (IOException ex) { throw new RuntimeException(ex); }
    }

    @Override
    public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackstate changed" + state);

        final Blprotocol.Event event = new Blprotocol.Event();

        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) event.type = Blprotocol.Event.UNPAUSED;
        else if (state.getState() == PlaybackStateCompat.STATE_PAUSED) event.type = Blprotocol.Event.PAUSED;
        else return;

        send(event);
    }

    /*@Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata != null) {
            Log.d(TAG, "onMetadataChanged changed" + metadata);
            final Blprotocol.Event event = new Blprotocol.Event();
            event.type = Blprotocol.Event.CHANGED;
        }
    }

    @Override
    public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
    }*/
}

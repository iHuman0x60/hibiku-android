package sonorita;

import android.bluetooth.BluetoothSocket;
import android.media.AudioManager;
import android.support.v4.media.session.MediaControllerCompat;
import org.tomahawk.libtomahawk.resolver.Query;
import org.tomahawk.tomahawk_android.utils.PlaybackManager;
import org.tomahawk.tomahawk_android.utils.ThreadManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;

public class RequestReceiver {
    private final PlaybackManager playbackManager;
    private final MediaControllerCompat mediaController;
    private final AudioManager audioManager;

    private final Dictionary<Query, String> trackIDs;

    public RequestReceiver(final PlaybackManager playbackManager, final MediaControllerCompat mediaController,
                final AudioManager audioManager, final Dictionary<Query, String> trackIDs) {
        this.playbackManager = playbackManager;
        this.mediaController = mediaController;
        this.audioManager = audioManager;
        this.trackIDs = trackIDs;
    }

    public void listen(final BluetoothSocket socket) {
        try {
            final InputStream inp = socket.getInputStream();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] buffer = new byte[1024];
                        inp.read(buffer);
                        handleRequest(Blprotocol.Request.parseFrom(buffer));
                    }
                    catch(IOException ex) { throw new RuntimeException(ex); }
                }
            }).start();
        }
        catch(IOException ex) { throw new RuntimeException(ex); }
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

    private Query transform(Blprotocol.Request.Track track) {
        final Query q = Query.get(track.title, track.album, track.artist, false);
        trackIDs.put(q, track.id);
        return q;
    }
}

package sonorita;

import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import org.tomahawk.libtomahawk.collection.StationPlaylist;
import org.tomahawk.tomahawk_android.services.PlaybackService;
import org.tomahawk.tomahawk_android.utils.PlaybackManager;

import java.util.List;

/**
 * Created by Vadim Liventsev on 29.07.2016.
 * <p>
 * All rights reserved
 */
public class EventListener extends MediaControllerCompat.Callback {
    @Override
    public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackstate changed" + state);
        mPlaybackState = state.getState();
        mPlaybackPanel.updatePlaybackState(state);
        if (getSupportMediaController() != null) {
            String playbackManagerId = getSupportMediaController().getExtras().getString(
                    PlaybackService.EXTRAS_KEY_PLAYBACKMANAGER);
            PlaybackManager playbackManager = PlaybackManager.getByKey(playbackManagerId);
            if (playbackManager != null && (playbackManager.getCurrentEntry() != null
                    || playbackManager.getPlaylist() instanceof StationPlaylist)) {
                showPanel();
            } else {
                hidePanel();
            }
        } else {
            hidePanel();
        }
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata != null) {
            Log.d(TAG, "onMetadataChanged changed" + metadata);
            mPlaybackPanel.updateMetadata(metadata);
        }
    }

    @Override
    public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
    }
}

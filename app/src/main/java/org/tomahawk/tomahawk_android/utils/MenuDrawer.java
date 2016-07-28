/* == This file is part of Tomahawk Player - <http://tomahawk-player.org> ===
 *
 *   Copyright 2016, Enno Gottschalk <mrmaffen@googlemail.com>
 *
 *   Tomahawk is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Tomahawk is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Tomahawk. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tomahawk.tomahawk_android.utils;

import org.jdeferred.DoneCallback;
import org.tomahawk.libtomahawk.authentication.AuthenticatorManager;
import org.tomahawk.libtomahawk.authentication.HatchetAuthenticatorUtils;
import org.tomahawk.libtomahawk.collection.Collection;
import org.tomahawk.libtomahawk.collection.CollectionManager;
import org.tomahawk.libtomahawk.collection.ScriptResolverCollection;
import org.tomahawk.libtomahawk.infosystem.User;
import org.tomahawk.tomahawk_android.R;
import org.tomahawk.tomahawk_android.TomahawkApp;
import org.tomahawk.tomahawk_android.activities.TomahawkMainActivity;
import org.tomahawk.tomahawk_android.adapters.TomahawkMenuAdapter;
import org.tomahawk.tomahawk_android.listeners.MenuDrawerListener;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class MenuDrawer extends DrawerLayout {

    private final static String TAG = MenuDrawer.class.getSimpleName();

    public static final String HUB_ID_USERPAGE = "userpage";

    public static final String HUB_ID_FEED = "feed";

    public static final String HUB_ID_CHARTS = "charts";

    public static final String HUB_ID_COLLECTION = "collection";

    public static final String HUB_ID_LOVEDTRACKS = "lovedtracks";

    public static final String HUB_ID_PLAYLISTS = "playlists";

    public static final String HUB_ID_STATIONS = "stations";

    public static final String HUB_ID_SETTINGS = "settings";

    public StickyListHeadersListView mDrawerList;

    public MenuDrawer(Context context) {
        super(context);
    }

    public MenuDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static void updateDrawer(MenuDrawer menuDrawer, TomahawkMainActivity activity) {
        if (menuDrawer != null) {
            Log.d(TAG, "updateDrawer - App is NOT running on a large landscape device");
            menuDrawer.mDrawerList =
                    (StickyListHeadersListView) menuDrawer.findViewById(R.id.left_drawer);
            updateDrawer(menuDrawer.mDrawerList, menuDrawer, activity);
        } else {
            Log.d(TAG, "updateDrawer - App is running on a large landscape device");
            StickyListHeadersListView drawerList =
                    (StickyListHeadersListView) activity.findViewById(R.id.left_drawer);
            updateDrawer(drawerList, null, activity);
        }
    }

    private static void updateDrawer(final StickyListHeadersListView drawerList,
            final MenuDrawer menuDrawer, final TomahawkMainActivity activity) {
        User.getSelf().done(new DoneCallback<User>() {
            @Override
            public void onDone(User user) {
                HatchetAuthenticatorUtils authenticatorUtils
                        = (HatchetAuthenticatorUtils) AuthenticatorManager.get()
                        .getAuthenticatorUtils(TomahawkApp.PLUGINNAME_HATCHET);
                // Set up the TomahawkMenuAdapter. Give it its set of menu item texts and icons to display
                final ArrayList<TomahawkMenuAdapter.ResourceHolder> holders = new ArrayList<>();
                TomahawkMenuAdapter.ResourceHolder holder
                        = new TomahawkMenuAdapter.ResourceHolder();
                Resources resources = activity.getResources();

                holder = new TomahawkMenuAdapter.ResourceHolder();
                holder.id = HUB_ID_SETTINGS;
                holder.title = resources.getString(R.string.drawer_title_settings);
                holder.iconResId = R.drawable.ic_action_settings;
                holders.add(holder);
                for (Collection collection : CollectionManager.get().getCollections()) {
                    if (collection instanceof ScriptResolverCollection) {
                        ScriptResolverCollection resolverCollection
                                = (ScriptResolverCollection) collection;
                        holder = new TomahawkMenuAdapter.ResourceHolder();
                        holder.collection = resolverCollection;
                        holder.isLoading = !resolverCollection.isInitialized();
                        holders.add(holder);
                    }
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (drawerList.getAdapter() == null) {
                            drawerList.setAdapter(new TomahawkMenuAdapter(holders));
                        } else {
                            ((TomahawkMenuAdapter) drawerList.getAdapter())
                                    .setResourceHolders(holders);
                        }
                    }
                });

                drawerList.setOnItemClickListener(
                        new MenuDrawerListener(activity, drawerList, menuDrawer));
            }
        });
    }

    public void closeDrawer() {
        closeDrawer(mDrawerList);
    }
}

package com.yoursway.projectsync.folders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.yoursway.projectsync.core.MonitoredFolder;
import com.yoursway.projectsync.core.internal.Activator;

public class FolderList {
    
    private static final String KEY = "monitoredFolders";
    
    public FolderList() {
    }
    
    public Collection<MonitoredFolder> get() {
        return internalGet();
    }

    @SuppressWarnings("unchecked")
    private Collection<MonitoredFolder> internalGet() {
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(readBytes());
            ObjectInputStream oin = new ObjectInputStream(bin);
            return (Collection<MonitoredFolder>) oin.readObject();
        } catch (IOException e) {
            return Collections.emptyList();
        } catch (ClassNotFoundException e) {
            return Collections.emptyList();
        }
    }
    
    public void remove(MonitoredFolder folderToRemove) {
        if (folderToRemove == null)
            throw new NullPointerException();
        Collection<MonitoredFolder> folders = new ArrayList<MonitoredFolder>(internalGet());
        folders.remove(folderToRemove);
        set(folders);
    }
    
    public void add(MonitoredFolder folderToAdd) {
        if (folderToAdd == null)
            throw new NullPointerException();
        Collection<MonitoredFolder> folders = new ArrayList<MonitoredFolder>(internalGet());
        folders.add(folderToAdd);
        set(folders);
    }
    
    public void set(Collection<MonitoredFolder> folders) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(new ArrayList<MonitoredFolder>(folders));
            oos.close();
            byte[] bytes = bos.toByteArray();
            writeBytes(bytes);
        } catch (IOException e) {
        }
    }
    
    private byte[] readBytes() {
        IPreferencesService service = Platform.getPreferencesService();
        Preferences node = service.getRootNode().node(InstanceScope.SCOPE).node(Activator.PLUGIN_ID);
        return node.getByteArray(KEY, new byte[0]);
    }
    
    public void writeBytes(byte[] bytes) {
        IPreferencesService service = Platform.getPreferencesService();
        Preferences node = service.getRootNode().node(InstanceScope.SCOPE).node(Activator.PLUGIN_ID);
        node.putByteArray(KEY, bytes);
        try {
            node.flush();
        } catch (BackingStoreException e) {
        }
    }
    
}

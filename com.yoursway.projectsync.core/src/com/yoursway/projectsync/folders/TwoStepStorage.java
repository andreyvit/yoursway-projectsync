package com.yoursway.projectsync.folders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class TwoStepStorage<T> {
    
    public static class NonOperationalException extends Exception {
        
        private static final long serialVersionUID = 1L;
        
        public NonOperationalException(String message) {
            super(message);
        }
        
        public NonOperationalException(Exception cause) {
            super(cause);
        }
        
    }
    
    private final File primaryStorage;
    private final File backupStorage;
    private final Class<T> klass;
    
    public TwoStepStorage(File primaryStorage, File backupStorage, Class<T> klass) {
        this.primaryStorage = primaryStorage;
        this.backupStorage = backupStorage;
        this.klass = klass;
    }
    
    public T load() throws NonOperationalException {
        fixup();
        try {
            FileInputStream in = new FileInputStream(primaryStorage);
            try {
                ObjectInputStream oin = new ObjectInputStream(in);
                Object obj = oin.readObject();
                return castToT(obj);
            } finally {
                in.close();
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (ClassNotFoundException e) {
            throw new NonOperationalException(e);
        } catch (IOException e) {
            throw new NonOperationalException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private T castToT(Object obj) {
        if (!klass.isInstance(obj))
            throw new ClassCastException(obj.getClass().getName());
        return (T) obj;
    }
    
    public void destroy() throws NonOperationalException {
        deletePrimary();
        deleteBackup();
    }
    
    private void fixup() throws NonOperationalException {
        if (backupStorage.exists()) {
            deletePrimary();
            renameBackupToPrimary();
        }
    }
    
    private void renameBackupToPrimary() throws NonOperationalException {
        if (!backupStorage.renameTo(primaryStorage))
            throw new NonOperationalException("Cannot rename " + backupStorage + " into " + primaryStorage);
    }
    
    private void deletePrimary() throws NonOperationalException {
        primaryStorage.delete();
        if (primaryStorage.exists())
            throw new NonOperationalException("Cannot delete " + primaryStorage);
    }
    
    private void deleteBackup() throws NonOperationalException {
        backupStorage.delete();
        if (backupStorage.exists())
            throw new NonOperationalException("Cannot delete " + backupStorage);
    }
    
}

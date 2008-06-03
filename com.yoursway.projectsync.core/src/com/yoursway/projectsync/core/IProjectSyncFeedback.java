package com.yoursway.projectsync.core;

import java.util.Collection;

public interface IProjectSyncFeedback {
    
    void finished(Collection<String> warnings);
    
}

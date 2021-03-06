package com.nicloud.workflowclient.data.data.observer;

/**
 * @author Danny Lin
 * @since 2015/10/6.
 */
public interface DataSubject {
    void registerDataObserver(DataObserver o);
    void removeDataObserver(DataObserver o);
    void notifyDataObservers();
}

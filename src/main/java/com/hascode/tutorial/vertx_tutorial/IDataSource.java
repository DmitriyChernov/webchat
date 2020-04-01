package com.hascode.tutorial.vertx_tutorial;

public interface IDataSource {
    public void putUser(String userName);
    public void putChat(String userName);
    public void getHistory(String userName);
}

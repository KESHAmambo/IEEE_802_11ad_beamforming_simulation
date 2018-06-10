package com.arkady.model;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by abara on 10.06.2018.
 */
public class CollisionPretender {
    public volatile AtomicBoolean checked = new AtomicBoolean(false);
    public final Connection connection;

    public CollisionPretender(Connection connection) {
        this.connection = connection;
    }
}

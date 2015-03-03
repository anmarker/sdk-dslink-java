package org.dsa.iot.dslink.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.dsa.iot.core.event.Event;
import org.dsa.iot.dslink.connection.Client;

/**
 * Posted when the DSLink connects to a web server.
 * @author Samuel Grenier
 */
@Getter
@AllArgsConstructor
public class ConnectedToServerEvent extends Event {
    
    @NonNull
    private final Client client;
}
package Communication;

public enum MessageType {
    PREPARE, // phase 1: prepare
    PREPARE_OK, // phase 1: promise
    ACCEPT, // phase 2: accept
    ACCEPT_OK,  // phase 2: accepted
    ACCEPT_REJECT, // phase 2: accepted
    DECIDE // phase 3: decide
}

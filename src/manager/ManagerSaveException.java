package manager;

import java.io.IOException;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String message, Throwable e){
        super(message, e);
    }
}

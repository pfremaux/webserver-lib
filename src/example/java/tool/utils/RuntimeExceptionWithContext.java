package tool.utils;

public class RuntimeExceptionWithContext extends RuntimeException{

    private final String context;

    public RuntimeExceptionWithContext(String message, String context, Throwable cause) {
        super(message, cause);
        this.context = context;
    }

    public RuntimeExceptionWithContext(Throwable cause, String context) {
        super(cause);
        this.context = context;
    }

    public RuntimeExceptionWithContext(String context) {
        super();
        this.context = context;
    }

    public String getContext() {
        return context;
    }
}

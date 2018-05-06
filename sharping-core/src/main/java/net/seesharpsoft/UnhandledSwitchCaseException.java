package net.seesharpsoft;

public class UnhandledSwitchCaseException extends RuntimeException {

    public UnhandledSwitchCaseException(Object switchCase) {
        super(switchCase == null ? "<NULL>" : switchCase.toString());
    }
}

package debugger;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public class Tester {

    private static final int debugPort = 8888;
    private static final int lineNumber = 20;
    private static final String className = "debuggee.Target";
    //private static final String[] varName = {"nextInt", "idade", "i"};

    public static void main(String[] args) throws Exception {
        AttachingController controller = new AttachingController(debugPort).attach();
        Location breakpoint = controller.getLocation(className, lineNumber);
        controller.getBreakpointRequest(breakpoint);

        EventQueue queue = controller.getEventQueue();

        while (true) {
            EventSet eventSet = queue.remove();

            try {
                for (EventIterator it = eventSet.eventIterator(); it.hasNext();) {
                    Event event = it.next();
                    if (event instanceof VMDeathEvent) {
                        break;
                    }

                    EventRequest eventReq = event.request();
                    if (!(eventReq instanceof BreakpointRequest)) {
                        continue;
                    }
                    BreakpointRequest bpReq = (BreakpointRequest) eventReq;
                    if (bpReq.location().lineNumber() != lineNumber) {
                        continue;
                    }

                    BreakpointEvent bpEvent = (BreakpointEvent) event;
                    ThreadReference threadRef = bpEvent.thread();
                    StackFrame frame = threadRef.frame(0);
                    for (LocalVariable var : frame.visibleVariables()) {
                        System.out.println(frame.getValue(var));
                    }

                }
                break;
            } finally {

                eventSet.resume();
            }
        }

    }
}

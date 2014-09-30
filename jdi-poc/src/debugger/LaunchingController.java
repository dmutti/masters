package debugger;

import java.util.*;
import org.apache.commons.lang3.*;
import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public class LaunchingController {

    private VirtualMachine vm;
    private final LaunchingConnector connector;
    private final VirtualMachineManager vmManager;
    private final Map<String, Argument> arguments;
    private final Set<Thread> streams = new HashSet<Thread>();
    private Process process;
    private Set<String> classLoadingEvents = new HashSet<String>();
    private static volatile boolean connected = true;

    public LaunchingController(String mainClassWithArguments, String... classpath) {
        vmManager = Bootstrap.virtualMachineManager();
        connector = getSocketConnector();
        arguments = connector.defaultArguments();

        arguments.get("options").setValue("-classpath " + StringUtils.join(classpath, System.getProperty("path.separator")));
        arguments.get("main").setValue(mainClassWithArguments);
        arguments.get("suspend").setValue("true");
        setUp();
    }

    private void setUp() {
        try {
            vm = connector.launch(arguments);
            process = vm.process();

            streams.add(new StreamRedirecter("error reader", process.getErrorStream(), System.err));
            streams.add(new StreamRedirecter("output reader", process.getInputStream(), System.out));

            startReadingStreams();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public LaunchingController start() {
        try {
            vm.resume();
            waitForReadingStreams();
            return this;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LaunchingConnector getSocketConnector() {
        for (Connector connector : vmManager.allConnectors()) {
            if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
                return (LaunchingConnector) connector;
            }
        }
        return null;
    }

    private void startReadingStreams() {
        for (Thread t : streams) {
            t.start();
        }
    }

    private void waitForReadingStreams() throws InterruptedException {
        for (Thread t : streams) {
            t.join();
        }
    }

    public Location getLocation(String className, int lineNumber) throws AbsentInformationException {
        for (ReferenceType refType : vm.classesByName(className)) {
            for (Location loc : refType.allLineLocations()) {
                if (loc.lineNumber() == lineNumber) {
                    return loc;
                }
            }
        }
        return null;
    }

    public void createClassLoadingEventFor(String className) {
        if (classLoadingEvents.contains(className)) {
            return;
        }
        if (vm.classesByName(className).isEmpty()) {
            ClassPrepareRequest cpr = vm.eventRequestManager().createClassPrepareRequest();
            cpr.addClassFilter(className);
            cpr.setEnabled(true);
        }
    }

    public BreakpointRequest getBreakpointRequest(Location arg) {
        EventRequestManager evtMgr = vm.eventRequestManager();
        BreakpointRequest req = evtMgr.createBreakpointRequest(arg);
        req.setSuspendPolicy(BreakpointRequest.SUSPEND_ALL);
        req.enable();
        return req;
    }

    public EventQueue getEventQueue() {
        return vm.eventQueue();
    }

    public static void main(String[] args) throws Throwable {
        int lineNumber = 20;
        String className = "debuggee.Target";
        LaunchingController controller = new LaunchingController("debuggee.Target 10 Danilo 30", "D:/Devel/svn/jdi-poc/src");
        controller.createClassLoadingEventFor(className);

        EventQueue queue = controller.getEventQueue();

        while (connected) {
            EventSet eventSet = queue.remove();

            try {
                for (EventIterator it = eventSet.eventIterator(); it.hasNext();) {
                    Event event = it.next();
                    if (event instanceof VMDeathEvent) {
                        break;
                    }

                    if (event instanceof VMDisconnectEvent) {
                        connected = false;
                        break;
                    }

                    EventRequest eventReq = event.request();
                    if (eventReq instanceof ClassPrepareRequest) {
                        controller.getBreakpointRequest(controller.getLocation(className, lineNumber));
                    }

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
                        System.out.println(var.name() + " " + frame.getValue(var));
                    }

                }
            } finally {

                eventSet.resume();
            }
        }
    }
}

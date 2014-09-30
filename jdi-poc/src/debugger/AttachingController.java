package debugger;

import java.util.*;
import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.Connector.IntegerArgument;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public class AttachingController {

    private final int debugPort;
    private VirtualMachine vm;

    public AttachingController(int debugPort) {
        this.debugPort = debugPort;
    }

    public AttachingController attach() {
        if (null != vm) {
            return this;
        }

        try {
            VirtualMachineManager vmMgr = Bootstrap.virtualMachineManager();

            AttachingConnector socketConnector = getSocketConnector(vmMgr);
            Map<String, Argument> params = socketConnector.defaultArguments();
            IntegerArgument portArg = (IntegerArgument) params.get("port");
            portArg.setValue(debugPort);
            vm = socketConnector.attach(params);

            return this;

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private AttachingConnector getSocketConnector(VirtualMachineManager vmMgr) {
        for (AttachingConnector connector : vmMgr.attachingConnectors()) {
            if (connector.transport().name().equals("dt_socket")) {
                return connector;
            }
        }
        return null;
    }

    public Location getLocation(String className, int lineNumber) throws AbsentInformationException {
        checkState();

        for (ReferenceType refType : vm.allClasses()) {
            if (!className.equals(refType.name())) {
                continue;
            }
            for (Location loc : refType.allLineLocations()) {
                if (loc.lineNumber() == lineNumber) {
                    return loc;
                }
            }
        }
        return null;
    }

    public BreakpointRequest getBreakpointRequest(Location arg) {
        checkState();
        EventRequestManager evtMgr = vm.eventRequestManager();
        BreakpointRequest req = evtMgr.createBreakpointRequest(arg);
        req.setSuspendPolicy(BreakpointRequest.SUSPEND_ALL);
        req.enable();
        return req;
    }

    public EventQueue getEventQueue() {
        checkState();
        return vm.eventQueue();
    }

    private void checkState() {
        if (null == vm) {
            throw new IllegalStateException("vm is null");
        }
    }
}

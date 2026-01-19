package main.model.state;

import main.model.enums.Status;
import java.util.EnumMap;
import java.util.Map;

/**
 * Factory to retrieve State instances based on Status enum avoiding switch
 */
public final class StateFactory {
    private static final Map<Status, TicketState> STATE_MAP = new EnumMap<>(Status.class);

    static {
        STATE_MAP.put(Status.OPEN, new OpenState());
        STATE_MAP.put(Status.IN_PROGRESS, new InProgressState());
        STATE_MAP.put(Status.RESOLVED, new ResolvedState());
        STATE_MAP.put(Status.CLOSED, new ClosedState());
    }

    private StateFactory() {
    }

    /**
     * Gets the state object for a given status
     *
     * @param status The status enum
     * @return The corresponding TicketState
     */
    public static TicketState getState(final Status status) {
        return STATE_MAP.get(status);
    }
}

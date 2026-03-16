package generic;

/**
 * Element interface - implemented by any unit that can receive and handle events
 * in the discrete event simulator.
 */
public interface Element {
	void handleEvent(Event e);
}

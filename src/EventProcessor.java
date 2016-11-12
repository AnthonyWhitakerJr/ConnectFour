import java.awt.*;
import java.util.LinkedList;

public class EventProcessor {
	private LinkedList<AWTEvent> eventList;
	private EventProcessable handler;

	public EventProcessor(EventProcessable handler) {
		this.handler = handler;
		eventList = new LinkedList<AWTEvent>();
	}

	public void addEvent(AWTEvent e) {
		synchronized(eventList) {
			eventList.add(e);
		}
	}

	public void processEventList() {
		AWTEvent event;
		while(eventList.size() > 0) {
			synchronized(eventList) {
				event = eventList.removeFirst();
			}
			handler.handleEvent(event);
		}
	}
}

package de.psi.paip.mes.frontend.messages;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@Component
public class MessageDispatcher{

    private static final Logger log = LoggerFactory.getLogger(MessageDispatcher.class);

    private BlockingQueue<Message> blockingQueue = new LinkedBlockingDeque<>();

    /**
     * Print all Entries
     * */
    public void printEntries() {
        log.trace("Print entries:");
        Iterator<Message> iterator = blockingQueue.iterator();

        while(iterator.hasNext()) {
            Message m = iterator.next();
            log.trace(m.toString());
        }
    }

    /**
     * Add Entry to Queue
     * @param m Message to be added
     * */
    public void addEntry(Message m){
        log.trace("Add Entry: "+ m.toString()+ " to Queue.");
        blockingQueue.add(m);
    }

    /**
     * Delete Entry from Queue
     * @param m Message to be added
     * */
    public void deleteEntry(Message m){
        try{
            blockingQueue.remove(m);
        }catch(Exception e){
            log.trace(e.toString());
        }
    }

}

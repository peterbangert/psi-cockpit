package de.psi.paip.mes.frontend.messages;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.GeneratedValue;
import java.util.Objects;

@Data
public class Message {

    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private final String terminalId;

    private final MessageType type;

    private final Object activity;

    public Message(MessageType type, String terminalId, Object activity){
        this.type= type;
        this.terminalId = terminalId;
        this.activity = activity;
    }

    public String toString(){
        return activity.toString();
    }

    public Object getActivity(){
        return activity;
    }

    public MessageType getMessageType(){
        return type;
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Message message = (Message) o;
        return Objects.equals(id, message.id) &&
                Objects.equals(id, message.id);
    }
}

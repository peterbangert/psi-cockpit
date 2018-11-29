package de.psi.paip.mes.frontend.terminalState.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.psi.paip.mes.frontend.terminal.model.Terminal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Embeddable
@Table(name = "terminalstate")
@NoArgsConstructor
@RequiredArgsConstructor
public class TerminalState {

   @Id
   @GeneratedValue(generator = "uuid")
   @GenericGenerator(name = "uuid", strategy = "uuid2")
   private String id;

   /**
    * The terminal this state belongs to
    */
   @JsonIgnore
   @NonNull
   @OneToOne
   private Terminal terminal;

   /**
    * The current status of the workplace.
    */
   private Status status = Status.DEFAULT;

   /**
    * If set it prevents any actions done by the terminal.
    */
   private Boolean locked = Boolean.FALSE;

   /**
    * A info message which can be shown on the UI in several states.
    */
   private String message;

   /**
    * operationId of the terminal
    */
   @Column()
   private String operationId;

   /**
    * mode of the terminal
    */
   @Column()
   private TerminalMode terminalMode = TerminalMode.firstOperation;
   
   @Override
   public String toString() {
	   return "TerminalState: "+id+ " Status: "+status+ " Mode: "+terminalMode;
   }
}

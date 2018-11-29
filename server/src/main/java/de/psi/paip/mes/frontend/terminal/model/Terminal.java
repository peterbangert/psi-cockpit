package de.psi.paip.mes.frontend.terminal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.psi.paip.mes.frontend.terminalState.model.TerminalState;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import org.hibernate.annotations.GenericGenerator;

import java.util.Objects;

import javax.persistence.*;

@Data
@Accessors(chain=true)
@Entity
@Table(name = "terminal")
@NoArgsConstructor
@RequiredArgsConstructor
public class Terminal {

   @Id
   @JsonIgnore
   @GeneratedValue(generator = "uuid")
   @GenericGenerator(name = "uuid", strategy = "uuid2")
   private String id;

   /**
    * Key to identify this terminal
    */
   @NonNull
   @Column(unique = true, nullable = false)
   private String businessKey;
   
   /**
    * Name of the terminal
    */
   @Column()
   private String name;
   
   /**
    * description of the terminal
    */
   @Column()
   private String description;

   @Column(unique = true)
   private Integer terminalOrder;

   @OneToOne(cascade = {CascadeType.ALL})
   @Embedded
   private TerminalState state;

   @PrePersist
   private void ensureNonNullState() {
      if (state == null) {
         state = new TerminalState(this);
      }
   }
   
   @Override
   public String toString() {
	   return "TerminalName: "+name;
   }
   
   public boolean equals(final Object o) {
       if (this == o) return true;
       if (o == null || getClass() != o.getClass()) return false;
       final Terminal terminal = (Terminal) o;
       return Objects.equals(id, terminal.id) &&
       Objects.equals(id, terminal.id);
   }
}

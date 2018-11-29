package de.psi.paip.mes.frontend.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@Entity
@Table(name = "buttonconfig")
@NoArgsConstructor
public class ButtonConfig implements Comparable<ButtonConfig> {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@NonNull
	@Column(nullable = false)
	private String buttonName;
	
	@NonNull
	@Column(nullable = false)
	private String workflow;
	
	@ManyToOne
    @JoinColumn(name = "config_id")
	private Config config;
	
	private String buttonType;
	
	private int buttonOrder;

	@Override
	public int compareTo(ButtonConfig o) {
		if(buttonOrder < o.buttonOrder) {
			return -1;
		} else if (buttonOrder > o.buttonOrder) {
			return 1;
		}
		
		return 0;
	}
	
	@Override
	public String toString() {
		return "Button"+id;
	}
}

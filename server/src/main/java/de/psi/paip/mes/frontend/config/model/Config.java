package de.psi.paip.mes.frontend.config.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "config")
@NoArgsConstructor
@RequiredArgsConstructor
public class Config {

	@Id
	@JsonIgnore
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	/**
	 * Key to identify this config
	 */
	@NonNull
	@Column(unique = true, nullable = false)
	private String businessKey;

	/**
	 * display number of the config
	 */
	@Column()
	private String displayNumber;

	@Override
	public String toString() {
		return "Config" + id;
	}

}

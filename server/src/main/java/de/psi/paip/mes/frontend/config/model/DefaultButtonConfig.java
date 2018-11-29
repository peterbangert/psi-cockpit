package de.psi.paip.mes.frontend.config.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Data
@Accessors(chain=true)
@Entity
@Table(name = "defaultbuttonconfig")
@NoArgsConstructor
public class DefaultButtonConfig {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private int id;

    @NonNull
    @Column(nullable = false)
    private String buttonName;

    @NonNull
    @Column(nullable = false)
    private String workflow;

    @NonNull
    @Column(nullable = false)
    private String buttonType;

    @NonNull
    @Column(nullable = false)
    private int buttonOrder;

    @Override
    public String toString() {
        return "DefaultButtonConfig"+id;
    }
}

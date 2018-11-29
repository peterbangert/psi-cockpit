package de.psi.paip.mes.frontend.openProcessInstances;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Objects;

@Data
@Accessors(chain=true)
@Entity
@Table(name = "openprocessinstance")
@NoArgsConstructor
@RequiredArgsConstructor
public class OpenProcessInstance {

    public OpenProcessInstance(String processInstanceId, String workStation, String workOperation, String workStep){
        setProcessInstanceId(processInstanceId);
        setWorkStation(workStation);
        setWorkOperation(workOperation);
        setWorkStep(workStep);
    }

    /**
     * process_instance_id
     */
    @Id
    private String processInstanceId;

    /**
     * station id
     */
    @NonNull
    @Column(nullable = false)
    private String workStation;

    /**
     * workOperation
     */
    @NonNull
    @Column(nullable = false)
    private String workOperation;

    /**
     * workStep
     */
    @NonNull
    @Column(nullable = false)
    private String workStep;

    @Override
    public String toString() {
        return "OpenProcessInstance with ProcessInstanceId: "+workStep+" for terminal:"+workStation+
                " with operationId:"+workOperation+ "and workstep:"+workStep;
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final OpenProcessInstance instance = (OpenProcessInstance) o;
        return Objects.equals(processInstanceId, instance.processInstanceId);
    }
}

package de.psi.paip.mes.frontend.openProcessInstances;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



    @Repository
    public interface OpenProcessInstanceRepository extends JpaRepository<OpenProcessInstance, String> {

        /**select all by processInstanceId
         * @param processInstanceId id of the OpenProcessInstance
         * @return requested OpenProcessInstance
         * */
        @Query("SELECT o FROM OpenProcessInstance o WHERE o.processInstanceId =:processInstanceId")
        Optional<OpenProcessInstance> findByProcessInstanceId(@Param("processInstanceId") String processInstanceId);


        /**select all open instances for a specific work_station
         * @param workStation workStation of the OpenProcessInstance
         * @return List of OpenProcessInstances
         * */
        @Query("SELECT o FROM OpenProcessInstance o WHERE o.workStation =:workStation ORDER BY o.workOperation, o.workStep")
        Optional<List<OpenProcessInstance>> findAllForStation(@Param("workStation") String workStation);


        /**select all open instances for a specific work_operation
         * @param workStation workStation of the OpenProcessInstance
         * @param workOperation workOperation of the OpenProcessInstance
         * @return List of OpenProcessInstances
         * */
        @Query("SELECT o FROM OpenProcessInstance o WHERE o.workStation =:workStation AND o.workOperation =:workOperation " +
                "ORDER BY o.workOperation, o.workStep")
        Optional<List<OpenProcessInstance>> findAllForOperation( @Param("workStation") String workStation,
                                                                 @Param("workOperation") String workOperation);

        /**delete by processInstanceId
         * @param processInstanceId of the OpenProcessInstance
         *  */
        @Modifying
        @Query("DELETE FROM OpenProcessInstance WHERE processInstanceId =:processInstanceId")
        void deleteByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

}

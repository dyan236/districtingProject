package com.example.demo.DB;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface IncumbentRepo extends CrudRepository<Incumbent, Integer>{
	@Query(value="SELECT * FROM incumbent WHERE state = :state", nativeQuery=true)
	List<Incumbent> retrieveForState(@Param("state") String state);
}

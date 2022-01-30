package com.example.demo;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface JobDescriptionRepo extends CrudRepository<JobDescription, Integer>{
	@Query(value="SELECT * FROM job_description WHERE state = :state", nativeQuery=true)
	List<JobDescription> retrieveForState(@Param("state") String state);
}

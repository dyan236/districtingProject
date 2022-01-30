package com.example.demo.DB;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface DistrictingPrecomputedRepo extends CrudRepository<DistrictingPrecomputed, Integer>{
	@Query(value="SELECT * FROM districting_precomputed WHERE job = :job", nativeQuery=true)
	List<DistrictingPrecomputed> retrieveForJob(@Param("job") String job);
}

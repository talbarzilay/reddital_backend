package com.project.reddital_backend.repositories;

import com.project.reddital_backend.models.SubReddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubRedditRepository  extends JpaRepository<SubReddit, Long> {

    /**
     * return a sub by its unique name
     * @param name the name to find the sub by
     * @return the sub by the given name
     */
    SubReddit findByName(String name);
}

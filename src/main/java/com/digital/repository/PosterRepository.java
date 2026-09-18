package com.digital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.digital.entity.Poster;
import com.digital.entity.User;
import java.util.List;

public interface PosterRepository extends JpaRepository<Poster, Long> {

    List<Poster> findByUser(User user);

    //List<Poster> findByPublishedTrue(); // public posters (admin approved)
    List<Poster> findByUserOrderByCreatedAtDesc(User user);

}


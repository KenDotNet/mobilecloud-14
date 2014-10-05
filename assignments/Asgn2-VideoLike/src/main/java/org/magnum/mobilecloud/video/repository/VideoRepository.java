package org.magnum.mobilecloud.video.repository;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * An interface for a repository that can store Video
 * objects and allow them to be searched by title.
 * 
 * @author jules
 *
 */
@Repository
public interface VideoRepository extends CrudRepository<Video, Long>{

	public Collection<Video> findByName(String title);

	public Collection<Video> findByDurationLessThan(Long duration);
	
}

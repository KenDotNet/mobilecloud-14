package org.magnum.dataup.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import org.magnum.dataup.model.Video;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Repository
public class VideoRepository implements CrudRepository<Video, Long> {
	
	private Map<Long, Video> vidoes = new HashMap<Long, Video>();
	private static final AtomicLong idService = new AtomicLong(0l);

	@Override
	public <S extends Video> S save(S entity) {
		this.checkAndSetId(entity);
		entity.setDataUrl(this.createDataUrl(entity.getId()));
		this.vidoes.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public <S extends Video> Iterable<S> save(Iterable<S> entities) {
		/*
		 * Not Implemented
		 */
		return null;
	}

	@Override
	public Video findOne(Long id) {
		return this.vidoes.get(id);
	}

	@Override
	public boolean exists(Long id) {
		return this.vidoes.containsKey(id);
	}

	@Override
	public Iterable<Video> findAll() {
		return this.vidoes.values();
	}

	@Override
	public Iterable<Video> findAll(Iterable<Long> ids) {
		/*
		 * Not Implemented
		 */
		return null;
	}

	@Override
	public long count() {
		return this.vidoes.size();
	}

	@Override
	public void delete(Long id) {
		/*
		 * Not Implemented
		 */
	}

	@Override
	public void delete(Video entity) {
		/*
		 * Not Implemented
		 */
	}

	@Override
	public void delete(Iterable<? extends Video> entities) {
		/*
		 * Not Implemented
		 */
	}

	@Override
	public void deleteAll() {
		/*
		 * Not Implemented
		 */
	}
	
	private String createDataUrl(long videoId){
		return String.format("%s/video/%s/data", this.getUrlBaseForLocalServer(), videoId);
    }

 	private String getUrlBaseForLocalServer() {
	   HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	   String base = "http://"+request.getServerName()  + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
	   return base;
	}
 	
 	private void checkAndSetId(Video entity) {
		if(entity.getId() == 0){
			entity.setId(VideoRepository.idService.incrementAndGet());
		}
	}

}

/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.common.collect.Lists;

@Controller
public class VideoController {
	private static final String ID_PARAM = "id";
	private static final String VIDEO_SVC_ID_SEARCH_PATH = VideoSvcApi.VIDEO_SVC_PATH + "/{id}";
	private static final String VIDEO_SVC_LIKE_PATH = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like";
	private static final String VIDEO_SVC_UNLIKE_PATH = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike";
	private static final String VIDEO_SVC_LIKEDBY_SEARCH_PATH = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby";
	
	@Autowired
	private VideoRepository videos;
	
	/*
	 GET /video
	   - Returns the list of videos that have been added to the
	     server as JSON. The list of videos should be persisted
	     using Spring Data. The list of Video objects should be able 
	     to be unmarshalled by the client into a Collection<Video>.
	   - The return content-type should be application/json, which
	     will be the default if you use @ResponseBody
	 */
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findAllVideos(){
		return Lists.newArrayList(this.videos.findAll());
	}
	
	/*
	 POST /video
	   - The video metadata is provided as an application/json request
	     body. The JSON should generate a valid instance of the 
	     Video class when deserialized by Spring's default 
	     Jackson library.
	   - Returns the JSON representation of the Video object that
	     was stored along with any updates to that object made by the server. 
	   - **_The server should store the Video in a Spring Data JPA repository.
	   	 If done properly, the repository should handle generating ID's._** 
	   - A video should not have any likes when it is initially created.
	   - You will need to add one or more annotations to the Video object
	     in order for it to be persisted with JPA.
	 */
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video){
		 Video saved = videos.save(video);
		 return saved;
	}
	
	/*
	 GET /video/{id}
	   - Returns the video with the given id or 404 if the video is not found.
	 */
	@RequestMapping(value=VideoController.VIDEO_SVC_ID_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Video findVideoById(@PathVariable(VideoController.ID_PARAM) long videoId){
		return this.videos.findOne(videoId);
	}
	
	/*
	 POST /video/{id}/like
	   - Allows a user to like a video. Returns 200 Ok on success, 404 if the
	     video is not found, or 400 if the user has already liked the video.
	   - The service should should keep track of which users have liked a video and
	     prevent a user from liking a video twice. A POJO Video object is provided for 
	     you and you will need to annotate and/or add to it in order to make it persistable.
	   - A user is only allowed to like a video once. If a user tries to like a video
	      a second time, the operation should fail and return 400 Bad Request.
	 */
	@RequestMapping(value=VideoController.VIDEO_SVC_LIKE_PATH, method=RequestMethod.POST)
	public @ResponseBody void likeVideo(@PathVariable(VideoController.ID_PARAM) long videoId, Principal principal, HttpServletResponse response) throws IOException{
		Video video = this.videos.findOne(videoId);
		if(video != null) {
			String userName = principal.getName();
			if(video.addLiker(userName)) {
				this.videos.save(video);
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "That you already liked it, you can't love it.");
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "That video was not found.");
		}
	}
	
	/*
	 POST /video/{id}/unlike
	   - Allows a user to unlike a video that he/she previously liked. Returns 200 OK
	      on success, 404 if the video is not found, and a 400 if the user has not 
	      previously liked the specified video.
	 */
	@RequestMapping(value=VideoController.VIDEO_SVC_UNLIKE_PATH, method=RequestMethod.POST)
	public @ResponseBody void unlikeVideo(@PathVariable(VideoController.ID_PARAM) long videoId, Principal principal, HttpServletResponse response) throws IOException{
		Video video = this.videos.findOne(videoId);
		if(video != null) {
			String userName = principal.getName(); // Need to get this for real
			if(video.removeLiker(userName)) {
				this.videos.save(video);
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You have to like it first, you can't hate it.");
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "That video was not found.");
		}
	}
	
	/*
	 GET /video/{id}/likedby
	   - Returns a list of the string usernames of the users that have liked the specified
	     video. If the video is not found, a 404 error should be generated.
	 */
	@RequestMapping(value=VideoController.VIDEO_SVC_LIKEDBY_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<String> findLikedBy(@PathVariable(VideoController.ID_PARAM) long videoId){
		Video video = this.videos.findOne(videoId);
		return video.getVideoLikers();
	}
	
	/*
	 GET /video/search/findByName?title={title}
	   - Returns a list of videos whose titles match the given parameter or an empty
	     list if none are found.
	 */
	@RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByName(@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title){
		return this.videos.findByName(title);
	}
	
	/*
	 GET /video/search/findByDurationLessThan?duration={duration}
	   - Returns a list of videos whose durations are less than the given parameter or
	     an empty list if none are found.	
	 */
	@RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(@RequestParam(VideoSvcApi.DURATION_PARAMETER) Long duration){
		return this.videos.findByDurationLessThan(duration);
	}
	
}

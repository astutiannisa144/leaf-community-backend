package com.lawencon.leaf.community.service;

import org.springframework.stereotype.Service;

import com.lawencon.base.ConnHandler;
import com.lawencon.leaf.community.dao.BookmarkDao;
import com.lawencon.leaf.community.dao.LikeDao;
import com.lawencon.leaf.community.dao.PostDao;
import com.lawencon.leaf.community.dao.UserDao;
import com.lawencon.leaf.community.model.Bookmark;
import com.lawencon.leaf.community.model.Post;
import com.lawencon.leaf.community.model.User;
import com.lawencon.leaf.community.pojo.PojoRes;
import com.lawencon.leaf.community.pojo.bookmark.PojoBookmarkReqInsert;
import com.lawencon.security.principal.PrincipalService;

@Service
public class BookmarkService {

	private final BookmarkDao bookmarkDao;
	private final PostDao postDao;
	private final UserDao userDao;
	private final PrincipalService principalService;

	public BookmarkService(BookmarkDao bookmarkDao, LikeDao likeDao, PostDao postDao, UserDao userDao,
			PrincipalService principalService) {
		this.bookmarkDao = bookmarkDao;
		this.postDao = postDao;
		this.userDao = userDao;
		this.principalService = principalService;
	}
	private void valIdNull(PojoBookmarkReqInsert bookmark) {
		if(bookmark==null) {
			throw new RuntimeException("Form cannot be empty");
		}
		if(bookmark.getId()!=null ) {
			throw new RuntimeException("Id must be empty");
		}
	}

	private void valNonBk(PojoBookmarkReqInsert bookmark) {
		if(bookmark==null) {
			throw new RuntimeException("Form cannot be empty");
		}
		if(bookmark.getPostId()==null) {
			throw new RuntimeException("Post cannot be empty");
		}
	}

	private void valIdExist(String id) {
		if(bookmarkDao.getById(id).isEmpty()) {
			throw new RuntimeException("Id cannot be empty");
		}
	}
	

	public PojoRes insert(PojoBookmarkReqInsert data) {
		ConnHandler.begin();
		valIdNull(data);
		valNonBk(data);
		
		Bookmark bookmark = new Bookmark();
		final Post post = postDao.getById(Post.class, data.getPostId());
		bookmark.setPost(post);

		final User user = userDao.getById(principalService.getAuthPrincipal()).get();
		bookmark.setMember(user);

		bookmark = bookmarkDao.save(bookmark);

		ConnHandler.commit();

		final PojoRes res = new PojoRes();
		res.setId(bookmark.getId());
		res.setMessage("Post added to bookmark");
		return res;
	}

	public PojoRes delete(String id) {

		try {
			ConnHandler.begin();
			valIdExist(id);
			bookmarkDao.deleteById(Bookmark.class, id);
		} catch (Exception e) {
			e.printStackTrace();
		}

		final PojoRes pojoRes = new PojoRes();
		pojoRes.setMessage("Post removed from bookmark");
		return pojoRes;
	}

}

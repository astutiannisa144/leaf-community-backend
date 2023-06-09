package com.lawencon.leaf.community.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lawencon.base.ConnHandler;
import com.lawencon.leaf.community.constant.PostPageType;
import com.lawencon.leaf.community.dao.BookmarkDao;
import com.lawencon.leaf.community.dao.CategoryDao;
import com.lawencon.leaf.community.dao.CommentDao;
import com.lawencon.leaf.community.dao.FileDao;
import com.lawencon.leaf.community.dao.LikeDao;
import com.lawencon.leaf.community.dao.PollingDao;
import com.lawencon.leaf.community.dao.PollingDetailDao;
import com.lawencon.leaf.community.dao.PostDao;
import com.lawencon.leaf.community.dao.PostFileDao;
import com.lawencon.leaf.community.dao.UserDao;
import com.lawencon.leaf.community.dao.UserPollingDao;
import com.lawencon.leaf.community.model.Bookmark;
import com.lawencon.leaf.community.model.Category;
import com.lawencon.leaf.community.model.Comment;
import com.lawencon.leaf.community.model.File;
import com.lawencon.leaf.community.model.Like;
import com.lawencon.leaf.community.model.Polling;
import com.lawencon.leaf.community.model.PollingDetail;
import com.lawencon.leaf.community.model.Post;
import com.lawencon.leaf.community.model.PostFile;
import com.lawencon.leaf.community.model.User;
import com.lawencon.leaf.community.model.UserPolling;
import com.lawencon.leaf.community.pojo.PojoRes;
import com.lawencon.leaf.community.pojo.polling.PollingDetailRes;
import com.lawencon.leaf.community.pojo.polling.PollingResGet;
import com.lawencon.leaf.community.pojo.post.PojoPostReqInsert;
import com.lawencon.leaf.community.pojo.post.PojoPostReqUpdate;
import com.lawencon.leaf.community.pojo.post.PojoPostResGetAll;
import com.lawencon.leaf.community.util.GenerateCodeUtil;
import com.lawencon.security.principal.PrincipalService;
import com.lawencon.security.principal.PrincipalServiceImpl;

@Service
public class PostService {

	private final PrincipalService principalService;
	private final PollingDetailDao pollingDetailDao;
	private final UserPollingDao userPollingDao;
	private final CategoryDao categoryDao;
	private final PostFileDao postFileDao;
	private final BookmarkDao bookmarkDao;
	private final PollingDao pollingDao;
	private final CommentDao commentDao;
	private final PostDao postDao;
	private final UserDao userDao;
	private final FileDao fileDao;
	private final LikeDao likeDao;

	public PostService(PostDao postDao, CategoryDao categoryDao, PollingDao pollingDao,
			PollingDetailDao pollingDetailDao, UserDao userDao, PrincipalServiceImpl principalServiceImpl,
			FileDao fileDao, PostFileDao postFileDao, LikeDao likeDao, CommentDao commentDao, BookmarkDao bookmarkDao,
			UserPollingDao userPollingDao) {
		this.principalService = principalServiceImpl;
		this.pollingDetailDao = pollingDetailDao;
		this.userPollingDao = userPollingDao;
		this.categoryDao = categoryDao;
		this.postFileDao = postFileDao;
		this.bookmarkDao = bookmarkDao;
		this.pollingDao = pollingDao;
		this.commentDao = commentDao;
		this.postDao = postDao;
		this.userDao = userDao;
		this.fileDao = fileDao;
		this.likeDao = likeDao;
	}
	
	private void valIdNull(PojoPostReqInsert post) {
		if(post==null) {
			throw new RuntimeException("Form cannot be empty");
		}
		if(post.getId()!=null ) {
			throw new RuntimeException("Id must be empty");
		}
	}

	private void valNonBk(PojoPostReqInsert post) {
		if(post==null) {
			throw new RuntimeException("Form cannot be empty");
		}
		if(post.getCategoryId()==null) {
			throw new RuntimeException("Category cannot be empty");
		}
		if(post.getContent()==null) {
			throw new RuntimeException("Content cannot be empty");
		}
		if(post.getTitle()==null) {
			throw new RuntimeException("Title cannot be empty");
		}
		if(post.getIsPremium()==null) {
			throw new RuntimeException("Premium cannot be empty");
		}
	}

	private void valNonBkUpdate(PojoPostReqUpdate post) {
		if(post==null) {
			throw new RuntimeException("Form cannot be empty");
		}
		if(post.getContent()==null) {
			throw new RuntimeException("Content cannot be empty");
		}
		if(post.getTitle()==null) {
			throw new RuntimeException("Title cannot be empty");
		}

	}
	
	private void valIdExist(String id) {
		if(postDao.getById(id).isEmpty()) {
			throw new RuntimeException("Id cannot be empty");
		}
	}
	public PojoRes insert(final PojoPostReqInsert data) {

		ConnHandler.begin();
		valIdNull(data);
		valNonBk(data);
		
		Post postInsert = new Post();
		postInsert.setPostCode(GenerateCodeUtil.generateCode(10));
		postInsert.setTitle(data.getTitle());
		postInsert.setContent(data.getContent());
		postInsert.setIsPremium(data.getIsPremium());

		final Category category = categoryDao.getById(Category.class, data.getCategoryId());
		postInsert.setCategory(category);

		final User user = userDao.getById(principalService.getAuthPrincipal()).get();
		postInsert.setMember(user);

		if (data.getPolling() != null) {
			if (data.getPolling().getContent() != null) {
				Polling polling = new Polling();
				polling.setContent(data.getPolling().getContent());
				polling.setExpired(data.getPolling().getExpired());

				polling = pollingDao.save(polling);

				postInsert.setPolling(polling);
				for (int i = 0; i < data.getPolling().getPollingDetail().size(); i++) {
					final PollingDetail pollingDetail = new PollingDetail();
					pollingDetail.setPolling(polling);
					pollingDetail.setContent(data.getPolling().getPollingDetail().get(i).getContent());
					pollingDetailDao.save(pollingDetail);
				}
			}
		}

		postInsert = postDao.save(postInsert);

		if (data.getFile() != null) {
			for (int i = 0; i < data.getFile().size(); i++) {
				File file = new File();
				file.setFileContent(data.getFile().get(i).getFileContent());
				file.setFileExtension(data.getFile().get(i).getFileExtension());
				file = fileDao.save(file);

				final PostFile postFile = new PostFile();
				postFile.setFile(file);
				postFile.setPost(postInsert);
				postFileDao.save(postFile);
			}
		}

		ConnHandler.commit();

		final PojoRes res = new PojoRes();
		res.setId(postInsert.getId());
		res.setMessage("Post created");
		return res;

	}

	public PojoRes update(PojoPostReqUpdate data) {
		ConnHandler.begin();
		valIdExist(data.getPostId());
		valNonBkUpdate(data);
		final Post postInsert = postDao.getById(data.getPostId()).get();
		postInsert.setTitle(data.getTitle());
		postInsert.setContent(data.getContent());

		postDao.save(postInsert);

		ConnHandler.commit();

		final PojoRes res = new PojoRes();
		res.setMessage("Post edited");
		return res;
	}

	public PojoRes delete(String id) {
		
		final PojoRes pojoRes = new PojoRes();
		
		try {
			valIdExist(id);
			ConnHandler.begin();
			
			Post post = postDao.getById(id).get();
			
			List<Like> likeList = likeDao.getLikeByPost(id);
			List<Bookmark> bookmarkList = bookmarkDao.getBookmarkByPost(id);
			List<Comment> commentList = commentDao.getCommentByPostId(id);
			
			commentList.forEach((c) -> commentDao.deleteById(Comment.class, c.getId()));
			bookmarkList.forEach((c) -> bookmarkDao.deleteById(Bookmark.class, c.getId()));
			likeList.forEach((c) -> likeDao.deleteById(Like.class, c.getId()));
			
			if (post.getPolling() != null) {
				Polling polling = pollingDao.getById(post.getPolling().getId()).get();
				
				List<PollingDetail> pollingDetailList = pollingDetailDao.getAllByPolling(polling.getId());
				
				List<UserPolling> userPollingList = userPollingDao.getAllByPollingId(polling.getId());
				
				if (userPollingList.size() > 0) {
					for (int i=0; i<userPollingList.size(); i++) {
						userPollingDao.deleteById(UserPolling.class, userPollingList.get(i).getId());
					}
				}
				
				pollingDetailList.forEach((c) -> pollingDetailDao.deleteById(PollingDetail.class, c.getId()));
				
			}
			
			List<PostFile> fileList = postFileDao.getAllByPost(id);
			
			if (fileList.size() > 0) {
				fileList.forEach((c) -> postFileDao.deleteById(PostFile.class, c.getId()));
				fileList.forEach((c) -> fileDao.deleteById(File.class, c.getFile().getId()));
			}
			
			postDao.deleteById(Post.class, id);
			
			if (post.getPolling() != null) {
				pollingDao.deleteById(Polling.class, pollingDao.getById(post.getPolling().getId()).get().getId());
			}
			
			ConnHandler.commit();
			
			pojoRes.setMessage("Post deleted");
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		return pojoRes;
	}

	public List<PojoPostResGetAll> getAll(int limit, int offset, String code) {
		List<Post> postList = new ArrayList<>();

		if (code == null) {
			postList = postDao.getAll(limit, offset);

		} else if (PostPageType.PROFILE.getCode().equals(code)) {
			postList = postDao.getAllByUser(limit, offset, principalService.getAuthPrincipal());

		} else if (PostPageType.LIKE.getCode().equals(code)) {
			postList = postDao.getAllByLike(limit, offset, principalService.getAuthPrincipal());

		} else if (PostPageType.BOOKMARK.getCode().equals(code)) {
			postList = postDao.getAllByBookmark(limit, offset, principalService.getAuthPrincipal());
		}

		final List<PojoPostResGetAll> resList = new ArrayList<>();

		for (int i = 0; i < postList.size(); i++) {
			final PojoPostResGetAll res = new PojoPostResGetAll();
			res.setPostId(postList.get(i).getId());
			res.setTitle(postList.get(i).getTitle());
			res.setContent(postList.get(i).getContent());
			res.setIsPremium(postList.get(i).getIsPremium());
			res.setCategoryId(postList.get(i).getCategory().getId());
			res.setCategoryName(postList.get(i).getCategory().getCategoryName());
			res.setMemberId(postList.get(i).getMember().getId());
			
			if (postList.get(i).getMember().getProfile().getFile() != null) {
				res.setFileId(postList.get(i).getMember().getProfile().getFile().getId());
			}
			
			res.setFullName(postList.get(i).getMember().getProfile().getFullName());
			res.setCreatedAt(postList.get(i).getCreatedAt());
			res.setLikeSum(likeDao.countLike(postList.get(i).getId()));
			res.setCommentSum(commentDao.countComment(postList.get(i).getId()));
			res.setVer(postList.get(i).getVer());

			if (likeDao.getId(principalService.getAuthPrincipal(), postList.get(i).getId()).isPresent()) {
				res.setLikeId(likeDao.getId(principalService.getAuthPrincipal(), postList.get(i).getId()).get());
			}

			if (bookmarkDao.getId(principalService.getAuthPrincipal(), postList.get(i).getId()).isPresent()) {
				res.setBookmarkId(
						bookmarkDao.getId(principalService.getAuthPrincipal(), postList.get(i).getId()).get());
			}

			if (postList.get(i).getPolling() != null) {
				final PollingResGet polling = new PollingResGet();
				polling.setPollingId(postList.get(i).getPolling().getId());
				polling.setContent(postList.get(i).getPolling().getContent());
				polling.setExpired(postList.get(i).getPolling().getExpired());

				final Long totalPolling = userPollingDao.countTotalPolling(postList.get(i).getPolling().getId());
				polling.setTotalPolling(totalPolling);

				if (userPollingDao.getId(principalService.getAuthPrincipal(), postList.get(i).getPolling().getId())
						.isPresent()) {
					polling.setUserPollingId(userPollingDao
							.getId(principalService.getAuthPrincipal(), postList.get(i).getPolling().getId()).get());
				}

				final List<PollingDetailRes> resPollingDetailList = new ArrayList<>();
				final List<PollingDetail> pollingDetailList = pollingDetailDao
						.getAllByPolling(postList.get(i).getPolling().getId());

				for (int j = 0; j < pollingDetailList.size(); j++) {
					final PollingDetailRes resPollingDetail = new PollingDetailRes();
					resPollingDetail.setPollingDetailId(pollingDetailList.get(j).getId());
					resPollingDetail.setContent(pollingDetailList.get(j).getContent());

					BigDecimal percentage = BigDecimal
							.valueOf(userPollingDao.countPercentage(pollingDetailList.get(j).getId()) * 100);
					if (totalPolling > 0) {
						percentage = percentage.divide(BigDecimal.valueOf(totalPolling), 2, RoundingMode.HALF_UP);
					} else {
						percentage = BigDecimal.valueOf(0);
					}

					resPollingDetail.setPercentage(percentage);

					resPollingDetailList.add(resPollingDetail);
				}
				polling.setPollingDetail(resPollingDetailList);
				res.setPolling(polling);
			}

			if (postFileDao.getAllByPost(postList.get(i).getId()).size() > 0) {
				final List<String> fileList = new ArrayList<>();
				final List<PostFile> postFileList = postFileDao.getAllByPost(postList.get(i).getId());

				for (int j = 0; j < postFileList.size(); j++) {
					fileList.add(postFileList.get(j).getFile().getId());
				}
				res.setFile(fileList);
			}
			resList.add(res);
		}

		return resList;
	}

}

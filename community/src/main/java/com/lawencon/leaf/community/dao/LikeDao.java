package com.lawencon.leaf.community.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.lawencon.base.ConnHandler;
import com.lawencon.leaf.community.model.Like;

@Repository
public class LikeDao extends BaseDao<Like> {

	@Override
	List<Like> getAll() {
		return null;
	}

	@Override
	public Optional<Like> getById(String id) {
		return Optional.ofNullable(super.getById(Like.class, id));
	}

	@Override
	Optional<Like> getByIdAndDetach(String id) {
		return Optional.empty();
	}
	
	public Long countLike(String id) {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(id) FROM t_like ");
		sql.append("WHERE post_id  = :id ");
		
		final Long count = Long.valueOf(ConnHandler.getManager()
				.createNativeQuery(sql.toString())
				.setParameter("id", id)
				.getSingleResult().toString());

		return count;
	}
	
	public Optional<String> getId(String uid, String pid) {
		String likeId = null;
		
		try {
			final StringBuilder sql = new StringBuilder();
			
			sql.append("SELECT id FROM t_like  ");
			sql.append("WHERE member_id = :uid ");
			sql.append("AND post_id = :pid ");
			
			likeId = ConnHandler.getManager()
					.createNativeQuery(sql.toString())
					.setParameter("uid", uid)
					.setParameter("pid", pid)
					.getSingleResult().toString();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Optional.ofNullable(likeId);
	}
	
	@SuppressWarnings("unchecked")
	public List<Like> getLikeByPost(String id) {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM t_like ");
		sql.append("WHERE post_id  = :id ");
		
		final List<Like> likeList = ConnHandler.getManager()
				.createNativeQuery(sql.toString(), Like.class)
				.setParameter("id", id)
				.getResultList();

		return likeList;
	}

}
